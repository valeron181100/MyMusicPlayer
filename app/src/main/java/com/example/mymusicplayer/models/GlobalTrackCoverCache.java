package com.example.mymusicplayer.models;

import android.graphics.Bitmap;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

public class GlobalTrackCoverCache extends LruCache<UUID, Bitmap> {
    private static GlobalTrackCoverCache instance;

    private static int maxSize = 20 * 1024 * 1024;

    private GlobalTrackCoverCache(int maximumSize) {
        super(maximumSize);
    }

    public static synchronized GlobalTrackCoverCache getInstance() {
        if(instance == null)
            instance = new GlobalTrackCoverCache(maxSize);
        return instance;
    }



    @Override
    protected int sizeOf(@NonNull UUID key, @NonNull Bitmap value) {
        return value.getByteCount();
    }
}
