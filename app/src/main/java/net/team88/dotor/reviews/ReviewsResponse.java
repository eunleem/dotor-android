package net.team88.dotor.reviews;

import com.google.gson.annotations.SerializedName;


import net.team88.dotor.shared.BasicResponse;

import java.util.ArrayList;

public class ReviewsResponse extends BasicResponse {

    @SerializedName("reviews")
    public ArrayList<Review> reviews;
}
