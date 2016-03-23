package net.team88.dotor.intro;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import net.team88.dotor.R;

import java.util.ArrayList;

public class IntroPagerAdapter extends PagerAdapter {

    private ArrayList<View> pageViews;
    private Context context;

    private Button buttonStart;

    private OnStartClickedListener mListener;

    public IntroPagerAdapter(ArrayList<View> pageViews, Context context) {
        super();
        this.pageViews = pageViews;
        this.context = context;
        mListener = (OnStartClickedListener) context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final int lastPagePosition = pageViews.size() - 1;

        if (position == lastPagePosition) {
            buttonStart = (Button) pageViews.get(position).findViewById(R.id.button_start);
            buttonStart.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onStartClicked(buttonStart);
                }
            });
        }

        container.addView(pageViews.get(position));
        return pageViews.get(position);
    }


    public interface OnStartClickedListener {
        void onStartClicked(Button btn);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return pageViews.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}