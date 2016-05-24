package net.team88.dotor.reviews;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import net.team88.dotor.R;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.NearbyRequest;
import net.team88.dotor.shared.Server;
import net.team88.dotor.shared.UserLocation;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewListActivity extends AppCompatActivity {

    public static final String KEY_MODE_LOCATION = "MODE_LOCATION";
    public static final String KEY_MODE_CATEGORY = "MODE_CATEGORY";
    private static final String TAG = "REVIEW_LIST";

    private NearbyRequest nearbyRequest;

    //private Pet pet;
    private String categories;
    private String currentLocationName;
    ArrayList<String> selectedCategories;

    private SearchSettings searchSettings;
    private TextView textNothingMessage;
    private Snackbar allReviewSnackbar;

    enum Mode {
        ALL,
        LOCATION,
        CATEGORY
    }

    Mode mode;

    SwipeRefreshLayout layoutSwipeRefresh;

    RecyclerView recyclerViewReviews;
    ReviewsRecyclerViewAdapter viewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);

        if (!Fabric.isInitialized()) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_review_list);

        setupBasicElements();
        registerElements();

        searchSettings = SearchSettings.getInstance(this);

        setMode();

        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        viewAdapter = new ReviewsRecyclerViewAdapter(this, this.mode);
        recyclerViewReviews.setAdapter(viewAdapter);

        setupSwipeRefreshLayout();

        getReviews();
    }

    private void setupBasicElements() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void registerElements() {
        layoutSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.layoutSwipeRefresh);
        recyclerViewReviews = (RecyclerView) findViewById(R.id.recyclerViewReviews);

        textNothingMessage = (TextView) findViewById(R.id.textNothingMessage);
    }

    private void setMode() {
        String action = getIntent().getAction();

        if (action.equalsIgnoreCase(KEY_MODE_LOCATION)) {
            Log.d(TAG, "setMode: Mode Location");
            mode = Mode.LOCATION;

            String locationName = searchSettings.getLocationName();
            if (locationName.isEmpty()) {
                // There is no previous Settings, Try to get Location.
                UserLocation userLocation = UserLocation.getInstance(this);
                Location location = userLocation.getLastLocation();
                currentLocationName = userLocation.getCityNameKorean();
                nearbyRequest = new NearbyRequest(location);

            } else {
                // Use Saved Settings
                currentLocationName = locationName;
                nearbyRequest = searchSettings.getNearbyRequest();
            }

        } else if (action.equalsIgnoreCase(KEY_MODE_CATEGORY)) {
            Log.d(TAG, "setMode: Mode Category");
            mode = Mode.CATEGORY;

            selectedCategories = searchSettings.getSelectedCategories();

            StringBuilder stringBuilder = new StringBuilder();

            for (String category : selectedCategories) {
                stringBuilder
                        .append(",")
                        .append(category);
            }

            if (stringBuilder.length() > 1) {
                stringBuilder.delete(0, 1);
            } else {
                // If Nothing is Selected, Select first item.
                String category = getResources().getStringArray(R.array.review_category_array)[0];
                stringBuilder.append(category);
            }

            categories = stringBuilder.toString();

        } else {
            Log.d(TAG, "setMode: Mode ALL");
            mode = Mode.ALL;
        }
    }

    private void setupSwipeRefreshLayout() {
        layoutSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getReviews();
            }
        });

        layoutSwipeRefresh.setDistanceToTriggerSync(300);
        layoutSwipeRefresh.setColorSchemeResources(R.color.colorAccent, R.color.colorSecondary, R.color.colorPrimary);
        TypedValue typed_value = new TypedValue();
        this.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        layoutSwipeRefresh.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (mode) {
            case ALL:
                break;
            case LOCATION:
                getMenuInflater().inflate(R.menu.menu_reviews_by_location, menu);
                break;
            case CATEGORY:
                getMenuInflater().inflate(R.menu.menu_reviews_by_category, menu);
                break;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getFragmentManager();
        switch (item.getItemId()) {
            case R.id.action_location: {
                // Set Location
                SetLocationDialogFragment dialogFragment = new SetLocationDialogFragment();
                dialogFragment.show(fm, "Set Location Fragment");
                break;
            }
            case R.id.action_category: {
                SetCategoriesDialogFragment frag = new SetCategoriesDialogFragment();
                frag.show(fm, "Set Categories Fragment");
                break;
            }

            case R.id.action_pet_filter: {
                SetFilterDialogFragment frag = new SetFilterDialogFragment();
                frag.show(fm, "Set Filter Fragment");
                break;
            }

            default:
                Log.d(TAG, "onOptionsItemSelected: Event Unhandled.");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public class SetCategoriesDialogFragment extends DialogFragment {

        private LinearLayout layoutRoot;

        private Button buttonReset;
        private Button buttonCancel;
        private Button buttonSetFilter;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_dialog_set_categories, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            layoutRoot = (LinearLayout) view.findViewById(R.id.layoutRoot);

            buttonReset = (Button) view.findViewById(R.id.buttonReset);
            buttonSetFilter = (Button) view.findViewById(R.id.buttonSetFilter);
            buttonCancel = (Button) view.findViewById(R.id.buttonCancel);

            getDialog().setTitle(R.string.visit_categories);

            int count = layoutRoot.getChildCount();
            for (int i = 0; i <= count; i++) {
                View child = layoutRoot.getChildAt(i);
                if (child instanceof ToggleButton) {
                    ToggleButton button = (ToggleButton) child;
                    button.setChecked(selectedCategories.contains(button.getText()));
                }
            }

            buttonReset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedCategories.clear();

                    int count = layoutRoot.getChildCount();
                    for (int i = 0; i <= count; i++) {
                        View child = layoutRoot.getChildAt(i);
                        if (child instanceof ToggleButton) {
                            ToggleButton button = (ToggleButton) child;
                            button.setChecked(false);
                        }
                    }
                }
            });

            buttonSetFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedCategories.clear();

                    int count = layoutRoot.getChildCount();
                    for (int i = 0; i <= count; i++) {
                        View child = layoutRoot.getChildAt(i);
                        if (child instanceof ToggleButton) {
                            ToggleButton button = (ToggleButton) child;
                            if (button.isChecked()) {
                                selectedCategories.add(button.getText().toString());
                            }
                        }
                    }

                    StringBuilder stringBuilder = new StringBuilder();

                    for (String category : selectedCategories) {
                        stringBuilder
                                .append(",")
                                .append(category);
                    }
                    if (stringBuilder.length() > 1) {
                        stringBuilder.delete(0, 1);
                    }

                    searchSettings = SearchSettings.getInstance(getApplicationContext());
                    categories = stringBuilder.toString();
                    searchSettings.setSelectedCategories(categories);
                    selectedCategories = searchSettings.getSelectedCategories();

                    Log.d(TAG, "onClick: Categories: " + categories);

                    getReviews();

                    getDialog().dismiss();
                }
            });

            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().cancel();
                }
            });

        }
    }

    public class SetLocationDialogFragment extends DialogFragment {

        private static final int REQUEST_PLACE_AUTOCOMPLETE = 1;
        private TextView textCurrentLocation;
        private Button buttonGetCurrentLocation;

        private SeekBar seekbarDistance;
        private TextView textDistance;

        private Button buttonSetFilter;
        private Button buttonCancel;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_dialog_set_location, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            textCurrentLocation = (TextView) view.findViewById(R.id.textCurrentLocation);

            buttonGetCurrentLocation = (Button) view.findViewById(R.id.buttonGetCurrentLocation);

            seekbarDistance = (SeekBar) view.findViewById(R.id.seekbarDistance);
            textDistance = (TextView) view.findViewById(R.id.textDistance);

            buttonSetFilter = (Button) view.findViewById(R.id.buttonSetFilter);
            buttonCancel = (Button) view.findViewById(R.id.buttonCancel);

            getDialog().setTitle(R.string.set_location);

            if (currentLocationName.isEmpty()) {
                textCurrentLocation.setText(getString(R.string.current_location_unavailable));
            } else {
                textCurrentLocation.setText(currentLocationName);
            }

            buttonGetCurrentLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final Button button = (Button) v;
                    Drawable marker = getResources().getDrawable(R.drawable.ic_map_marker_grey600_18dp);
                    button.setCompoundDrawablesRelative(marker, null, null, null);
                    final String orgText = button.getText().toString();
                    button.setText("...");

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            UserLocation userLocation = UserLocation.getInstance(getApplicationContext());
                            Location location = userLocation.getLastBestLocation();
                            if (location == null) {
                                v.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Snackbar.make(v, R.string.msg_error_current_location_fail, Snackbar.LENGTH_LONG)
                                                .show();
                                        button.setCompoundDrawablesRelative(null, null, null, null);
                                        button.setText(orgText);
                                    }
                                });
                                return;
                            }

                            currentLocationName = userLocation.getCityNameKorean(location);
                            textCurrentLocation.post(new Runnable() {
                                @Override
                                public void run() {
                                    textCurrentLocation.setText(currentLocationName);
                                    button.setCompoundDrawablesRelative(null, null, null, null);
                                    button.setText(orgText);
                                }
                            });

                            nearbyRequest.longitude = location.getLongitude();
                            nearbyRequest.latitude = location.getLatitude();
                        }
                    });

                }
            });

            int progress = (int) (nearbyRequest.distance / 1000);
            if (progress == 0) {
                progress = 1;
            }
            seekbarDistance.setProgress(progress);
            textDistance.setText(String.valueOf(progress) + "km");
            seekbarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (progress == 0) {
                        progress = 1;
                    }
                    textDistance.setText(String.valueOf(progress) + "km");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (seekBar.getProgress() == 0) {
                        seekBar.setProgress(1);
                    }
                }
            });


            textCurrentLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS)
                                .build();

                        Intent intentPlace = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                .setFilter(typeFilter)
                                .setBoundsBias(new LatLngBounds(
                                        new LatLng(33.023, 125.650),
                                        new LatLng(38.866, 131.760)
                                ))
                                .build(ReviewListActivity.this);

                        startActivityForResult(intentPlace, REQUEST_PLACE_AUTOCOMPLETE);

                    } catch (GooglePlayServicesRepairableException e) {
                        // TODO: Handle the error.
                        Log.e(TAG, "getLocation", e);
                        Crashlytics.log(Log.ERROR, "GooglePlaces", e.getMessage());
                        v.setEnabled(true);

                    } catch (GooglePlayServicesNotAvailableException e) {
                        // TODO: Handle the error.
                        Log.e(TAG, "getLocationService", e);
                        Crashlytics.log(Log.ERROR, "GooglePlaces", e.getMessage());
                        v.setEnabled(true);
                    }

                }
            });

            buttonSetFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();
                    nearbyRequest.distance = (double) seekbarDistance.getProgress() * 1000;
                    getReviews();
                }
            });

            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {

            if (requestCode == REQUEST_PLACE_AUTOCOMPLETE) {
                if (resultCode == RESULT_OK) {
                    Place selectedPlace = PlaceAutocomplete.getPlace(getApplicationContext(), data);
                    Log.i(TAG, "Place: " + selectedPlace.getName());
                    Log.i(TAG, "Place: " + selectedPlace.toString());
                    String address = selectedPlace.getAddress().toString().replaceFirst("대한민국 ", "");
                    textCurrentLocation.setText(address);

                    currentLocationName = address;

                    nearbyRequest.longitude = selectedPlace.getLatLng().longitude;
                    nearbyRequest.latitude = selectedPlace.getLatLng().latitude;

                    Log.i(TAG, "Place Latitude: " + String.valueOf(selectedPlace.getLatLng().latitude));
                    Log.i(TAG, "Place Longitude: " + String.valueOf(selectedPlace.getLatLng().longitude));

                } else {

                }
            }
        }
    }

    public class SetFilterDialogFragment extends DialogFragment {

        private Button buttonSetFilter;

        private RadioButton radioPetTypeAny;
        private RadioButton radioPetTypeDog;
        private RadioButton radioPetTypeCat;

        private RadioButton radioPetGenderAny;
        private RadioButton radioPetGenderMale;
        private RadioButton radioPetGenderFemale;

        private RadioButton radioPetSizeAny;
        private RadioButton radioPetSizeSmall;
        private RadioButton radioPetSizeMedium;
        private RadioButton radioPetSizeLarge;
        private RadioButton radioPetSizeXLarge;

        private EditText textPetAgeMin;
        private EditText textPetAgeMax;


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_dialog_set_filter, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            buttonSetFilter = (Button) view.findViewById(R.id.buttonSetFilter);

            radioPetTypeAny = (RadioButton) view.findViewById(R.id.radioPetTypeAny);
            radioPetTypeDog = (RadioButton) view.findViewById(R.id.radioPetTypeDog);
            radioPetTypeCat = (RadioButton) view.findViewById(R.id.radioPetTypeCat);

            radioPetGenderAny = (RadioButton) view.findViewById(R.id.radioPetGenderAny);
            radioPetGenderMale = (RadioButton) view.findViewById(R.id.radioPetGenderMale);
            radioPetGenderFemale = (RadioButton) view.findViewById(R.id.radioPetGenderFemale);

            radioPetSizeAny = (RadioButton) view.findViewById(R.id.radioPetSizeAny);
            radioPetSizeSmall = (RadioButton) view.findViewById(R.id.radioPetSizeSmall);
            radioPetSizeMedium = (RadioButton) view.findViewById(R.id.radioPetSizeMedium);
            radioPetSizeLarge = (RadioButton) view.findViewById(R.id.radioPetSizeLarge);
            radioPetSizeXLarge = (RadioButton) view.findViewById(R.id.radioPetSizeXLarge);


            textPetAgeMin = (EditText) view.findViewById(R.id.textPetAgeMin);
            textPetAgeMax = (EditText) view.findViewById(R.id.textPetAgeMax);

            getDialog().setTitle(R.string.set_filter);

            //setSpinnerListItems(spinnerPetSize, R.array.pet_size_array);

            buttonSetFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });


        }


    }

    private void setSpinnerListItems(Spinner spinner, int arrayResourceId) {
        String[] stringArray = getResources().getStringArray(arrayResourceId);
        SpinnerAdapter adapterPetWeight = new ArrayAdapter<>(this, R.layout.spinner_item_center, stringArray);
        spinner.setAdapter(adapterPetWeight);
    }

    private void getReviews() {
        if (allReviewSnackbar != null) {
            allReviewSnackbar.dismiss();
        }
        layoutSwipeRefresh.setRefreshing(true);
        DotorWebService webServiceApi = Server.getInstance(this).getService();
        Call<ReviewsResponse> call;

        switch (mode) {
            case ALL:
                call = webServiceApi.getReviews();
                break;
            case LOCATION:
                if (nearbyRequest == null) {
                    Log.e(TAG, "getReviews: nearByRequest is null.");
                    Crashlytics.log(Log.WARN, TAG, "nearByRequest is null. Getting all reviews...");
                    call = webServiceApi.getReviews();
                } else {
                    searchSettings.setLocationSetting(currentLocationName, nearbyRequest);
                    call = webServiceApi.getReviewsByLocation(nearbyRequest);
                }

                break;
            case CATEGORY:
                if (categories == null) {
                    Log.e(TAG, "getReviews: category is null.");
                    Crashlytics.log(Log.WARN, TAG, "category is null. Getting all reviews...");
                    call = webServiceApi.getReviews();
                } else {
                    call = webServiceApi.getReviewsByCategories(categories);
                }
                break;
            default:
                return;
        }

        getReviews(call);
    }

    private void getReviewsByMode(Mode mode) {
        if (allReviewSnackbar != null) {
            allReviewSnackbar.dismiss();
        }
        layoutSwipeRefresh.setRefreshing(true);
        DotorWebService webServiceApi = Server.getInstance(this).getService();
        Call<ReviewsResponse> call;

        switch (mode) {
            case ALL:
                call = webServiceApi.getReviews();
                allReviewSnackbar = Snackbar.make(textNothingMessage, R.string.msg_all_reviews_showing, Snackbar.LENGTH_INDEFINITE);
                allReviewSnackbar.show();
                break;
            case LOCATION:
                if (nearbyRequest == null) {
                    Log.e(TAG, "getReviews: nearByRequest is null.");
                    Crashlytics.log(Log.WARN, TAG, "nearByRequest is null. Getting all reviews...");
                    call = webServiceApi.getReviews();
                } else {
                    searchSettings.setLocationSetting(currentLocationName, nearbyRequest);
                    call = webServiceApi.getReviewsByLocation(nearbyRequest);
                }

                break;
            case CATEGORY:
                if (categories == null) {
                    Log.e(TAG, "getReviews: category is null.");
                    Crashlytics.log(Log.WARN, TAG, "category is null. Getting all reviews...");
                    call = webServiceApi.getReviews();
                } else {
                    call = webServiceApi.getReviewsByCategories(categories);
                }
                break;
            default:
                return;
        }

        getReviews(call);
    }

    private void getReviews(Call<ReviewsResponse> call) {
        call.enqueue(new Callback<ReviewsResponse>() {
            @Override
            public void onResponse(Call<ReviewsResponse> call, Response<ReviewsResponse> response) {

                if (response.isSuccessful() == false) {
                    layoutSwipeRefresh.setRefreshing(false);
                    Log.e(TAG, "getReviews failed!");
                    Crashlytics.log(Log.WARN, TAG, "GetReviews failed. Code A. Response.isSuccessful returned false.");
                    Snackbar.make(textNothingMessage, R.string.msg_error_bad_server, Snackbar.LENGTH_INDEFINITE)
                            .show();
                    return;
                }

                ReviewsResponse body = response.body();
                if (body == null) {
                    Crashlytics.log(Log.WARN, TAG, "GetReviews failed. Code B. Body Null");

                    layoutSwipeRefresh.setRefreshing(false);
                    Snackbar.make(textNothingMessage, R.string.msg_error_bad_server, Snackbar.LENGTH_INDEFINITE)
                            .show();
                    return;
                }

                if (body.status < 0) {
                    Crashlytics.log(Log.WARN, TAG, "GetReviews failed. Code C. " + body.message);
                    Log.e(TAG, "getReviews returned status non-zero! message: " + body.message);

                    layoutSwipeRefresh.setRefreshing(false);
                    Snackbar.make(textNothingMessage, R.string.msg_error_bad_server, Snackbar.LENGTH_INDEFINITE)
                            .show();
                    return;
                }

                if (body.status == 1) {
                    // No REVIEW
                    viewAdapter.clear();
                    viewAdapter.notifyDataSetChanged();
                    layoutSwipeRefresh.setRefreshing(false);
                    textNothingMessage.setVisibility(View.VISIBLE);
                    return;

                } else if (body.status == 2) {
                    // No REVIEW
                    layoutSwipeRefresh.setRefreshing(false);

                    allReviewSnackbar = Snackbar.make(textNothingMessage, R.string.msg_no_reviews_by_location, Snackbar.LENGTH_INDEFINITE);
                    allReviewSnackbar.show();
                }

                Log.i(TAG, "GetReviews: count: " + String.valueOf(body.reviews.size()));

                ArrayList<Review> reviews = body.reviews;

                viewAdapter.clear();
                viewAdapter.addItems(reviews);
                viewAdapter.notifyDataSetChanged();
                layoutSwipeRefresh.setRefreshing(false);
                textNothingMessage.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ReviewsResponse> call, Throwable t) {
                Crashlytics.log(Log.WARN, TAG, "GetReviews failed. Code D. " + t.getMessage());
                layoutSwipeRefresh.setRefreshing(false);
                Log.e(TAG, "GetReviews onFailure: " + t.getMessage());
                Snackbar.make(textNothingMessage, R.string.msg_error_bad_connection, Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
        });
    }
}
