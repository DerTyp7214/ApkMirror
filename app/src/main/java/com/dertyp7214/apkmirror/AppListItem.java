/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.net.URL;

public class AppListItem {

    private String url, title, publisher;
    private URL icon;
    private Bitmap image;

    public AppListItem(String url, String title, String publisher, URL icon){
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

    public Bitmap getIcon(Context context) {
        try {
            if(image==null)
                image=BitmapFactory.decodeStream(icon.openConnection().getInputStream());
            return image;
        } catch (IOException e) {
            return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        }
    }

    public String getPublisher() {
        return publisher;
    }
}
