package com.example.mymusicplayer.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.mymusicplayer.MainActivity;
import com.example.mymusicplayer.R;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class TrackPagerAdapter extends PagerAdapter {

    private Context mContext;
    private TrackStorage mTrackStorage;

    public TrackPagerAdapter(Context context){
        mContext = context;
        mTrackStorage = TrackStorage.getInstance();
    }

    @Override
    public int getCount() {
        return mTrackStorage.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        ImageView view = new ImageView(mContext);
        view.setMaxWidth(MainActivity.convertDpToPixels(300, mContext));
        view.setMaxHeight(MainActivity.convertDpToPixels(300, mContext));
        view.setMinimumWidth(MainActivity.convertDpToPixels(300, mContext));
        view.setMinimumHeight(MainActivity.convertDpToPixels(300, mContext));


        Track track = mTrackStorage.getTracks().get(position);

        Bitmap bitmap = GlobalTrackCoverCache.getInstance().get(track.getID());
        if(bitmap != null){
            view.setImageDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
        }else{
            view.setImageResource(R.drawable.ic_music);
            view.setBackgroundResource(R.color.secondaryLightColor);
        }

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public static class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }
}
