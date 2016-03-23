package net.team88.dotor.notifications;

import com.google.gson.annotations.SerializedName;

import net.team88.dotor.utils.GsonUtils;

/**
 * Created by Eun Leem on 2/25/2016.
 */
public class PushSetting {
    @SerializedName("token")
    public String token;

    @SerializedName("ispushon")
    public boolean isPushOn;

    @SerializedName("getlikes")
    public boolean getLikes;

    @SerializedName("getcomments")
    public boolean getComments;

    @Override
    public String toString() {
        return GsonUtils.getGson().toJson(this);
    }
}
