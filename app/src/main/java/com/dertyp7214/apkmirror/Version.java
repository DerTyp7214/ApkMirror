/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.graphics.Bitmap;

import org.apache.commons.lang3.StringEscapeUtils;

public class Version {

    private String title, date, url;
    private Bitmap icon;

    public Version(String title, String date, Bitmap icon, String url){
        this.title=title;
        this.date=date;
        this.icon=icon;
        this.url=url;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public String getDate() {
        return StringEscapeUtils.unescapeHtml4(date);
    }

    public String getTitle() {
        return StringEscapeUtils.unescapeHtml4(title);
    }

    public String getUrl() {
        return url;
    }
}
