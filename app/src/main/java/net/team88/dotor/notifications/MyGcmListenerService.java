package net.team88.dotor.notifications;

import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;


import net.team88.dotor.R;
import net.team88.dotor.reviews.ReviewViewActivity;
import net.team88.dotor.shared.BasicResponse;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.Server;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Eun Leem on 2/25/2016.
 */
public class MyGcmListenerService extends GcmListenerService {
    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        // TODO Make it richer Push Notification
        String notificationId = data.getString("notification_id", "");
        String message = data.getString("message");
        String relatedType = data.getString("related_type", "");
        String relatedId = data.getString("related_id", "");
        Log.d(TAG, "From: " + from);

        Log.d(TAG, "PushId: " + notificationId);
        Log.d(TAG, "RelatedType: " + relatedType);
        Log.d(TAG, "RelatedId: " + relatedId);
        Log.d(TAG, "Message: " + message);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */
        DotorWebService service = Server.getInstance(this).getService();
        Call<BasicResponse> call = service.receivedNotfication(notificationId);
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccess() == false) {
                    Log.d(TAG, "receivedNotification: failed.");
                    return;
                }

                if (response.body().status < 0) {
                    Log.d(TAG, "receivedNotification: failed. msg: " + response.body().message);
                    return;
                }

                Log.d(TAG, "receviedNotification GOOD. ");
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.d(TAG, "receivedNotification: failed.");
            }
        });

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(notificationId, relatedType, relatedId, message);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String notificationid, String relatedType, String relatedId, String message) {
        Intent intent = new Intent();
        if (relatedType.equalsIgnoreCase("review")) {
            intent.setClass(this, ReviewViewActivity.class);
            intent.putExtra("notificationid", notificationid);
            intent.putExtra("reviewid", relatedId);

        } else {
            intent.setClass(this, NotificationListActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_logo_white_24dp)
                .setContentTitle(this.getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setGroup(relatedType + relatedId)
                .setSound(defaultSoundUri)
                .setGroupSummary(true)
                .setContentIntent(pendingIntent);

        //.setNumber(2) // TODO Grouped Notification
        // notificationBuilder.setDeleteIntent(); // TODO On Swipe Dismiss

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);
                        //(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(10 /* ID of notification */,
                notificationBuilder.build());
    }
}
