package com.example.mymusicplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mymusicplayer.models.CoverDownloaderThread;
import com.example.mymusicplayer.models.TrackModelAdapter;

import java.util.HashMap;


public class MainViewFragment extends Fragment {

    private RecyclerView mTracksRV;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private MediaPlayer mPlayer;
    private CoverDownloaderThread<TrackModelAdapter.TrackHolder> mCoverDownloaderThread;
    private Handler mResponseCoverHandler;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_main_view, container, false);

        mTracksRV = v.findViewById(R.id.tracksRV);
        mLayoutManager = new LinearLayoutManager(this.getContext());
        mPlayer = new MediaPlayer();
        mAdapter = new TrackModelAdapter((AppCompatActivity) getActivity());

        mTracksRV.setLayoutManager(mLayoutManager);
        mTracksRV.setAdapter(mAdapter);
        mResponseCoverHandler = new Handler();
        mCoverDownloaderThread = new CoverDownloaderThread<>(mResponseCoverHandler);
        mCoverDownloaderThread.start();
        mCoverDownloaderThread.getLooper();

        mCoverDownloaderThread.setOnCoverDownloadedListener(
                new CoverDownloaderThread.OnCoverDownloadedListener<TrackModelAdapter.TrackHolder>() {
            @Override
            public void downloaded(TrackModelAdapter.TrackHolder holder, Bitmap cover) {

                holder.bindDrawable(new BitmapDrawable(getResources(), cover));

            }
        });

        return v;

    }

    public void sendCoverRequest(TrackModelAdapter.TrackHolder holder, String url){
        mCoverDownloaderThread.queueMessage(holder, url);
    }

    @Override
    public void onDestroyView() {
        mCoverDownloaderThread.clearQueue();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mCoverDownloaderThread.quit();
        super.onDestroy();
    }
}
