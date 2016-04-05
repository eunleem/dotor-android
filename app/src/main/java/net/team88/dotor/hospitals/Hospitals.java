package net.team88.dotor.hospitals;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.NearbyRequest;
import net.team88.dotor.shared.Server;
import net.team88.dotor.shared.UserLocation;
import net.team88.dotor.utils.GsonUtils;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Eun Leem on 3/7/2016.
 */
public class Hospitals {
    private static final String TAG = "Hospitals";

    private static final String KEY_HOSPITALS = "HOSPITALS";
    private static final String KEY_LAST_SELECTED_HOSPITAL_ID = "LAST_SELECTED_HOSPITAL_ID";

    private static Hospitals sInstance;
    private Context context;

    SharedPreferences mData;
    LinkedHashMap<ObjectId, Hospital> hospitalHashMap;

    ObjectId lastSelectedHospitalId;

    private Hospitals(Context context) {
        this.context = context.getApplicationContext();
        this.mData = this.context.getSharedPreferences("Hospitals", Context.MODE_PRIVATE);
        this.hospitalHashMap = new LinkedHashMap<>();
        load();
    }

    public static synchronized Hospitals getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Hospitals(context);
        }
        return sInstance;
    }

    private void load() {
        String hospitalsStr = this.mData.getString(KEY_HOSPITALS, "");
        if (hospitalsStr.isEmpty() == false) {
            // REF: http://stackoverflow.com/a/17300003/4694036
            Log.d(TAG, "load: HospitalStr: " + hospitalsStr);
            Hospital[] hospitalArray = GsonUtils.getGson().fromJson(hospitalsStr, Hospital[].class);
            for (Hospital hospital : hospitalArray) {
                hospitalHashMap.put(hospital.id, hospital);
            }

            Log.d(TAG, String.valueOf(hospitalArray.length) + " hospitals loaded.");
        }

        String lastSelectedHospitalIdStr = this.mData.getString(KEY_LAST_SELECTED_HOSPITAL_ID, "");
        if (lastSelectedHospitalIdStr.isEmpty() == false) {
            lastSelectedHospitalId = new ObjectId(lastSelectedHospitalIdStr);
        }
    }

    private void save() {
        String hospitalsJsonStr = GsonUtils.getGson().toJson(this.hospitalHashMap.values());
        this.mData.edit().putString(KEY_HOSPITALS, hospitalsJsonStr).apply();
    }

    public void reset() {
        this.mData.edit().clear().commit();
        sInstance = null;
    }

    public void setHospitals(List<Hospital> hospitalList) {
        hospitalHashMap.clear();
        for (Hospital hospital : hospitalList) {
            hospitalHashMap.put(hospital.id, hospital);
        }
        Log.d(TAG, "setHospitals: hospitalHashMap size:" + String.valueOf(hospitalHashMap.size()));
        this.save();
    }

    public void setLastSelectedHospitalId(ObjectId lastSelectedHospitalId) {
        if (lastSelectedHospitalId == null) {
            Log.e(TAG, "setLastSelectedHospitalId: Null");
            return;
        }
        this.lastSelectedHospitalId = lastSelectedHospitalId;
        mData.edit().putString(KEY_LAST_SELECTED_HOSPITAL_ID, lastSelectedHospitalId.toHexString()).apply();
    }

    public void setLastSelectedHospital(Hospital hospital) {
        this.setLastSelectedHospitalId(hospital.id);
    }

    public Hospital getLastSelectedHospital() {
        if (this.lastSelectedHospitalId == null) {
            return null;
        }

        return this.hospitalHashMap.get(this.lastSelectedHospitalId);
    }


    public Collection<Hospital> getHospitals() {
        return this.hospitalHashMap.values();
    }

    public void getHospitalsFromServer(double latitude, double longitude, double distance, final Runnable onSuccess) {
        NearbyRequest nearbyRequest = new NearbyRequest();
        nearbyRequest.longitude = longitude;
        nearbyRequest.latitude = latitude;
        nearbyRequest.distance = distance;

        DotorWebService service = Server.getInstance(context).getService();
        service.getHospitalsNearby(nearbyRequest).enqueue(new Callback<HospitalsResponse>() {
            @Override
            public void onResponse(Call<HospitalsResponse> call, final Response<HospitalsResponse> response) {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "nearbyRequest: Failed.");
                    return;
                }

                if (response.body().status < 0) {
                    Log.d(TAG, "nearbyRequest: Failed. msg: " + response.body().message);
                    return;
                }

                int count = 0;
                if (response.body().hospitals != null) {
                    count = response.body().hospitals.size();
                }

                Log.d(TAG, "nearbyRequest: Successful. " + String.valueOf(count));

                final ArrayList<Hospital> hospitals = response.body().hospitals;
                setHospitals(response.body().hospitals);
                onSuccess.run();
            }

            @Override
            public void onFailure(Call<HospitalsResponse> call, Throwable t) {
                Log.d(TAG, "nearbyRequest: Failed. " + t.getMessage());
            }
        });
    }

    public void getNearbyHospitalsFromServer(double distance, final Runnable onSuccess) {
        NearbyRequest nearbyRequest = createNearbyRequest(distance);
        DotorWebService service = Server.getInstance(context).getService();
        service.getHospitalsNearby(nearbyRequest).enqueue(new Callback<HospitalsResponse>() {
            @Override
            public void onResponse(Call<HospitalsResponse> call, final Response<HospitalsResponse> response) {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "nearbyRequest: Failed.");
                    return;
                }

                if (response.body().status < 0) {
                    Log.d(TAG, "nearbyRequest: Failed. msg: " + response.body().message);
                    return;
                }

                int count = 0;
                if (response.body().hospitals != null) {
                    count = response.body().hospitals.size();
                }

                Log.d(TAG, "nearbyRequest: Successful. " + String.valueOf(count));

                final ArrayList<Hospital> hospitals = response.body().hospitals;
                setHospitals(hospitals);
                onSuccess.run();
            }

            @Override
            public void onFailure(Call<HospitalsResponse> call, Throwable t) {
                Log.d(TAG, "nearbyRequest: Failed. " + t.getMessage());
            }
        });
    }

    private NearbyRequest createNearbyRequest(double distance) {
        Location location = UserLocation.getInstance(context).getLastBestLocation();
        NearbyRequest nearbyRequest = new NearbyRequest();
        nearbyRequest.distance = distance;
        if (location == null) {
            Answers.getInstance().logCustom(
                    new CustomEvent("Location")
                            .putCustomAttribute("GetLocation", "Failed")
            );
            Log.d(TAG, "GetLocation: returned null. Fixed default values are used for coordinates.");
            //nearbyRequest.longitude = 127.126344; // TODO Default longitude
            //nearbyRequest.latitude = 37.412049;
            nearbyRequest.longitude = 137.126344;
            nearbyRequest.latitude = 47.412049;

        } else {
            Answers.getInstance().logCustom(
                    new CustomEvent("Location")
                            .putCustomAttribute("GetLocation", "Successful")
            );
            nearbyRequest.longitude = location.getLongitude();
            nearbyRequest.latitude = location.getLatitude();

        }
        return nearbyRequest;
    }
}
