package net.team88.dotor.notifications;

import com.google.gson.annotations.SerializedName;

import net.team88.dotor.shared.BasicResponse;

import java.util.ArrayList;

public class NotificationsResponse extends BasicResponse {
    @SerializedName("notifications")
    public ArrayList<Notification> notifications;
}
