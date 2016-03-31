package net.team88.dotor.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;


import net.team88.dotor.R;
import net.team88.dotor.account.MyAccount;
import net.team88.dotor.hospitals.Hospitals;
import net.team88.dotor.intro.SplashActivity;
import net.team88.dotor.notifications.PushSetting;
import net.team88.dotor.pets.MyPets;
import net.team88.dotor.reviews.SearchSettings;
import net.team88.dotor.shared.BasicResponse;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.Server;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "Settings";

    View viewRoot;
    private SwitchCompat switchNotification;
    private SwitchCompat switchNotificationLikes;
    private SwitchCompat switchNotificationComments;
    //private Button buttonResetDb;
    //private Button buttonResetAll;

    private PushSetting pushSetting;
    private TextView textTerms;
    private TextView textLicense;
    private View viewAppVersion;
    private View viewCreatedBy;
    private TextView textVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupBasicElements();

        registerElements();

        loadPushSetting();

        registerEvents();


        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            String versionCode = String.valueOf(packageInfo.versionCode);
            textVersion.setText(versionName + " (" + versionCode + ")");

        } catch(PackageManager.NameNotFoundException e) {
            textVersion.setText("Could not retrieve version info.");
        }

    }

    private void loadPushSetting() {
        pushSetting = Settings.getInstance(this).getPushSetting();

        switchNotification.setChecked(pushSetting.isPushOn);
        switchNotificationLikes.setChecked(pushSetting.getLikes);
        switchNotificationComments.setChecked(pushSetting.getComments);

        // Disable children is isPushOn is off.
        switchNotificationLikes.setEnabled(pushSetting.isPushOn);
        switchNotificationComments.setEnabled(pushSetting.isPushOn);
    }

    private void setupBasicElements() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_arrow_left_white_24dp);
    }

    private void registerElements() {
        viewRoot = findViewById(R.id.viewRoot);

        //buttonResetAll = (Button) findViewById(R.id.buttonResetAll);
        //buttonResetDb = (Button) findViewById(R.id.buttonResetDb);

        switchNotification = (SwitchCompat) findViewById(R.id.switchNotification);
        switchNotificationLikes = (SwitchCompat) findViewById(R.id.switchNotificationLikes);
        switchNotificationComments = (SwitchCompat) findViewById(R.id.switchNotificationComments);

        textTerms = (TextView) findViewById(R.id.textTerms);
        textLicense = (TextView) findViewById(R.id.textLicense);
        viewAppVersion = (View) findViewById(R.id.layoutAppVersion);
        viewCreatedBy = (View) findViewById(R.id.layoutCreatedBy);

        textVersion = (TextView) findViewById(R.id.textVersion);
    }

    private void registerEvents() {

        switchNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pushSetting.isPushOn = isChecked;
                switchNotificationLikes.setEnabled(isChecked);
                switchNotificationComments.setEnabled(isChecked);

                updateNotificationSettings();
            }
        });

        switchNotificationLikes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pushSetting.getLikes = isChecked;
                updateNotificationSettings();
            }
        });

        switchNotificationComments.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pushSetting.getComments = isChecked;
                updateNotificationSettings();
            }
        });


        textTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://dotor.team88.net/terms");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        textLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.team88.net/dotor/licenses");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        viewAppVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.team88.net/dotor/version");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        viewCreatedBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.team88.net/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });


//        buttonResetAll.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ResetConfirmDialogFragment frag = new ResetConfirmDialogFragment();
//                frag.setDbReset(false);
//                frag.show(getSupportFragmentManager(), "Reset");
//            }
//        });
//
//        buttonResetDb.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ResetConfirmDialogFragment frag = new ResetConfirmDialogFragment();
//                frag.setDbReset(true);
//                frag.show(getSupportFragmentManager(), "Reset");
//            }
//        });
    }

    private void updateNotificationSettings() {
        DotorWebService service = Server.getInstance(this).getService();
        Call<BasicResponse> call = service.upsertPushSetting(pushSetting);
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccess() == false) {
                    Log.d(TAG, "UpdateNotification failed.");
                    return;
                }

                BasicResponse json = response.body();
                if (json.status < 0) {
                    Log.d(TAG, "UpdateNotification failed. Message: " + json.message);
                    return;
                }

                Log.d(TAG, "Notification updated! pushSetting:" + pushSetting.toString());
                Settings.getInstance(getBaseContext()).setPushSetting(pushSetting);
                loadPushSetting();
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.d(TAG, "UpdateNotification failed. Message: " + t.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        save();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                save();
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    private void save() {

    }

    public void resetDb() {
        final Context ctx = this.getApplicationContext();
        DotorWebService webServiceApi = Server.getInstance(this).getService();
        webServiceApi.resetDb().enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccess() == false) {
                    Toast.makeText(ctx, "RESET FAILED", Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                if (response.body().status < 0) {
                    Toast.makeText(ctx, "RESET FAILED", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                Toast.makeText(ctx, "RESET COMPLETED", Toast.LENGTH_LONG)
                        .show();
                reset();
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Toast.makeText(ctx, "RESET FAILED", Toast.LENGTH_LONG)
                        .show();
                return;

            }
        });
    }

    public void reset() {
        MyPets.getInstance(this).reset();
        MyAccount.getInstance(this).reset();
        Server.getInstance(this).reset();
        Settings.getInstance(this).reset();
        Hospitals.getInstance(this).reset();
        SearchSettings.getInstance(this).reset();


        // #TODO Reset Reviews and other data

        Intent intent = new Intent(SettingsActivity.this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public static class ResetConfirmDialogFragment extends DialogFragment {

        boolean resetDb = false;

        public void setDbReset(boolean val) {
            resetDb = val;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.settings_reset_confirm)
                    .setPositiveButton(R.string.settings_reset, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            SettingsActivity act = (SettingsActivity) getActivity();
                            act.reset();
                            if (resetDb) {
                                act.resetDb();
                            }

                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ResetConfirmDialogFragment.this.getDialog().cancel();
                        }
                    });

            return builder.create();
        }

        @Override
        public void onStart() {
            super.onStart();
            AlertDialog dialog = (AlertDialog) getDialog();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
        }
    }
}
