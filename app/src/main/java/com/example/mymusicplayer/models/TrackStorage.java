package com.example.mymusicplayer.models;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import com.example.mymusicplayer.BottomSheetFragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.Nullable;

public class TrackStorage {

    private static TrackStorage instance;
    private ArrayList<Track> mTracks;
    private ArrayList<OnTracksDownloadedListener> listeners;
    private MediaPlayer mPlayer;
    private UUID mNowPlaying;
    private ObservableBoolean mIsPlayerPlaying;

    private TrackStorage(){
        mTracks = new ArrayList<>();
        listeners = new ArrayList<>();
        mPlayer = new MediaPlayer();
        mPlayer.setLooping(true);
        mNowPlaying = null;
        mIsPlayerPlaying = new ObservableBoolean(false);

        loadTracksAsync();

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                int curIndex = mTracks.indexOf(getTrackById(mNowPlaying));
                if(curIndex != mTracks.size() - 1)
                    playSong(mTracks.get(curIndex + 1));
            }
        });
    }

    public ObservableBoolean getIsPlayerPlaying() {
        return mIsPlayerPlaying;
    }

    public void setIsPlayerPlaying(boolean isPlayerPlaying) {
        mIsPlayerPlaying.setValue(isPlayerPlaying);
    }

    public UUID getNowPlaying() {
        return mNowPlaying;
    }


    public void setNowPlaying(UUID nowPlaying) {
        mNowPlaying = nowPlaying;
    }

    public MediaPlayer getPlayer() {
        return mPlayer;
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

    @Nullable
    public Track getTrackById(UUID id){
        for(Track p : mTracks){
            if(p.getID().equals(id)) return p;
        }
        return null;
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

    public void playNext(){
        getPlayer().reset();
        mIsPlayerPlaying.setValue(false);
        getPlayer().setAudioStreamType(AudioManager.STREAM_MUSIC);
        int indexCurrent = mTracks.indexOf(getTrackById(mNowPlaying));
        mNowPlaying = mTracks.get((indexCurrent == mTracks.size() - 1 ? -1 : indexCurrent) + 1).getID();
        Track t = getTrackById(mNowPlaying);
        NetHelper.LoadHtmlAsyncTask loadHtmlAsyncTask = new NetHelper.LoadHtmlAsyncTask(getTrackById(mNowPlaying).getLink());
        loadHtmlAsyncTask.addOnLoadedHtmlListener(new NetHelper.LoadHtmlAsyncTask.OnLoadedHtmlListener() {
            @Override
            public void run(final String htmlStr) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Track track = getTrackById(mNowPlaying);

                        try {
                            getPlayer().setDataSource(new JSONObject(htmlStr).getString("url"));
                            getPlayer().prepare();
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                        getPlayer().start();
                        mIsPlayerPlaying.setValue(true);
                    }
                }).start();

            }
        });

        loadHtmlAsyncTask.execute();
    }

    public void playPrevious(){
        getPlayer().reset();
        mIsPlayerPlaying.setValue(false);
        getPlayer().setAudioStreamType(AudioManager.STREAM_MUSIC);

        int indexCurrent = mTracks.indexOf(getTrackById(mNowPlaying));
        mNowPlaying = mTracks.get((indexCurrent == 0 ? mTracks.size() : indexCurrent) - 1).getID();
        NetHelper.LoadHtmlAsyncTask loadHtmlAsyncTask = new NetHelper.LoadHtmlAsyncTask(getTrackById(mNowPlaying).getLink());
        loadHtmlAsyncTask.addOnLoadedHtmlListener(new NetHelper.LoadHtmlAsyncTask.OnLoadedHtmlListener() {
            @Override
            public void run(final String htmlStr) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {


                        Track track = getTrackById(mNowPlaying);

                        try {
                            getPlayer().setDataSource(new JSONObject(htmlStr).getString("url"));
                            getPlayer().prepare();
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                        getPlayer().start();
                        mIsPlayerPlaying.setValue(true);
                    }
                }).start();

            }
        });

        loadHtmlAsyncTask.execute();
    }

    public void playOrPause(){
        if(mNowPlaying == null){
            playNext();
        }
        if(getPlayer().isPlaying()) {
            getPlayer().pause();
            mIsPlayerPlaying.setValue(false);
        }
        else {
            getPlayer().start();
            mIsPlayerPlaying.setValue(true);
        }
    }

    public void playSong(final Track mTrack){
        NetHelper.LoadHtmlAsyncTask loadHtmlAsyncTask = new NetHelper.LoadHtmlAsyncTask(mTrack.getLink());
        loadHtmlAsyncTask.addOnLoadedHtmlListener(new NetHelper.LoadHtmlAsyncTask.OnLoadedHtmlListener() {
            @Override
            public void run(final String htmlStr) {
                getPlayer().reset();
                //mTrackStorage.setIsPlayerPlaying(false);
                getPlayer().setAudioStreamType(AudioManager.STREAM_MUSIC);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String url = new JSONObject(htmlStr).getString("url");
                            getPlayer().setDataSource(url);
                            getPlayer().prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        BottomSheetFragment.mScheduler.schedule(new Runnable() {
                            @Override
                            public void run() {
                                int mCurrentPosition = TrackStorage.getInstance().getPlayer().getCurrentPosition() / 1000;
                                BottomSheetFragment.mMusicSeekBar.setProgress(mCurrentPosition);
                                BottomSheetFragment.mHandler.postDelayed(this, 1000);
                            }
                        }, 1000, TimeUnit.MILLISECONDS);

                        getPlayer().start();
                        setIsPlayerPlaying(true);
                    }
                }).start();
                setNowPlaying(mTrack.getID());
            }
        });


        loadHtmlAsyncTask.execute();
    }

    public interface OnTracksDownloadedListener{
        void run(TrackStorage trackStorage);
    }


    class LoadTracksAsyncTask extends AsyncTask<String, ArrayList<Track>, ArrayList<Track>> {

        @Override
        protected ArrayList<Track> doInBackground(String... strings) {
            String rootUrlStr = "https://zaycev.net";
            String urlStr = "https://zaycev.net/musicset/music2000.shtml";
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

                Elements selectedArray = document.select(".musicset-track-list__items>div");
                int ctr = 0;
                for(Element element : selectedArray){
                    ctr++;
                    String titleAuthor = element.attr("title");
                    if(titleAuthor.isEmpty()) continue;
                    String[] split = titleAuthor.split(" – ");
                    if(ctr == 32)
                        Log.d("Track", titleAuthor);
                    String title = split[1];
                    String authorName = titleAuthor.split(" ")[1];
                    String dataLink = rootUrlStr + element.attr("data-url");
                    if(dataLink.equals(rootUrlStr)) continue;
                    String coverLink = null;
                    int size = element.childNodes().size();
                    if(size >= 4) {
                         coverLink = rootUrlStr + element.child(3).attr("href");
                    }
                    Track track = new Track(title, authorName, dataLink, coverLink);

                    list.add(track);
                }

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
            mNowPlaying = mTracks.get(0).getID();
        }

    }

}
