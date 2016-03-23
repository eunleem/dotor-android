package net.team88.dotor.reviews;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import net.team88.dotor.R;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.Server;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyReviewListActivity extends AppCompatActivity {

    private static final String TAG = "MyReviews";

    SwipeRefreshLayout layoutSwipeRefresh;
    RecyclerView recyclerView;

    ReviewsRecyclerViewAdapter viewAdapter;
    private TextView textNothingMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_review_list);

        setupBasicElements();
        registerElements();
        registerEvents();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        viewAdapter = new ReviewsRecyclerViewAdapter(this);
        recyclerView.setAdapter(viewAdapter);

        setupSwipeRefreshLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getReviews();
    }

    void setupBasicElements() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void registerElements() {
        layoutSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.layoutSwipeRefresh);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewReviews);

        textNothingMessage = (TextView) findViewById(R.id.textNothingMessage);
    }

    void registerEvents() {

    }

    void setupSwipeRefreshLayout() {
        layoutSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getReviews();
            }
        });

        layoutSwipeRefresh.setDistanceToTriggerSync(500);
        layoutSwipeRefresh.setColorSchemeResources(R.color.colorAccent, R.color.colorSecondary, R.color.colorPrimary);
        TypedValue typed_value = new TypedValue();
        this.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        layoutSwipeRefresh.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
    }

    void getReviews() {
        layoutSwipeRefresh.setRefreshing(true);

        DotorWebService webServiceApi = Server.getInstance(this).getService();
        Call<ReviewsResponse> call = webServiceApi.getMyReviews();

        call.enqueue(new Callback<ReviewsResponse>() {
            @Override
            public void onResponse(Call<ReviewsResponse> call, Response<ReviewsResponse> response) {

                if (response.isSuccess() == false) {
                    Log.e(TAG, "getReviews failed!");
                    layoutSwipeRefresh.setRefreshing(false);
                    return;
                }

                ReviewsResponse body = response.body();

                if (body.status < 0) {
                    Log.e(TAG, "getReviews returned status non-zero! message: " + body.message);
                    layoutSwipeRefresh.setRefreshing(false);
                    return;
                }

                //Log.i(TAG, "GetReviews: count: " + String.valueOf(body.reviews.size()));

                final ArrayList<Review> reviews = body.reviews;
                if (reviews == null || reviews.size() == 0) {
                    layoutSwipeRefresh.setRefreshing(false);
                    textNothingMessage.setVisibility(View.VISIBLE);
                    Snackbar.make(layoutSwipeRefresh, R.string.msg_no_my_reviews, Snackbar.LENGTH_INDEFINITE)
                            .show();
                    return;
                }

                textNothingMessage.setVisibility(View.INVISIBLE);

                viewAdapter.clear();
                viewAdapter.addItems(reviews);
                viewAdapter.notifyDataSetChanged();
                layoutSwipeRefresh.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ReviewsResponse> call, Throwable t) {
                layoutSwipeRefresh.setRefreshing(false);
                Log.e(TAG, "GetReviews onFailure: " + t.getMessage());
            }
        });
    }

}
