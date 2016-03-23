package net.team88.dotor.account;

import com.google.gson.annotations.SerializedName;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Eun Leem on 3/21/2016.
 */
public class Account {

    @SerializedName("username")
    String username;

    @SerializedName("password")
    String password;

    @SerializedName("email")
    String email;

    @SerializedName("nickname")
    String nickname;

    @SerializedName("imageid")
    ObjectId imageId;

    @SerializedName("my_location")
    ArrayList<Double> coordinates;

    @SerializedName("hospitalid")
    ObjectId hospitalId;

    @SerializedName("isemailverified")
    boolean isEmailVerified;

    @SerializedName("lastlogin")
    Date lastLogin;


    public Account() {
        this.isEmailVerified = false;
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.setIsEmailVerified(false);
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public ObjectId getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(ObjectId hospitalId) {
        this.hospitalId = hospitalId;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void setIsEmailVerified(boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
}
