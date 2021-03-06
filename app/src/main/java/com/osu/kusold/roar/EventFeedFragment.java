package com.osu.kusold.roar;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;

import java.util.ArrayList;

/**
 * A fragment representing a list of Event Items.
 *
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 */
public class EventFeedFragment extends Fragment implements AbsListView.OnItemClickListener {

    public Firebase fRef, fRefEvents;
    private GeoFire geoFire;
    private OnFragmentInteractionListener mListener;
    SwipeRefreshLayout mSwipeRefreshLayout;
    GeoQuery geoQuery;
    private Location location;
    EventFetchTask eventFetchTask;
    private Button mGeoSortButton;
    LimitedGeoQueryEventListener mLimitedGeoQuery;

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
    public EventFeedFragment() {
    }

    /* Sets up firebase and the adapter */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Firebase.setAndroidContext(getActivity());
        fRef = new Firebase(getString(R.string.firebase_ref));
        fRefEvents = fRef.child("events");
        geoFire = new GeoFire(fRef.child("GeoFire"));
        mAdapter = new EventPostAdapter(getActivity(), new ArrayList<EventPost>());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eventfeed, container, false);
        // set up the geo sort button
        mGeoSortButton = (Button) view.findViewById(R.id.geo_sort);
        mGeoSortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("CLICK", "clickclickclick");
                if (isInternetAvailable()) {
                    sortByLocation();
                }
                else {
                    Toast.makeText(getActivity(), "No internet connection present :(", Toast.LENGTH_LONG).show();
                }
                mAdapter.notifyDataSetChanged();
            }
        });

        /*
        *   A swipe to refresh layout wraps around the list view to give refresh animation
        *   and provides a callback method for onRefresh so we know when to pull from Firebase.
         */
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.event_feed_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Get 20 event snapshot from firebase
                refreshEventFeed();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.yellow, R.color.green, R.color.orange);
        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        if(mAdapter.isEmpty()) {
            refreshEventFeed();
            Log.d("EventFeedFragment", "mAdapter empty, refresh.");
        }

        return view;
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
        Log.i("Event fragment ID: ", eventUid);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, ViewEventFragment.newInstance(eventUid), "selectedEvent").commit();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String id);
    }

    public void addEventToAdapter(EventPost event) {
        mAdapter.add(event);
    }

    /*
    *   Used by the listener to stop querying GeoFire after limited results.
     */
    public void removeRefreshGeoQueryListener() {
        if(geoQuery != null) {
            geoQuery.removeAllListeners();
        }
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void refreshEventFeed() {
        if(mLimitedGeoQuery != null) {
            mLimitedGeoQuery.setIgnoreEvents();
        }
        mAdapter.clear();
        eventFetchTask = new EventFetchTask(getActivity(), this);
        eventFetchTask.execute();
    }

    public void onSaveInstanceState(Bundle savedState) {

        super.onSaveInstanceState(savedState);
        // Put all event posts in intent and put scroll location too
        geoQuery.removeAllListeners();

    }

    @Override
    public void onPause() {
        super.onPause();
        removeRefreshGeoQueryListener();
        eventFetchTask.cancel(true);
        Log.v("EventFetchFragment", "onPause called.");
    }

    /**
     * Represents an asynchronous event fetch task used to retrieve events from
     * firebase.
     */
    public class EventFetchTask extends AsyncTask<Void, Void, Boolean> {

        Context mContext;
        EventFeedFragment mEventFeedFragment;

        EventFetchTask(Context context, EventFeedFragment fragment) {
            mContext = context.getApplicationContext();
            mEventFeedFragment = fragment;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.v("EventFetchTask", "Beginning EventFetch background process.");

            LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            double latitude, longitude;
            // GEOFIRE TEST VARS (S.E.L.)
            latitude = 40.0016740;
            longitude = -83.0134160;
            location = new Location("");
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location != null) {
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                }
            }else{
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(),"No GPS signal available", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            // 20 km radius search for events
            geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), 20.0);
            // limit query to at most 20 results
            mLimitedGeoQuery = new LimitedGeoQueryEventListener(mEventFeedFragment, 20);
            geoQuery.addGeoQueryEventListener(mLimitedGeoQuery);
            SystemClock.sleep(3000);
            Log.v("EventFetchTask", "Exit doBackgroundProcess");
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mSwipeRefreshLayout.setRefreshing(false);
            Log.v("EventFetchTask", "Exit onPostExecute");
        }

        @Override
        protected void onCancelled() {
            Log.v("EventFetchTask", "Exit onCancelled");
        }
    }

    /* Sorts the fragments by location nearest to the user */
    public void sortByLocation(){
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            if(location == null){
                // GEOFIRE TEST VARS (S.E.L.)
                double latitude, longitude;
                latitude = 40.0016740;
                longitude = -83.0134160;
                location = new Location("");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
            }
            LocationComparator cmp = new LocationComparator();
            cmp.setUserLocation(location.getLatitude(), location.getLongitude());
            Log.v("SORTING", "sorting?");
            mAdapter.sort(cmp);
        }else{
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity(),"No GPS signal available", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
