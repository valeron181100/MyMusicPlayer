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

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mymusicplayer.models.TrackStorage;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MainActivity extends AppCompatActivity {

    private static final String MAIN_VIEW_FRAGMENT_TAG = "MAIN_VIEW_FRAGMENT_TAG";
    private static final String BOTTOM_SHEET_FRAGMENT_TAG = "BOTTOM_SHEET_FRAGMENT_TAG";
    private static final String TAG = "MMP";

    private ImageButton mCollaseBottomBarButton;
    private LinearLayout mBottomSheet;
    private RelativeLayout mBottomSheetExpanded;
    private LinearLayout mBottomLayout;
    private float mpreviousBottomSheetOffset = 0.0f;
    private TrackStorage mTrackStorage;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomSheet = findViewById(R.id.bottomSheet);
        mBottomLayout = findViewById(R.id.bottomLayout);
        mBottomSheetExpanded = findViewById(R.id.bottomSheetExpanded);
        mTrackStorage = TrackStorage.getInstance();


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

        fmTransaction.add(R.id.mainViewFragment, fragment, MAIN_VIEW_FRAGMENT_TAG).
                add(R.id.bottomFragmentContainer, bottomFragment, BOTTOM_SHEET_FRAGMENT_TAG).commit();




        mLLBehaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {


            @Override
            public void onStateChanged(@NonNull View view, int i) {

            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                if(mpreviousBottomSheetOffset <= v) {
                    if (v <= 0.5f)
                        mBottomSheet.setAlpha(1.0f - v * 2);
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
                        mBottomSheet.setAlpha(1.0f - v * 2);
                    }
                }
                Log.d(TAG, "Before"+mpreviousBottomSheetOffset);
                mpreviousBottomSheetOffset = v;
                Log.d(TAG, "After"+mpreviousBottomSheetOffset);
            }
        });

    }
}
