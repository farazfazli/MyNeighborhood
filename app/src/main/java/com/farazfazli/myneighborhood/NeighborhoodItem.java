package com.farazfazli.myneighborhood;

/**
 * Created by faraz on 2/11/16.
 */
public class NeighborhoodItem {
    private String mTitle;
    private String mMedia;

    public NeighborhoodItem(String title, String media) {
        mTitle = title;
        mMedia = media;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getMedia() {
        return mMedia;
    }

    public void setMedia(String media) {
        mMedia = media;
    }
}