/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

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

    public void onClick(){
        if(onClickListener!=null)
            onClickListener.onClick(name);
    }

    public interface settingsOnClickListener{
        void onClick(String name);
    }
}
