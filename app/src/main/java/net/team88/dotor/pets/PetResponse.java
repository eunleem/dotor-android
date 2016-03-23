package net.team88.dotor.pets;

import com.google.gson.annotations.SerializedName;

import net.team88.dotor.shared.BasicResponse;

/**
 * Created by jeind on 3/1/2016.
 */
public class PetResponse extends BasicResponse {
    @SerializedName("pet")
    public Pet pet;
}
