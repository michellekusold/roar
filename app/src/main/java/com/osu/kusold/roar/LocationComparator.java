package com.osu.kusold.roar;

import android.location.Location;
import android.util.Log;

import com.firebase.client.Firebase;

import java.util.Comparator;

/**
 * Location Comparator uses the user's current location and each event
 * location to find the event with the shortest distance and return it
 * to be used to arrange the Event Feed Fragments.
 */
public class LocationComparator implements Comparator<EventPost> {
        private double userLat = 40.0016740;
        private double userLong = -83.0134160;
        private Location userLoc;

        @Override
        public int compare(EventPost a, EventPost b) {
            if(userLoc != null) {
                Location eventLocA = setEventLocation(a);
                Location eventLocB = setEventLocation(b);

                // get event distances from user
                float distanceA = userLoc.distanceTo(eventLocA);
                float distanceB = userLoc.distanceTo(eventLocB);

                if (distanceA < distanceB) {
                    return 1;
                } else
                    return (-1);
            }
            else{
                return 0;
            }
        }

        // Sets up the users' location to be used for comparison to the events
        public void setUserLocation(double lat, double lng){
            userLat = lat;
            userLong = lng;
            userLoc = new Location("");
            userLoc.setLatitude(userLat);
            userLoc.setLatitude(userLong);
        }

        // creates a location for each event to be used for comparison to the user location
        private Location setEventLocation(EventPost event){
            Location loc = new Location("");
            loc.setLatitude(Double.parseDouble(event.latitude));
            loc.setLongitude(Double.parseDouble(event.longitude));
            return loc;
        }

}
