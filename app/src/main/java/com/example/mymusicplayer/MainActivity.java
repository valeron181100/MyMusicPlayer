package com.example.mymusicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Transition;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mymusicplayer.models.ObservableBoolean;
import com.example.mymusicplayer.models.Track;
import com.example.mymusicplayer.models.TrackStorage;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String MAIN_VIEW_FRAGMENT_TAG = "MAIN_VIEW_FRAGMENT_TAG";
    private static final String BOTTOM_SHEET_FRAGMENT_TAG = "BOTTOM_SHEET_FRAGMENT_TAG";
    private static final String TAG = "MMP";

    private ImageButton mCollaseBottomBarButton;
    private RelativeLayout mBottomSheet;
    private RelativeLayout mBottomSheetExpanded;
    private LinearLayout mBottomLayout;
    private float mpreviousBottomSheetOffset = 0.0f;
    private TrackStorage mTrackStorage;
    private BottomNavigationView mBottomNavigation;


    private ImageButton mPlayButton;
    private Handler mHandler;
    private TextView mAuthorNameTV;
    private TextView mTitleTV;




    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomSheet = findViewById(R.id.bottomSheet);
        mBottomLayout = findViewById(R.id.bottomLayout);
        mBottomSheetExpanded = findViewById(R.id.bottomSheetExpanded);
        mTrackStorage = TrackStorage.getInstance();
        mBottomNavigation = findViewById(R.id.bottom_navigation);
        mPlayButton = findViewById(R.id.playTrackButton);
        mAuthorNameTV = findViewById(R.id.trackAuthorTV);
        mTitleTV = findViewById(R.id.trackTitleTV);
        mHandler = new Handler();

        TrackStorage.getInstance().getIsPlayerPlaying().addOnChangeValueListener(onChangeValueListener);

        mPlayButton.setOnClickListener(playerConrolButtonListener);

        BottomSheetBehavior<LinearLayout> mLLBehaviour = BottomSheetBehavior.from(mBottomLayout);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(MAIN_VIEW_FRAGMENT_TAG);
        Fragment bottomFragment = fm.findFragmentByTag(BOTTOM_SHEET_FRAGMENT_TAG);
        FragmentTransaction fmTransaction = fm.beginTransaction();

        if(fragment == null){
            fragment = new MainViewFragment();
        }
        if(bottomFragment == null){
            bottomFragment = new BottomSheetFragment();
        }

        fmTransaction.replace(R.id.mainViewFragment, fragment, MAIN_VIEW_FRAGMENT_TAG).
                replace(R.id.bottomFragmentContainer, bottomFragment, BOTTOM_SHEET_FRAGMENT_TAG).commit();


        mLLBehaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {


            @Override
            public void onStateChanged(@NonNull View view, int i) {

            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                if(mpreviousBottomSheetOffset <= v) {
                    if (v <= 0.5f)
                        mBottomSheet.setAlpha(1.0f - v * 4);
                        if(v <= 0.25){
                            float translateY = v * convertDpToPixels(400, MainActivity.this);
                            mBottomNavigation.setTranslationY(translateY);
                        }
                    else {
                        if (mBottomSheet.getAlpha() != 0.0f) {
                            mBottomSheet.setAlpha(0.0f);
                        }
                        mBottomSheetExpanded.setAlpha((v - 0.5f) * 2);
                    }
                }
                else{
                    if(v > 0.5f){
                        mBottomSheetExpanded.setAlpha((v - 0.5f) * 2);

                    }else{
                        if (mBottomSheetExpanded.getAlpha() != 0.0f) {
                            mBottomSheetExpanded.setAlpha(0.0f);

                            mBottomSheet.setAlpha(0.0f);

                        }

                        if(v <= 0.25){
                            float translateY = v * convertDpToPixels(400, MainActivity.this);
                            mBottomNavigation.setTranslationY(translateY);
                        }

                        mBottomSheet.setAlpha(1.0f - v * 4);
                    }
                }
                Log.d(TAG, "Before"+mpreviousBottomSheetOffset);
                mpreviousBottomSheetOffset = v;
                Log.d(TAG, "After"+mpreviousBottomSheetOffset);
            }
        });

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
                    if(TrackStorage.getInstance().getPlayer().isPlaying())
                        mPlayButton.setImageResource(R.drawable.ic_pause_black_35dp);
                    else
                        mPlayButton.setImageResource(R.drawable.ic_play_arrow_black_35dp);
                }
            });


        }
    };

    private View.OnClickListener playerConrolButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TrackStorage trackStorage = TrackStorage.getInstance();
            trackStorage.playOrPause();
            if(TrackStorage.getInstance().getPlayer().isPlaying())
                mPlayButton.setImageResource(R.drawable.ic_pause_black_35dp);
            else
                mPlayButton.setImageResource(R.drawable.ic_play_arrow_black_35dp);
        }
    };

    public static int convertDpToPixels(float dp, Context context) {
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return px;
    }
}
