package net.team88.dotor.reviews;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class Reviews {
    private final String TAG = "Reviews";

    private static Reviews instance;
    private Context context;

    private ArrayList<Review> myReviews;

    private SharedPreferences data;

    private Reviews(Context context) {
        this.context = context;
        this.data = context.getSharedPreferences("Reviews", Context.MODE_PRIVATE);
        this.myReviews = new ArrayList<>();
    }

    public static synchronized Reviews getInstance(Context context) {
        if (instance == null) {
            instance = new Reviews(context);
        }
        return instance;
    }

    public boolean insert(Review item) {
        if (item == null) {
            return false;
        }
        this.myReviews.add(item);
        return true;
    }

    public ArrayList<Review> getReviewsByRegion(String region) {
        // #TODO Implement this
        return myReviews;
    }

    public void reset() {
        this.data.edit().clear().commit();
    }

}
