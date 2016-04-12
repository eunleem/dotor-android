package net.team88.dotor.reviews;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import net.team88.dotor.BuildConfig;
import net.team88.dotor.R;
import net.team88.dotor.hospitals.Hospital;
import net.team88.dotor.hospitals.Hospitals;
import net.team88.dotor.pets.MyPets;
import net.team88.dotor.pets.Pet;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.InsertResponse;
import net.team88.dotor.shared.InvalidInputException;
import net.team88.dotor.shared.NearbyRequest;
import net.team88.dotor.shared.PhotoGetter;
import net.team88.dotor.shared.Server;
import net.team88.dotor.shared.UserScreen;
import net.team88.dotor.shared.image.ImageEditActivity;
import net.team88.dotor.shared.image.ImageUploadService;
import net.team88.dotor.utils.ImageUtils;
import net.team88.dotor.utils.Utils;

import org.bson.types.ObjectId;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewPostActivity extends AppCompatActivity {
    static final String TAG = "PostReview";

    static final int REQUEST_PERMISSION = 1;
    static final int REQUEST_TAKE_PHOTO = 10;
    static final int REQUEST_GET_FROM_GALLERY = 11;
    static final int REQUEST_IMAGE_EDIT = 15;

    static final int REQUEST_PLACE_AUTOCOMPLETE = 20;

    private static final String KEY_SELECTED_HOSPITAL = "Hospital";
    private static final String KEY_SELECTED_CATEGORIES = "categories";
    private static final String KEY_IMAGE_FILE_PATH = "IMAGE_FILE_PATH";

    private View viewRoot;


    private ProgressBar progressBar;

    private Spinner spinnerPet;

    private AutoCompleteTextView textHospital;
    private TextView textLocation;
    private ObjectId selectedHospitalId;
    private Place selectedPlace;
    private TextView textHospitalMessage;

    private Spinner spinnerCategory;
    private EditText editTextCost;

    private TextView textCategories;
    private ArrayList<String> selectedCategories;

    private EditText editTextReview;

    private ImageView imageViewAddImage;
    private File imageFile;

    private ImageMenuDialogFragment imageMenuDialogFragment;
    private CategoriesDialogFragment categoriesDialogFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_post);

        if (savedInstanceState != null) {
            String imageFilePath = savedInstanceState.getString(KEY_IMAGE_FILE_PATH, "");
            if (imageFilePath.isEmpty() == false) {
                imageFile = new File(imageFilePath);
            }

            String selectedHospitalIdStr = savedInstanceState.getString(KEY_SELECTED_HOSPITAL, "");
            if (selectedHospitalIdStr.isEmpty() == false) {
                this.selectedHospitalId = new ObjectId(selectedHospitalIdStr);
            }

            this.selectedCategories = savedInstanceState.getStringArrayList(KEY_SELECTED_CATEGORIES);
        }

        if (this.selectedCategories == null) {
            this.selectedCategories = new ArrayList<>();
        }

        setupBasicElements();
        registerElements();

        setSpinnerPet();
        setSpinnerCategory();

        registerEvents();

        checkPermissions();

        // Try to load Last Selected Hospital
        final Hospitals hospitalsInstance = Hospitals.getInstance(this);
        Hospital lastSelectedHospital = hospitalsInstance.getLastSelectedHospital();
        if (lastSelectedHospital != null) {
            textHospital.setText(lastSelectedHospital.name);
            textLocation.setText(lastSelectedHospital.address);
            textHospital.setBackgroundColor(Color.argb(30, 0, 150, 10));
            textLocation.setBackgroundColor(Color.argb(30, 0, 150, 10));

            // This line must be located after textHospital.setText() as
            // TextChangedEvent will set selectedHospitalId to null
            this.selectedHospitalId = lastSelectedHospital.id;

            textLocation.setEnabled(false);

            textHospitalMessage.setVisibility(View.GONE);
        }


        loadNearbyHospitals(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void setupBasicElements() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (imageFile != null) {
            String imageFilePath = imageFile.getAbsolutePath();
            outState.putString(KEY_IMAGE_FILE_PATH, imageFilePath);
        }

        if (this.selectedHospitalId != null) {
            outState.putString(KEY_SELECTED_HOSPITAL, this.selectedHospitalId.toHexString());
        }

        if (this.selectedCategories.size() > 0) {
            outState.putStringArrayList(KEY_SELECTED_CATEGORIES, this.selectedCategories);
        }
        super.onSaveInstanceState(outState);
    }

    private void registerElements() {
        viewRoot = find(R.id.view_root);

        progressBar = find(R.id.progressBar);

        spinnerPet = find(R.id.spinnerPet);
        textHospital = find(R.id.textHospital);
        textLocation = find(R.id.textLocation);
        textHospitalMessage = find(R.id.textHospitalMessage);
        //textLocation = find(R.id.textLocation);
        //editTextHospital = find(R.id.editTextHospital);

        textCategories = find(R.id.textCategories);
        spinnerCategory = find(R.id.spinnerCategory);
        editTextCost = find(R.id.editTextCost);
        editTextReview = find(R.id.editTextReview);

        imageViewAddImage = find(R.id.imageViewAddImage);

        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
    }

    private void registerEvents() {

        textCategories.setOnClickListener(onTextCategoriesClicked());

        textHospital.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (selectedHospitalId != null) {
                    selectedHospitalId = null;

                    textHospital.setBackgroundColor(Color.WHITE);

                    textLocation.setEnabled(true);
                    textLocation.setBackgroundColor(Color.argb(30, 160, 10, 10));
                    textLocation.setText("");

                }
                textHospitalMessage.setVisibility(View.VISIBLE);

                Log.d(TAG, "onTextChanged: SelectedHospitalId is null");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        textHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Reuse of Adapter for efficient Memory Usage.
                Hospitals hospitalsInstance = Hospitals.getInstance(getApplicationContext());
                final ArrayList<Hospital> hospitalsList = new ArrayList<>(hospitalsInstance.getHospitals());
                HospitalAutoCompleteTextViewAdapter adapter =
                        new HospitalAutoCompleteTextViewAdapter(getApplicationContext(),
                                R.id.textHospital, hospitalsList);

                textHospital.setAdapter(adapter);
                textHospital.showDropDown();
            }
        });

        textHospital.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Hospitals hospitalsInstance = Hospitals.getInstance(getApplicationContext());
                final ArrayList<Hospital> hospitalsList = new ArrayList<>(hospitalsInstance.getHospitals());
                Hospital selectedHospital = hospitalsList.get(position);

                selectedHospitalId = selectedHospital.id;

                textHospital.setBackgroundColor(Color.argb(30, 0, 150, 10));

                textLocation.setEnabled(false);
                textLocation.setText(selectedHospital.address);
                textLocation.setBackgroundColor(Color.argb(30, 0, 150, 10));

                textHospitalMessage.setVisibility(View.GONE);

                textHospital.setError(null);
                textLocation.setError(null);

                Log.d(TAG, "onItemClick: SelectedHospitalId" + selectedHospitalId.toHexString());
            }
        });

        textLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
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
                            .build(ReviewPostActivity.this);

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

        editTextCost.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String org = s.toString().trim().replace(",", "");

                    String format = NumberFormat.getNumberInstance(Locale.US).format(Integer.valueOf(org));
                    editTextCost.removeTextChangedListener(this);
                    editTextCost.setText(format);
                    editTextCost.setSelection(editTextCost.getText().toString().length());
                    editTextCost.addTextChangedListener(this);

                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });

        imageViewAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageMenuDialogFragment == null) {
                    imageMenuDialogFragment = new ImageMenuDialogFragment();
                }
                FragmentManager fm = getFragmentManager();
                imageMenuDialogFragment.show(fm, "ImageMenuFragment");
            }
        });
    }

    public class ImageMenuDialogFragment extends DialogFragment {

        private Button buttonCamera;
        private Button buttonGallery;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_image_menu_dialog, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            getDialog().setTitle(R.string.add_image);

            buttonCamera = (Button) view.findViewById(R.id.buttonCamera);
            buttonGallery = (Button) view.findViewById(R.id.buttonGallery);


            buttonCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PackageManager packageManager = getPackageManager();
                    if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false) {
                        Snackbar.make(editTextReview, "This device does not have a camera.", Snackbar.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    // #FIXME existing picture gets overwritten and become null file if new instance gets cancelled.
                    File tempImageFile = PhotoGetter.dispatchTakePictureIntent(ReviewPostActivity.this, REQUEST_TAKE_PHOTO);
                    if (tempImageFile == null) {
                        Log.e(TAG, "No Image file..");
                        return;
                    } else {
                        imageFile = tempImageFile;
                    }
                }
            });

            buttonGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    getActivity().startActivityForResult(Intent.createChooser(intent,
                            "Select Picture"), REQUEST_GET_FROM_GALLERY);
                    //startActivityForResult(intent, REQUEST_GET_FROM_GALLERY);
                }
            });


        }
    }

    @NonNull
    private View.OnClickListener onTextCategoriesClicked() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                if (categoriesDialogFragment == null) {
                    categoriesDialogFragment = new CategoriesDialogFragment();
                }
                categoriesDialogFragment.show(fm, "Categories Fragment");
            }
        };
    }

    public class CategoriesDialogFragment extends DialogFragment {

        private ViewGroup table;
        private Button buttonCancel;
        private Button buttonOk;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_dialog_set_categories, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            table = (ViewGroup) view.findViewById(R.id.tableLayout);

            buttonOk = (Button) view.findViewById(R.id.buttonSetFilter);
            buttonCancel = (Button) view.findViewById(R.id.buttonCancel);

            getDialog().setTitle(R.string.visit_categories);
            addCategories();


            buttonOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedCategories.clear();
                    for (int i = 0; i < table.getChildCount(); ++i) {
                        ViewGroup row = (ViewGroup) table.getChildAt(i);

                        for (int j = 0; j < row.getChildCount(); ++j) {
                            ToggleButton button = (ToggleButton) row.getChildAt(j);
                            if (button.isChecked()) {
                                selectedCategories.add(button.getText().toString());
                            }
                        }
                    }

                    StringBuilder stringBuilder = new StringBuilder();

                    for (String category : selectedCategories) {
                        stringBuilder
                                .append(", ")
                                .append(category);
                    }
                    if (stringBuilder.length() > 2) {
                        stringBuilder.delete(0, 2);
                    }

                    String s = stringBuilder.toString();
                    if (s.isEmpty() == false) {
                        textCategories.setError(null);
                    }
                    textCategories.setText(s);

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

        private void addCategories() {
            String[] categories = getResources().getStringArray(R.array.review_category_array);

            final int numButtonsPerRow = 3;
            final int numCategories = categories.length;
            Log.d(TAG, "onCreateView: numCategories" + String.valueOf(numCategories));

            final int numRows = (int) Math.ceil((double) numCategories / numButtonsPerRow);

            Log.d(TAG, "onCreateView: numRows: " + String.valueOf(numRows));

            int count = 0;

            for (int i = 0; i < numRows; i++) {
                TableRow row = new TableRow(getActivity());
                row.setGravity(Gravity.CENTER);

                for (int j = 0; j < numButtonsPerRow && count < numCategories; j++, count++) {
                    ToggleButton button = new ToggleButton(getActivity());
                    // Very First element is Checked by Default.
                    if (i == 0 && j == 0) {
                        button.setChecked(true);
                    }
                    button.setChecked(selectedCategories.contains(categories[count]));
                    button.setText(categories[count]);
                    button.setTextOn(categories[count]);
                    button.setTextOff(categories[count]);
                    row.addView(button);
                }
                table.addView(row);
            }
        }
    }


    private void setSpinnerPet() {
        final LinkedHashMap<String, Pet> pets = MyPets.getInstance(this).getPets();
        if (pets.size() == 0) {
            // TODO Display No Pet added message
            return;
        }

        ArrayList<String> list = new ArrayList<>(pets.keySet());
        Collections.sort(list);
        String[] petNames = list.toArray(new String[pets.size()]);
        if (petNames.length == 0) {
            petNames = new String[1];
            petNames[0] = getString(R.string.post_no_pet);
            spinnerPet.setEnabled(false);
        }

        SpinnerAdapter adapterPets = new ArrayAdapter<>(this, R.layout.spinner_item_center, petNames);
        spinnerPet.setAdapter(adapterPets);
    }


    private void setSpinnerCategory() {
        String[] stringArray = getResources().getStringArray(R.array.review_category_array);
        SpinnerAdapter adapterPetWeight = new ArrayAdapter<>(this, R.layout.spinner_item_left, stringArray);
        this.spinnerCategory.setAdapter(adapterPetWeight);
    }

    private void loadNearbyHospitals(final boolean showDropdownList) {

        final Hospitals hospitalsInstance = Hospitals.getInstance(this);

        hospitalsInstance.getNearbyHospitalsFromServer(2000.00, new Runnable() {
            @Override
            public void run() {
                final ArrayList<Hospital> hospitalsList = new ArrayList<>(hospitalsInstance.getHospitals());

                HospitalAutoCompleteTextViewAdapter adapter =
                        new HospitalAutoCompleteTextViewAdapter(getApplicationContext(),
                                R.id.textHospital, hospitalsList);

                textHospital.setAdapter(adapter);

                if (showDropdownList) {
                    textHospital.showDropDown();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ok, menu);
        //menu.getItem(R.menu.ok).setTitle("Post");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // #TODO Confirm Cancel
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
                break;

            case R.id.action_ok:
                post();
                break;

            default:
                Log.e(TAG, "Unhandled Option Item.");
                break;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_EDIT) {
            if (resultCode == RESULT_OK) {
                if (imageMenuDialogFragment != null) {
                    imageMenuDialogFragment.dismiss();
                }

                final String processedImagePath = data.getStringExtra(ImageEditActivity.KEY_PROCESSED_IMAGE_PATH);
                this.imageFile = new File(processedImagePath);

                final UserScreen userScreen = UserScreen.getInstance(this);


                final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) imageViewAddImage.getLayoutParams();
                params.gravity = Gravity.CENTER;
                params.width = Utils.convertDpToPx(this, userScreen.getScreenWidthDp());
                params.height = Utils.convertDpToPx(this, 300);
                imageViewAddImage.setLayoutParams(params);

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        int screenWidthDp = userScreen.getScreenWidthDp();
                        int screenHeightDp = userScreen.getScreenHeightDp();
                        int maxSize = Math.min(screenWidthDp, screenHeightDp);
                        final Bitmap scaledBitmap = ImageUtils.scale(processedImagePath, maxSize, maxSize);

                        imageViewAddImage.post(new Runnable() {
                            @Override
                            public void run() {
                                imageViewAddImage.setImageBitmap(scaledBitmap);
                            }
                        });

                    }
                });

            } else {
                Log.i(TAG, "onActivityResult: returned non ok");
            }

        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {

                Intent intent = new Intent(ReviewPostActivity.this, ImageEditActivity.class);
                intent.putExtra(ImageEditActivity.KEY_IMAGE_PATH, this.imageFile.getAbsolutePath());

                startActivityForResult(intent, REQUEST_IMAGE_EDIT);
                return;

            } else if (resultCode == RESULT_CANCELED) {
                if (this.imageFile != null) {
                    this.imageFile.delete();
                    this.imageViewAddImage.setImageResource(R.drawable.ic_plus_grey600_48dp);
                }

            } else {

            }

        } else if (requestCode == REQUEST_GET_FROM_GALLERY) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String path = getPath(this, uri);
                Log.d(TAG, "onActivityResult: ImagePath from Gallery:" + path);

                Intent intent = new Intent(ReviewPostActivity.this, ImageEditActivity.class);
                intent.putExtra(ImageEditActivity.KEY_IMAGE_PATH, path);
                startActivityForResult(intent, REQUEST_IMAGE_EDIT);

            } else {
                Log.d(TAG, "onActivityResult: REUQEST_GET_FROM_GALLERY code:" + String.valueOf(resultCode));

            }
        } else if (requestCode == REQUEST_PLACE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                selectedPlace = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + selectedPlace.getName());
                Log.i(TAG, "Place: " + selectedPlace.toString());
                String address = selectedPlace.getAddress().toString().replaceFirst("대한민국 ", "");
                textLocation.setEnabled(true);

                textLocation.setText(address);
                textLocation.setError(null);
                textLocation.setBackgroundColor(Color.argb(30, 0, 150, 10));

                final Hospitals hospitalsInstance = Hospitals.getInstance(this);
                hospitalsInstance.getHospitalsFromServer(
                        selectedPlace.getLatLng().latitude, selectedPlace.getLatLng().longitude,
                        4000.00,
                        new Runnable() {
                            @Override
                            public void run() {
                                final ArrayList<Hospital> hospitalsList = new ArrayList<>(hospitalsInstance.getHospitals());
                                HospitalAutoCompleteTextViewAdapter adapter =
                                        new HospitalAutoCompleteTextViewAdapter(getApplicationContext(),
                                                R.id.textHospital, hospitalsList);

                                textHospital.setAdapter(adapter);
                                textHospital.showDropDown();
                            }
                        }
                );

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e(TAG, status.getStatusMessage());
                Crashlytics.log(Log.ERROR, "GooglePlace", status.getStatusMessage());
                Snackbar.make(textLocation, R.string.msg_error_autocomplete_location, Snackbar.LENGTH_LONG)
                        .show();
                textLocation.setEnabled(true);

            } else if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "Cancelled");
                textLocation.setEnabled(true);

            } else {
                textLocation.setEnabled(true);

            }
        } else {
            Log.i(TAG, "onActivityResult: ");
        }
    }

    private Review getReviewFromUserInput() throws InvalidInputException {
        String petName = spinnerPet.getSelectedItem().toString();
        String hospitalName = textHospital.getText().toString().trim();
        String locationName = textLocation.getText().toString().trim();
        //String category = spinnerCategory.getSelectedItem().toString();

        if (hospitalName.isEmpty()) {
            textHospital.setError(getString(R.string.msg_error_hospital_required));
            throw new InvalidInputException(InvalidInputException.Type.REQUIRED);
        }

        if (locationName.isEmpty()) {
            textLocation.setError(getString(R.string.msg_error_location_required));
            throw new InvalidInputException(InvalidInputException.Type.REQUIRED);
        }

        if (selectedCategories.size() == 0) {
            textCategories.setError(getString(R.string.msg_error_categories_required));
            throw new InvalidInputException(InvalidInputException.Type.REQUIRED);
        }

        String costStr = editTextCost.getText().toString().trim().replace(",", "");

        int cost = 0;
        if (costStr.isEmpty() == false) {
            cost = Integer.valueOf(costStr);
        } else {
            editTextCost.setError(getString(R.string.msg_error_cost_required));
            throw new InvalidInputException(InvalidInputException.Type.REQUIRED);
        }


        String reviewStr = editTextReview.getText().toString().trim();
        if (reviewStr.isEmpty()) {
            editTextReview.setError(getString(R.string.msg_error_review_required));
            throw new InvalidInputException(InvalidInputException.Type.REQUIRED);
        }


        Review newReview = new Review();

        Pet pet = MyPets.getInstance(this).getPet(petName);

        newReview.petid = pet.getId();
        newReview.petType = pet.getType();
        newReview.petGender = pet.getGender();
        newReview.petAge = pet.getAge();
        newReview.petSize = pet.getSize();


        if (selectedHospitalId != null) {
            newReview.hospitalid = selectedHospitalId;
        }

        newReview.hospitalName = hospitalName;
        newReview.locationName = locationName;

        newReview.categories = selectedCategories;
        newReview.cost = cost;
        newReview.reviewBody = reviewStr;

        if (imageFile != null && imageFile.getAbsolutePath().isEmpty() == false) {
            // If Picture is attached, mark it as draft till image upload is finished.
            newReview.isDraft = true;
        } else {
            newReview.isDraft = false;
        }
        if (newReview.categories != null) {
            Log.d(TAG, "getReviewFromUserInput: CategoriesCount" + String.valueOf(newReview.categories.size()));
        } else {
            Log.d(TAG, "getReviewFromUserInput: categoriesCount 0");
        }

        return newReview;
    }

    private boolean post() {
        progressBar.setVisibility(View.VISIBLE);

        final Review newReview;
        try {
            newReview = getReviewFromUserInput();

        } catch (InvalidInputException e) {
            Snackbar.make(viewRoot, R.string.msg_error_addreview, Snackbar.LENGTH_LONG)
                    .show();
            return false;
        }

        if (this.selectedHospitalId != null) {
            Hospitals hospitals = Hospitals.getInstance(this);
            hospitals.setLastSelectedHospitalId(this.selectedHospitalId);
        }

        if (this.selectedPlace != null) {
            NearbyRequest nearbyRequest = new NearbyRequest();
            nearbyRequest.distance = 4000.00;
            nearbyRequest.latitude = selectedPlace.getLatLng().latitude;
            nearbyRequest.longitude = selectedPlace.getLatLng().longitude;

            SearchSettings.getInstance(this).setLocationSetting(selectedPlace.getName().toString(), nearbyRequest);
        }

        String selectedCategoriesStr = this.textCategories.getText().toString().replace(" ", "");
        SearchSettings.getInstance(this).setSelectedCategories(selectedCategoriesStr);


        DotorWebService service = Server.getInstance(this).getService();
        service.insertReview(newReview).enqueue(PostReviewCallback(newReview));

        return true;
    }

    @NonNull
    private Callback<InsertResponse> PostReviewCallback(final Review newReview) {
        return new Callback<InsertResponse>() {
            @Override
            public void onResponse(Call<InsertResponse> call, Response<InsertResponse> response) {
                if (response.isSuccessful() == false) {
                    // Server Level Error
                    String errorMessage = "";
                    try {
                        errorMessage = response.errorBody().string();
                    } catch (IOException e) {
                    }

                    Crashlytics.log(Log.ERROR, TAG,
                            "Could not insert review. msg: " + errorMessage);

                    Snackbar.make(viewRoot, R.string.msg_error_addreview, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                InsertResponse json = response.body();

                if (json.status < 0) {
                    // Server Application Level Error
                    Crashlytics.log(Log.ERROR, TAG,
                            "Could not insert review. Server returned status: " +
                                    String.valueOf(json.status) + " message: " + json.message);

                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(viewRoot, R.string.msg_error_addreview, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (json.newid == null) {
                    Log.e(TAG, "Server has not returned objectid. message: " + json.message);
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(viewRoot, R.string.msg_error_addreview, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (BuildConfig.DEBUG) {
                    if (ObjectId.isValid(json.newid)) {
                        newReview.id = new ObjectId(json.newid);
                    } else {
                        Log.e(TAG, "Server has returned an invalid objectid");
                    }
                } else {
                    newReview.id = new ObjectId(json.newid);
                    newReview.created = new Date();
                }

                Reviews reviews = Reviews.getInstance(getApplicationContext());
                reviews.insert(newReview);

                if (imageFile != null && imageFile.getAbsolutePath().isEmpty() == false) {
                    Intent serviceIntent = new Intent(ReviewPostActivity.this, ImageUploadService.class);
                    serviceIntent.putExtra("image_filepath", imageFile.getAbsolutePath());
                    serviceIntent.putExtra("category", "review");
                    serviceIntent.putExtra("relatedid", newReview.id.toHexString());
                    startService(serviceIntent);

                } else {
                    Intent intent = new Intent();

                    intent.setClass(getApplicationContext(), ReviewViewActivity.class);
                    intent.putExtra("reviewid", json.newid);
                    String message = getString(R.string.post_review_finished_text);

                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(
                                    getApplicationContext(),
                                    0 /* Request code */, intent,
                                    PendingIntent.FLAG_ONE_SHOT
                            );

                    Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_logo_white_24dp)
                            .setContentTitle(getApplicationContext().getString(R.string.post_review_finished))
                            .setContentText(message)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);


                    NotificationManager man = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    man.notify(0, notificationBuilder.build());

                }

                if (newReview.hospitalid != null) {
                    Hospitals.getInstance(getApplicationContext()).setLastSelectedHospitalId(newReview.hospitalid);
                }

                progressBar.setVisibility(View.GONE);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onFailure(Call<InsertResponse> call, Throwable t) {
                // Either No Network Connection or Server is down.
                progressBar.setVisibility(View.GONE);
                Crashlytics.log(Log.ERROR, TAG,
                        "Could not insert a review. Error:" + t.getMessage());

                Snackbar.make(viewRoot, R.string.msg_error_addreview, Snackbar.LENGTH_LONG)
                        .show();
            }
        };
    }

    // REF: http://stackoverflow.com/questions/33208911/get-realpath-return-null-on-android-marshmallow

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    //region Permission Related
    private static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private void checkPermissions() {
        int storagePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int locationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (storagePermission != PackageManager.PERMISSION_GRANTED ||
                locationPermission != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    REQUEST_PERMISSION
            );
        } else {
            Log.i(TAG, "Permission Granted!");
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T find(int id) {
        return (T) findViewById(id);
    }
    //endregion
}
