package net.team88.dotor.shared;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Eun Leem on 2/19/2016.
 */
public class PhotoGetter {
    private static final String TAG = "PhotoGetter";

    public static File dispatchTakePictureIntent(Activity activity, final int requestId) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e(TAG, "No Save Picture");

            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
            }
            activity.startActivityForResult(takePictureIntent, requestId);
            return photoFile;
        } else {
            // #TODO Handle no camera situation.
            Log.w(TAG, "dispatchTakePictureIntent: No Camera Activity.");

        }
        return null;
    }

    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //File storageDir = getFilesDir();
        Log.i(TAG, "storageDir:" + storageDir.getAbsolutePath());
        File image = new File(storageDir, imageFileName + ".jpg");
        Log.i(TAG, "imagePath: " + image.getAbsolutePath());

        // #TODO Create a temp file fails.

        // Save a file: path for use with ACTION_VIEW intents
        //imageFilepath = image.getAbsolutePath();
        return image;
    }
}
