/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.graphics.Bitmap;

public class AppListItem {

    private String url, title, publisher;
    private Bitmap icon;

    public AppListItem(String url, String title, String publisher, Bitmap icon){
        this.url=url;
        this.title=title;
        this.publisher=publisher;
        this.icon=icon;
    }

    public String getUrl() {
        return "https://www.apkmirror.com"+url;
    }

    public String getTitle() {
        return title;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public String getPublisher() {
        return publisher;
    }
}
