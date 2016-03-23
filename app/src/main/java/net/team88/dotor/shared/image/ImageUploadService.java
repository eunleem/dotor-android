package net.team88.dotor.shared.image;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.team88.dotor.MainActivity;
import net.team88.dotor.R;
import net.team88.dotor.reviews.ReviewViewActivity;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.Server;

import org.bson.types.ObjectId;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageUploadService extends IntentService {

    static final String TAG = "ImageUploadService";

    public static final String KEY_IMAGE_FILE_PATH = "IMAGE_FILE_PATH";

    private static final String KEY_CATEGORY = "CATEGORY";


    public ImageUploadService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent called. Sync starting!");

        String filepath = intent.getStringExtra("image_filepath");
        if (filepath == null || filepath.isEmpty()) {
            // Error
            Log.e(TAG, "FilePath is null");
            return;
        }
        Log.i(TAG, "Received filePath: " + filepath);

        final String categoryStr = intent.getStringExtra("category");
        if (categoryStr == null || categoryStr.isEmpty()) {
            Log.e(TAG, "category is not received.");
            return;
        }
        Log.d(TAG, "category: " + categoryStr);

        final String relatedIdStr = intent.getStringExtra("relatedid");
        if (relatedIdStr == null || relatedIdStr.isEmpty() || !ObjectId.isValid(relatedIdStr)) {
            Log.e(TAG, "reviewId invalid");
            return;
        }


        File file = new File(filepath);
        RequestBody image = RequestBody.create(MediaType.parse("image/*"), file);
        RequestBody category = RequestBody.create(MediaType.parse("multipart/form-data"), categoryStr);
        RequestBody relatedId = RequestBody.create(MediaType.parse("multipart/form-data"), relatedIdStr);


        final Context context = this.getApplicationContext();

        DotorWebService webServiceApi = Server.getInstance(context).getService();
        Call<ImageInsertResponse> call = webServiceApi.uploadImage(image, category, relatedId);
        call.enqueue(new Callback<ImageInsertResponse>() {
            @Override
            public void onResponse(Call<ImageInsertResponse> call, Response<ImageInsertResponse> response) {
                if (response.isSuccess() == false) {
                    // Server Level Error
                    Log.e(TAG, "Upload failed.");
                    return;
                }

                ImageInsertResponse json = response.body();
                if (json.status < 0) {
                    // Server Application Level Error
                    Log.e(TAG, "Upload Failed. message: " + json.message);
                    return;
                }

                //Reviews.getInstance(context).get
                // TODO Update Local Review data in Reviews. (Mark it isDraft = false)


                String message = context.getString(R.string.app_name);
                Intent intent = new Intent();

                if (categoryStr.equalsIgnoreCase("review")) {
                    intent.setClass(context, ReviewViewActivity.class);
                    intent.putExtra("reviewid", relatedIdStr);
                    message = context.getString(R.string.post_review_finished_text);
                }

                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_logo_white_24dp)
                        .setContentTitle(context.getString(R.string.post_review_finished))
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);


                NotificationManager man = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                man.notify(0, notificationBuilder.build());
            }

            @Override
            public void onFailure(Call<ImageInsertResponse> call, Throwable t) {
                Intent intent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);

                Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_logo_white_24dp)
                        .setContentTitle(context.getString(R.string.post_review_failed_title))
                        .setContentText(context.getString(R.string.post_review_failed_text))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);


                NotificationManager man = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                man.notify(0, notificationBuilder.build());
            }
        });


        Notification n = new Notification.Builder(context)
                .setSmallIcon(android.R.drawable.stat_sys_upload)
                .setContentTitle(context.getString(R.string.uploading_image_title))
                .setContentText(context.getString(R.string.uploading_image_text))
                .setAutoCancel(false)
                .setOngoing(true)
                .build();

        //notificationBuilder.setProgress(100, 3) // TODO Display Upload Progress.

        NotificationManager man = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        man.notify(0, n);
    }
}
