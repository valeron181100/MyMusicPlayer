package com.example.mymusicplayer.models;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.mymusicplayer.MainActivity;
import com.example.mymusicplayer.R;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        TrackStorage trackStorage = TrackStorage.getInstance();
        trackStorage.addOnTracksDownloadedListener(new TrackStorage.OnTracksDownloadedListener() {
            @Override
            public void run(TrackStorage trackStorage) {
                onCompleted();
            }
        });

    }

    private void onCompleted(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
