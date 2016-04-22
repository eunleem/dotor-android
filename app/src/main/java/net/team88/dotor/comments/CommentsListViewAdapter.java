package net.team88.dotor.comments;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.team88.dotor.R;

import java.util.ArrayList;


public class CommentsListViewAdapter extends ArrayAdapter<Comment> {

    private static class ViewHolder {
        View layoutRoot;
        TextView textNickname;
        TextView textCommentBody;
        TextView textTimestamp;
    }

    Context context;
    DialogFragment commentMenuDialogFragment;

    public CommentsListViewAdapter(Context context,
                                   CommentMenuDialogFragment commentMenuDialogFragment,
                                   ArrayList<Comment> comments) {
        super(context, 0, comments);
        this.context = context;
        this.commentMenuDialogFragment = commentMenuDialogFragment;
    }


    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
            viewHolder.layoutRoot = (View) convertView.findViewById(R.id.layoutRoot);
            viewHolder.textNickname = (TextView) convertView.findViewById(R.id.textNickname);
            viewHolder.textCommentBody = (TextView) convertView.findViewById(R.id.textCommentBody);
            viewHolder.textTimestamp = (TextView) convertView.findViewById(R.id.textTimestamp);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Comment comment = getItem(position);

        viewHolder.textNickname.setText(comment.nickname);
        CharSequence time = DateUtils.getRelativeTimeSpanString(this.getContext(), comment.created.getTime());
        viewHolder.textTimestamp.setText(time.toString());
        viewHolder.textCommentBody.setText(comment.commentBody);

        viewHolder.layoutRoot.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Bundle args = new Bundle();
                args.putString("commentid", comment.id.toHexString());
                commentMenuDialogFragment.setArguments(args);
                FragmentManager fm = ((FragmentActivity) parent.getContext()).getFragmentManager();
                commentMenuDialogFragment.show(fm, "CommentMenu");
                return false;
            }
        });


        return convertView;
    }



}

