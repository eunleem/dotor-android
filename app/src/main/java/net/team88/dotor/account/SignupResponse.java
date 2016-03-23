package net.team88.dotor.account;

import com.google.gson.annotations.SerializedName;

import net.team88.dotor.shared.BasicResponse;

public class SignupResponse extends BasicResponse {
    @SerializedName("username")
    public String username;

    @SerializedName("password")
    public String password;
}
