package net.team88.dotor.comments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import net.team88.dotor.R;

import org.bson.types.ObjectId;

/**
 * Created by Eun Leem on 4/17/2016.
 */

public class CommentMenuDialogFragment extends DialogFragment {

    public static CommentEvent event;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comment_menu_dialog, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonDelete = (Button) view.findViewById(R.id.buttonDelete);
        Button buttonReport = (Button) view.findViewById(R.id.buttonReport);

        Bundle args = getArguments();

        final String commentIdStr = args.getString("commentid");
        if (commentIdStr == null) {
            return;
        }

        if (ObjectId.isValid(commentIdStr) == false) {
            return;
        }

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                event.onDeleteComment(commentIdStr);
                getDialog().dismiss();
            }
        });

        buttonReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                event.onReportComment(commentIdStr);
                getDialog().dismiss();
            }
        });

    }
}




