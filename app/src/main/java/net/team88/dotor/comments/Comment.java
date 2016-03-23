package net.team88.dotor.comments;

import com.google.gson.annotations.SerializedName;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Eun Leem on 2/24/2016.
 */
public class Comment {
    @SerializedName("id")
    public ObjectId id;

    @SerializedName("reviewid")
    public ObjectId reviewId;

    @SerializedName("userid")
    public ObjectId userId;

    @SerializedName("nickname")
    public String nickname;

    @SerializedName("commentbody")
    public String commentBody;

    @SerializedName("likes")
    public ArrayList<ObjectId> likes;

    @SerializedName("created")
    public Date created;
}
