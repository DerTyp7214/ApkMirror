/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Notifications {

    private int id;
    private int max;
    private Context context;
    private String title, subTitle, content;
    private Bitmap icon;
    private boolean progress;
    private NotificationCompat.Builder builder;
    private static NotificationManager notificationManager;
    private static List<Integer> ids = new ArrayList<>();

    public Notifications(Context context, int id, String title, String subTitle, String content, Bitmap icon, boolean progress) {
        this.context = context;
        this.title = title;
        this.subTitle=subTitle;
        this.content = content;
        this.icon = icon;
        this.progress = progress;
        this.max = 100;
        this.id=id;

        if(notificationManager==null)
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = null;
        builder = new NotificationCompat.Builder(context)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setLargeIcon(icon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSubText(subTitle);
        if(progress)
            builder.setSubText("0%");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String groupId = "apkmirror_id";
            CharSequence groupName = "Apkmirror";
            NotificationChannel channel = new NotificationChannel(groupId, groupName, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(groupId);
        }
    }

    public void setSmallIcon(int image){
        builder.setSmallIcon(image);
    }

    public void showNotification(){
        ids.add(ids.size()!=0?ids.get(ids.size()-1)+1:1);
        //id=ids.get(ids.size()-1);
        notificationManager.notify(id, builder.build());
    }

    public void addButton(int icon, String text, Intent intent){
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(icon, text, pendingIntent);
    }

    public void setProgress(int progress){
        if(this.progress){
            //builder.setOngoing(true);
            builder.setSmallIcon(android.R.drawable.stat_sys_download);
            builder.setSubText(progress+"%");
            builder.setProgress(this.max, progress, false);
            notificationManager.notify(id, builder.build());
        }
    }

    public void clearActions() {
        builder.mActions.clear();
    }

    public void removeProgress(){
        builder.setProgress(0, 0, false);
        builder.setSubText(null);
        builder.setOngoing(false);
    }

    public void setFinished(){
        removeProgress();
        clearActions();
        builder.setSubText("Finished");
        builder.setOngoing(false);
        builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
        notificationManager.notify(id, builder.build());
    }

    public void setCanceled(){
        setCanceled("Canceled");
    }

    public void setCanceled(String message){
        removeProgress();
        clearActions();
        builder.setSubText(message);
        builder.setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel);
        Log.d("CANCEL", id+"");
        notificationManager.notify(id, builder.build());
    }

    public void removeNotification(){
        notificationManager.cancel(id);
    }
}
