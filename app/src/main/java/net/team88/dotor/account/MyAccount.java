package net.team88.dotor.account;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;

import net.team88.dotor.BuildConfig;
import net.team88.dotor.R;
import net.team88.dotor.notifications.GcmRegistrationService;
import net.team88.dotor.shared.BasicResponse;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.Server;
import net.team88.dotor.utils.GsonUtils;
import net.team88.dotor.utils.Utils;

import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * MyAccount class in Singleton to mange User Account Information.
 */
public class MyAccount {
    static final String TAG = "MyAccount";
    static final String KEY_MY_ACCOUNT = "my_account";
    private static final String KEY_IS_GCM_TOKEN_SET = "gcm_token_set";

    static MyAccount sInstance;
    Context context;
    SharedPreferences storage;

    boolean isGcmTokenSet = false;

    Account account;

    private MyAccount(Context context) {
        this.context = context;
        this.storage = this.context.getSharedPreferences(TAG, Context.MODE_PRIVATE);

        String myAccountJson = this.storage.getString(KEY_MY_ACCOUNT, "");
        if (myAccountJson.isEmpty() == false) {
            Gson gson = GsonUtils.getGson();
            account = gson.fromJson(myAccountJson, Account.class);
        }

        this.isGcmTokenSet = this.storage.getBoolean(KEY_IS_GCM_TOKEN_SET, false);
    }

    public static synchronized MyAccount getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MyAccount(context.getApplicationContext());
        }
        return sInstance;
    }

    public void save() {
        Gson gson = GsonUtils.getGson();
        final String json = gson.toJson(account);
        storage.edit()
                .putString(KEY_MY_ACCOUNT, json)
                .apply();

        if (BuildConfig.DEBUG) Log.d(TAG, "MyAccount Saved! " + json);
    }

    public void reset() {
        this.storage.edit().clear().apply();
        sInstance = null;
    }

    public boolean isRegistered() {
        if (this.account == null || this.account.getUsername() == null || this.account.getUsername().isEmpty()) {
            return false;
        }

        return true;
    }

    public boolean isNicknameSet() {
        if (account == null || account.getNickname() == null || account.getNickname().isEmpty()) {
            return false;
        }
        return true;
    }

    public boolean setAccount(String username, String password) {
        if (this.isRegistered()) {
            if (BuildConfig.DEBUG) Log.e(TAG, "setAccount: Account already exists.");
            return false;
        }

        this.account = new Account();
        this.account.setUsername(username);
        this.account.setPassword(password);

        if (Utils.isPlayServicesAvailable(this.context)) {
            Intent intent = new Intent(this.context, GcmRegistrationService.class);
            this.context.startService(intent);
        }

        this.save();

        return true;
    }

    public boolean switchAccount(String username, String password) {
        this.account = new Account();
        this.account.setUsername(username);
        this.account.setPassword(password);

        if (Utils.isPlayServicesAvailable(this.context)) {
            Intent intent = new Intent(this.context, GcmRegistrationService.class);
            this.context.startService(intent);
        }

        this.save();

        return true;
    }

    public Account getAccount() {
        return account;
    }

    public void setNickname(String nickname) {
        getAccount().setNickname(nickname);
        this.save();
    }

    public void setLastLogin(Date lastLogin) {
        this.account.lastLogin = lastLogin;
        this.save();
    }

    public boolean isLoginNeeded() {
        if (this.account.lastLogin == null) {
            return true;
        }

        // Needs login after 1 minutes
        if (this.account.lastLogin.getTime() + (1000 * 60 * 1) < (new Date()).getTime())  {
            return true;
        }

        return false;
    }

    public void login(final View snackBarParent) {
        DotorWebService service = Server.getInstance(this.context).getService();
        service.login(new LoginRequest(this.getAccount())).enqueue(
                new Callback<BasicResponse>() {
                    @Override
                    public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                        final String TAG = "Login";
                        if (response.isSuccessful() == false) {
                            // Server Level Error
                            Log.e(TAG, "Request failed. Msg: " + response.message());
                            final String message = context.getString(R.string.msg_error_login);
                            Snackbar.make(snackBarParent, message, Snackbar.LENGTH_INDEFINITE).show();
                            return;
                        }

                        BasicResponse json = response.body();

                        Log.i(TAG, "status: " + String.valueOf(json.status));
                        Log.i(TAG, "message: " + json.message);

                        if (json.status < 0) {
                            // Application Level Error
                            final String message = context.getString(R.string.msg_error_login);
                            Snackbar.make(snackBarParent, message, Snackbar.LENGTH_INDEFINITE).show();
                            return;
                        }

                        if (BuildConfig.DEBUG) {
                            //Snackbar.make(viewRoot, "Logged in!", Snackbar.LENGTH_LONG).show();
                        }

                        if (json.status == 2) {
                            // Email Verified
                            //setEmailVerified();
                        }


                        setLastLogin(new Date());
                    }

                    @Override
                    public void onFailure(Call<BasicResponse> call, Throwable t) {
                        final String message = context.getString(R.string.msg_error_bad_connection);
                        Snackbar.make(snackBarParent, message, Snackbar.LENGTH_INDEFINITE).show();
                    }
                }
        );
    }

    /**
     * Validates Nickname and returns error messages.
     *
     * @param nickname
     * @return ArrayList of String Resource ids. If size() == 0, no error and it's valid.
     */
    public static ArrayList<Integer> validateNickname(String nickname) {
        ArrayList<Integer> errorMessages = new ArrayList<>();

        nickname = nickname.trim();

        if (nickname.length() <= 2) {
            errorMessages.add(R.string.nickname_invalid_too_short);
        }

        if (nickname.contains("  ")) { // Disallow Double space.
            errorMessages.add(R.string.nickname_invalid_double_space);
        }

        return errorMessages;
    }

    public void setIsGcmTokenSet(boolean isGcmTokenSet) {
        this.isGcmTokenSet = isGcmTokenSet;
    }
}
