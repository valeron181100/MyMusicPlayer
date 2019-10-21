package com.example.mymusicplayer.models;

import android.os.AsyncTask;

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

import androidx.annotation.Nullable;

public class NetHelper {


    static class LoadHtmlAsyncTask extends AsyncTask<String, String, String> {

        private String url;
        private ArrayList<OnLoadedHtmlListener> listeners;


        public LoadHtmlAsyncTask(String url) {
            this.url = url;
            listeners = new ArrayList<>();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(this.url);
                URLConnection connection = url.openConnection();

                InputStream stream = connection.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buff = new byte[1024];

                while (stream.read(buff) != -1) {
                    baos.write(buff);
                }
                String xmlStr = new String(baos.toByteArray());
                return xmlStr;
            }catch (IOException e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            for(OnLoadedHtmlListener p : listeners){
                if(p != null)
                    p.run(s);
            }
        }

        public void addOnLoadedHtmlListener(OnLoadedHtmlListener listener){
            listeners.add(listener);
        }

        interface OnLoadedHtmlListener{
            void run(String htmlStr);
        }

    }
}
