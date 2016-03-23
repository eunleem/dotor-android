package net.team88.dotor.notifications;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;


import net.team88.dotor.R;
import net.team88.dotor.account.MyAccount;
import net.team88.dotor.shared.BasicResponse;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.Server;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by Eun Leem on 2/25/2016.
 */
public class GcmRegistrationService extends IntentService {
    private static final String TAG = "GcmRegistrationService";

    private static final String[] TOPICS = {"global"};

    public GcmRegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            // [START register_for_gcm]
            // Initially this call goes out to the network to retrieve the token, subsequent calls
            // are local.
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "GCM Registration Token: " + token);

            // TODO: Implement this method to send any registration to your app's servers.
            sendRegistrationToServer(token);

            // Subscribe to topic channels
            subscribeTopics(token);

            // You should store a boolean that indicates whether the generated token has been
            // sent to your server. If the boolean is false, send the token to your server,
            // otherwise your server should have already received the token.
            MyAccount.getInstance(this).setIsGcmTokenSet(true);
            // [END register_for_gcm]
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            MyAccount.getInstance(this).setIsGcmTokenSet(false);
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        //Intent registrationComplete = new Intent("GCM_REGISTRAION_COMPLETE");
        //LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Persist registration to third-party servers.
     * <p/>
     * Modify this method to associate the user's GCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        PushSetting setting = new PushSetting();
        setting.token = token;
        setting.isPushOn = true;
        setting.getComments = true;
        setting.getLikes = true;

        DotorWebService webServiceApi = Server.getInstance(this).getService();
        Call<BasicResponse> call = webServiceApi.upsertPushSetting(setting);
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccess() == false) {
                    Log.d(TAG, "InsertPushSetting failed.");
                    return;
                }

                BasicResponse body = response.body();

                if (body.status < 0) {
                    Log.d(TAG, "InsertPushSetting failed. message: " + body.message);
                    return;
                }
                Log.d(TAG, "InsertPushSetting: Success " + body.message);
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.d(TAG, "InsertPushSetting error: " + t.getMessage());
            }
        });
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]
}
