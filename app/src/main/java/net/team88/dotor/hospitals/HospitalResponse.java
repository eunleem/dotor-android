package net.team88.dotor.hospitals;


import com.google.gson.annotations.SerializedName;

import net.team88.dotor.shared.BasicResponse;

/**
 * Created by Eun Leem on 3/7/2016.
 */
public class HospitalResponse extends BasicResponse {
    @SerializedName("hospital")
    public Hospital hospital;
}
