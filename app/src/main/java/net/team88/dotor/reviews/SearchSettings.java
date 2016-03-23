package net.team88.dotor.reviews;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import net.team88.dotor.shared.NearbyRequest;

import java.util.ArrayList;

/**
 * Created by Eun Leem on 3/11/2016.
 */
public class SearchSettings {
    private static final String TAG = "SearchSettings";
    private static final String KEY_LOCATION_NAME = "LocationName";
    private static final String KEY_LOCATION_LATITUDE = "Latitude";
    private static final String KEY_LOCATION_LONGITUDE = "Longitude";
    private static final String KEY_LOCATION_DISTANCE = "Distance";
    private static final String KEY_CATEGORIES = "Categories";

    private class PetFilter {
        boolean dog;
        boolean cat;

        boolean male;
        boolean female;

        int size;

        int ageMin;
        int ageMax;
    }

    private static SearchSettings sInstance;
    private Context context;

    private SharedPreferences data;
    private NearbyRequest nearbyRequest;

    private String locationName;

    private ArrayList<String> selectedCategories;

    private PetFilter petFiler;


    private SearchSettings(Context context) {
        this.context = context;
        this.data = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);

        locationName = this.data.getString(KEY_LOCATION_NAME, "");

        nearbyRequest = new NearbyRequest();
        nearbyRequest.latitude = (double) this.data.getLong(KEY_LOCATION_LATITUDE, 37);
        nearbyRequest.longitude = (double) this.data.getLong(KEY_LOCATION_LONGITUDE, 127);
        nearbyRequest.distance = (double) this.data.getLong(KEY_LOCATION_DISTANCE, 1000);

        String categories = this.data.getString(KEY_CATEGORIES, "");
        this.selectedCategories = new ArrayList<>();
        if (!categories.isEmpty()) {
            String[] split = categories.split(",");
            for (String cate : split) {
                selectedCategories.add(cate);
            }
        }
    }

    public static synchronized SearchSettings getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SearchSettings(context.getApplicationContext());
        }
        return sInstance;
    }

    public String getLocationName() {
        return this.locationName;
    }

    public NearbyRequest getNearbyRequest() {
        return this.nearbyRequest;
    }

    public void setLocationSetting(String name, NearbyRequest nearbyRequest) {
        this.locationName = name;
        if (nearbyRequest == null) {
            Log.d(TAG, "setLocationSetting: null");
            return;
        }
        this.nearbyRequest = nearbyRequest;

        this.data.edit()
                .putString(KEY_LOCATION_NAME, this.locationName)
                .putLong(KEY_LOCATION_LATITUDE, (long) nearbyRequest.latitude.doubleValue())
                .putLong(KEY_LOCATION_LONGITUDE, (long) nearbyRequest.longitude.doubleValue())
                .putLong(KEY_LOCATION_DISTANCE, (long) nearbyRequest.distance.doubleValue())
                .apply();
    }


    public ArrayList<String> getSelectedCategories() {
        return this.selectedCategories;
    }

    public void setSelectedCaterories(String categories) {
        this.selectedCategories.clear();
        String[] split = categories.split(",");
        for (String cate : split) {
            this.selectedCategories.add(cate);
        }

        this.data.edit()
                .putString(KEY_CATEGORIES, categories)
                .apply();
    }


}
