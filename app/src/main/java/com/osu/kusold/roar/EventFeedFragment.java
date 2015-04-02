package com.osu.kusold.roar;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.osu.kusold.roar.dummy.DummyContent;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class EventFeedFragment extends Fragment implements AbsListView.OnItemClickListener {

    public Firebase fRef, fRefEvents;
    private GeoFire geoFire;
    private OnFragmentInteractionListener mListener;
    SwipeRefreshLayout mSwipeRefreshLayout;
    GeoQuery geoQuery;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Firebase.setAndroidContext(getActivity());
        fRef = new Firebase(getString(R.string.firebase_ref));
        fRefEvents = fRef.child("events");
        geoFire = new GeoFire(fRef.child("GeoFire"));

        // TODO: Change Adapter to display your content
        mAdapter = new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS);
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
                refreshEventFeed();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.yellow, R.color.green, R.color.orange);
        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    public void refreshEventFeed() {
        new EventFetchTask(getActivity(), this).execute();
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
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
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
        // TODO: change adapter to hold EventPost instead of strings
        mAdapter.add(event.eventName);
    }

    /*
    *   Used by the listener to stop querying GeoFire after limited results.
     */
    public void removeRefreshGeoQueryListener(GeoQueryEventListener listener) {
        geoQuery.removeGeoQueryEventListener(listener);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Represents an asynchronous event fetch task used to retrieve events from
     * firebase.
     */
    public class EventFetchTask extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;
        EventFeedFragment mEventFeedFragment;

        EventFetchTask(Context context, EventFeedFragment fragment) {
            mContext = context.getApplicationContext();
            mEventFeedFragment = fragment;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.v("EventFetchTask", "Beginning EventFetch background process.");
            /*LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);*/
            double latitude, longitude;

                // GEOFIRE TEST VARS (S.E.L.)
                latitude = 40.0016740;
                longitude = -83.0134160;
            /*
            else {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }*/
            Log.v("CurrentLocation", "GPS location on refresh (lag, long): " + latitude + " " + longitude);

            // 20 km radius search for events
            geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), 20.0);
            // limit query to at most 20 results
            LimitedGeoQueryEventListener limitedQuery = new LimitedGeoQueryEventListener(mEventFeedFragment, 20);

            geoQuery.addGeoQueryEventListener(limitedQuery);
            Log.v("EventFetchTask", "Exit doBackgroundProcess");
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            Log.v("EventFetchTask", "Exit onPostExecute");
        }

        @Override
        protected void onCancelled() {
        }
    }

}
