package net.team88.dotor.notifications;


import com.google.gson.annotations.SerializedName;

import net.team88.dotor.shared.BasicResponse;


public class NotificationResponse extends BasicResponse {
    @SerializedName("notification")
    public Notification notification;
}
