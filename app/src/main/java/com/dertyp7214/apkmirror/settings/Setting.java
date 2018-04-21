/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Setting {

    protected String text;
    protected String name;
    protected String subTitle;
    protected Context context;
    protected settingsOnClickListener onClickListener;

    public Setting(String name, String text, Context context){
        this.name=name;
        this.text=text;
        this.context=context;
    }

    public Setting setSubTitle(String subTitle){
        this.subTitle=subTitle;
        return this;
    }

    public String getSubTitle(){
        return subTitle!=null?subTitle:"";
    }

    public String getText() {
        return this.text;
    }

    public void saveSetting(){
    }

    public void loadSetting(){
    }

    public Setting addSettingsOnClick(settingsOnClickListener onClickListener){
        this.onClickListener=onClickListener;
        return this;
    }

    public void onClick(TextView subTitle, ProgressBar imageRight){
        if(onClickListener!=null)
            onClickListener.onClick(name, this, subTitle, imageRight);
    }

    public interface settingsOnClickListener{
        void onClick(String name, Setting setting, TextView subTitle, ProgressBar imageRight);
    }
}
