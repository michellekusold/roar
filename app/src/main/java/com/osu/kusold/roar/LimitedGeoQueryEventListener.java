package com.osu.kusold.roar;

import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;

import java.util.Map;

/**
 * Created by johnsgresham on 4/2/15.
 */
public class LimitedGeoQueryEventListener implements GeoQueryEventListener {

    int numOfEventsToQuery, numOfEventsLoaded;
    EventFeedFragment mEventFeedFragment;

    public LimitedGeoQueryEventListener(EventFeedFragment eventFeedFragment, int numOfEvents) {
        mEventFeedFragment = eventFeedFragment;
        numOfEventsToQuery = numOfEvents;
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        final String uid = key;
        mEventFeedFragment.fRefEvents.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> eventData = (Map<String, Object>) dataSnapshot.getValue();
                if(eventData != null) {
                    EventPost post = new EventPost(eventData);
                    post.eventUid = uid;
                    mEventFeedFragment.addEventToAdapter(post);
                    numOfEventsLoaded++;
                    if(numOfEventsLoaded > numOfEventsToQuery) {
                        mEventFeedFragment.removeRefreshGeoQueryListener();
                    }
                    Log.v("EventFetchTask", "Event: " + eventData.get("name").toString() + " added to EventNameList of size: " + numOfEventsLoaded+ " / " + numOfEventsToQuery);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.v("LimitedGeoQuery", "LimitedGeoQuery.OnDataChange Firebase error: " + firebaseError.toString());
            }
        });
    }

    @Override
    public void onKeyExited(String key) {

    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(FirebaseError error) {
        Log.v("LimitedGeoQuery", "LimitedGeoQuery.OnGeoQueryError Firebase error: " + error.toString());
    }
}
