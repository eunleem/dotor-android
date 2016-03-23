package net.team88.dotor.shared;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by Eun Leem on 2/25/2016.
 */
public class UserScreen {
    private static final String TAG = "UserScreen";

    private static UserScreen sInstance;

    private Context context;
    DisplayMetrics displayMetrics;

    float displayWidthDp;
    float displayHeightDp;

    private UserScreen(Context context) {
        this.context = context;
        calcScreenSize(context);

    }

    public void calcScreenSize(Context context) {
        if (context == null) {
            displayMetrics = this.context.getResources().getDisplayMetrics();
            displayHeightDp = displayMetrics.heightPixels / displayMetrics.density;
            displayWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        } else {
            displayMetrics = context.getResources().getDisplayMetrics();
            displayHeightDp = displayMetrics.heightPixels / displayMetrics.density;
            displayWidthDp = displayMetrics.widthPixels / displayMetrics.density;
        }
    }

    public static synchronized UserScreen getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new UserScreen(context);
        }
        return sInstance;
    }

    public int getDpToPxRatio() {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return Math.round(metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public int getScreenWidthDp() {
        return Math.round(displayWidthDp);
    }

    public int getScreenHeightDp() {
        return Math.round(displayHeightDp);
    }
}
