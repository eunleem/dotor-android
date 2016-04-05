package net.team88.dotor.account;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.team88.dotor.BuildConfig;
import net.team88.dotor.R;
import net.team88.dotor.shared.BasicResponse;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.Server;
import net.team88.dotor.utils.Utils;

import java.util.ArrayList;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileEditActivity extends AppCompatActivity {

    enum Mode {
        INITIAL_SETUP, // ADD
        EDIT
    }

    final String TAG = "EDIT_PROFILE";

    Mode mode;
    Menu menu;

    View viewRoot;
    ProgressDialog progressBar;

    EditText editTextNickname;
    EditText editTextEmail;

    TextView textEmailMessage;

    @SuppressWarnings("unchecked")
    public <T extends View> T find(int id) {
        return (T) findViewById(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        setupBasicElements();
        registerElements();
        registerEvents();

        MyAccount myAccount = MyAccount.getInstance(this);
        if (myAccount.isNicknameSet() == false) {
            // Initial Setup
            setModeInitialSetup();
        } else {
            setModeEdit();
        }


    }

    private void setupBasicElements() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void registerElements() {
        viewRoot = find(R.id.viewRoot);

        editTextNickname = find(R.id.editTextNickname);
        editTextEmail = find(R.id.editTextEmail);
        textEmailMessage = find(R.id.textEmailMessage);

        progressBar = new ProgressDialog(this);
        progressBar.setIndeterminate(true);
        progressBar.setMessage(getString(R.string.profile_saving_changes));
    }

    private void registerEvents() {
        editTextNickname.setFilters(new InputFilter[]{filterAlphaNumKor});

    }

    public InputFilter filterAlphaNumKor = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9ㄱ-ㅣ가-힣]*$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };

    private void setModeInitialSetup() {
        this.mode = Mode.INITIAL_SETUP;
        final android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setTitle(R.string.title_activity_setup_profile);

        textEmailMessage.setText(R.string.email_will_be_verified);
        textEmailMessage.setTextColor(Color.DKGRAY);
        textEmailMessage.setVisibility(View.GONE);
    }

    private void setModeEdit() {
        this.mode = Mode.EDIT;
        final android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.title_activity_edit_profile);

        MyAccount myAccount = MyAccount.getInstance(this);

        String nickname = myAccount.getAccount().getNickname();
        String email = myAccount.getAccount().getEmail();

        if (nickname != null) {
            editTextNickname.setText(nickname);
        }

        if (email != null) {
            editTextEmail.setText(email);
        }


        if (myAccount.getAccount().isEmailVerified()) {
            textEmailMessage.setText(getString(R.string.email_verified));
            textEmailMessage.setTextColor(Color.GREEN);
        } else {
            textEmailMessage.setText(getString(R.string.email_unverified));
            textEmailMessage.setTextColor(Color.RED);
        }

        textEmailMessage.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();

        } else if (id == R.id.action_save) {
            saveProfile();

        } else if (id == R.id.action_skip) {
            setResult(RESULT_CANCELED);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (this.mode == Mode.INITIAL_SETUP) {
            // Back Button Disabled.
            return;
        }
        super.onBackPressed();
    }

    private boolean isUserInputValid(boolean displayErrorMessage) {
        final String nickname = editTextNickname.getText().toString();
        final String email = editTextEmail.getText().toString();

        ArrayList<Integer> nicknameErrorMessages = MyAccount.validateNickname(nickname);
        if (displayErrorMessage && nicknameErrorMessages.size() > 0) {
            editTextNickname.setError(getString(nicknameErrorMessages.get(0)));
        } else {
            editTextNickname.setError(null);
        }

        boolean isVaildEmail = Utils.isValidEmail(email);
        if (displayErrorMessage && isVaildEmail == false && email.isEmpty() == false) {
            editTextEmail.setError(getString(R.string.msg_error_email));
        } else {
            editTextEmail.setError(null);
            isVaildEmail = true;
        }

        return nicknameErrorMessages.size() == 0 && isVaildEmail;
    }

    private boolean saveProfile() {

        if (isUserInputValid(true) == false) {
            Snackbar.make(editTextNickname, R.string.msg_error_invalid_input, Snackbar.LENGTH_LONG)
                    .show();
            return false;
        }


        final String nickname = editTextNickname.getText().toString().trim();

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "nickname: " + nickname);
        }

        final MyAccount myAccount = MyAccount.getInstance(this);

        progressBar.show();

        final DotorWebService webServiceApi = Server.getInstance(this).getService();
        webServiceApi
                .updateNickname(nickname)
                .enqueue(new Callback<BasicResponse>() {
                    @Override
                    public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                        if (response.isSuccessful() == false) {
                            Log.d(TAG, "Update Nickname failed.");
                            Snackbar.make(viewRoot, R.string.msg_error_nickname, Snackbar.LENGTH_LONG)
                                    .show();
                            return;
                        }

                        if (response.body().status < 0) {
                            Log.d(TAG, "Update Nickname failed. Message: " + response.body().message);
                            Snackbar.make(viewRoot, R.string.msg_error_nickname, Snackbar.LENGTH_LONG)
                                    .show();
                            return;
                        }

                        myAccount.setNickname(nickname);

                        progressBar.hide();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<BasicResponse> call, Throwable t) {
                        progressBar.hide();
                        Log.d(TAG, "Update Nickname failed. Message: " + t.getMessage());
                        Snackbar.make(viewRoot, R.string.msg_error_nickname, Snackbar.LENGTH_LONG)
                                .show();
                    }
                });


        return true;
    }
}
