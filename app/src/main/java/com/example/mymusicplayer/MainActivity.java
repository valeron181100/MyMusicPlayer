package com.example.mymusicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MainActivity extends AppCompatActivity {

    private static final String MAIN_VIEW_FRAGMENT_TAG = "MAIN_VIEW_FRAGMENT_TAG";
    private static final String TAG = "MMP";

    private LinearLayout mBottomSheet;
    private CoordinatorLayout mCoordLayout;
    private LinearLayout mBottomLayout;




    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomSheet = findViewById(R.id.bottomSheet);
        mCoordLayout = findViewById(R.id.main_coordinator);
        mBottomLayout = findViewById(R.id.bottomLayout);

        BottomSheetBehavior<LinearLayout> mLLBehaviour = BottomSheetBehavior.from(mBottomLayout);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(MAIN_VIEW_FRAGMENT_TAG);
        if(fragment == null){
            fragment = new MainViewFragment();
        }
        fm.beginTransaction().add(R.id.mainViewFragment, fragment, MAIN_VIEW_FRAGMENT_TAG).commit();


        mLLBehaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View view, int i) {
                view.invalidate();
            }



            @Override
            public void onSlide(@NonNull View view, float v) {
                mBottomSheet.setAlpha(1.0f - v);
            }
        });

    }
}
