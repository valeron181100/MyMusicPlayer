package com.example.mymusicplayer.models;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mymusicplayer.BottomSheetFragment;
import com.example.mymusicplayer.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TrackModelAdapter extends RecyclerView.Adapter<TrackModelAdapter.TrackHolder> {

    private TrackStorage mTrackStorage;
    private NetHelper.LoadHtmlAsyncTask.OnLoadedHtmlListener mOnLoadedHtmlListener;


    public void setOnLoadedHtmlListener(NetHelper.LoadHtmlAsyncTask.OnLoadedHtmlListener onLoadedHtmlListener) {
        mOnLoadedHtmlListener = onLoadedHtmlListener;
    }

    public TrackModelAdapter() {
        mTrackStorage = TrackStorage.getInstance();

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

    class TrackHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        //TODO: CoverTrackImage

        private TextView mTitleTV;
        private TextView mAuthorTV;
        private Track mTrack;


        public TrackHolder(@NonNull View itemView) {
            super(itemView);
            mTitleTV = itemView.findViewById(R.id.trackTitleTV);
            mAuthorTV = itemView.findViewById(R.id.trackAuthorTV);

            itemView.setOnClickListener(this);
        }

        public void bind(Track track){
            mTitleTV.setText(track.getTitle());
            mAuthorTV.setText(track.getAuthorName());
            mTrack = track;
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
                        mTrackStorage.setIsPlayerPlaying(false);
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
