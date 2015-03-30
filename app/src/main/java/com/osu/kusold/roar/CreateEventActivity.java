package com.osu.kusold.roar;


import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class CreateEventActivity extends ActionBarActivity {
    // Firebase Variables
    private Firebase fRef, fRefEvents, fRefNewEvent;
    private GeoFire geoFire;

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
        geoFire = new GeoFire(fRef.child("GeoFire"));

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

        // Name
        fRefNewEvent.child("name").setValue(mEventName.getText().toString());

        // Geocode address and store it as long/lat pair
        double longitude, latitude;
        String geoRequest = "https://maps.googleapis.com/maps/api/geocode/json?key=" + getString(R.string.api_key);
        // TODO: do we need checking here to make sure an address exists or is it mandatory?
        geoRequest= geoRequest + "&address=" + geoFormat(mEventAddress1);
        geoRequest = geoRequest + ",+" + geoFormat(mEventCity);
        geoRequest = geoRequest + ",+" + geoFormat(mEventZip);
        geocodeAddr(geoRequest);

        Firebase fRefAddr1 = fRefNewEvent.child("address1");
        fRefAddr1.child("readable").setValue(mEventAddress1.getText().toString());
        fRefAddr1.child("latitude").setValue(geoLat);
        fRefAddr1.child("longitude").setValue(geoLong);

        fRefNewEvent.child("venue").setValue(mEventVenue.getText().toString());
        fRefNewEvent.child("city").setValue(mEventCity.getText().toString());
        fRefNewEvent.child("zip").setValue(mEventZip.getText().toString());

        // TODO: currently ignoring address2 for geocoding
        fRefNewEvent.child("address2").setValue(mEventAddress2.getText().toString());
        Firebase fRefAddr = fRefNewEvent.child("address2");

        // Date and time
        fRefNewEvent.child("date").setValue(mEventDate.getCalendarView().getDate());
        if (mEventTime.getCurrentMinute() < 10){
            fRefNewEvent.child("time").setValue(mEventTime.getCurrentHour().toString() + "0" + mEventTime.getCurrentMinute().toString());
        }
        else {
            fRefNewEvent.child("time").setValue(mEventTime.getCurrentHour().toString() + mEventTime.getCurrentMinute().toString());
        }
        // Cost
        fRefNewEvent.child("cost").setValue(mEventCost.getText().toString());
        // Category
        fRefNewEvent.child("category").setValue(mCategoryOptions.getSelectedItem().toString());
        // Max Attendance
        fRefNewEvent.child("maxAttendance").setValue(mEventMaxAttendance.getText().toString());
        // Description
        fRefNewEvent.child("description").setValue(mEventDescription.getText().toString());

        // Submit to GeoFire
        // TODO: Currently we just use the GPS location, but it should be the long & lat of the event
        /*LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double latitude, longitude;
        if(location == null){
            // GEOFIRE TEST VARS (S.E.L.)
            latitude = 40.0016740;
            longitude = -83.0134160;
        }
        else {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
        geoFire.setLocation(eventId, new GeoLocation(latitude, longitude));*/
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

    /* Formats EditText into the correct structure for geocoding */
    private String geoFormat(EditText geoStr){
        return geoStr.getText().toString().replaceAll(" ", "+");
    }

    private void geocodeAddr(String geoRequest){
        HttpGet httpGet = new HttpGet(geoRequest);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(stringBuilder.toString());

            double lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");

            double lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");

            Log.d("latitude", "" + lat);
            Log.d("longitude", "" + lng);
            geoLat = lat;
            geoLong = lng;
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}


