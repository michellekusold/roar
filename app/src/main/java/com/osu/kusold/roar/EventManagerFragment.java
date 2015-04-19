package com.osu.kusold.roar;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.geofire.GeoQueryEventListener;

import java.util.ArrayList;
import java.util.Map;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class EventManagerFragment extends Fragment implements AbsListView.OnItemClickListener {

    public Firebase fRef, fRefUser, fRefUserEvents;
    private OnFragmentInteractionListener mListener;
    SwipeRefreshLayout mSwipeRefreshLayout;
    public final static String EVENT_UID = "com.osu.kusold.roar.EVENT_UID_MESSAGE";

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventManagerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.v("EventManagerTask", "EventManagerFragment OnCreate begin.");

        Firebase.setAndroidContext(getActivity());
        fRef = new Firebase(getString(R.string.firebase_ref));
        fRefUser = fRef.child("users").child(fRef.getAuth().getUid());
        fRefUserEvents = fRefUser.child("events");
        mAdapter = new EventPostAdapter(getActivity(), new ArrayList<EventPost>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eventfeed, container, false);

        /*
        *   A swipe to refresh layout wraps around the list view to give refresh animation
        *   and provides a callback method for onRefresh so we know when to pull from Firebase.
         */
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.event_feed_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Get 20 event snapshot from firebase
                mAdapter.clear();
                refreshEventManager();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.yellow, R.color.green, R.color.orange);
        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        refreshEventManager();

        return view;
    }

    public void refreshEventManager() {
        new EventManagerTask(getActivity()).execute();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        EventPost post = (EventPost) mAdapter.getItem(position);
        String eventUid = post.eventUid;
        Log.v("EventManagerTask", "EventManagerFragment creating ViewEventActivity with eventId: " + eventUid);
        Intent eventIntent = new Intent(getActivity(), ViewEventActivity.class);
        eventIntent.putExtra(EVENT_UID , eventUid);
        startActivity(eventIntent);
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

    public void addEventToAdapter(EventPost event) {
        mAdapter.add(event);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Represents an asynchronous event fetch task used to retrieve events from
     * firebase.
     */
    public class EventManagerTask extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;
        GeoQueryEventListener mLimitedQuery;

        EventManagerTask(Context context) {
            mContext = context.getApplicationContext();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Log.v("EventManagerTask", "Beginning EventManager background process.");

            fRefUserEvents.addChildEventListener(new ChildEventListener() {

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.v("EventManagerTask", dataSnapshot.toString());
                    Log.v("EventManagerTask", dataSnapshot.getChildren().toString());

                        final String eventId = dataSnapshot.getKey();
                        Log.v("EventManagerTask", "Event " + eventId + " found for current user.");

                        fRef.child("events").child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map<String, Object> eventData = (Map<String, Object>) dataSnapshot.getValue();
                                if(eventData != null) {
                                    EventPost post = new EventPost(eventData);
                                    post.eventUid = eventId;
                                    addEventToAdapter(post);
                                    Log.v("EventManagerTask", "Event: " + eventData.get("name").toString() + " added to adapter.");
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                Log.v("EventManager", "EventManager.OnCreate.OnDataChange Firebase error: " + firebaseError.toString());
                            }
                        });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });

            SystemClock.sleep(8000);
            Log.v("EventFetchTask", "Exit doBackgroundProcess");
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mListView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
            Log.v("EventFetchTask", "Exit onPostExecute");
        }

        @Override
        protected void onCancelled() {
        }
    }

}
