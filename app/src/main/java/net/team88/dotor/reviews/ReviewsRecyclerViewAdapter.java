package net.team88.dotor.reviews;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


import net.team88.dotor.R;
import net.team88.dotor.pets.Pet;
import net.team88.dotor.shared.Server;
import net.team88.dotor.shared.image.ImageViewActivity;
import net.team88.dotor.utils.Utils;

import org.bson.types.ObjectId;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ReviewsRecyclerViewAdapter extends RecyclerView.Adapter<ReviewsRecyclerViewAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardview;
        View layoutCardBody;

        TextView textCategory;
        TextView textLocation;
        TextView textPetInfo;
        TextView textCost;

        ImageView imageViewReviewThumbnail;

        TextView textReviewLikes;
        TextView textReviewComments;

        View layoutLikes;
        View layoutComments;

        //Button buttonLike;

        public ViewHolder(View view) {
            super(view);

            this.cardview = (CardView) view.findViewById(R.id.cardview);
            this.layoutCardBody = view.findViewById(R.id.layoutCardBody);

            this.textCategory = (TextView) view.findViewById(R.id.textCategory);
            this.textLocation = (TextView) view.findViewById(R.id.textLocation);
            this.textPetInfo = (TextView) view.findViewById(R.id.textPetInfo);
            this.textCost = (TextView) view.findViewById(R.id.textCost);
            this.imageViewReviewThumbnail = (ImageView) view.findViewById(R.id.imageViewReviewThumbnail);

            this.textReviewLikes = (TextView) view.findViewById(R.id.textReviewLikes);
            this.textReviewComments = (TextView) view.findViewById(R.id.textReviewComments);

            this.layoutLikes = view.findViewById(R.id.layoutLikes);
            this.layoutComments = view.findViewById(R.id.layoutComments);

            //this.buttonLike = (Button) view.findViewById(R.id.buttonLike);
        }
    }

    private static final String TAG = "ReviewsRecyclerView";

    private Context context;

    List<Review> reviews;

    ReviewListActivity.Mode mode;

    public ReviewsRecyclerViewAdapter(Context context) {
        this.context = context;
        this.reviews = new ArrayList<>();
        this.mode = ReviewListActivity.Mode.ALL;
    }

    public ReviewsRecyclerViewAdapter(Context context, ReviewListActivity.Mode mode) {
        this.context = context;
        this.reviews = new ArrayList<>();
        this.mode = mode;
    }

    public void clear() {
        this.reviews.clear();
        notifyDataSetChanged();
    }

    public void addItems(ArrayList<Review> reviews) {
        this.reviews.addAll(reviews);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Review review = this.reviews.get(position);

        if (review.categories != null) {
            StringBuilder categories = new StringBuilder();
            for (String category : review.categories) {
                categories
                        .append(", ")
                        .append(category);
            }

            if (categories.length() > 2) {
                categories.delete(0, 2);
            }

            holder.textCategory.setText(categories.toString());
        }

        if (review.locationName.isEmpty()) {
            holder.textLocation.setVisibility(View.GONE);
        } else {
            holder.textLocation.setText(review.locationName);
        }
        Pet pet = new Pet();
        pet.type = review.petType;
        pet.gender = review.petGender;
        pet.size = review.petSize;

        StringBuilder petInfo = new StringBuilder();
        petInfo.append(pet.getTypeString(context))
                .append(", ")
                .append(String.valueOf(review.petAge))
                .append(context.getString(R.string.age_unit))
                .append(", ")
                .append(pet.getSizeString(context));


        holder.textPetInfo.setText(petInfo.toString());

        String costStr = NumberFormat.getInstance().format(review.cost);
        String costPostfix = context.getString(R.string.money_unit);
        holder.textCost.setText(costStr + costPostfix);

        if (review.images != null && review.images.size() > 0) {
            holder.imageViewReviewThumbnail.setVisibility(View.VISIBLE);

            String baseUrl = Server.getInstance(context).getServerUrl();
            baseUrl += "/img/";

            for (ObjectId imageId : review.images) {
                Log.d("a", "imageId: " + imageId.toHexString());
            }

            ObjectId imageId = review.images.get(0);
            final String imageUrl = baseUrl + imageId.toHexString() + ".jpg";
            Glide.with(context)
                    .load(imageUrl)
                    .centerCrop()
                    .fallback(R.drawable.ic_image_grey600_48dp)
                    .into(holder.imageViewReviewThumbnail);

            holder.imageViewReviewThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ImageViewActivity.class);
                    intent.putExtra("image_url", imageUrl);
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation((Activity) context,
                                    (View) holder.imageViewReviewThumbnail,
                                    "review_image");
                    context.startActivity(intent, options.toBundle());
                }
            });
        } else {
            holder.imageViewReviewThumbnail.setVisibility(View.GONE);
        }

        {

        }

        int primaryColor = context.getResources().getColor(R.color.colorPrimary);

        Utils.changeIconColor(holder.textReviewLikes, primaryColor);
        Utils.changeIconColor(holder.textReviewComments, primaryColor);

        int likes = 0;
        if (review.likes != null) {
            likes = review.likes.size();
        }
        holder.textReviewLikes.setText(String.valueOf(likes));

        int comments = 0;
        if (review.comments != null) {
            comments = review.comments.size();
        }

        holder.textReviewComments.setText(String.valueOf(comments));


        holder.layoutCardBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ReviewViewActivity.class);
                intent.putExtra("reviewid", review.id.toHexString());
//                ActivityOptionsCompat options = ActivityOptionsCompat.
//                        makeSceneTransitionAnimation((Activity) context,
//                                (View) holder.imageViewReviewThumbnail,
//                                "review_image");
//                context.startActivity(intent, options.toBundle());
                context.startActivity(intent);
            }
        });

        holder.layoutLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Open Likes Activity
                Intent intent = new Intent(context, ReviewViewActivity.class);
                intent.putExtra("reviewid", review.id.toHexString());
                context.startActivity(intent);
            }
        });

        holder.layoutComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ReviewViewActivity.class);
                intent.putExtra("reviewid", review.id.toHexString());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.reviews.size();
    }

}

