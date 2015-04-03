package com.osu.kusold.roar;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.Map;

/* Class to view the user's Profile */
public class ViewEventActivity extends ActionBarActivity {

    private Firebase fRef, fRefEvent;
    private TextView mEventName;
    private TextView mEventVenue, mEventAddress1, mEventAddress2, mEventCity, mEventZip;
    private TextView mEventDate,mEventTime;
    private TextView mEventCost, mEventMaxAttendance, mEventCurrentAttendance, mEventDescription, mEventCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        // Name of event that is passed in through calling this activity
        Bundle bundle = this.getIntent().getExtras();
        final String eventName = bundle.getString("param1");

        // Firebase root setup
        Firebase.setAndroidContext(this);
        fRef = new Firebase(getString(R.string.firebase_ref));
        fRefEvent = fRef.child("events").child(eventName);

        // Firebase Data

        // UI objects
        // Name
        mEventName = (TextView) findViewById(R.id.tvEventName);
        // Address
        mEventVenue = (TextView) findViewById(R.id.tvVenue);
        mEventAddress1 = (TextView) findViewById(R.id.tvAddressLine1);
        mEventCity = (TextView) findViewById(R.id.tvCity);
        mEventZip = (TextView) findViewById(R.id.tvZip);
        // Date and Time
        mEventDate = (TextView) findViewById(R.id.dpDate);
        mEventTime = (TextView) findViewById(R.id.tvTime);
        // Cost
        mEventCost = (TextView) findViewById(R.id.tvCost);
        // Category
        mEventCategory = (TextView) findViewById(R.id.categoryOptions);
        // Attendance
        mEventMaxAttendance = (TextView) findViewById(R.id.tvMaxAttendance);
        mEventCurrentAttendance = (TextView) findViewById(R.id.tvCurrentAttendance);
        // Description
        mEventDescription = (TextView) findViewById(R.id.tvEventDescription);

        fRefEvent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                // Retrieve from firebase
                Map<String, Object> eventData = (Map<String, Object>) snapshot.getValue();
                String eventName = eventData.get("name").toString();
                String eventAddress1 = eventData.get("eventAddress1").toString();
                String eventCity = eventData.get("city").toString();
                String eventVenue = eventData.get("venue").toString();
                String eventZip = eventData.get("zip").toString();
                String eventDate = eventData.get("date").toString();
                String eventTime = eventData.get("time").toString();
                String eventCost = eventData.get("cost").toString();
                String eventCategory = eventData.get("category").toString();
                String eventMaxAttendance = eventData.get("maxAttendance").toString();
                String eventCurrentAttendance = eventData.get("currentAttendance").toString();
                String eventDescription = eventData.get("description").toString();

                // Display on screen
                mEventName.setText(eventName);
                mEventAddress1.setText(eventAddress1);
                mEventCity.setText(eventCity);
                mEventVenue.setText(eventVenue);
                mEventZip.setText(eventZip);
                mEventDate.setText(eventDate);
                mEventTime.setText(eventTime);
                mEventCost.setText(eventCost);
                mEventCategory.setText(eventCategory);
                mEventMaxAttendance.setText(eventMaxAttendance);
                mEventCurrentAttendance.setText(eventCurrentAttendance);
                mEventDescription.setText(eventDescription);
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        Button editProfileButton = (Button) findViewById(R.id.edit_event);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToEditEvent();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* intent to switch back to editing the user's profile*/
    private void switchToEditEvent() {
        Intent intent = new Intent(this, CreateEventActivity.class);
        startActivity(intent);
    }
}