package net.team88.dotor.notifications;

import com.google.gson.annotations.SerializedName;

import org.bson.types.ObjectId;

import java.util.Date;


public class Notification {
    @SerializedName("id")
    public ObjectId id;

    @SerializedName("type")
    public String type;

    @SerializedName("message")
    public String message;

    @SerializedName("relatedtype")
    public String relatedType;

    @SerializedName("relatedid")
    public ObjectId relatedId;

    @SerializedName("isread")
    public boolean isRead;

    @SerializedName("created")
    public Date created;
}
