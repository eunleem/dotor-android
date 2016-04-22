package net.team88.dotor.comments;

/**
 * Created by Eun Leem on 4/17/2016.
 */
public interface CommentEvent {
    void onDeleteComment(String commentIdStr);
    void onReportComment(String commentIdStr);
}

