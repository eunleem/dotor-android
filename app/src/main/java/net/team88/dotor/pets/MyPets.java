package net.team88.dotor.pets;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.team88.dotor.utils.GsonUtils;

import org.bson.types.ObjectId;

import java.util.LinkedHashMap;

public class MyPets {
    private static final String TAG = "MyPets";

    private static MyPets sInstance;

    private Context context;

    private SharedPreferences storage;

    private Gson gson;

    private LinkedHashMap<String, Pet> myPetsByName;


    private MyPets(Context context) {
        this.context = context;
        this.storage = context.getApplicationContext().getSharedPreferences("MyPets", Context.MODE_PRIVATE);
        this.myPetsByName = new LinkedHashMap<>();

        this.gson = GsonUtils.getGson();

        final int numPets = this.load();
        Log.i(TAG, String.valueOf(numPets) + " pets loaded!");
    }


    public static synchronized MyPets getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MyPets(context.getApplicationContext());
        }
        return sInstance;
    }

    public final LinkedHashMap<String, Pet> getPets() {
        return myPetsByName;
    }

    public int size() {
        return myPetsByName.size();
    }

    public boolean insert(Pet pet) {
        String petName = pet.name;
        this.myPetsByName.put(petName, pet);
        this.save();
        return true;
    }


    public void update(String petName, Pet modified) {
        modified.id = this.myPetsByName.get(petName).id;

        this.myPetsByName.remove(petName);
        this.myPetsByName.put(modified.name, modified);

        this.save();
    }

    /**
     * Delete the pet by name. NOT YET IMPLEMENTED!
     * @param petName
     */
    public void delete(String petName) {
        // #TODO Implement delete pet function
    }

    public void reset() {
        this.storage.edit().clear().apply();
        sInstance = null;
    }

    public Pet getPet(final String name) {
        return this.myPetsByName.get(name);
    }

    public Pet getPet(final ObjectId id) {
        for (Pet pet : myPetsByName.values()) {
            if (pet.id.equals(id)) {
                return pet;
            }
        }
        return null;
    }

    private void save() {
        String json = gson.toJson(myPetsByName);
        storage.edit().putString("pets", json).apply();

        //Log.i(TAG, String.valueOf(numPets) + " pet(s) saved.");
    }

    public int load() {
        myPetsByName.clear();

        final String petsStr = storage.getString("pets", "");

        if (petsStr.isEmpty() == false) {
            myPetsByName = gson.fromJson(petsStr, new TypeToken<LinkedHashMap<String, Pet>>() {}.getType());
        }

        return myPetsByName.size();
    }

}

