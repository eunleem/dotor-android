package net.team88.dotor.shared.image;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import net.team88.dotor.R;
import net.team88.dotor.shared.UserScreen;
import net.team88.dotor.utils.ImageUtils;

import java.io.IOException;

import me.littlecheesecake.croplayout.EditPhotoView;
import me.littlecheesecake.croplayout.EditableImage;
import me.littlecheesecake.croplayout.handler.OnBoxChangedListener;

public class ImageEditActivity extends AppCompatActivity {

    private static final String TAG = "EditImage";

    public static final String KEY_IMAGE_PATH = "ImagePath";
    public static final String KEY_PROCESSED_IMAGE_PATH = "ProcessedImagePath";
    public static final String KEY_TAILORED_IMAGE_PATH = "Tailored";

    private String orgImagePath;

    private ImageButton buttonRotate;
    private Button buttonCrop;
    private ProgressBar progressBar;

    private EditPhotoView imageView;
    private EditableImage editableImage;
    private String tailoredImagePath;

    private class ImageEditInfo {
        public int rotation = 0;
        public int[] crop = new int[4];
        public double scaleFactor = 1;
    }

    ImageEditInfo editInfo = new ImageEditInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        setupBasicElements();


        orgImagePath = getIntent().getStringExtra(KEY_IMAGE_PATH);
        if (orgImagePath == null || orgImagePath.isEmpty()) {
            Log.e(TAG, "Must supply Image Path for ImageEditActivity.");
            setResult(RESULT_FIRST_USER);
            finish();
            return;
        }

        registerElements();
        registerEvents();

        String tailoredImagePath = createTailoredImage(orgImagePath);

        editInfo.scaleFactor = getScaleRatio(orgImagePath, tailoredImagePath);

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(orgImagePath, bmOptions);
        editableImage = new EditableImage(tailoredImagePath);
        editInfo.crop[0] = 0;
        editInfo.crop[1] = 0;
        editInfo.crop[2] = bmOptions.outWidth;
        editInfo.crop[3] = bmOptions.outHeight;

        imageView.initView(this, editableImage);

        imageView.setOnBoxChangedListener(new OnBoxChangedListener() {
            @Override
            public void onChanged(int x1, int y1, int x2, int y2) {
                editInfo.crop[0] = (int) (x1 * editInfo.scaleFactor);
                editInfo.crop[1] = (int) (y1 * editInfo.scaleFactor);
                editInfo.crop[2] = (int) (x2 * editInfo.scaleFactor);
                editInfo.crop[3] = (int) (y2 * editInfo.scaleFactor);
                //Log.i(TAG, String.format("crop: (%d, %d) (%d, %d)", x1, y1, x2, y2));
                Log.i(TAG, String.format("crop: (%d, %d) (%d, %d)",
                        editInfo.crop[0],
                        editInfo.crop[1],
                        editInfo.crop[2],
                        editInfo.crop[3]));

            }
        });

    }

    /**
     * Creates a resized image that fits screen from an image file.
     * This helps a lot with memory usage.
     *
     * @param orgImagePath Original Image file
     * @return Resized Image file path under App's cache directory.
     */
    @NonNull
    private String createTailoredImage(String orgImagePath) {
        UserScreen userScreen = UserScreen.getInstance(this);
        int screenWidthDp = userScreen.getScreenWidthDp();
        int screenHeightDp = userScreen.getScreenHeightDp();
        int maxSize = Math.min(screenWidthDp, screenHeightDp);
        Bitmap scaledBitmap = ImageUtils.scale(orgImagePath, maxSize, maxSize);

        String dirPath = this.getCacheDir().getAbsolutePath();
        tailoredImagePath = dirPath + "/temp_thumb.jpg";
        ImageUtils.saveJpg(scaledBitmap, tailoredImagePath, 70);
        return tailoredImagePath;
    }

    private double getScaleRatio(String orgFilePath, String thumbFilePath) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(orgFilePath, bmOptions);
        int orgWidth = bmOptions.outWidth;
        BitmapFactory.decodeFile(thumbFilePath, bmOptions);
        int thumbWidth = bmOptions.outWidth;

        Log.i(TAG, "Original Image Width: " + String.valueOf(orgWidth));
        Log.i(TAG, "Thumbnail Image Width: " + String.valueOf(thumbWidth));

        return (double) orgWidth / (double) thumbWidth;
    }

    private void registerElements() {
        imageView = (EditPhotoView) findViewById(R.id.imageEditable);
        buttonRotate = (ImageButton) findViewById(R.id.buttonRotate);
        buttonCrop = (Button) findViewById(R.id.buttonCrop);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

    }

    private void registerEvents() {
        buttonRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.rotateImageView();
                editInfo.rotation += 90;
                if (editInfo.rotation == 360) {
                    editInfo.rotation = 0;
                }
            }
        });

        buttonCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private void setupBasicElements() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ok, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // #TODO Confirm Cancel
                setResult(RESULT_CANCELED);
                finish();
                break;

            case R.id.action_ok:
                saveEditedImageAndFinish();
                break;

            default:
                break;
        }

        return true;
    }

    private void saveEditedImageAndFinish() {
        progressBar.setVisibility(View.VISIBLE);

        final String outputDirPath = this.getCacheDir().getAbsolutePath();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                int orientation = 0;
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(orgImagePath);

                    exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);
                } catch (IOException e) {
                    //e.printStackTrace();
                    Log.e(TAG, "saveEditedImageAndFinish: Could not get Exif Orientation Information.", e);
                    orientation = 0;
                }

                // #REF: http://developer.android.com/training/displaying-bitmaps/manage-memory.html

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inPurgeable = true;

                Bitmap image = BitmapFactory.decodeFile(orgImagePath, bmOptions);

                switch (editInfo.rotation) {
                    case 90:
                        orientation = ExifInterface.ORIENTATION_ROTATE_90;
                        break;
                    case 180:
                        orientation = ExifInterface.ORIENTATION_ROTATE_180;
                        break;
                    case 270:
                        orientation = ExifInterface.ORIENTATION_ROTATE_270;
                        break;
                    default:
                        break;
                }

                Bitmap rotated = ImageUtils.rotate(image, orientation);

                int x = editInfo.crop[0];
                int y = editInfo.crop[1];
                int width = editInfo.crop[2] - x;
                int height = editInfo.crop[3] - y;

                Bitmap cropped = Bitmap.createBitmap(rotated, x, y, width, height);


                final String outputFilePath = outputDirPath + "/tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";

                ImageUtils.saveJpg(cropped, outputFilePath, 80);

                Log.d(TAG, "saveEditedImageAndFinish: Saved Image at " + outputFilePath);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        Intent intent = new Intent();
                        intent.putExtra(KEY_PROCESSED_IMAGE_PATH, outputFilePath);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }
        });

    }
}
