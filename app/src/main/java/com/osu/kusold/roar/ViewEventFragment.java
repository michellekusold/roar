package com.osu.kusold.roar;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewEventFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ViewEventFragment extends Fragment {
    private Firebase fRef, fRefEvent, fRefUser;
    private TextView mEventName, mEventVenue, mEventAddress1, mEventAddress2, mEventCity, mEventZip;
    private TextView mEventDate,mEventTime;
    private TextView mEventCost, mEventMaxAttendance, mEventCurrentAttendance, mEventDescription, mEventCategory;
    private Intent viewEventIntent;
    String eventName, eventHost, eventCity, eventVenue, eventZip, eventAddress1, eventDate, eventTime, eventCost,
            eventCategory, eventMaxAttendance, eventCurrentAttendance, eventDescription, eventId;
    String userAttendanceStatus;

    private OnFragmentInteractionListener mListener;

    public ViewEventFragment() {
        // Required empty public constructor
    }

    public static ViewEventFragment newInstance(String EventArgument) {

        ViewEventFragment newViewEventFragment = new ViewEventFragment();

        Bundle bund = new Bundle();
        bund.putString("selectedEvent", EventArgument);
        newViewEventFragment.setArguments(bund);

        return newViewEventFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String selectedEvent = getArguments().getString("selectedEvent");
        // Firebase root setup
        Firebase.setAndroidContext(getActivity());
        fRef = new Firebase(getString(R.string.firebase_ref));
        fRefUser = fRef.child("users").child(fRef.getAuth().getUid());
        Log.i("selected_event_id: ", selectedEvent);

        System.out.println("VIEW EVENT ID: " + selectedEvent);
        fRefEvent = fRef.child("events").child(selectedEvent);
        eventId = selectedEvent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_view_event, container, false);

        Button joinEventButton = (Button) view.findViewById(R.id.join_event_btn);
        joinEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Firebase fRefAttendingEventUser = fRef.child("attendance").child(eventId).child(fRef.getAuth().getUid());

                if(eventHost.equals(fRef.getAuth().getUid())) {
                    Toast.makeText(getActivity(), "You are hosting the event.", Toast.LENGTH_SHORT).show();
                } else if(userAttendanceStatus != null) {
                    if(userAttendanceStatus.equals("attending")) {
                        Toast.makeText(getActivity(), "You are already attending the event." , Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    fRefUser.child("events").child(eventId).setValue("attending");
                    fRefAttendingEventUser.setValue("attending");
                    int numAttend = Integer.parseInt(eventCurrentAttendance);
                    numAttend++;
                    fRefEvent.child("currentAttendance").setValue(Integer.toString(numAttend));
                    Toast.makeText(getActivity(), "You have joined the event." , Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button editEventButton = (Button) view.findViewById(R.id.edit_event);
        editEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToEditEvent();
            }
        });

        fRefEvent.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(isAdded()) {
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
                    mEventName = (TextView) view.findViewById(R.id.tvEventName);
                    // Address
                    mEventVenue = (TextView) view.findViewById(R.id.tvVenue);
                    mEventAddress1 = (TextView) view.findViewById(R.id.tvAddressLine1);
                    mEventCity = (TextView) view.findViewById(R.id.tvCity);
                    mEventZip = (TextView) view.findViewById(R.id.tvZip);
                    // Date and Time
                    mEventDate = (TextView) view.findViewById(R.id.tvDate);
                    mEventTime = (TextView) view.findViewById(R.id.tvTime);
                    // Cost
                    mEventCost = (TextView) view.findViewById(R.id.tvCost);
                    // Category
                    mEventCategory = (TextView) view.findViewById(R.id.tvCategory);
                    // Attendance
                    mEventMaxAttendance = (TextView) view.findViewById(R.id.tvMaxAttendance);
                    mEventCurrentAttendance = (TextView) view.findViewById(R.id.tvCurrentAttendance);
                    // Description
                    mEventDescription = (TextView) view.findViewById(R.id.tvEventDescription);


                    // Update Displayed Values
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
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        return view;
    }

    /* intent to switch back to editing the event*/
    private void switchToEditEvent() {
        Intent intent = new Intent(getActivity(), CreateEventActivity.class);
        startActivity(intent);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        public void onFragmentInteraction(Uri uri);
    }

}
