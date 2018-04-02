/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class SettingCheckBox extends Setting {

    private boolean checked;

    public SettingCheckBox(String name, String text, Context context, boolean checked) {
        super(name, text, context);
        this.checked=checked;
        loadSetting();
    }

    public boolean isChecked(){
        return this.checked;
    }

    public void setChecked(boolean checked){
        this.checked=checked;
        saveSetting();
    }

    @Override
    public void saveSetting(){
        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits")
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(name, checked);
        editor.apply();
    }

    @Override
    public void loadSetting(){
        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        this.checked=preferences.getBoolean(name, checked);
    }
}
