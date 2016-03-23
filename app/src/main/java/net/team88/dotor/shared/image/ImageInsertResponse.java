package net.team88.dotor.shared.image;

import com.google.gson.annotations.SerializedName;

import net.team88.dotor.shared.InsertResponse;


/**
 * Created by Eun Leem on 2/19/2016.
 */
public class ImageInsertResponse extends InsertResponse {
    @SerializedName("filename")
    public String filename;
}
