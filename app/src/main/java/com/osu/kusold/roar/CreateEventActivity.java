package com.osu.kusold.roar;


import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.firebase.client.Firebase;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;

import java.util.List;

public class CreateEventActivity extends ActionBarActivity {
    // Firebase Variables
    private Firebase fRef, fRefEvents, fRefUser, fRefNewEvent;
    private GeoFire geoFire;
    private String mUid;

    // Navigational Variables
    private String[] mDrawerTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private android.support.v7.widget.Toolbar mToolbar;

    // Event Variables
    private EditText mEventName, mEventVenue, mEventAddress1, mEventAddress2, mEventCity, mEventZip;
    private DatePicker mEventDate;
    private TimePicker mEventTime;
    private EditText mEventCost, mEventMaxAttendance, mEventDescription;
    private Spinner mCategoryOptions;
    private double geoLong, geoLat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Firebase root setup
        Firebase.setAndroidContext(this);
        fRef = new Firebase(getString(R.string.firebase_ref));
        fRefEvents = fRef.child("events");

        // ##### Set-up Input Areas
        // Name
        mEventName = (EditText) findViewById(R.id.create_event_name);
        // Address
        mEventVenue = (EditText) findViewById(R.id.etVenue);
        mEventAddress1 = (EditText) findViewById(R.id.etAddressLine1);
        mEventAddress2 = (EditText) findViewById(R.id.etAddressLine2);
        mEventCity = (EditText) findViewById(R.id.etCity);
        mEventZip = (EditText) findViewById(R.id.etZip);
        // Date and Time
        mEventDate = (DatePicker) findViewById(R.id.dpDate);
        mEventTime = (TimePicker) findViewById(R.id.tpTime);
        // Cost
        mEventCost = (EditText) findViewById(R.id.etCost);
        // Category
        mCategoryOptions = (Spinner) findViewById(R.id.categoryOptions);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategoryOptions.setAdapter(adapter);
        // Max Attendance
        mEventMaxAttendance = (EditText) findViewById(R.id.etMaxAttendance);
        // Description
        mEventDescription = (EditText) findViewById(R.id.etEventDescription);

        // ##### Top Bar and Settings Drawer #####
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
            // Called when a drawer has settled in a completely closed state.
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            // Called when a drawer has settled in a completely open state.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Roar");
                }
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if(getSupportActionBar() != null) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.setLogo(R.drawable.ic_launcher);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        Button submitProfileButton = (Button) findViewById(R.id.create_event_submit);
        submitProfileButton.setOnClickListener(eventSubmit);
    }


    /* Method to handle the event submission */
    private View.OnClickListener eventSubmit =new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            submitEvent();
            switchToEventFeed();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_create_event, menu);
        return true;
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position, id);
        }
    }

    // Swaps fragments in the main content view
    private void selectItem(int position, long id) {
        // Highlight the selected item, update the title, and close the drawer
        if(position == 3) {
            logout();
        } else {
            setTitle(mDrawerTitles[position]);
        }
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
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
        else if (id == R.id.action_create_event){
            Intent intent = new Intent(CreateEventActivity.this, CreateEventActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /* Stores the event's information as well as location details in the DB */
    private void submitEvent() {
        // Create Event Child (push generates a unique id for events (like users))
        // The eventId is then used as the key in GeoFire, and this is how we get the
        // key for nearby events.
        fRefNewEvent = fRefEvents.push();
        String eventId = fRefNewEvent.getKey();
        // store the event id in the user created events DB
        mUid = fRef.getAuth().getUid();
        fRefUser = fRef.child("users").child(mUid);

        fRefUser.child("events").child("created").setValue(eventId);
        fRefNewEvent.child("name").setValue(mEventName.getText().toString());

        // Geocoding
        Firebase fRefAddr1 = fRefNewEvent.child("address1");
        try {
            Geocoder geocoder = new Geocoder(this);
            //String theEventAddress = mEventVenue.getText().toString() + " " + mEventAddress1.getText().toString() + " " + mEventCity.getText().toString() + " " + mEventZip.getText().toString();
            String theEventAddress = mEventAddress1.getText().toString() + " " + mEventCity.getText().toString() + " " + mEventZip.getText().toString();
            Log.v("GeoCoder", "Address that will be input to GeoCoder: " + theEventAddress);
            List<Address> address = geocoder.getFromLocationName(theEventAddress, 1);
            if (address != null && address.size() > 0) {
                Log.v("GeoCoder", "GeoCoder found at least 1 address associated with the event info.");
                Address mAddr = address.get(0);
                Log.v("GeoCoder", "(Lat, Long): " + mAddr.getLatitude() + " , " + mAddr.getLongitude());
                fRefAddr1.child("readable").setValue(theEventAddress);
                geoLat = mAddr.getLatitude();
                geoLong = mAddr.getLongitude();
                fRefAddr1.child("latitude").setValue(geoLat);
                fRefAddr1.child("longitude").setValue(geoLong);

            } else {
                Log.v("GeoCoder", "GeoCoder found 0 address associated with the event info.");
                fRefAddr1.child("latitude").setValue(0);
                fRefAddr1.child("longitude").setValue(0);
            }
        }catch (Exception ex) {
            Log.v("CreateEventActivity", "Error: " + ex.toString());
        }
        // Attendance
        fRefNewEvent.child("attendance").child("currentAttendance").setValue(0); //No people are attending at creation
        fRefNewEvent.child("attendance").child("pending"); // UID of requested users
        fRefNewEvent.child("attendance").child("accepted"); // UID of users approved by host
        // Date and time
        fRefNewEvent.child("date").setValue(mEventDate.getCalendarView().getDate());
        if (mEventTime.getCurrentMinute() < 10){
            fRefNewEvent.child("time").setValue(mEventTime.getCurrentHour().toString() + "0" + mEventTime.getCurrentMinute().toString());
        }
        else {
            fRefNewEvent.child("time").setValue(mEventTime.getCurrentHour().toString() + mEventTime.getCurrentMinute().toString());
        }
        // venue
        fRefNewEvent.child("venue").setValue(mEventVenue.getText().toString());
        // Cost
        fRefNewEvent.child("cost").setValue(mEventCost.getText().toString());
        // Category
        fRefNewEvent.child("category").setValue(mCategoryOptions.getSelectedItem().toString());
        // Max Attendance
        fRefNewEvent.child("maxAttendance").setValue(mEventMaxAttendance.getText().toString());
        // Description
        fRefNewEvent.child("description").setValue(mEventDescription.getText().toString());
        // Host
        fRefNewEvent.child("host").setValue(fRef.getAuth().getUid());

        // Submit to GeoFire
        geoFire = new GeoFire(fRefNewEvent.child("GeoFire"));
        geoFire.setLocation(eventId, new GeoLocation(geoLat, geoLong));
    }

    private void switchToEventFeed() {
        Intent intent = new Intent(this, EventFeedActivity.class);
        startActivity(intent);
    }

    private void logout() {
        fRef.unauth();
        Intent intent = new Intent(CreateEventActivity.this, LoginActivity.class);
        startActivity(intent);
    }

}


