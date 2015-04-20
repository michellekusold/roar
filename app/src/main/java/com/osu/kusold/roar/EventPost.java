package com.osu.kusold.roar;

import java.util.Map;

/**
 * Created by johnsgresham on 4/2/15.
 */
public class EventPost {

    Map<String, Object> mEventData;
    public String eventName, venue, city, zip, date, time, cost, category;
    public String maxAttendance, description, hostUid, image, thumbnail, eventUid;
    public String latitude, longitude;

    public EventPost(Map<String, Object> eventData) {
        mEventData = eventData;
        eventName = eventData.get("name").toString();
        // location
        // parse the data from the address json string
        String addr = eventData.get("address1").toString();
        String[] tokens = addr.replaceAll("=", " ").split(" ");

        for(int i=0; i<tokens.length; i++){
            System.out.println("TOKEN: " + i+ " " + tokens[i]);
            if(tokens[i].contains("lat") && i+1<tokens.length){
                tokens[i] = tokens[i+1].replaceAll("[^\\d.]", "");
                tokens[i+1] = tokens[i+1].replaceAll(",$", "");
                tokens[i+1] = tokens[i+1].replaceAll("[{}]]", "");
                if (tokens[i+1].length() > 0) {
                    tokens[i+1] = tokens[i+1].substring(0, tokens[i+1].length()-1);
                }
                latitude = tokens[i+1];
            }
            if(tokens[i].contains("long") && i+1<tokens.length){
                tokens[i] = tokens[i+1].replaceAll("[^\\d.]", "");
                tokens[i+1] = tokens[i+1].replaceAll(",$", "");
                tokens[i+1] = tokens[i+1].replaceAll("[{}]", "");
                longitude = tokens[i+1];
            }
        }
        System.out.println("EVENT DATA: lat " + latitude);
        System.out.println("EVENT DATA: long " + longitude);

        //venue = eventData.get("venue").toString();
        city = eventData.get("city").toString();
        zip = eventData.get("zip").toString();
        if(eventData.containsKey("image")) {
            image = eventData.get("image").toString();
        }if(eventData.containsKey("thumbnail")) {
            thumbnail = eventData.get("thumbnail").toString();
        }
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
