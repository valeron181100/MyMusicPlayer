package com.example.mymusicplayer.models;

import android.content.Context;
import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

public class TrackStorage {

    private static TrackStorage instance;
    private ArrayList<Track> mTracks;
    private ArrayList<OnTracksDownloadedListener> listeners;

    private TrackStorage(){
        mTracks = new ArrayList<>();
        listeners = new ArrayList<>();
        loadTracksAsync();
    }

    public static TrackStorage getInstance(){
        if(instance == null)
            instance = new TrackStorage();
        return instance;
    }

    private void loadTracksAsync(){
        LoadTracksAsyncTask tracksAsyncTask = new LoadTracksAsyncTask();
        tracksAsyncTask.execute();
    }

    public int size(){
       return mTracks.size();
    }

    public ArrayList<Track> getTracks() {
        return mTracks;
    }

    public void addOnTracksDownloadedListener(OnTracksDownloadedListener listener){
        listeners.add(listener);
    }

    interface OnTracksDownloadedListener{
        void run(TrackStorage trackStorage);
    }


    class LoadTracksAsyncTask extends AsyncTask<String, ArrayList<Track>, ArrayList<Track>> {

        @Override
        protected ArrayList<Track> doInBackground(String... strings) {
            String urlStr = "https://zaycev.net";
            ArrayList<Track> list = new ArrayList<>();
            try {
                URL url = new URL(urlStr);
                URLConnection connection = url.openConnection();

                InputStream stream = connection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buff = new byte[1024];

                while(stream.read(buff) != -1){
                    baos.write(buff);
                }
                String xmlStr = new String(baos.toByteArray());

                Document document = Jsoup.parse(xmlStr);

                Elements selectedArray = document.select(".columns-block-tracks .musicset-track-list__items [itemprop='track']");

                for(Element element : selectedArray){
                    String titleAuthor = element.attr("title");
                    String[] split = titleAuthor.split(" – ");
                    String title = split[1];
                    String authorName = titleAuthor.split(" ")[1];
                    String dataLink = urlStr + element.attr("data-url");
                    //TODO: Cover Image downloading

                    Track track = new Track(title, authorName, dataLink);
                    list.add(track);
                }

                int k =0;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<Track> list) {
            mTracks.addAll(list);
            for(OnTracksDownloadedListener p : listeners){
                p.run(TrackStorage.this);
            }
        }

    }

}
