package net.team88.dotor.hospitals;


import com.google.gson.annotations.SerializedName;

import net.team88.dotor.shared.BasicResponse;

import java.util.ArrayList;

/**
 * Created by Eun Leem on 3/7/2016.
 */
public class HospitalsResponse extends BasicResponse {
    @SerializedName("hospitals")
    public ArrayList<Hospital> hospitals;
}
