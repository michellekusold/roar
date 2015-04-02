package com.osu.kusold.roar;


import android.os.Bundle;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.firebase.client.Firebase;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.widget.ListView;
import android.widget.AdapterView;
import android.content.Intent;
import android.widget.EditText;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Button;

public class CreateEventActivity extends ActionBarActivity {
    // Firebase Variables
    private Firebase fRef, fRefUser, fRefEvent;
    private String mUid;

    // Navigational Variables
    private String[] mDrawerTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private android.support.v7.widget.Toolbar mToolbar;

    // Event Variables
    private Spinner mCategoryOptions;
    private EditText mNameView;
    private EditText mEventName;
    private EditText mEventVenue;
    private EditText mEventAddress1;
    private EditText mEventAddress2;
    private EditText mEventCity;
    private EditText mEventZip;
    private DatePicker mEventDate;
    private TimePicker mEventTime;
    private EditText mEventCost;
    private EditText mEventMaxAttendance;
    private EditText mEventDescription;
    private String mEvent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // Firebase root setup
        Firebase.setAndroidContext(this);
        fRef = new Firebase(getString(R.string.firebase_ref));
        mUid = fRef.getAuth().getUid();
        fRefEvent = fRef.child("users").child(mUid);


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
        submitProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // Age is a number editable view
                submitEvent();
                switchToEventFeed();
            }
        });
    }


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

    private void submitEvent() {

        // ##### store the event's information #####
        // Name
        fRefEvent.child("name").setValue(mNameView.getText().toString());

        //Address
        fRefEvent.child("venue").setValue(mEventVenue.getText().toString());
        fRefEvent.child("address1").setValue(mEventAddress1.getText().toString());
        fRefEvent.child("address2").setValue(mEventAddress2.getText().toString());
        fRefEvent.child("city").setValue(mEventCity.getText().toString());
        fRefEvent.child("zip").setValue(mEventZip.getText().toString());

        // Date and time
        fRefEvent.child("date").setValue(mEventDate.getCalendarView().getDate());
        fRefEvent.child("time").setValue(mEventTime.getCurrentHour().toString() + mEventTime.getCurrentMinute().toString());

        // Cost
        fRefEvent.child("cost").setValue(mEventCost.getText().toString());

        // Category
        fRefEvent.child("category").setValue(mCategoryOptions.getSelectedItem().toString());

        // Attendance
        fRefEvent.child("maxAttendance").setValue(mEventMaxAttendance.getText().toString());
        fRefEvent.child("currentAttendance").setValue(0); //No people are attending at creation

        // Description
        fRefEvent.child("description").setValue(mEventDescription.getText().toString());


        SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.share_pref_file), MODE_PRIVATE).edit();
        editor.putBoolean(fRef.getAuth().getUid() + R.string.is_profile_info_complete, true);
        editor.apply();
    }

    /* The next intent logically to occur after profile creation has completed */
    private void switchToEventFeed() {
        //Intent intent = new Intent(this, EventFeedActivity.class);
// uncomment to test view profile
        Intent intent = new Intent(this, ViewProfileActivity.class);
        startActivity(intent);
    }

    private void logout() {
        fRef.unauth();
        Intent intent = new Intent(CreateEventActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}

