package net.team88.dotor.shared;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

import net.team88.dotor.reviews.SearchSettings;

/**
 * Created by Eun Leem on 3/7/2016.
 */
public class NearbyRequest {

    public NearbyRequest() {
        latitude = SearchSettings.DEFAULT_LATITUDE;
        longitude = SearchSettings.DEFAULT_LONGITUDE;
        distance = 1000.00;
    }

    public NearbyRequest(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            distance = 1000.00;
        } else {
            latitude = SearchSettings.DEFAULT_LATITUDE;
            longitude = SearchSettings.DEFAULT_LONGITUDE;
            distance = 1000.00;
        }
    }

    @SerializedName("latitude")
    public Double latitude;

    @SerializedName("longitude")
    public Double longitude;

    @SerializedName("distance")
    public Double distance;
}
