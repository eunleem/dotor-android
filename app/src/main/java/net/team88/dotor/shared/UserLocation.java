package net.team88.dotor.shared;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;


import net.team88.dotor.BuildConfig;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class UserLocation {
    private static final String TAG = "UserLocation";
    private static final String KEY_LONGITUDE = "LONGITUDE";
    private static final String KEY_LATITUDE = "LATITUDE";
    private static final String KEY_PLACE_NAME = "PLACE_NAME";


    private static UserLocation sInstance;

    private Context context;

    LocationManager locationManager;

    Location lastKnownLocation;
    String lastKnownLocationName;

    private UserLocation(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        SharedPreferences data = this.context.getSharedPreferences("UserLocation", Activity.MODE_PRIVATE);
        lastKnownLocation = new Location("Provider");

        // There is no putDouble. Use putLong instead and do manual conversion
        // #REF: http://stackoverflow.com/a/18098090/4694036
        lastKnownLocation.setLatitude((double) data.getLong(KEY_LATITUDE, Double.doubleToLongBits(37.605886)));
        lastKnownLocation.setLongitude((double) data.getLong(KEY_LONGITUDE, Double.doubleToLongBits(126.922720)));

        lastKnownLocationName = data.getString(KEY_PLACE_NAME, "");
    }

    public static synchronized UserLocation getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new UserLocation(context);
        }
        return sInstance;
    }

    private void setLastKnownLocation(Location location) {
        lastKnownLocation = location;
        final SharedPreferences data = this.context.getSharedPreferences("UserLocation", Activity.MODE_PRIVATE);
        // There is not putDouble. Use putLong instead and do manual conversion
        // #REF: http://stackoverflow.com/a/18098090/4694036
        data.edit()
                .putLong(KEY_LONGITUDE, (long) location.getLongitude())
                .putLong(KEY_LATITUDE, (long) location.getLatitude())
                .apply();

        // Get Addresses use network call.
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<Address> addresses = getAddresses(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 3, Locale.KOREA);
                if (addresses != null && addresses.size() > 0) {
                    String cityNameKorean = getCityNameKorean(addresses.get(0));
                    data.edit()
                            .putString(KEY_PLACE_NAME, cityNameKorean)
                            .apply();
                }
            }
        });
    }

    /**
     * Last known location
     *
     * @return null when permission is not granted or last known location is unknown.
     */
    public Location getLastLocation() {
        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);

            if (location == null) {
                Answers.getInstance().logCustom(
                        new CustomEvent("Location")
                                .putCustomAttribute("GetLastLocation", "Failed")
                );
            } else {
                this.setLastKnownLocation(location);
                Answers.getInstance().logCustom(
                        new CustomEvent("Location")
                                .putCustomAttribute("GetLastLocation", "Successful")
                );
            }

            return location;
        }

        Answers.getInstance().logCustom(
                new CustomEvent("Location")
                        .putCustomAttribute("GetLastLocation", "PermissionDenied")
        );
        Log.d(TAG, "getLastLocation: Permission Denied.");
        if (this.lastKnownLocation != null) {
            // When everything fails return Default Location.
            return this.lastKnownLocation;
        }
        return null;
    }

    /**
     * @return the last know best location
     */
    public Location getLastBestLocation() {
        Location location = null;
        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {

            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            long GPSLocationTime = 0;
            if (locationGPS != null) {
                GPSLocationTime = locationGPS.getTime();
            }

            long NetLocationTime = 0;

            if (locationNet != null) {
                NetLocationTime = locationNet.getTime();
            }

            if (0 < GPSLocationTime - NetLocationTime) {
                Log.d(TAG, "getLastBestLocation: Used GPS location");
                location = locationGPS;
            } else {
                Log.d(TAG, "getLastBestLocation: Used Network location");
                location = locationNet;
            }

            if (location == null) {
                Answers.getInstance().logCustom(
                        new CustomEvent("Location")
                                .putCustomAttribute("GetLastBestLocation", "Failed")
                );
                if (this.lastKnownLocation != null) {
                    Log.d(TAG, "getLastBestLocation: Returning last known location");
                    // When everything fails return Default Location.
                    return this.lastKnownLocation;
                }

                Log.d(TAG, "getLastBestLocation: Returning null");
            } else {
                this.setLastKnownLocation(location);
                Answers.getInstance().logCustom(
                        new CustomEvent("Location")
                                .putCustomAttribute("GetLastBestLocation", "Successful")
                );
                Log.d(TAG, "getLastBestLocation: LngLat: " +
                        String.valueOf(location.getLongitude()) + "," +
                        String.valueOf(location.getLatitude()));
            }
        }
        return location;
    }

    /**
     * Get City Name by Locale
     *
     * @param locale
     * @return City name on success. Null on failure.
     */
    public String getCityName(Locale locale) {
        Location location = getLastLocation();
        if (location == null) {
            Log.d(TAG, "Could not get city name.");
            return "";
        }
        List<Address> addresses = getAddresses(location.getLatitude(), location.getLongitude(), 5, locale);
        if (BuildConfig.DEBUG) {
            for (Address addr : addresses) {
                Log.d(TAG, "Current Location candidate: " + addr.toString());
            }
        }

        if (addresses == null || addresses.size() == 0) {
            return null;
        }

        if (locale == Locale.KOREAN || locale == Locale.KOREA) {
            return getCityNameKorean(addresses.get(0));

        } else {
            return getCityNameUsa(addresses.get(0));
        }

    }

    public String getCityNameUsa() {
        Location location = getLastLocation();
        if (location == null) {
            Log.d(TAG, "Could not get city name.");
            return "";
        }
        List<Address> addresses = getAddresses(location.getLatitude(), location.getLongitude(), 5, Locale.ENGLISH);
        if (BuildConfig.DEBUG) {
            for (Address addr : addresses) {
                Log.i(TAG, "Address from Addresses: " + addr.toString());
            }
        }
        return getCityNameUsa(addresses.get(0));
    }

    public String getCityNameKorean(Location location) {
        if (location == null) {
            Log.d(TAG, "Could not get city name.");
            return "";
        }

        List<Address> addresses = getAddresses(location.getLatitude(), location.getLongitude(), 5, Locale.KOREAN);
        if (BuildConfig.DEBUG) {
            for (Address addr : addresses) {
                Log.i(TAG, "Address from Addresses: " + addr.toString());
            }
        }
        return getCityNameKorean(addresses.get(0));
    }

    public String getCityNameKorean() {
        Location location = getLastLocation();
        if (location == null) {
            Log.d(TAG, "Could not get city name.");
            return "";
        }

        List<Address> addresses = getAddresses(location.getLatitude(), location.getLongitude(), 5, Locale.KOREAN);
        if (BuildConfig.DEBUG) {
            for (Address addr : addresses) {
                Log.i(TAG, "Address from Addresses: " + addr.toString());
            }
        }
        return getCityNameKorean(addresses.get(0));
    }

    private String getCityNameUsa(Address address) {
        if (address == null) {
            Log.e(TAG, "getCityName: Address is null! Returning empty string.");
            return "";
        }

        StringBuilder builder = new StringBuilder();

        if (address.getLocality() != null) {
            builder.append(" ").append(address.getLocality());
        }
        if (address.getAdminArea() != null) {
            builder.append(" ").append(address.getAdminArea());
        }

        return builder.toString().trim();
    }

    public String getCityNameKorean(Address address) {
        if (address == null) {
            Log.e(TAG, "getCityName: Address is null! Returning empty string.");
            return "";
        }

        StringBuilder builder = new StringBuilder();
        if (address.getAdminArea() != null) {
            builder.append(" ").append(address.getAdminArea());
        }

        if (address.getLocality() != null) {
            builder.append(" ").append(address.getLocality());
        }

        if (address.getThoroughfare() != null) {
            builder.append(" ").append(address.getThoroughfare());
        }

        return builder.toString().replaceFirst("대한민국", "").trim();
    }

    public List<Address> getAddresses(double latitude, double longitude, int count, Locale locale) {
        if (locale == null) {
            locale = Locale.ENGLISH;
        }

        Geocoder gcd = new Geocoder(context, locale);
        try {
            return gcd.getFromLocation(latitude, longitude, count);
        } catch (IOException e) {
            Log.w(TAG, "getAddresses: Failed to get Addresses from from Coordinates.", e);
        }
        return null;
    }
}


