package net.team88.dotor.hospitals;

import com.google.gson.annotations.SerializedName;


import net.team88.dotor.shared.GeoJson;

import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Created by Eun Leem on 3/2/2016.
 */
public class Hospital {
    @SerializedName("id")
    public ObjectId id;

    @SerializedName("name")
    public String name;

    @SerializedName("location")
    public GeoJson geoJson;

    @SerializedName("address")
    public String address;

    @SerializedName("phone_number")
    public String phoneNumber;

    @SerializedName("updated")
    Date updated;

    @SerializedName("created")
    Date created;

}
