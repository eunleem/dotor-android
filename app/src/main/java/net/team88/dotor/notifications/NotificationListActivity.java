package net.team88.dotor.notifications;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class NotificationListActivity extends AppCompatActivity {

    private static final String TAG = "Notifications";

    SwipeRefreshLayout layoutSwipeRefresh;

    private RecyclerView recyclerViewNoficiations;
    private TextView textNothingMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);

        setupBasicElements();
        registerElements();

        recyclerViewNoficiations.setLayoutManager(new LinearLayoutManager(this));

        setupSwipeRefreshLayout();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchNotifications();
    }

    private void setupSwipeRefreshLayout() {
        layoutSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNotifications();
            }
        });

        layoutSwipeRefresh.setDistanceToTriggerSync(500);
        layoutSwipeRefresh.setColorSchemeResources(R.color.colorAccent, R.color.colorSecondary, R.color.colorPrimary);
        TypedValue typed_value = new TypedValue();
        this.getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
        layoutSwipeRefresh.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
    }


    private void fetchNotifications() {
        layoutSwipeRefresh.setRefreshing(true);

        DotorWebService webServiceApi = Server.getInstance(this).getService();
        Call<NotificationsResponse> call = webServiceApi.getNotifications();
        call.enqueue(new Callback<NotificationsResponse>() {
            @Override
            public void onResponse(Call<NotificationsResponse> call, Response<NotificationsResponse> response) {
                layoutSwipeRefresh.setRefreshing(false);
                if (response.isSuccessful() == false) {
                    Log.e(TAG, "Failed to get Notifications.");
                    return;
                }
                if (response.body().status < 0) {
                    Log.e(TAG, "Failed to get Notification. Message: " + response.body().message);
                    return;
                }

                ArrayList<Notification> notifications = response.body().notifications;
                if (notifications != null) {
                    Notifications.getInstance(getBaseContext()).setNotifications(notifications);
                    final NotificationsRecyclerViewAdapter adapter = new NotificationsRecyclerViewAdapter(getBaseContext());
                    recyclerViewNoficiations.setAdapter(adapter);
                    Log.d(TAG, "Fetched Notifications from Server. Size: " + String.valueOf(notifications.size()));
                    adapter.notifyDataSetChanged();
                }

                if (notifications == null || notifications.size() == 0) {
                    textNothingMessage.setVisibility(View.VISIBLE);
                } else {
                    textNothingMessage.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<NotificationsResponse> call, Throwable t) {
                layoutSwipeRefresh.setRefreshing(false);
                Log.e(TAG, "Failed to get Notifications. t: " + t.getMessage());
            }
        });
    }

    private void registerElements() {
        layoutSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.layoutSwipeRefresh);
        recyclerViewNoficiations = (RecyclerView) findViewById(R.id.recyclerViewNotifications);

        textNothingMessage = (TextView) findViewById(R.id.textNothingMessage);
    }

    private void setupBasicElements() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
