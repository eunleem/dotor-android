package net.team88.dotor.notifications;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eun Leem on 2/24/2016.
 */
public class Notifications {
    private static final String TAG = "Notifications";

    private static Notifications sInstance;

    private Context context;


    List<Notification> notifications;

    private Notifications(Context context) {
        this.context = context;
        this.notifications = new ArrayList<>();
    }

    public static synchronized Notifications getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Notifications(context);
        }
        return sInstance;
    }


    public List<Notification> getNotifications() {
        return this.notifications;
    }

    public void setNotifications(List<Notification> notis) {
        this.notifications = notis;
    }

}

