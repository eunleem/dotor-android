package net.team88.dotor.comments;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.team88.dotor.R;

import java.util.ArrayList;

public class CommentsListViewAdapter extends ArrayAdapter<Comment> {

    // View lookup cache
    private static class ViewHolder {
        View layoutRoot;
        TextView textNickname;
        TextView textCommentBody;
        TextView textTimestamp;
    }

    public CommentsListViewAdapter(Context context, ArrayList<Comment> comments) {
        super(context, 0, comments);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_comment, parent, false);
            viewHolder.layoutRoot = (View) convertView.findViewById(R.id.layoutRoot);
            viewHolder.textNickname = (TextView) convertView.findViewById(R.id.textNickname);
            viewHolder.textCommentBody = (TextView) convertView.findViewById(R.id.textCommentBody);
            viewHolder.textTimestamp = (TextView) convertView.findViewById(R.id.textTimestamp);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Comment comment = getItem(position);

        viewHolder.textNickname.setText(comment.nickname);
        CharSequence time = DateUtils.getRelativeTimeSpanString(this.getContext(), comment.created.getTime());
        viewHolder.textTimestamp.setText(time.toString());
        viewHolder.textCommentBody.setText(comment.commentBody);


        return convertView;
    }

}

