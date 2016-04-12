package net.team88.dotor.pets;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;

import net.team88.dotor.R;
import net.team88.dotor.BuildConfig;
import net.team88.dotor.shared.BasicResponse;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.InsertResponse;
import net.team88.dotor.shared.InvalidInputException;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ConcurrentModificationException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PetEditActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 10;
    private static final int REQUEST_GET_FROM_GALLERY = 20;
    private static final int REQUEST_IMAGE_EDIT = 30;
    private static final int REQUEST_PERMISSION = 50;
    private final String TAG = "EDIT_PET";

    public static final String KEY_PET_NAME = "PET_NAME";
    public static final String KEY_MODE = "mode";
    public static final String VALUE_MODE_EDIT = "edit";

    // FEATURES ENABLED
    private static final boolean ENABLE_CANCEL_CONFIRMATION = true;

    private SimpleDateFormat birthdayDateFormat;
    private ImageView imagePet;
    private ImageView imagePetAdd;
    private ImageMenuDialogFragment imageMenuDialogFragment;

    private enum Mode {
        ADD,
        EDIT
    }

    private Mode mode;

    private View layoutRoot;

    private EditText editTextPetName;
    private RadioGroup radioGroupPetType;
    private RadioGroup radioGroupPetGender;
    private TextView textPetBirthday;
    private DatePickerDialog datePickerDialogPetBirthday;
    private Spinner spinnerPetSize;
    private ProgressBar progressBar;

    private Calendar petBirthday;

    private String petName; // For edit mode

    private File imageFile;


    @SuppressWarnings("unchecked")
    public <T extends View> T find(int id) {
        return (T) findViewById(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_edit);

        setupBasicElements();
        registerElements();
        registerEvents();

        setSpinnerListItems(spinnerPetSize, R.array.pet_size_array);
        setupBirthdayPickerDialog();


        final String modeStr = getIntent().getStringExtra(KEY_MODE);
        if (modeStr != null && modeStr.equals(VALUE_MODE_EDIT)) {
            editMode();

        } else {
            addMode();

        }
    }

    private void setupBasicElements() {
        birthdayDateFormat = new SimpleDateFormat(getString(R.string.birthday_format), Locale.KOREA);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
    }

    private void registerElements() {
        layoutRoot = find(R.id.viewRoot);

        imagePet = find(R.id.imagePet);
        imagePetAdd = find(R.id.imagePetAdd);

        editTextPetName = find(R.id.editTextPetName);

        radioGroupPetType = find(R.id.radioGroupPetType);
        radioGroupPetGender = find(R.id.radioGroupPetGender);

        textPetBirthday = find(R.id.textPetBirthday);
        spinnerPetSize = find(R.id.spinnerPetWeight);
        progressBar = find(R.id.progressBar);

        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
    }

    private void registerEvents() {
        textPetBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialogPetBirthday.show();
            }
        });

        imagePet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        imagePet.setOnClickListener(new View.OnClickListener() {
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

            checkPermissions();


            buttonCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PackageManager packageManager = getPackageManager();
                    if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false) {
                        Snackbar.make(v, "This device does not have a camera.", Snackbar.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    // #FIXME existing picture gets overwritten and become null file if new instance gets cancelled.
                    File tempImageFile = PhotoGetter.dispatchTakePictureIntent(PetEditActivity.this, REQUEST_TAKE_PHOTO);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ok, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                cancelAction();
                break;
            case R.id.action_ok:
                if (this.mode == Mode.ADD) {
                    addPet();
                } else if (this.mode == Mode.EDIT) {
                    editPet();
                }
                break;

            default:
                Log.i(TAG, "OptionsItemSelected");
                break;
        }
        return true;
    }


    private void setupBirthdayPickerDialog() {
        final Calendar currentTime = Calendar.getInstance();

        datePickerDialogPetBirthday = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar selectedTime = Calendar.getInstance();
                        selectedTime.set(year, monthOfYear, dayOfMonth);
                        petBirthday = selectedTime;
                        int age = Utils.getAge(selectedTime);
                        String ageStr = String.valueOf(age) + getString(R.string.age_unit);
                        textPetBirthday.setText(birthdayDateFormat.format(selectedTime.getTime()) + " (" + ageStr + ")");
                    }
                },
                currentTime.get(Calendar.YEAR),
                currentTime.get(Calendar.MONTH),
                currentTime.get(Calendar.DAY_OF_MONTH)
        );
    }

    private void setSpinnerListItems(Spinner spinner, int arrayResourceId) {
        String[] stringArray = getResources().getStringArray(arrayResourceId);
        SpinnerAdapter adapterPetWeight = new ArrayAdapter<>(this, R.layout.spinner_item_center, stringArray);
        spinner.setAdapter(adapterPetWeight);
    }

    private void addMode() {
        this.mode = Mode.ADD;
        Calendar currentDate = Calendar.getInstance();
        petBirthday = currentDate;
        textPetBirthday.setText(birthdayDateFormat.format(currentDate.getTime()) + " (0" + getString(R.string.age_unit) + ")");
    }

    private void editMode() {
        this.mode = Mode.EDIT;
        final android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle(R.string.title_activity_edit_pet);

        this.petName = getIntent().getStringExtra(KEY_PET_NAME);
        setPetInfo(this.petName);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancelAction();
    }

    private void cancelAction() {
        //Utils.hideKeyboard(this);

        if (ENABLE_CANCEL_CONFIRMATION) {
            CancelConfirmDialogFragment frag = new CancelConfirmDialogFragment();
            frag.show(getFragmentManager(), TAG);

        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void setPetInfo(String petName) {
        MyPets myPets = MyPets.getInstance(this);
        Pet pet = myPets.getPet(petName);
        if (pet == null) {
            Log.e(TAG, "Could not set pet. No Pet " + petName + " found.");
            Crashlytics.log(Log.ERROR, TAG, "Could not set pet. No Pet " + petName + " found.");
            return;
        }

        this.editTextPetName.setText(pet.name);
        this.radioGroupPetType.check(pet.type == Pet.Type.DOG ? R.id.radioPetTypeDog : R.id.radioPetTypeCat);
        this.radioGroupPetGender.check(pet.gender == Pet.Gender.MALE ? R.id.radioButtonGenderMale : R.id.radioButtonGenderFemale);

        final long petBirthdayInMillis = pet.getBirthday().getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(petBirthdayInMillis);
        int age = Utils.getAge(cal);
        String ageStr = String.valueOf(age) + getString(R.string.age_unit);
        textPetBirthday.setText(birthdayDateFormat.format(cal.getTime()) + " (" + ageStr + ")");

        petBirthday = cal;

        datePickerDialogPetBirthday.updateDate(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        this.spinnerPetSize.setSelection(pet.size);

        if (pet.imageid != null && pet.imageFileName != null && pet.imageFileName.isEmpty() == false) {
            final String imageBaseUrl = Server.getInstance(getBaseContext()).getServerUrl() + "/img/";
            final String imageUrl = imageBaseUrl + pet.imageFileName;

            final int size = (int) ImageUtils.convertDpToPixel(160.00f, getApplicationContext());

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap bitmap = Glide.with(getApplicationContext())
                                .load(imageUrl)
                                .asBitmap()
                                .centerCrop()
                                .skipMemoryCache(true)
                                .into(size, size)
                                .get();

                        final Bitmap cropCircle = ImageUtils.cropCircle(getApplicationContext(),
                                bitmap, size / 2, size, size, false, false, false, false);

                        imagePet.post(new Runnable() {
                            @Override
                            public void run() {
                                imagePet.setImageBitmap(cropCircle);
                            }
                        });

                    } catch (InterruptedException e) {
                        Log.e(TAG, e.toString());

                    } catch (ExecutionException e) {
                        Log.e(TAG, e.toString());
                    }

                }
            });

        }
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

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {

                        int size = (int) ImageUtils.convertDpToPixel(160.00f, getApplicationContext());
                        Bitmap bitmap = ImageUtils.scale(processedImagePath, size, size);
                        ImageUtils.saveJpg(bitmap, processedImagePath, 70);
                        final Bitmap cropCircle = ImageUtils.cropCircle(getApplicationContext(),
                                bitmap, size / 2, size, size, false, false, false, false);

                        imagePet.post(new Runnable() {
                            @Override
                            public void run() {
                                imagePet.setImageBitmap(cropCircle);
                            }
                        });

                    }
                });

            } else {
                Log.i(TAG, "onActivityResult: returned non ok");
            }

        } else if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {

                Intent intent = new Intent(this, ImageEditActivity.class);
                intent.putExtra(ImageEditActivity.KEY_IMAGE_PATH, this.imageFile.getAbsolutePath());

                startActivityForResult(intent, REQUEST_IMAGE_EDIT);
                return;

            } else if (resultCode == RESULT_CANCELED) {
                if (this.imageFile != null) {
                    this.imageFile.delete();
                    this.imageFile = null;
                    this.imagePet.setImageResource(R.drawable.logo_circle);
                }
            }

        } else if (requestCode == REQUEST_GET_FROM_GALLERY) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String path = getPath(this, uri);
                Log.d(TAG, "onActivityResult: ImagePath from Gallery:" + path);

                Intent intent = new Intent(this, ImageEditActivity.class);
                intent.putExtra(ImageEditActivity.KEY_IMAGE_PATH, path);
                startActivityForResult(intent, REQUEST_IMAGE_EDIT);

            } else {
                Log.d(TAG, "onActivityResult: REUQEST_GET_FROM_GALLERY code:" + String.valueOf(resultCode));

            }
        }
    }

    private void addPet() {
        progressBar.setVisibility(View.VISIBLE);

        final MyPets myPets = MyPets.getInstance(this);

        if (myPets.getPets().size() > 25) {
            Snackbar.make(layoutRoot, R.string.msg_error_addpet_too_many, Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        final Pet pet;
        try {
            // Locally add and save pet.
            pet = getPetFromUserInput();

        } catch (InvalidInputException e) {
            progressBar.setVisibility(View.GONE);
            return;
        }


        DotorWebService webServiceApi = Server.getInstance(this).getService();
        webServiceApi.insertPet(pet).enqueue(new Callback<InsertResponse>() {
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
                            "Could not insert pet. msg: " + errorMessage);

                    Snackbar.make(layoutRoot, R.string.msg_error_addpet, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                InsertResponse json = response.body();

                if (json.status < 0) {
                    // Server Application Level Error
                    Crashlytics.log(Log.ERROR, TAG,
                            "Could not insert pet. Server returned status: " +
                                    String.valueOf(json.status) + " message: " + json.message);

                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(layoutRoot, R.string.msg_error_addpet, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (json.newid == null) {
                    Log.e(TAG, "Server has not returned objectid. message: " + json.message);
                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(layoutRoot, R.string.msg_error_addpet, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (BuildConfig.DEBUG) {
                    if (ObjectId.isValid(json.newid)) {
                        pet.id = new ObjectId(json.newid);
                    } else {
                        Log.e(TAG, "Server has returned an invalid objectid");
                    }
                } else {
                    pet.id = new ObjectId(json.newid);
                }


                if (imageFile != null && imageFile.getAbsolutePath().isEmpty() == false) {
                    Intent serviceIntent = new Intent(PetEditActivity.this, ImageUploadService.class);
                    serviceIntent.putExtra("image_filepath", imageFile.getAbsolutePath());
                    serviceIntent.putExtra("category", "pet");
                    serviceIntent.putExtra("relatedid", pet.id.toHexString());
                    startService(serviceIntent);
                }

                myPets.insert(pet);

                progressBar.setVisibility(View.GONE);
                Intent intent = new Intent();
                intent.putExtra("pet_name", pet.name);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onFailure(Call<InsertResponse> call, Throwable t) {
                // Either No Network Connection or Server is down.
                progressBar.setVisibility(View.GONE);
                Crashlytics.log(Log.ERROR, TAG,
                        "Could not insert pet. Error:" + t.getMessage());

                Snackbar.make(layoutRoot, R.string.msg_error_addpet, Snackbar.LENGTH_LONG)
                        .show();

            }
        });
    }


    private void editPet() {
        progressBar.setVisibility(View.VISIBLE);

        final Pet pet;
        try {
            // Locally add and save pet.
            pet = getPetFromUserInput();

        } catch (InvalidInputException e) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (this.petName == null || this.petName.isEmpty()) {
            Log.e(TAG, "Pet name is null!");
            return;
        }

        final MyPets myPets = MyPets.getInstance(this);

        pet.id = myPets.getPet(this.petName).id;

        DotorWebService webServiceApi = Server.getInstance(this).getService();
        webServiceApi.updatePet(pet).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() == false) {
                    // Server Level Error
                    String errorMessage = "";
                    try {
                        errorMessage = response.errorBody().string();
                    } catch (IOException e) {
                    }

                    Log.e(TAG, "Could not update pet. msg: " + errorMessage);
                    Crashlytics.log(Log.ERROR, TAG,
                            "Could not update pet. msg: " + errorMessage);

                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(layoutRoot, R.string.msg_error_editpet, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                BasicResponse json = response.body();
                if (json.status < 0) {
                    // Server Application Level Error
                    Log.e(TAG, "Could not update pet. msg: " + json.message);
                    Crashlytics.log(Log.ERROR, TAG,
                            "Could not update pet. Server returned status: " +
                                    String.valueOf(json.status) + " message: " + json.message);

                    progressBar.setVisibility(View.GONE);
                    Snackbar.make(layoutRoot, R.string.msg_error_editpet, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (imageFile != null && imageFile.getAbsolutePath().isEmpty() == false) {
                    Intent serviceIntent = new Intent(PetEditActivity.this, ImageUploadService.class);
                    serviceIntent.putExtra("image_filepath", imageFile.getAbsolutePath());
                    serviceIntent.putExtra("category", "pet");
                    serviceIntent.putExtra("relatedid", pet.id.toHexString());
                    startService(serviceIntent);
                }

                myPets.update(petName, pet);

                progressBar.setVisibility(View.GONE);
                Intent intent = new Intent();
                intent.putExtra("pet_name", pet.name);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                // Either No Network Connection or Server is down.
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Could not update pet Error. msg: " + t.getMessage());
                Crashlytics.log(Log.ERROR, TAG,
                        "Could not insert pet. Error:" + t.getMessage());

                Snackbar.make(layoutRoot, R.string.msg_error_addpet, Snackbar.LENGTH_LONG)
                        .show();

            }
        });
    }

    private Pet getPetFromUserInput() throws InvalidInputException {

        final String petName = editTextPetName.getText().toString().trim();
        if (petName.isEmpty()) {
            final String errorMessage = getString(R.string.msg_error_add_pet_name_required);
            editTextPetName.setError(errorMessage);
            throw new InvalidInputException(InvalidInputException.Type.REQUIRED, errorMessage);
        } else {
            // #TODO Check whether name contains invalid characters or not.
            // Only allow alphanums and single space. No punctuations and multiple spaces.
        }

        Pet pet = new Pet();

        pet.name = petName;
        pet.type = Pet.Type.CAT;
        pet.gender = Pet.Gender.FEMALE;

        final int selectedButtonIdType = radioGroupPetType.getCheckedRadioButtonId();
        if (selectedButtonIdType == R.id.radioPetTypeDog) {
            pet.type = Pet.Type.DOG;
        }

        final int selectedButtonIdGender = radioGroupPetGender.getCheckedRadioButtonId();
        if (selectedButtonIdGender == R.id.radioButtonGenderMale) {
            pet.gender = Pet.Gender.MALE;
        }

        pet.setBirthday(petBirthday.getTime());

        final int pos = spinnerPetSize.getSelectedItemPosition();

        switch (pos) {
            case 0:
                pet.size = Pet.Size.SMALL;
                break;
            case 1:
                pet.size = Pet.Size.MEDIUM;
                break;
            case 2:
                pet.size = Pet.Size.LARGE;
                break;
            case 3:
                pet.size = Pet.Size.XLARGE;
                break;
            case 4:
                pet.size = Pet.Size.XXLARGE;
                break;
            default:
                pet.size = Pet.Size.SMALL;
                break;
        }

        return pet;
    }

    public static class CancelConfirmDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.title_fragment_cancel_confirm);

            // #TODO Set proper confirm message according to the mode.
            builder.setMessage(R.string.add_pet_cancel_confirm)
                    .setPositiveButton(R.string.label_discard, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Activity act = getActivity();
                            act.setResult(act.RESULT_CANCELED);
                            act.finish();
                        }
                    })
                    .setNegativeButton(R.string.label_stay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CancelConfirmDialogFragment.this.getDialog().cancel();
                        }
                    });

            return builder.create();
        }

        @Override
        public void onStart() {
            super.onStart();
            AlertDialog dialog = (AlertDialog) getDialog();
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
        }
    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            //Log.d(TAG, "permissions: " + permissions.toString());
            for (int result : grantResults) {
                Log.d(TAG, "grantResults: " + String.valueOf(result));
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
}
