package net.team88.dotor.shared;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Eun Leem on 3/7/2016.
 */
public class GeoJson {
    @SerializedName("type")
    public String type = "Point";

    @SerializedName("coordinates")
    public Double[] coordinates;
}
