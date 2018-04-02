/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Set;

public class Reciever extends AppCompatActivity {

    public static String ACTION = "action";
    public static String CANCEL_DOWNLOAD = "cancel_download";
    public static String ID = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if(!extras.isEmpty()) {
            String action = extras.getString(ACTION);
            assert action != null;
            if(action.equals(CANCEL_DOWNLOAD)){
                App.cancel(extras.getInt(ID));
                Set<String> keys = extras.keySet();
                for(String key : keys)
                    Log.d("RECIEVER", extras.get(key)+"     "+key);
            }
        }
        finish();
    }
}
