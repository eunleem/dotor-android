package net.team88.dotor.reviews;

import com.google.gson.annotations.SerializedName;

import net.team88.dotor.utils.GsonUtils;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;

public class Review implements Comparable<Review> {
    @Override
    public int compareTo(Review another) {
        return created.compareTo(another.created);
    }

    @SerializedName("id")
    public ObjectId id;

    @SerializedName("petid")
    public ObjectId petid;

    @SerializedName("pet_type")
    public int petType;

    @SerializedName("pet_gender")
    public int petGender;

    @SerializedName("pet_age")
    public int petAge;

    @SerializedName("pet_size")
    public int petSize;

    @SerializedName("hospitalid")
    public ObjectId hospitalid;

    @SerializedName("hospital_name")
    public String hospitalName;

    //@SerializedName("location")
    public ArrayList<Double> locationCoordinates;

    @SerializedName("location_name")
    public String locationName;

    // TODO this is DEPRECATED
    @SerializedName("category")
    public String category;

    @SerializedName("categories")
    public ArrayList<String> categories;

    @SerializedName("cost")
    public int cost;

    @SerializedName("reviewbody")
    public String reviewBody;

    @SerializedName("images")
    public ArrayList<ObjectId> images;

    @SerializedName("likes")
    public ArrayList<ObjectId> likes;

    @SerializedName("comments")
    public ArrayList<ObjectId> comments;

    @SerializedName("isdraft")
    public boolean isDraft;

    @SerializedName("created")
    public Date created;


    @Override
    public String toString() {
        return GsonUtils.getGson().toJson(this);
    }
}
