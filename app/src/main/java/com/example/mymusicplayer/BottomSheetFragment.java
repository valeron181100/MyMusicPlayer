package com.example.mymusicplayer;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.mymusicplayer.models.GlobalTrackCoverCache;
import com.example.mymusicplayer.models.ObservableBoolean;
import com.example.mymusicplayer.models.Track;
import com.example.mymusicplayer.models.TrackPagerAdapter;
import com.example.mymusicplayer.models.TrackStorage;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class BottomSheetFragment extends Fragment {
    private static final String MUSIC_SEEK_BAR_TAG = "MUSIC_SEEK_BAR_TAG";


    private ImageButton mPlayButton;
    private ImageButton mPlayPrevButton;
    private ImageButton mPlayNextButton;
    private ViewPager mCoverTrackVP;

    private TextView mTitleTV;
    private TextView mAuthorNameTV;

    public static SeekBar mMusicSeekBar;

    public static Handler mHandler;
    public static ScheduledExecutorService mScheduler;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);

        mTitleTV = v.findViewById(R.id.trackTitleTV);
        mAuthorNameTV = v.findViewById(R.id.trackAuthorTV);

        mPlayButton = v.findViewById(R.id.playTrackButton);
        mPlayPrevButton = v.findViewById(R.id.backTrackButton);
        mPlayNextButton = v.findViewById(R.id.nextTrackButton);

        mMusicSeekBar = v.findViewById(R.id.musicSeekBar);

        mPlayButton.setOnClickListener(playerConrolButtonsListener);
        mPlayNextButton.setOnClickListener(playerConrolButtonsListener);
        mPlayPrevButton.setOnClickListener(playerConrolButtonsListener);

        mCoverTrackVP = v.findViewById(R.id.trackCoverVP);
        mCoverTrackVP.setAdapter(new TrackPagerAdapter(getContext()));
//        mCoverTrackVP.setPageTransformer(true, new TrackPagerAdapter.ZoomOutPageTransformer());
        int pagePadding = MainActivity.convertDpToPixels(20, getContext());
        mCoverTrackVP.setClipToPadding(false);
        mCoverTrackVP.setPadding(pagePadding * 2, 0, pagePadding * 2, 0);
        mCoverTrackVP.setPageMargin(pagePadding);
        mCoverTrackVP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                TrackStorage trackStorage = TrackStorage.getInstance();
                trackStorage.playSong(trackStorage.getTracks().get(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



        mHandler = new Handler();
        mScheduler = Executors.newScheduledThreadPool(1);

        TrackStorage.getInstance().getIsPlayerPlaying().addOnChangeValueListener(onChangeValueListener);

        TrackStorage.getInstance().getPlayer().setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMusicSeekBar.setMax(mp.getDuration() / 1000);
            }
        });

        TrackStorage.getInstance().getPlayer().setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                mMusicSeekBar.setSecondaryProgress(percent * 2);
            }
        });

        mMusicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(TrackStorage.getInstance().getPlayer() != null && fromUser){
                    TrackStorage.getInstance().getPlayer().seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        v.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        return v;
    }



    private ObservableBoolean.OnChangeValueListener onChangeValueListener = new ObservableBoolean.OnChangeValueListener() {
        @Override
        public void onChange(Boolean oldVal, final Boolean newVal) {
            final Track track = TrackStorage.getInstance().getTrackById(TrackStorage.getInstance().getNowPlaying());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAuthorNameTV.setText(track.getAuthorName());
                    mTitleTV.setText(track.getTitle());
                    mCoverTrackVP.setCurrentItem(TrackStorage.getInstance().getTracks().indexOf(track), true);
                    if(TrackStorage.getInstance().getPlayer().isPlaying())
                        mPlayButton.setImageResource(R.drawable.ic_pause_circle_outline_black);
                    else
                        mPlayButton.setImageResource(R.drawable.ic_play_circle_outline_black);
                }
            });


        }
    };

    private View.OnClickListener playerConrolButtonsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TrackStorage trackStorage = TrackStorage.getInstance();
            switch (v.getId()){
                case R.id.playTrackButton:
                    trackStorage.playOrPause();
                    if(TrackStorage.getInstance().getPlayer().isPlaying())
                        mPlayButton.setImageResource(R.drawable.ic_pause_circle_outline_black);
                    else
                        mPlayButton.setImageResource(R.drawable.ic_play_circle_outline_black);
                    break;

                case R.id.backTrackButton:
                    trackStorage.playPrevious();
                    if(TrackStorage.getInstance().getPlayer().isPlaying())
                        mPlayButton.setImageResource(R.drawable.ic_pause_circle_outline_black);
                    else
                        mPlayButton.setImageResource(R.drawable.ic_play_circle_outline_black);
                    break;

                case R.id.nextTrackButton:
                    trackStorage.playNext();
                    if(TrackStorage.getInstance().getPlayer().isPlaying())
                        mPlayButton.setImageResource(R.drawable.ic_pause_circle_outline_black);
                    else
                        mPlayButton.setImageResource(R.drawable.ic_play_circle_outline_black);
                    break;
            }

            mMusicSeekBar.setMax(TrackStorage.getInstance().getPlayer().getDuration()/1000);

            mScheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    int mCurrentPosition = TrackStorage.getInstance().getPlayer().getCurrentPosition() / 1000;
                    mMusicSeekBar.setProgress(mCurrentPosition);
                    mHandler.postDelayed(this, 1000);
                }
            }, 1000, TimeUnit.MILLISECONDS);
        }
    };

}
