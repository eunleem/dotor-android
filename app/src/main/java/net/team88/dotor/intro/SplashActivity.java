package net.team88.dotor.intro;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;


import net.team88.dotor.MainActivity;
import net.team88.dotor.account.MyAccount;

import io.fabric.sdk.android.Fabric;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        MyAccount myAccount = MyAccount.getInstance(this);

        Intent intent;
        if (myAccount.isRegistered()) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, IntroActivity.class);
        }

        startActivity(intent);
        finish();
    }

}
