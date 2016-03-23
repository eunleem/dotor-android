package net.team88.dotor.shared;

import android.location.Location;

/**
 * Created by Eun Leem on 3/7/2016.
 */
public class NearbyRequest {
    public NearbyRequest() {
        latitude = 37.00;
        longitude = 127.00;
        distance = 1000.00;
    }

    public NearbyRequest(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            distance = 1000.00;
        } else {
            latitude = 37.00;
            longitude = 127.00;
            distance = 1000.00;
        }
    }

    public Double latitude;
    public Double longitude;
    public Double distance;
}
