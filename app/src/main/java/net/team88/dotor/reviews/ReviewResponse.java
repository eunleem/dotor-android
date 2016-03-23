package net.team88.dotor.reviews;

import com.google.gson.annotations.SerializedName;

import net.team88.dotor.pets.Pet;
import net.team88.dotor.shared.BasicResponse;

public class ReviewResponse extends BasicResponse {
    @SerializedName("review")
    public Review review;

    @SerializedName("pet")
    public Pet pet;

}
