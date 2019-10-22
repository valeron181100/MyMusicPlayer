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
    private ConcurrentHashMap<T,String> mRequestMap;
    private OnCoverDownloadedListener<T> mDownloadedListener;
    private Handler mResponseHandler;
    private boolean mHasQuit = false;

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

    public void queueMessage(T holder, String url){
        if(url == null){
            mRequestMap.remove(holder);
        }else{
            mRequestMap.put(holder, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, holder).sendToTarget();
        }
    }

    public void handleRequest(final T holder){
        final String url = mRequestMap.get(holder);
        try {
            if (url == null) {
                return;
            }

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
                baos = new ByteArrayOutputStream();

                while (stream.read(buff) != -1) {
                    baos.write(buff);
                }

                byte[] bitMapBytes = baos.toByteArray();
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bitMapBytes, 0, bitMapBytes.length);

                mResponseHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mRequestMap.get(holder) != url || mHasQuit){
                            return;
                        }

                        mRequestMap.remove(holder);
                        if(mDownloadedListener != null){
                            mDownloadedListener.downloaded(holder, bitmap);
                        }

                    }
                });
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public interface OnCoverDownloadedListener<T>{
        void downloaded(T holder, Bitmap cover);
    }

}
