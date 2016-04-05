package net.team88.dotor.pets;

import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import net.team88.dotor.R;
import net.team88.dotor.utils.Utils;

import org.bson.types.ObjectId;

import java.util.Date;


/**
 * Implements Comparable for Sorting
 * #TODO
 * Convert public members to private? Maybe..
 */
public class Pet implements Comparable<Pet> {
    private final transient String TAG = "Pet";

    @Override
    public int compareTo(Pet another) {
        return (this.name.compareTo(another.name));
    }

    public static class Type {
        public static int DOG = 0;
        public static int CAT = 1;
    }

    public static class Gender {
        public static int MALE = 0;
        public static int FEMALE = 1;
    }

    public static class Size {
        public static int SMALL = 0;
        public static int MEDIUM = 1;
        public static int LARGE = 2;
        public static int XLARGE = 3;
        public static int XXLARGE = 4;
    }

    @SerializedName("petid")
    public ObjectId id;

    @SerializedName("name")
    public String name;

    @SerializedName("type")
    public int type;

    @SerializedName("gender")
    public int gender;

    @SerializedName("birthday")
    private Date birthday;

    @SerializedName("size")
    public int size;

    @SerializedName("profile_imageid")
    public ObjectId imageid;

    public String imageFileName;

    public boolean setId(ObjectId id) {
        if (ObjectId.isValid(id.toHexString()) == false) {
            Log.e(TAG, "ObjectId is invalid.");
            return false;
        }
        this.id = id;
        return true;
    }

    public boolean setName(String name) {
        this.name = name;
        return true;
    }

    public boolean setType(int type) {
        this.type = type;
        return true;
    }

    public boolean setGender(int gender) {
        this.gender = gender;
        return true;
    }

    public boolean setBirthday(Date birthday) {
        this.birthday = birthday;
        return true;
    }


    public boolean setSize(int size) {
        this.size = size;
        return true;
    }

    public ObjectId getId() {
        if (this.id == null) {
            Log.e(TAG, "Getting null Id.");
        }
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getType() {
        return this.type;
    }

    public String getTypeString(Context context) {
        int resid = 0;
        if (this.type == Type.DOG) {
            resid = R.string.puppy;
        } else if (this.type == Type.CAT) {
            resid = R.string.kitten;
        }
        return context.getString(resid);
    }

    public int getGender() {
        return this.gender;
    }

    public String getGenderString(Context context) {
        int resid = 0;
        if (this.gender == Gender.FEMALE) {
            resid = R.string.female;
        } else if (this.gender == Gender.MALE) {
            resid = R.string.male;
        }
        return context.getString(resid);
    }

    public Date getBirthday() {
        return this.birthday;
    }

    public int getAge() {
        return Utils.getAge(this.birthday);
    }

    public int getSize() {
        return this.size;
    }

    public String getSizeString(Context context) {
        int resid = 0;
        if (this.size == Size.SMALL) {
            resid = R.string.pet_size_small;
        } else if (this.size == Size.MEDIUM) {
            resid = R.string.pet_size_medium;
        } else if (this.size == Size.LARGE) {
            resid = R.string.pet_size_large;
        } else if (this.size == Size.XLARGE) {
            resid = R.string.pet_size_xlarge;
        }
        return context.getString(resid);
    }
}

