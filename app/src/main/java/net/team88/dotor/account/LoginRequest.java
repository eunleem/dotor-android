package net.team88.dotor.account;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Eun Leem on 2/17/2016.
 */
public class LoginRequest {
    public LoginRequest(Account account) {
        username = account.getUsername();
        password = account.getPassword();
    }
    @SerializedName("username")
    String username;

    @SerializedName("password")
    String password;
}
