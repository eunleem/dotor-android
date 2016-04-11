package net.team88.dotor.settings;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.team88.dotor.R;
import net.team88.dotor.account.Account;
import net.team88.dotor.account.MyAccount;
import net.team88.dotor.pets.MyPets;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.Server;

public class MyAccountActivity extends AppCompatActivity {

    private TextView textPassword;
    private TextView textUsername;
    private Button buttonSetAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        setupBasicElements();

        registerElements();

        registerEvents();

        Account account = MyAccount.getInstance(this).getAccount();
        String username = account.getUsername();
        String password = account.getPassword();

        textUsername.setText(username);
        textPassword.setText(password);

        buttonSetAccount.setVisibility(View.GONE);
    }

    private void setupBasicElements() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void registerElements() {
        textUsername = (TextView) findViewById(R.id.textUsername);
        textPassword = (TextView) findViewById(R.id.textPassword);

        buttonSetAccount = (Button) findViewById(R.id.buttonSetAccount);
    }

    private void registerEvents() {
        buttonSetAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyAccount myAccount = MyAccount.getInstance(getApplicationContext());
                myAccount.switchAccount(textUsername.getText().toString(), textPassword.getText().toString());
                Snackbar.make(buttonSetAccount, "Saved", Snackbar.LENGTH_LONG)
                        .show();
            }
        });
    }

}
