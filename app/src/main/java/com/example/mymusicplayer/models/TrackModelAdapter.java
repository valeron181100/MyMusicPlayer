package com.example.mymusicplayer.models;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mymusicplayer.BottomSheetFragment;
import com.example.mymusicplayer.MainActivity;
import com.example.mymusicplayer.MainViewFragment;
import com.example.mymusicplayer.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class TrackModelAdapter extends RecyclerView.Adapter<TrackModelAdapter.TrackHolder> {
    private static ReentrantLock lock = new ReentrantLock();
    private TrackStorage mTrackStorage;
    private AppCompatActivity mMainActivity;
    private NetHelper.LoadHtmlAsyncTask.OnLoadedHtmlListener mOnLoadedHtmlListener;


    public void setOnLoadedHtmlListener(NetHelper.LoadHtmlAsyncTask.OnLoadedHtmlListener onLoadedHtmlListener) {
        mOnLoadedHtmlListener = onLoadedHtmlListener;
    }

    public TrackModelAdapter(AppCompatActivity activity) {
        mTrackStorage = TrackStorage.getInstance();
        mMainActivity = activity;
    }

    @NonNull
    @Override
    public TrackModelAdapter.TrackHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_template, parent, false);
        return new TrackModelAdapter.TrackHolder(v);
    }



    @Override
    public void onBindViewHolder(@NonNull TrackModelAdapter.TrackHolder holder, int position) {
        holder.bind(mTrackStorage.getTracks().get(position));
    }

    @Override
    public int getItemCount() {
        return mTrackStorage.size();
    }

    public class TrackHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        //TODO: CoverTrackImage

        private TextView mTitleTV;
        private TextView mAuthorTV;
        private Track mTrack;
        private ImageView mCoverIV;



        public TrackHolder(@NonNull View itemView) {
            super(itemView);
            mTitleTV = itemView.findViewById(R.id.trackTitleTV);
            mAuthorTV = itemView.findViewById(R.id.trackAuthorTV);
            mCoverIV = itemView.findViewById(R.id.trackCoverIV);

            itemView.setOnClickListener(this);
        }

        public void bind(Track track){
            mTitleTV.setText(track.getTitle());
            mAuthorTV.setText(track.getAuthorName());
            mTrack = track;
            mCoverIV.setImageResource(R.drawable.ic_musical_note);

            lock.lock();

            GlobalTrackCoverCache cache = GlobalTrackCoverCache.getInstance();
            Bitmap bitmap = cache.get(mTrack.getID());
            if(bitmap != null) {
                mCoverIV.setImageDrawable(new BitmapDrawable(mMainActivity.getResources(), bitmap));
            }else {
                Fragment fragment = mMainActivity.getSupportFragmentManager().findFragmentByTag(MainActivity.MAIN_VIEW_FRAGMENT_TAG);
                if (fragment != null) {
                    ((MainViewFragment) fragment).sendCoverRequest(this, mTrack);
                }
            }
            lock.unlock();
        }

        public Track getTrack() {
            return mTrack;
        }

        public void bindDrawable(Drawable drawable){
            mCoverIV.setImageDrawable(drawable);
        }

        @Override
        public void onClick(View v) {
            if(mTrack != null){
                NetHelper.LoadHtmlAsyncTask loadHtmlAsyncTask = new NetHelper.LoadHtmlAsyncTask(mTrack.getLink());
                loadHtmlAsyncTask.addOnLoadedHtmlListener(mOnLoadedHtmlListener);
                loadHtmlAsyncTask.addOnLoadedHtmlListener(new NetHelper.LoadHtmlAsyncTask.OnLoadedHtmlListener() {
                    @Override
                    public void run(final String htmlStr) {
                        mTrackStorage.getPlayer().reset();
                        //mTrackStorage.setIsPlayerPlaying(false);
                        mTrackStorage.getPlayer().setAudioStreamType(AudioManager.STREAM_MUSIC);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String url = new JSONObject(htmlStr).getString("url");
                                    mTrackStorage.getPlayer().setDataSource(url);
                                    mTrackStorage.getPlayer().prepare();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                BottomSheetFragment.mScheduler.schedule(new Runnable() {
                                    @Override
                                    public void run() {
                                        int mCurrentPosition = TrackStorage.getInstance().getPlayer().getCurrentPosition() / 1000;
                                        BottomSheetFragment.mMusicSeekBar.setProgress(mCurrentPosition);
                                        BottomSheetFragment.mHandler.postDelayed(this, 1000);
                                    }
                                }, 1000, TimeUnit.MILLISECONDS);

                                mTrackStorage.getPlayer().start();
                                mTrackStorage.setIsPlayerPlaying(true);
                            }
                        }).start();
                        mTrackStorage.setNowPlaying(mTrack.getID());
                    }
                });


                loadHtmlAsyncTask.execute();

            }
        }
    }
}
