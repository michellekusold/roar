package com.osu.kusold.roar;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;


public class EventFeedActivity extends ActionBarActivity implements EventFeedFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener, ViewEventFragment.OnFragmentInteractionListener, EventManagerFragment.OnFragmentInteractionListener {

    private Firebase fRef, fRefUser, fRefProfile;
    private String[] mDrawerTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private android.support.v7.widget.Toolbar mToolbar;
    String mUid;
    double longitude, latitude;
    GeoFire geoFire;
    EventFeedFragment mPersistantEventFeedFragment;
    public Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_feed);

        // Firebase root setup
        Firebase.setAndroidContext(this);
        fRef = new Firebase(getString(R.string.firebase_ref));
        // GeoFire setup
        mUid = fRef.getAuth().getUid();
        if(mUid == null) {
            logout();
        }
        fRefUser = fRef.child("users").child(mUid);
        fRefProfile = fRef.child("profile");
        geoFire = new GeoFire(fRefUser);

        // get current long/lat of user
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        setLocation(location);
        Log.v("CURRENT LOCATION 1: ", "lat=" + latitude + " lng=" + longitude);


        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);

        mDrawerTitles = getResources().getStringArray(R.array.drawer_option_strings);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mDrawerTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_closed) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        if(getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setLogo(R.drawable.ic_launcher);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle("Event Feed");
        }

        // Default show event feed
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mPersistantEventFeedFragment = new EventFeedFragment();
        fragmentTransaction.add(R.id.fragment_container, mPersistantEventFeedFragment);
        fragmentTransaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.event_feed_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        else if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        else if (id == R.id.action_create_event){
            createEvent();
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        fRef.unauth();
        Intent intent = new Intent(EventFeedActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void createEvent() {
        Intent intent = new Intent(EventFeedActivity.this, CreateEventActivity.class);
        startActivity(intent);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            Log.v("EventFeedActivity", "Item clicked: " + position + " and id: " + id);
            selectItem(position, id);
        }
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position, long id) {
        // Highlight the selected item, update the title, and close the drawer
        if(position == 0) {             // Event Feed
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            EventFeedFragment fragment = new EventFeedFragment();
            fragmentTransaction.replace(R.id.fragment_container, mPersistantEventFeedFragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle("Event Feed");
        } else if (position == 1) {     // Event Manager
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            EventManagerFragment fragment = new EventManagerFragment();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle("My Events");
        } else if (position == 2) {     // Profile
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ProfileFragment fragment = new ProfileFragment();
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            getSupportActionBar().setTitle("Profile");
        } else if (position == 3) {     // Logout
            logout();
        }
        else {
            setTitle(mDrawerTitles[position]);
        }
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    /* Sets the current location of a user in geofire. If none available, use the SEL as a base*/
    public void setLocation(Location loc){
        location = loc;
        if(location == null){
            // GEOFIRE TEST VARS (S.E.L.)
            latitude = 40.0016740;
            longitude =-83.0134160;
        }
        else {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        // set the current location of a user
        geoFire.setLocation("currentLocation", new GeoLocation(latitude, longitude));
    }

    /*
    *   Fragment-to-activity communication require containter activity to
    *   implement an interface with this method.
     */
    @Override
    public void onFragmentInteraction(String id) {


    }
    /*
   *   Fragment-to-activity communication require containter activity to
   *   implement an interface with this method.
    */
    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
