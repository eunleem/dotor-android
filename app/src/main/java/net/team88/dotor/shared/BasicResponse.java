package net.team88.dotor.shared;

import com.google.gson.annotations.SerializedName;

/**
 * All the responses from Dotor Server contains these basic fields.
 */
public class BasicResponse {
    @SerializedName("status")
    public int status;

    @SerializedName("message")
    public String message;
}
