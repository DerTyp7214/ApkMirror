/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror.notify;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

import com.dertyp7214.apkmirror.Home;
import com.dertyp7214.apkmirror.R;
import com.dertyp7214.apkmirror.Utils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCM_Messaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getData().get("title");
        String content = remoteMessage.getData().get("content");

        Notifications.addNotification(new Notifications(title, content, Utils.getCurrentTimeStamp("HH:mm")), this);
        if (Home.instance != null) {
            com.dertyp7214.apkmirror.Notifications notification = new com.dertyp7214.apkmirror.Notifications(getApplicationContext(), 1, title, title, content, null, false);
            notification.setSmallIcon(R.mipmap.ic_launcher_foreground);
            notification.showNotification();
            Home.instance.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Home.instance.notificationsAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}
