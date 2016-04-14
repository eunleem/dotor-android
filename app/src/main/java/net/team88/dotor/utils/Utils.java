package net.team88.dotor.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import net.team88.dotor.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Eun Leem on 3/21/2016.
 */
public class Utils {

    private static final String TAG = "Utils";

    public static boolean isPlayServicesAvailable(final Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.i(TAG, "This device is not supported.");
            return false;
        }
        return true;
    }

    public static boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static int convertDpToPx(Context context, float dp) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics());
    }

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        }

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static int getAge(Calendar past) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        return currentYear - past.get(Calendar.YEAR);
    }

    public static int getAge(Date d) {
        Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        c.setTimeInMillis(d.getTime());
        return currentYear - c.get(Calendar.YEAR);
    }

    public static void changeIconColor(TextView view, int color) {
        Drawable[] drawables = view.getCompoundDrawablesRelative();
        for (Drawable drawable : drawables) {
            if (drawable != null) {
                changeIconColor(drawable, color);
            }
        }
    }

    public static void changeIconColor(Drawable drawable, int color) {
        if (drawable == null) {
            Log.e(TAG, "changeIconColor: Drawable is null. skipping...");
            return;
        }

        drawable.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }
}
