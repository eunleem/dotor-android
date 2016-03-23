package net.team88.dotor.shared.image;

import com.google.gson.annotations.SerializedName;

import net.team88.dotor.shared.BasicResponse;


/**
 * Created by jeind on 3/1/2016.
 */
public class ImageResponse extends BasicResponse {
    @SerializedName("image_url")
    public String imageUrl;

    @SerializedName("thumbnail_url")
    public String thumbnailUrl;
}
