package net.team88.dotor.reviews;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import net.team88.dotor.R;
import net.team88.dotor.account.MyAccount;
import net.team88.dotor.comments.Comment;
import net.team88.dotor.comments.CommentEvent;
import net.team88.dotor.comments.CommentMenuDialogFragment;
import net.team88.dotor.comments.CommentsListViewAdapter;
import net.team88.dotor.comments.CommentsResponse;
import net.team88.dotor.pets.Pet;
import net.team88.dotor.shared.BasicResponse;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.Server;
import net.team88.dotor.shared.image.ImageViewActivity;
import net.team88.dotor.utils.ImageUtils;
import net.team88.dotor.utils.Utils;

import org.bson.types.ObjectId;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewViewActivity extends AppCompatActivity
    implements CommentEvent {

    private static final String TAG = "ReviewView";

    ObjectId reviewId;

    private FloatingActionButton fab;

    private ScrollView scrollViewRoot;
    private View layoutReviewImage;

    private ImageView imageViewReviewImage;

    private TextView textHospitalName;
    private TextView textHospitalAddress;
    private ImageView imageViewPetThumbnail;

    private TextView textPetName;
    private TextView textPetTypeGender;
    private TextView textPetAgeSize;

    private TextView textReviewCategory;
    private TextView textReviewCost;
    private TextView textReviewBody;

    private TextView textLikes;
    private TextView textComments;

    Review reviewCached;
    Pet petCached;
    private EditText textCommentBody;
    private ImageButton buttonPostComment;
    private ListView listViewComments;
    private ProgressDialog progressDialog;
    private CommentMenuDialogFragment commentMenuDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_view);

        if (!Fabric.isInitialized()) {
            Fabric.with(this, new Crashlytics());
        }

        setupBasicElements();
        registerElements();
        registerEvents();

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setMessage(getString(R.string.msg_progress_loading));
            progressDialog.show();
        }


        Intent intent = getIntent();
        String reviewIdStr = intent.getStringExtra("reviewid"); // TODO replace hard-coded string with Const String
        if (reviewIdStr == null || reviewIdStr.isEmpty()) {
            Log.e(TAG, "No review id.");
        } else {
            this.reviewId = new ObjectId(reviewIdStr);
        }

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Review")
                .putContentType("Review")
                .putContentId(reviewIdStr));

        String notificationIdStr = intent.getStringExtra("notificationid"); // TODO replace hard-coded value
        if (notificationIdStr == null || notificationIdStr.isEmpty()) {
            Log.d(TAG, "No notification id which is all good.");

        } else {
            DotorWebService service = Server.getInstance(this).getService();
            Call<BasicResponse> call = service.readNotfication(notificationIdStr);
            call.enqueue(new Callback<BasicResponse>() {
                @Override
                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                    if (response.isSuccessful() && response.body().status < 0) {
                        Log.d(TAG, "ReadNotification good");
                    } else {
                        Log.d(TAG, "ReadNotification failed");
                    }
                }

                @Override
                public void onFailure(Call<BasicResponse> call, Throwable t) {
                    Log.d(TAG, "ReadNotification failed");
                }
            });
        }

        CommentMenuDialogFragment.event = this;

        commentMenuDialogFragment = new CommentMenuDialogFragment();

        CommentsListViewAdapter adapter = new CommentsListViewAdapter(
                getBaseContext(),
                commentMenuDialogFragment,
                new ArrayList<Comment>());

        listViewComments.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        fetchReviewDetail();
    }

    private void setupBasicElements() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        //ab.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
    }

    private void registerElements() {
        fab = (FloatingActionButton) findViewById(R.id.fab);

        scrollViewRoot = (ScrollView) findViewById(R.id.scrollViewRoot);

        layoutReviewImage = (View) findViewById(R.id.layoutReviewImage);

        imageViewReviewImage = (ImageView) findViewById(R.id.imageViewReviewImage);
        imageViewPetThumbnail = (ImageView) findViewById(R.id.imageViewPetThumbnail);

        textHospitalName = (TextView) findViewById(R.id.textHospitalName);
        textHospitalAddress = (TextView) findViewById(R.id.textHospitalAddress);

        textPetName = (TextView) findViewById(R.id.textPetName);
        textPetTypeGender = (TextView) findViewById(R.id.textPetTypeGender);
        textPetAgeSize = (TextView) findViewById(R.id.textPetAgeSize);


        textReviewCategory = (TextView) findViewById(R.id.textReviewCategory);
        textReviewCost = (TextView) findViewById(R.id.textReviewCost);

        textReviewBody = (TextView) findViewById(R.id.textReviewBody);

        textLikes = (TextView) findViewById(R.id.textLikes);
        textComments = (TextView) findViewById(R.id.textComments);


        textCommentBody = (EditText) findViewById(R.id.textCommentBody);
        buttonPostComment = (ImageButton) findViewById(R.id.buttonPostComment);
        listViewComments = (ListView) findViewById(R.id.listViewComments);
    }

    private void registerEvents() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                likeReview();
            }
        });

        textLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likeReview();
            }
        });

        textComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Support Category and replace reviewid to relatedid
                //Intent intent = new Intent(getBaseContext(), CommentsActivity.class);
                //intent.putExtra("reviewid", reviewId.toHexString());
                //startActivity(intent);

                scrollViewRoot.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollViewRoot.fullScroll(ScrollView.FOCUS_DOWN);
                        textCommentBody.requestFocus();
                    }
                });

                textCommentBody.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        InputMethodManager keyboard = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        keyboard.showSoftInput(textCommentBody, 0);
                    }
                }, 300);

            }
        });

        buttonPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertComment();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_review_detail, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == android.R.id.home) {
            finish();
            return true;
        }

        if (id == R.id.action_delete) {
            confirmDeleteReview();

        } else if (id == R.id.action_modify) {
            editReview();

        } else if (id == R.id.action_flag) {
            reportReview();
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteReview() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.title_delete_review)
                .setMessage(R.string.msg_delete_review)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteReview();
                    }

                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void editReview() {
        Snackbar.make(textReviewBody, R.string.modify_not_yet_supported, Snackbar.LENGTH_LONG)
                .show();
    }

    private void reportReview() {
        DotorWebService webServiceApi = Server.getInstance(this).getService();
        Call<BasicResponse> call = webServiceApi.insertReport("review", this.reviewId.toHexString());
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() == false) {
                    Log.e(TAG, "insertReport failed!");
                    Snackbar.make(textReviewBody, R.string.report_failed, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                BasicResponse body = response.body();
                if (body.status < 0) {
                    Log.e(TAG, "insertReport returned status non-zero! message: " + body.message);
                    Snackbar.make(textReviewBody, R.string.report_failed, Snackbar.LENGTH_LONG)
                            .show();
                    return;

                } else if (body.status == 1) {
                    // Already Reported
                    Snackbar.make(textReviewBody, R.string.already_reported, Snackbar.LENGTH_LONG)
                            .show();

                } else if (body.status == 0) {
                    Snackbar.make(textReviewBody, R.string.reported, Snackbar.LENGTH_LONG)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.d(TAG, "insertReport Failed. " + t.getMessage());
                Snackbar.make(textReviewBody, R.string.report_failed, Snackbar.LENGTH_LONG)
                        .show();
            }
        });
    }

    private void deleteReview() {
        DotorWebService webServiceApi = Server.getInstance(this).getService();
        Call<BasicResponse> call = webServiceApi.deleteReview(this.reviewId.toHexString());
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() == false) {
                    Log.e(TAG, "DeleteReview failed!");
                    Snackbar.make(textReviewBody, R.string.msg_error_delete_review, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                BasicResponse body = response.body();
                if (body.status == -1) {
                    Log.e(TAG, "DeleteReview returned status non-zero! message: " + body.message);
                    Snackbar.make(textReviewBody, R.string.msg_error_delete_review, Snackbar.LENGTH_LONG)
                            .show();
                    return;

                } else if (body.status == -2) {
                    // Not Yours
                    Snackbar.make(textReviewBody, R.string.msg_error_delete_review_notyours, Snackbar.LENGTH_LONG)
                            .show();

                } else if (body.status == 0) {
                    Toast.makeText(getApplicationContext(), R.string.deleted, Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.d(TAG, "Delete Review Failed. " + t.getMessage());
                Snackbar.make(textReviewBody, R.string.msg_error_delete_review, Snackbar.LENGTH_LONG)
                        .show();
            }
        });
    }

    private void fetchReviewDetail() {
        DotorWebService webServiceApi = Server.getInstance(this).getService();
        Call<ReviewResponse> call = webServiceApi.getReview(this.reviewId.toHexString());
        call.enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful() == false) {
                    Log.e(TAG, "getReviews failed!");
                    Toast.makeText(ReviewViewActivity.this, R.string.msg_error_review_view_failed,
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                ReviewResponse body = response.body();
                if (body.status < 0) {
                    Log.e(TAG, "getReviews returned status non-zero! message: " + body.message);
                    Toast.makeText(ReviewViewActivity.this, R.string.msg_error_review_view_failed,
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                progressDialog.hide();

                petCached = body.pet;
                reviewCached = body.review;

                final String imageBaseUrl = Server.getInstance(getBaseContext()).getServerUrl() + "/img/";


                if (reviewCached.images != null && reviewCached.images.size() > 0) {
                    // #LATER currently only support one picture.
                    layoutReviewImage.setVisibility(View.VISIBLE);
                    final String imageUrl = imageBaseUrl + reviewCached.images.get(0).toHexString() + ".jpg";
                    Glide.with(getApplicationContext())
                            .load(imageUrl)
                            .centerCrop()
                            .placeholder(R.drawable.ic_image_white_48dp)
                            .skipMemoryCache(true)
                            .into(imageViewReviewImage);
                    imageViewReviewImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ReviewViewActivity.this, ImageViewActivity.class);
                            intent.putExtra("image_url", imageUrl);
                            startActivity(intent);
                        }
                    });
                } else {
                    layoutReviewImage.setVisibility(View.GONE);
                }

                textPetName.setText(petCached.getName());
                textPetTypeGender.setText(petCached.getTypeString(getBaseContext()) + "   " + petCached.getGenderString(getBaseContext())); // TODO Add more info

                String ageStr = String.valueOf(reviewCached.petAge) + getApplicationContext().getString(R.string.age_unit);
                textPetAgeSize.setText(ageStr + "   " + petCached.getSizeString(getApplicationContext()));

                if (petCached.imageid != null) {
                    Log.i(TAG, "PetImageId Not Null");
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String imageUrl = imageBaseUrl + petCached.imageid.toHexString() + ".jpg";

                                final int size = (int) ImageUtils.convertDpToPixel(64.00f, getApplicationContext());
                                Bitmap bitmap = Glide.with(getApplicationContext())
                                        .load(imageUrl)
                                        .asBitmap()
                                        .centerCrop()
                                        .skipMemoryCache(true)
                                        .into(size, size)
                                        .get();

                                final Bitmap cropCircle = ImageUtils.cropCircle(getApplicationContext(),
                                        bitmap, size / 2, size, size, false, false, false, false);

                                imageViewPetThumbnail.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageViewPetThumbnail.setImageBitmap(cropCircle);
                                    }
                                });

                                imageViewPetThumbnail.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(ReviewViewActivity.this, ImageViewActivity.class);
                                        intent.putExtra("image_url", imageUrl);
                                        startActivity(intent);
                                    }
                                });

                            } catch (InterruptedException e) {
                                Log.e(TAG, e.toString());

                            } catch (ExecutionException e) {
                                Log.e(TAG, e.toString());
                            }

                        }
                    });
                } else {
                    imageViewPetThumbnail.setImageResource(R.drawable.ic_dotor_circle_48dp);
                }

                updateReviewInfo();
                updateLikes();
                updateComments();
                if (reviewCached.comments != null && reviewCached.comments.size() > 0) {
                    fetchComments(false);
                }

                // TODO If My Review, Add Options to edit or delete.
            }

            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                Log.e(TAG, "onFailure: Failed to load review. " + t.getMessage());
                Toast.makeText(ReviewViewActivity.this, R.string.msg_error_review_view_failed,
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void updateReviewInfo() {
        textHospitalName.setText(reviewCached.hospitalName);
        textHospitalAddress.setText(reviewCached.locationName);

        if (reviewCached.categories != null) {
            StringBuilder categories = new StringBuilder();
            for (String category : reviewCached.categories) {
                categories
                        .append(", ")
                        .append(category);
            }

            if (categories.length() > 2) {
                categories.delete(0, 2);
            }

            textReviewCategory.setText(categories.toString());
        }

        String costStr = NumberFormat.getInstance().format(reviewCached.cost);
        String costPostfix = getString(R.string.money_unit);
        textReviewCost.setText(costStr + costPostfix);

        textReviewBody.setText(reviewCached.reviewBody);
    }

    private void updateLikes() {
        int likes = 0;
        if (reviewCached.likes != null) {
            likes = reviewCached.likes.size();
        }
        textLikes.setText(String.valueOf(likes));

        int primaryColor = getResources().getColor(R.color.colorPrimary);
        Utils.changeIconColor(textLikes, primaryColor);
    }

    private void updateComments() {
        int comments = 0;
        if (reviewCached.comments != null) {
            comments = reviewCached.comments.size();
        }
        textComments.setText(String.valueOf(comments));

        int primaryColor = getResources().getColor(R.color.colorPrimary);
        Utils.changeIconColor(textComments, primaryColor);

    }

    private void likeReview() {
        //fab.setImageResource(R.drawable.w);
        //fab.setEnabled(false);
        DotorWebService webServiceApi = Server.getInstance(getBaseContext()).getService();
        Call<BasicResponse> call = webServiceApi.likeReview(reviewId.toHexString());

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                //fab.setEnabled(true);
                if (response.isSuccessful() == false) {
                    Snackbar.make(textPetName, R.string.review_like_failed, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                BasicResponse body = response.body();
                if (body == null) {
                    Snackbar.make(textPetName, R.string.review_like_failed, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (body.status < 0) {
                    Log.e(TAG, "likeReview returned status non-zero! message: " + body.message);
                    Snackbar.make(textPetName, R.string.review_like_failed, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (body.status == 1) {
                    Snackbar.make(textPetName, R.string.already_liked, Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                if (body.status == 2) {
                    Log.d(TAG, "Liked own review.");
                }

                Snackbar.make(textPetName, R.string.review_liked, Snackbar.LENGTH_SHORT)
                        .show();

                if (reviewCached.likes == null) {
                    reviewCached.likes = new ArrayList<ObjectId>();
                }
                reviewCached.likes.add(new ObjectId()); // FIXME Use MyAccount's userid instead.
                updateLikes();
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                //fab.setEnabled(true);
                Crashlytics.log(Log.ERROR, "ReviewLike", t.getMessage());
                Snackbar.make(textPetName, R.string.review_like_failed, Snackbar.LENGTH_LONG)
                        .show();

            }
        });
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    private void insertComment() {
        textCommentBody.setEnabled(false);
        String commentBody = textCommentBody.getText().toString().trim();
        if (commentBody.isEmpty()) {
            textCommentBody.setError(getString(R.string.comment_required));
            return;
        }

        Comment comment = new Comment();
        comment.commentBody = commentBody;
        comment.reviewId = this.reviewId;

        DotorWebService webServiceApi = Server.getInstance(getBaseContext()).getService();
        Call<BasicResponse> call = webServiceApi.insertComment("review", this.reviewId.toHexString(), comment);

        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                textCommentBody.setEnabled(true);
                if (response.isSuccessful() == false) {
                    Log.e(TAG, "CommentInsert failed!");
                    Snackbar.make(textCommentBody, R.string.comment_insert_failed, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                BasicResponse body = response.body();
                if (body.status < 0) {
                    Snackbar.make(textCommentBody, R.string.comment_insert_failed, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }
                Snackbar.make(textCommentBody, R.string.comment_inserted, Snackbar.LENGTH_LONG)
                        .show();

                textCommentBody.setText("");
                fetchComments(true);
                updateComments();
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                textCommentBody.setEnabled(true);
                Snackbar.make(textCommentBody, R.string.comment_insert_failed, Snackbar.LENGTH_LONG)
                        .show();
                Crashlytics.log(Log.ERROR, "InsertReview", t.getMessage());
            }
        });
    }

    private void fetchComments(final boolean scrollToBottom) {
        DotorWebService webServiceApi = Server.getInstance(this).getService();
        Call<CommentsResponse> call = webServiceApi.getComments("review", this.reviewId.toHexString());
        call.enqueue(new Callback<CommentsResponse>() {
            @Override
            public void onResponse(Call<CommentsResponse> call, Response<CommentsResponse> response) {
                if (response.isSuccessful() == false) {
                    Log.e(TAG, "getReviews failed!");
                    return;
                }

                CommentsResponse body = response.body();
                if (body.status < 0) {
                    Log.e(TAG, "getReviews returned status non-zero! message: " + body.message);
                    return;
                }

                final ArrayList<Comment> comments = body.comments;
                if (comments == null) {
                    Log.d(TAG, "No comments do display");
                    // TODO Display no comments message
                    return;
                }

                final CommentsListViewAdapter adapter = (CommentsListViewAdapter) listViewComments.getAdapter();
                adapter.clear();
                adapter.addAll(comments);
                adapter.notifyDataSetChanged();

                textComments.post(new Runnable() {
                    @Override
                    public void run() {
                        textComments.setText(String.valueOf(comments.size()));
                    }
                });

                setListViewHeightBasedOnChildren(listViewComments);

                if (scrollToBottom) {
                    scrollViewRoot.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollViewRoot.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<CommentsResponse> call, Throwable t) {
                Log.e(TAG, "Could not fetch Comments: " + t.getMessage());
            }
        });
    }

    @Override
    public void onDeleteComment(String commentIdStr) {
        Server server = Server.getInstance(this);
        DotorWebService service = server.getService();
        Call<BasicResponse> call = service.deleteComment(commentIdStr);
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() == false) {
                    Snackbar.make(textComments, R.string.comment_delete_failed, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (response.body().status == -1) {
                    Snackbar.make(textComments, R.string.comment_delete_failed, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (response.body().status == -2) {
                    Snackbar.make(textComments, R.string.comment_delete_failed_not_yours, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                Snackbar.make(textComments, R.string.comment_deleted, Snackbar.LENGTH_LONG)
                        .show();

                fetchComments(false);
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Snackbar.make(textComments, R.string.comment_delete_failed, Snackbar.LENGTH_LONG)
                        .show();
            }
        });

    }

    @Override
    public void onReportComment(String commentIdStr) {
        Server server = Server.getInstance(this);
        DotorWebService service = server.getService();
        Call<BasicResponse> call = service.insertReport("comment", commentIdStr);
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccessful() == false) {
                    Snackbar.make(textComments, R.string.comment_report_failed, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                if (response.body().status < 0) {
                    Snackbar.make(textComments, R.string.comment_report_failed, Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }

                Snackbar.make(textComments, R.string.comment_reported, Snackbar.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Snackbar.make(textComments, R.string.comment_report_failed, Snackbar.LENGTH_LONG)
                        .show();

            }
        });
    }
}
