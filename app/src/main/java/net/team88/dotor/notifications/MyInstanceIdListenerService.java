package net.team88.dotor.notifications;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by Eun Leem on 2/25/2016.
 */
public class MyInstanceIdListenerService extends InstanceIDListenerService {
    private static final String TAG = "MyInstanceIdls";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        Intent intent = new Intent(this, GcmRegistrationService.class);
        startService(intent);
    }
    // [END refresh_token]

}
