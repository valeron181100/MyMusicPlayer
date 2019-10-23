package com.example.mymusicplayer.models;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;

public class CoverDownloaderThread<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;
    private ConcurrentHashMap<T,Track> mRequestMap;
    private OnCoverDownloadedListener<T> mDownloadedListener;
    private Handler mResponseHandler;
    private boolean mHasQuit = false;

    private Track mTrack;

    @Override
    public boolean quit(){
        mHasQuit = true;
        return super.quit();
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    public void setOnCoverDownloadedListener(OnCoverDownloadedListener<T> downloadedListener) {
        mDownloadedListener = downloadedListener;
    }

    public CoverDownloaderThread(Handler responseHandler) {
        super(TAG);
        mRequestMap = new ConcurrentHashMap<>();
        mResponseHandler = responseHandler;
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == MESSAGE_DOWNLOAD){
                    T holder = (T) msg.obj;
                    handleRequest(holder);
                }
            }
        };
    }

    public void queueMessage(T holder, Track track){
        mTrack = track;
        if(mTrack.getCoverLink() == null){
            mRequestMap.remove(holder);
        }else{
            mRequestMap.put(holder, track);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, holder).sendToTarget();
        }
    }

    public void handleRequest(final T holder){
        final Track track = mRequestMap.get(holder);
        try {
            if (track == null) {
                return;
            }

            final String url = track.getCoverLink();

            URLConnection connection = new URL(url).openConnection();

            InputStream stream = connection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];

            while (stream.read(buff) != -1) {
                baos.write(buff);
            }
            String xmlStr = new String(baos.toByteArray());
            Document document = Jsoup.parse(xmlStr);
            Elements elements = document.select("#audiotrack-info>a>img");
            if(elements.size() == 0){
                return;
            }else{
                String coverUrl = elements.get(0).attr("src");
                connection = new URL(coverUrl).openConnection();
                stream = connection.getInputStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(stream);

                mResponseHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Track track1 = mRequestMap.get(holder);
                        if(track1 != null)
                        if(track1.getCoverLink() != url || mHasQuit){
                            return;
                        }

                        mRequestMap.remove(holder);
                        if(mDownloadedListener != null){
                            mDownloadedListener.downloaded(holder, bitmap, track);
                        }

                    }
                });
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public interface OnCoverDownloadedListener<T>{
        void downloaded(T holder, Bitmap cover, Track realTrack);
    }

}
