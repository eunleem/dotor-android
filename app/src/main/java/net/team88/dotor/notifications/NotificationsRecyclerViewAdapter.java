package net.team88.dotor.notifications;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.team88.dotor.R;
import net.team88.dotor.reviews.ReviewViewActivity;
import net.team88.dotor.shared.BasicResponse;
import net.team88.dotor.shared.DotorWebService;
import net.team88.dotor.shared.Server;

import org.bson.types.ObjectId;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Eun Leem on 2/24/2016.
 */
public class NotificationsRecyclerViewAdapter extends RecyclerView.Adapter<NotificationsRecyclerViewAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutRoot;

        ObjectId notificationId;

        TextView textTimestamp;
        TextView textMessage;

        public ViewHolder(View view) {
            super(view);
            this.layoutRoot = (LinearLayout) view.findViewById(R.id.layoutNotification);

            this.textTimestamp = (TextView) view.findViewById(R.id.textTimestamp);
            this.textMessage = (TextView) view.findViewById(R.id.textMessage);
        }
    }


    Context context;

    public NotificationsRecyclerViewAdapter(Context context) {
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    private static final String TAG = "NotificationsRecycleVA";

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Notification notification = Notifications.getInstance(context).getNotifications().get(position);

        CharSequence time = DateUtils.getRelativeTimeSpanString(context, notification.created.getTime());
        holder.textTimestamp.setText(time.toString());

        String message = "";
        String nickname = notification.message;
        if (notification.type.equalsIgnoreCase("review_comment")) {
            message = String.format(context.getString(R.string.msg_notification_comment), nickname);
        } else if (notification.type.equalsIgnoreCase("review_like")) {
            message = String.format(context.getString(R.string.msg_notification_like), nickname);
        }

        holder.textMessage.setText(message);
        if (notification.isRead) {
            holder.layoutRoot.setBackgroundColor(Color.WHITE);
        } else {
            holder.layoutRoot.setBackgroundColor(Color.argb(45, 255, 200, 200));
        }


        holder.layoutRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO

                if (notification.relatedType.equalsIgnoreCase("review")) {
                    // TODO RelatedType now is only REVIEW. But handle case when it's Comment.
                    // Open CommentActivity in that case and highlight the related comment.
                    Intent intent = new Intent(context, ReviewViewActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("reviewid", notification.relatedId.toHexString());
                    context.startActivity(intent);
                }

                if (notification.isRead == false) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            markRead(notification, holder);
                        }
                    });
                }

            }
        });
    }

    private void markRead(Notification notification, final ViewHolder holder) {

        DotorWebService service = Server.getInstance(context).getService();
        Call<BasicResponse> call = service.readNotfication(notification.id.toHexString());
        call.enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                if (response.isSuccess() == false) {
                    Log.d(TAG, "MarkRead: Failed");
                    return;
                }

                BasicResponse body = response.body();
                if (body.status < 0) {
                    Log.d(TAG, "MarkRead Failed. message: " + body.message);
                    return;
                }

                Log.d(TAG, "MarkRead Good.");
                holder.layoutRoot.setBackgroundColor(Color.WHITE);
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                Log.d(TAG, "MarkRead Failed. t: " + t.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return Notifications.getInstance(context).getNotifications().size();
    }
}
