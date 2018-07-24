/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror.notify;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.tech.NfcB;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Toast;

import com.dertyp7214.apkmirror.R;
import com.dertyp7214.apkmirror.components.BottomPopup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static android.content.Context.MODE_PRIVATE;

public class Notifications {

    public final String TITLE, SUBTITLE, TIME;
    public final long UUID;
    private BottomPopup notificationPopup;

    public Notifications(String title, String subTitle, String time) {
        this(title, subTitle, time, new Random().nextLong());
    }

    public Notifications(String title, String subTitle, String time, long UUID) {
        this.TITLE = title;
        this.SUBTITLE = subTitle;
        this.TIME = time;
        this.UUID = UUID;
    }

    public static List<Notifications> getNotificationsList(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("notify", MODE_PRIVATE);
        try {
            List<Notifications> notifications = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(preferences.getString("notiJSON", "[]"));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                notifications.add(new Notifications(object.getString("title"),
                        object.getString("sub_title"), object.getString("time"),
                        Long.parseLong(object.getString("UUID"))));
            }
            return notifications;
        } catch (JSONException ignored) {
        }
        return new ArrayList<>();
    }

    public static void addNotification(Notifications notifications, final Activity context, final RecyclerView.Adapter adapter) {
        addNotification(notifications, context);
        context.runOnUiThread(() -> {
            synchronized (context) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public static void addNotification(Notifications notifications, Context context) {
        List<Notifications> notificationsList = getNotificationsList(context);
        notificationsList.add(notifications);
        setNotifications(notificationsList, context);
    }

    public static void setNotifications(List<Notifications> notifications, Context context) {
        Log.d("NOT", "GEHT======================================================================");
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < notifications.size(); i++) {
            json.append("{\"title\":\"").append(notifications.get(i).TITLE)
                    .append("\",\"sub_title\":\"").append(notifications.get(i).SUBTITLE)
                    .append("\",\"time\":\"").append(notifications.get(i).TIME)
                    .append("\",\"UUID\":\"").append(notifications.get(i).UUID).append("\"}");
            if (i < notifications.size() - 1)
                json.append(",");
            Log.d("JSON", json.toString());
        }
        json.append("]");
        context.getSharedPreferences("notify", MODE_PRIVATE).edit()
                .putString("notiJSON", json.toString()).apply();
    }

    public static void removeNotification(Notifications notifications, Context context) {
        List<Notifications> notificationsList = new ArrayList<>();
        for (Notifications notification : getNotificationsList(context))
            if (notification.UUID != notifications.UUID)
                notificationsList.add(notification);
        setNotifications(notificationsList, context);
    }

    public void onClick(int currentColor, Activity activity, View parent, View root_layout) {
        notificationPopup = new BottomPopup(currentColor, parent, activity,
                activity.getSharedPreferences("settings", MODE_PRIVATE)
                        .getBoolean("blur_dialog", false));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            notificationPopup.setText(Html.fromHtml(
                    "<h2>" + TITLE + "</h2><p>" + SUBTITLE + "</p><p><small>" + TIME
                            + "</small></p>", Html.FROM_HTML_MODE_LEGACY));
        else
            notificationPopup.setText(Html.fromHtml(
                    "<h2>" + TITLE + "</h2><p>" + SUBTITLE + "</p><p><small>" + TIME
                            + "</small></p>"));
        notificationPopup.setUp(root_layout, R.layout.notification_popup);
        notificationPopup.show();
    }
}
