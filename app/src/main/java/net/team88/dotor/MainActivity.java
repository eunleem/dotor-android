package net.team88.dotor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import net.team88.dotor.account.MyAccount;
import net.team88.dotor.account.ProfileEditActivity;
import net.team88.dotor.notifications.NotificationListActivity;
import net.team88.dotor.pets.MyPets;
import net.team88.dotor.pets.PetEditActivity;
import net.team88.dotor.pets.PetListActivity;
import net.team88.dotor.reviews.MyReviewListActivity;
import net.team88.dotor.reviews.ReviewListActivity;
import net.team88.dotor.reviews.ReviewPostActivity;
import net.team88.dotor.reviews.SearchSettings;
import net.team88.dotor.settings.SettingsActivity;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Main";

    private static final String KEY_INITIAL_SETUP = "INIT_SETUP";

    public static final int REQUEST_ADD_PET = 0;
    public static final int REQUEST_EDIT_PROFILE = 1;
    public static final int REQUEST_POST_REVIEW = 2;
    public static final int REQUEST_FEEDBACK = 3;


    View viewRoot;
    ProgressDialog progressDialog;
    NavigationView navigationView;

    Button buttonWriteReview;

    View layoutReviewsByLocation;
    TextView textLocationSubtitle;

    View layoutReviewsByCategories;
    TextView textCategorySubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Fabric.isInitialized()) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_main);

        setupBasicElements();

        registerBasicElements();
        registerEvents();

        login();

        initialSetup();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SearchSettings searchSettings = SearchSettings.getInstance(this);

        String locationName = searchSettings.getLocationName();
        if (!locationName.isEmpty()) {
            textLocationSubtitle.setText(locationName);
        }

        StringBuilder categoryBuilder = new StringBuilder();
        ArrayList<String> selectedCategories = searchSettings.getSelectedCategories();

        for (String cate : selectedCategories) {
            categoryBuilder
                    .append(", ")
                    .append(cate);
        }

        if (categoryBuilder.length() > 1) {
            categoryBuilder.delete(0, 1);
        } else {
            categoryBuilder.append(getString(R.string.no_categories_selected));
        }

        textCategorySubtitle.setText(categoryBuilder.toString());
        updateUserProfileInfo();
    }

    private void setupBasicElements() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        this.navigationView = (NavigationView) findViewById(R.id.nav_view);
        this.navigationView.setNavigationItemSelectedListener(this);
    }

    void initialSetup() {
        // Clear intent action. Prevents duplicate triggering on screen rotation.
        MyPets myPets = MyPets.getInstance(this);
        if (myPets.size() == 0) {
            Intent addPetIntent = new Intent(MainActivity.this, PetEditActivity.class);
            addPetIntent.putExtra(KEY_INITIAL_SETUP, true);
            startActivityForResult(addPetIntent, REQUEST_ADD_PET);
        }

        MyAccount myAccount = MyAccount.getInstance(this);
        if (myAccount.isNicknameSet() == false) {
            Intent editProfileIntent = new Intent(MainActivity.this, ProfileEditActivity.class);
            editProfileIntent.putExtra(KEY_INITIAL_SETUP, true);
            startActivityForResult(editProfileIntent, REQUEST_EDIT_PROFILE);
        }

    }

    private void registerBasicElements() {
        viewRoot = findViewById(R.id.layoutRoot);

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.msg_progress_logging_in));
            progressDialog.setIndeterminate(true);
            progressDialog.setCanceledOnTouchOutside(false);
        }

        buttonWriteReview = (Button) findViewById(R.id.buttonWriteReview);

        layoutReviewsByLocation = (View) findViewById(R.id.layout_reviews_by_location);
        layoutReviewsByCategories = (View) findViewById(R.id.layout_reviews_by_categories);

        textLocationSubtitle = (TextView) findViewById(R.id.textLocationSubtitle);
        textCategorySubtitle = (TextView) findViewById(R.id.textCategorySubtitle);
    }

    private void registerEvents() {
        buttonWriteReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyPets myPets = MyPets.getInstance(getApplicationContext());
                if (myPets.getPets() == null || myPets.getPets().size() == 0) {
                    Snackbar.make(buttonWriteReview, R.string.msg_error_no_pet, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, ReviewPostActivity.class);
                startActivity(intent);
            }
        });

        layoutReviewsByLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReviewListActivity.class);
                intent.setAction(ReviewListActivity.KEY_MODE_LOCATION);
                startActivity(intent);
            }
        });

        layoutReviewsByCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReviewListActivity.class);
                intent.setAction(ReviewListActivity.KEY_MODE_CATEGORY);
                startActivity(intent);
            }
        });
    }

    private void login() {
        MyAccount myAccount = MyAccount.getInstance(this);
        if (myAccount.isRegistered()) {
            if (myAccount.isLoginNeeded()) {
                myAccount.login(viewRoot);
            }
        }
    }

    void updateUserProfileInfo() {
        View headerLayout = this.navigationView.getHeaderView(0);

        TextView textViewNickName = (TextView) headerLayout.findViewById(R.id.textViewNickname);
        TextView textViewEmail = (TextView) headerLayout.findViewById(R.id.textViewEmail);

        MyAccount myAccount = MyAccount.getInstance(this);

        String nickname = myAccount.getAccount().getNickname();
        String email = myAccount.getAccount().getEmail();

        if (nickname != null && !nickname.isEmpty()) {
            textViewNickName.setText(nickname);
        }

        if (email != null && !email.isEmpty()) {
            textViewEmail.setText(email);
        }

        headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileEditActivity.class);
                startActivityForResult(intent, REQUEST_EDIT_PROFILE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_PET:
                if (resultCode == RESULT_OK) {
                    final String petName = data.getStringExtra("pet_name");
                    final String message = String.format(getString(R.string.msg_ok_addpet), petName);
                    Snackbar.make(viewRoot, message, Snackbar.LENGTH_SHORT)
                            .show();

                } else if (resultCode == RESULT_CANCELED) {
                    Snackbar.make(viewRoot, getString(R.string.msg_cancel_addpet), Snackbar.LENGTH_LONG)
                            .show();

                } else {
                    Snackbar.make(viewRoot, getString(R.string.msg_error_addpet), Snackbar.LENGTH_LONG)
                            .show();
                }
                break;


            case REQUEST_EDIT_PROFILE:
                if (resultCode == RESULT_OK) {
                    updateUserProfileInfo();

                    //} else if (resultCode == RESULT_CANCELED) {

                }
                break;


            case REQUEST_FEEDBACK:
                if (resultCode == RESULT_OK) {
                    Snackbar.make(viewRoot, getString(R.string.msg_feedback_sent), Snackbar.LENGTH_LONG)
                            .show();

                    //} else if (resultCode == RESULT_CANCELED) {

                }
                break;

            case REQUEST_POST_REVIEW:
                if (resultCode == RESULT_OK) {
                    //Snackbar.make(viewRoot, getString(R.string.msg_), Snackbar.LENGTH_LONG).show();
                    //} else if (resultCode == RESULT_CANCELED) {
                }
                break;

            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_notification:
                Intent intent = new Intent(MainActivity.this, NotificationListActivity.class);
                startActivity(intent);
                break;

            default:
                Log.d(TAG, "onOptionsItemSelected: Event Unhandled.");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_my_pet_list: {
                Intent intent = new Intent(MainActivity.this, PetListActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_my_reviews: {
                Intent intent = new Intent(MainActivity.this, MyReviewListActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_settings: {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.nav_share:
                break;

            case R.id.nav_like: {
                //Intent intent = new Intent(MainActivity.this, KudoActivity.class);
                //startActivity(intent);

                break;
            }
            case R.id.nav_send_feedback: {
                //Intent intent = new Intent(MainActivity.this, FeedbackActivity.class);
                //startActivityForResult(intent, REQUEST_FEEDBACK);

                break;
            }
            case R.id.dev_reset:

                break;
            case R.id.dev_hospital_insert: {
                //Intent intent = new Intent(MainActivity.this, InsertHospitalActivity.class);
                //startActivity(intent);

                break;
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
