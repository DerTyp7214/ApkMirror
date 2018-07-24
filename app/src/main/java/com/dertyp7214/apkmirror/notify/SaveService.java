/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror.notify;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dertyp7214.apkmirror.Utils;

public class SaveService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("SERVICE", "BIND");
        Bundle bundle = intent.getExtras();
        if (bundle != null)
            save(bundle.getString("title"), bundle.getString("content"));
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void save(String title, String content) {
        Notifications.addNotification(
                new Notifications(title, content, Utils.getCurrentTimeStamp("HH:mm")), this);
    }
}
