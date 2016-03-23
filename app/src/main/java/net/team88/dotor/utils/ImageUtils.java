package net.team88.dotor.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;


import net.team88.dotor.shared.UserScreen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();

        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public static void savePng(Bitmap bitmap, String destFilepath) {
        if (!destFilepath.endsWith("png")) {
            Log.e("IMAGE", "must end with png");
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(destFilepath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveJpg(Bitmap bitmap, String destFilepath, int quality) {
        if (!destFilepath.endsWith("jpg")) {
            Log.e("IMAGE", "must end with png");
        }

        FileOutputStream out = null;
        try {
            Log.i(TAG, "saveJpg: Saving Jpg");
            Log.i(TAG, "saveJpg: path: " + destFilepath);
            out = new FileOutputStream(destFilepath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            try {
                if (out != null) {
                    Log.i(TAG, "saveJpg: Saved Jpg.");
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static BitmapFactory.Options getBitmapOptions(String path) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);

        return bmOptions;
    }

    public static Bitmap getFitScreenImage(Context context, String path) {
        UserScreen userScreen = UserScreen.getInstance(context);
        int screenWidthDp = userScreen.getScreenWidthDp();
        int screenHeightDp = userScreen.getScreenHeightDp();
        int maxSize = Math.min(screenWidthDp, screenHeightDp);
        return ImageUtils.scale(path, maxSize, maxSize);
    }

    public static int getScaleFactor(String filePath, int maxWidthPx, int maxHeightPx) {
        BitmapFactory.Options bmOptions = getBitmapOptions(filePath);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        return Math.min(photoW / maxWidthPx, photoH / maxHeightPx);
    }

    public static Bitmap scale(String filePath, int maxWidth, int maxHeight) {
        int orientation = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filePath);

            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
        } catch (IOException e) {
            e.printStackTrace();
            orientation = 0;
        }


        BitmapFactory.Options bmOptions = getBitmapOptions(filePath);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / maxWidth, photoH / maxHeight);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmOptions);
        return rotate(bitmap, orientation);
    }

    public static Bitmap rotate(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File createImageFile(File storageDir) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String imageFileName = "IMG" + timeStamp;
        Log.i("PATH1", imageFileName);
        Log.i("PATH2", storageDir.getAbsolutePath());

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        Log.i("FILE", image.getAbsolutePath());
        Log.i("FILE", "canWrite: " + String.valueOf(image.canWrite()));
        Log.i("FILE", "canRead: " + String.valueOf(image.canRead()));

        return image;
    }

    public static Bitmap cropCircle(Bitmap bmp) {
        Bitmap output = Bitmap.createBitmap(bmp.getWidth(),
                bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(bmp.getWidth() / 2 + 0.7f, bmp.getHeight() / 2 + 0.7f,
                bmp.getWidth() / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bmp, rect, rect, paint);

        return output;
    }

    public static int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath) {
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);

            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            return orientation;

//        switch (orientation) {
//        case ExifInterface.ORIENTATION_ROTATE_270:
//
//            break;
//        case ExifInterface.ORIENTATION_ROTATE_180:
//            break;
//        case ExifInterface.ORIENTATION_ROTATE_90:
//            break;
//        }
//
//        Log.i("RotateImage", "Exif orientation: " + orientation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Bitmap cropCircle(Context context, Bitmap input, int pixels, int w, int h, boolean squareTL, boolean squareTR, boolean squareBL, boolean squareBR) {
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, w, h);
        final RectF rectF = new RectF(rect);

        //make sure that our rounded corner is scaled appropriately
        final float roundPx = pixels * densityMultiplier;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        //draw rectangles over the corners we want to be square
        if (squareTL) {
            canvas.drawRect(0, h / 2, w / 2, h, paint);
        }
        if (squareTR) {
            canvas.drawRect(w / 2, h / 2, w, h, paint);
        }
        if (squareBL) {
            canvas.drawRect(0, 0, w / 2, h / 2, paint);
        }
        if (squareBR) {
            canvas.drawRect(w / 2, 0, w, h / 2, paint);
        }

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(input, 0, 0, paint);

        return output;
    }
}
