package com.osu.kusold.roar;

import java.util.Map;

/**
 * Created by johnsgresham on 4/2/15.
 */
public class EventPost {

    public String eventName;
    public String venue;
    public String city;
    public String zip;
    public String date;
    public String time;
    public String cost;
    public String category;
    public String maxAttendance;
    public String description;
    public String hostUid;
    Map<String, Object> mEventData;

    public EventPost(Map<String, Object> eventData) {
        mEventData = eventData;
        eventName = eventData.get("name").toString();
        //venue = eventData.get("venue").toString();
        city = eventData.get("city").toString();
        zip = eventData.get("zip").toString();

        // Date and time
        date = eventData.get("date").toString();

        time = eventData.get("time").toString();
        // Cost
        cost = eventData.get("cost").toString();
        // Category
        category = eventData.get("category").toString();
        // Max Attendance
        maxAttendance = eventData.get("maxAttendance").toString();
        // Description
        description = eventData.get("description").toString();
        if(eventData.get("host") != null) {
            hostUid = eventData.get("host").toString();
        }
    }
}
