package net.team88.dotor.shared;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Eun Leem on 3/21/2016.
 */
public class InsertResponse  extends BasicResponse{
    @SerializedName("newid")
    public String newid;
}
