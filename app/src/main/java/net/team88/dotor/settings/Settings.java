package net.team88.dotor.settings;

import android.content.Context;
import android.content.SharedPreferences;

import net.team88.dotor.notifications.PushSetting;


public class Settings {
    private final String TAG = "SETTINGS";

    private static Settings sInstance;
    private static Context context;

    private SharedPreferences storage;


    PushSetting pushSetting;

    private Settings(Context context) {
        Settings.context = context;
        storage = context.getApplicationContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);

        pushSetting = new PushSetting();

        loadPushSetting();
    }

    public static synchronized Settings getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Settings(context);
        }
        return sInstance;
    }

    public void reset() {
        storage.edit().clear().apply();
        sInstance = null;
    }

    public PushSetting getPushSetting() {
        return pushSetting;
    }

    public void setPushSetting(PushSetting pushSetting) {
        this.pushSetting = pushSetting;
        savePushSetting(pushSetting);
    }

    private void savePushSetting(PushSetting pushSetting) {
        storage.edit()
                .putBoolean("is_push_on", pushSetting.isPushOn)
                .putBoolean("get_likes", pushSetting.getLikes)
                .putBoolean("get_comments", pushSetting.getComments)
                .apply();
    }

    private void loadPushSetting() {
        pushSetting.isPushOn = storage.getBoolean("is_push_on", true);
        pushSetting.getLikes = storage.getBoolean("get_likes", true);
        pushSetting.getComments = storage.getBoolean("get_comments", true);
    }



}
