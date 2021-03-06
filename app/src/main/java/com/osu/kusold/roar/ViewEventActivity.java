package com.osu.kusold.roar;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Map;

/* Class to view the user's Profile */
public class ViewEventActivity extends ActionBarActivity {

    private Firebase fRef, fRefEvent, fRefUser;
    private TextView mEventName, mEventVenue, mEventAddress1, mEventAddress2, mEventCity, mEventZip;
    private TextView mEventDate,mEventTime;
    private TextView mEventCost, mEventMaxAttendance, mEventCurrentAttendance, mEventDescription, mEventCategory;
    private Intent viewEventIntent;
    String eventName, eventHost, eventCity, eventVenue, eventZip, eventAddress1, eventDate, eventTime, eventCost,
        eventCategory, eventMaxAttendance, eventCurrentAttendance, eventDescription, eventId;
    String userAttendanceStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        // Firebase root setup
        Firebase.setAndroidContext(this);
        fRef = new Firebase(getString(R.string.firebase_ref));
        fRefUser = fRef.child("users").child(fRef.getAuth().getUid());
        Intent viewEventIntent = getIntent();
        eventId = viewEventIntent.getStringExtra(EventManagerFragment.EVENT_UID);
        System.out.println("VIEW EVENT ID: " + eventId);
        fRefEvent = fRef.child("events").child(eventId);

        fRefEvent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                // Retrieve from firebase
                Map<String, Object> eventData = (Map<String, Object>) snapshot.getValue();
                eventName = eventData.get("name").toString();
                eventHost = eventData.get("host").toString();
                System.out.println("VIEW EVENT: name: " + eventName);
                DataSnapshot addrSnapshot = snapshot.child("address1");
                Map<String,Object> addrData = (Map<String,Object>) addrSnapshot.getValue();
                eventAddress1 = addrData.get("readable").toString();

                System.out.println("VIEW EVENT: addr" + eventAddress1);
                eventCity = eventData.get("city").toString();
                eventVenue = eventData.get("venue").toString();
                eventZip = eventData.get("zip").toString();
                eventDate = eventData.get("date").toString();
                eventTime = eventData.get("time").toString();
                eventCost = eventData.get("cost").toString();
                eventCategory = eventData.get("category").toString();
                eventMaxAttendance = eventData.get("maxAttendance").toString();
                eventCurrentAttendance = "0";
                if(eventData.containsKey("currentAttendance")) {
                    eventCurrentAttendance = eventData.get("currentAttendance").toString();
                }
                eventDescription = eventData.get("description").toString();

                // Get UI references
                // Display on screen
                mEventName = (TextView) findViewById(R.id.tvEventName);
                // Address
                mEventVenue = (TextView) findViewById(R.id.tvVenue);
                mEventAddress1 = (TextView) findViewById(R.id.tvAddressLine1);
                mEventCity = (TextView) findViewById(R.id.tvCity);
                mEventZip = (TextView) findViewById(R.id.tvZip);
                // Date and Time
                mEventDate = (TextView) findViewById(R.id.tvDate);
                mEventTime = (TextView) findViewById(R.id.tvTime);
                // Cost
                mEventCost = (TextView) findViewById(R.id.tvCost);
                // Category
                mEventCategory = (TextView) findViewById(R.id.tvCategory);
                // Attendance
                mEventMaxAttendance = (TextView) findViewById(R.id.tvMaxAttendance);
                mEventCurrentAttendance = (TextView) findViewById(R.id.tvCurrentAttendance);
                // Description
                mEventDescription = (TextView) findViewById(R.id.tvEventDescription);


                // Name
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

        Button joinEventButton = (Button) findViewById(R.id.join_event_btn);
        joinEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isInternetAvailable()) {
                    Firebase fRefAttendingEventUser = fRef.child("attendance").child(eventId).child(fRef.getAuth().getUid());

                    if (eventHost.equals(fRef.getAuth().getUid())) {
                        Toast.makeText(getApplicationContext(), "You are hosting the event.", Toast.LENGTH_SHORT).show();
                    } else if (userAttendanceStatus != null) {
                        if (userAttendanceStatus.equals("attending")) {
                            Toast.makeText(getApplicationContext(), "You are already attending the event.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        fRefUser.child("events").child(eventId).setValue("attending");
                        fRefAttendingEventUser.setValue("attending");
                        int numAttend = Integer.parseInt(eventCurrentAttendance);
                        numAttend++;
                        fRefEvent.child("currentAttendance").setValue(Integer.toString(numAttend));
                        Toast.makeText(getApplicationContext(), "You have joined the event.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "No internet connection present :(", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button editProfileButton = (Button) findViewById(R.id.edit_event);
        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToEditEvent();
            }
        });

        fRefUser.child("events").child(eventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(eventId)) {
                    userAttendanceStatus = dataSnapshot.getValue().toString();
                    Log.d("ViewEventActivity", "For event, user has status: " + userAttendanceStatus);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("ViewEventActivity", firebaseError.toString());
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

    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}