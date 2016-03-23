package net.team88.dotor.intro;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.crashlytics.android.Crashlytics;

import net.team88.dotor.MainActivity;
import net.team88.dotor.R;
import net.team88.dotor.account.MyAccount;
import net.team88.dotor.account.SignupResponse;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.Server;
import net.team88.dotor.utils.Utils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IntroActivity extends AppCompatActivity implements IntroPagerAdapter.OnStartClickedListener {

    private static final String TAG = "Intro";

    @SuppressWarnings("unchecked")
    public <T extends View> T find(int id) {
        return (T) findViewById(id);
    }

    private RelativeLayout layoutRoot;
    private Button buttonStart;
    private ProgressBar progressBar;


    private IntroViewPager viewPager;
    private ArrayList<View> pageViews;
    private ViewGroup pointLayout;
    private ImageView[] pointViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        registerElements();
        setIntroPage();
        setProgressBarIndeterminate(true);
        progressBar.setVisibility(View.GONE);
    }

    private void registerElements() {
        layoutRoot = find(R.id.layoutRoot);
        progressBar = find(R.id.progress);
        viewPager = find(R.id.viewpager);
        pointLayout = find(R.id.pointlayout);

    }

    private void setIntroPage() {
        LayoutInflater inflater = getLayoutInflater();
        pageViews = new ArrayList<>();
        pageViews.add(inflater.inflate(R.layout.intro_page1, null));
        pageViews.add(inflater.inflate(R.layout.intro_page2, null));
        pageViews.add(inflater.inflate(R.layout.intro_page3, null));
        pageViews.add(inflater.inflate(R.layout.intro_page4, null));

        pointViews = new ImageView[pageViews.size()];

        for (int i = 0; i < pageViews.size(); i++) {
            pointViews[i] = new ImageView(IntroActivity.this);
            int width = Utils.convertDpToPx(this, 10);
            int height = Utils.convertDpToPx(this, 10);
            int margin = Utils.convertDpToPx(this, 3);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            params.setMargins(margin, 0, margin, 0);
            pointViews[i].setLayoutParams(params);


            if (i == 0) {
                pointViews[i].setBackgroundResource(R.drawable.intro_circle_current);
            } else {
                pointViews[i].setBackgroundResource(R.drawable.intro_circle_no);
            }

            pointLayout.addView(pointViews[i]);
        }

        viewPager.setAdapter(new IntroPagerAdapter(pageViews, this));
        viewPager.addOnPageChangeListener(new PageChangeListener(pointViews));
    }

    public void onStartClicked(Button btn) {
        buttonStart = btn;
        progressBar.setVisibility(View.VISIBLE);
        buttonStart.setEnabled(false);
        layoutRoot.setEnabled(false);
        viewPager.setSwipeEnabled(false);

        DotorWebService service = Server.getInstance(this).getService();
        service.signup().enqueue(new SignupCallback());

    }

    protected class SignupCallback implements Callback<SignupResponse> {
        @Override
        public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
            buttonStart.setEnabled(true);
            layoutRoot.setEnabled(true);
            viewPager.setSwipeEnabled(true);
            progressBar.setVisibility(View.GONE);

            if (response.isSuccess() == false) {
                // Server Level Error
                Log.e(TAG, "Request failed. Msg: " + response.message());
                final String message = getString(R.string.msg_error_register);
                Snackbar.make(buttonStart, message, Snackbar.LENGTH_LONG).show();
                return;
            }

            SignupResponse responseBody = response.body();

            Log.i(TAG, "status: " + String.valueOf(responseBody.status));
            Log.i(TAG, "message: " + responseBody.message);

            if (responseBody.status < 0) {
                // Application Level Error
                final String message = getString(R.string.msg_error_register);
                Snackbar.make(buttonStart, message, Snackbar.LENGTH_LONG).show();
                return;
            }


            Log.i(TAG, "username: " + responseBody.username);

            MyAccount myAccount = MyAccount.getInstance(getApplicationContext());
            myAccount.setAccount(responseBody.username, responseBody.password);

            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        @Override
        public void onFailure(Call<SignupResponse> call, Throwable t) {
            // Network Level Error
            final String Tag = "Register";
            final String logMessage = "Failed to make request. msg: " + t.getMessage();

            Log.e(Tag, logMessage);
            Crashlytics.log(Log.ERROR, Tag, logMessage);

            final String message = getString(R.string.msg_error_bad_connection);
            Snackbar.make(layoutRoot, message, Snackbar.LENGTH_LONG)
                    .show();

            buttonStart.setEnabled(true);
            layoutRoot.setEnabled(true);
            viewPager.setSwipeEnabled(true);
            progressBar.setVisibility(View.GONE);
        }
    }


    public class PageChangeListener implements ViewPager.OnPageChangeListener {
        private ImageView[] pointViews;

        public PageChangeListener(ImageView[] pointViews) {
            super();
            this.pointViews = pointViews;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            for (int i = 0; i < this.pointViews.length; i++) {
                if (position == i) {
                    this.pointViews[i].setBackgroundResource(R.drawable.intro_circle_current);
                } else {
                    this.pointViews[i].setBackgroundResource(R.drawable.intro_circle_no);

                }
            }
        }
    }
}

class IntroViewPager extends ViewPager {

    private boolean enabled;

    public IntroViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (this.enabled)
            return super.onTouchEvent(ev);
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (this.enabled)
            return super.onInterceptTouchEvent(ev);
        return false;
    }

    public void setSwipeEnabled(boolean bool) {
        this.enabled = bool;
    }
}
