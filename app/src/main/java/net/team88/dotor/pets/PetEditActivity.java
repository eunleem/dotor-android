package net.team88.dotor.pets;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import net.team88.dotor.R;
import net.team88.dotor.BuildConfig;
import net.team88.dotor.shared.BasicResponse;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.InsertResponse;
import net.team88.dotor.shared.InvalidInputException;
import net.team88.dotor.shared.Server;
import net.team88.dotor.utils.Utils;

import org.bson.types.ObjectId;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PetEditActivity extends AppCompatActivity {

    private final String TAG = "EDIT_PET";

    public static final String KEY_PET_NAME = "PET_NAME";
    public static final String KEY_MODE = "mode";
    public static final String VALUE_MODE_EDIT = "edit";

    // FEATURES ENABLED
    private static final boolean ENABLE_CANCEL_CONFIRMATION = true;

    private SimpleDateFormat birthdayDateFormat;

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

    private void registerEvents() {
        textPetBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialogPetBirthday.show();
            }
        });
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
        editTextPetName = find(R.id.editTextPetName);

        radioGroupPetType = find(R.id.radioGroupPetType);
        radioGroupPetGender = find(R.id.radioGroupPetGender);

        textPetBirthday = find(R.id.textPetBirthday);
        spinnerPetSize = find(R.id.spinnerPetWeight);
        progressBar = find(R.id.progressBar);

        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);
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
                if (response.isSuccess() == false) {
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
                if (response.isSuccess() == false) {
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
}
