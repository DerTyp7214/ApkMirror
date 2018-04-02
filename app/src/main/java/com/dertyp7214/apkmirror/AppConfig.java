/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppConfig {

    public static String MAX_DOWNLOADS = "max_downloads";

    private static AppConfig instance;
    private Context context;
    private SharedPreferences sharedPreferences;

    public AppConfig(Context context){
        instance=this;
        this.context=context;
        this.sharedPreferences=PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static AppConfig getInstance(Context context){
        if(instance==null)
            new AppConfig(context);
        return instance;
    }

    public String getString(String key){
        return sharedPreferences.getString(key, "");
    }

    public void writeString(String key, String value){
        sharedPreferences.edit().putString(key, value).apply();
    }

    public int getInteger(String key){
        return sharedPreferences.getInt(key, 1000);
    }

    public void writeInteger(String key, int value){
        sharedPreferences.edit().putInt(key, value).apply();
    }

}
