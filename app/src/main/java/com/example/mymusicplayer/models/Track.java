package com.example.mymusicplayer.models;

import android.view.View;
import android.widget.TextView;

import com.example.mymusicplayer.R;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Track {

    private UUID mID;
    private String title;
    private String authorName;
    private String link;
    private String coverLink;

    public Track(String title, String authorName, String link) {
        this.title = title;
        this.authorName = authorName;
        this.link = link;
        mID = UUID.randomUUID();
    }

    public Track(String title, String authorName, String link, String coverLink) {
        this.title = title;
        this.authorName = authorName;
        this.link = link;
        this.coverLink = coverLink;
        mID = UUID.randomUUID();
    }

    public UUID getID() {
        return mID;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getLink() {
        return link;
    }

    public String getCoverLink() {
        return coverLink;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setCoverLink(String coverLink) {
        this.coverLink = coverLink;
    }

}
