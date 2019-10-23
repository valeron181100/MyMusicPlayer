package com.example.mymusicplayer.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.mymusicplayer.MainActivity;
import com.example.mymusicplayer.R;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

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
        }

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
