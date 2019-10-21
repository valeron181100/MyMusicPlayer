package com.example.mymusicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mymusicplayer.models.TrackModelAdapter;

import java.util.HashMap;


public class MainViewFragment extends Fragment {

    private RecyclerView mTracksRV;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private MediaPlayer mPlayer;

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
        mAdapter = new TrackModelAdapter();

        mTracksRV.setLayoutManager(mLayoutManager);
        mTracksRV.setAdapter(mAdapter);

        return v;

    }
}
