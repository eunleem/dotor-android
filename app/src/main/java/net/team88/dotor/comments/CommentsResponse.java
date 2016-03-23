package net.team88.dotor.comments;

import com.google.gson.annotations.SerializedName;


import net.team88.dotor.shared.BasicResponse;

import java.util.ArrayList;

public class CommentsResponse extends BasicResponse {
    @SerializedName("comments")
    public ArrayList<Comment> comments;
}
