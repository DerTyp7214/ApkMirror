/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import static com.dertyp7214.apkmirror.Utils.apps;

public class AppVersionAdapter extends RecyclerView.Adapter<AppVersionAdapter.MyViewHolder> {

    private List<Version> itemList;
    private Activity context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, date;
        public ImageView icon;
        public RelativeLayout card;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.txt_title);
            date = view.findViewById(R.id.txt_date);
            icon = view.findViewById(R.id.img_icon);
            card = view.findViewById(R.id.card);
        }
    }


    public AppVersionAdapter(List<Version> itemList, Activity context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_version, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final Version listItem = itemList.get(position);
        final ThemeManager themeManager = ThemeManager.getInstance(context);
        themeManager.isDarkTheme();
        holder.title.setTextColor(themeManager.getTitleTextColor());
        holder.date.setTextColor(themeManager.getSubTitleTextColor());
        holder.card.setBackgroundColor(themeManager.getElementColor());
        holder.title.setText(listItem.getTitle());
        holder.date.setText(listItem.getDate());
        holder.icon.setImageBitmap(listItem.getIcon());
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Home.progressDialogApp = new ProgressDialog(context, themeManager.getProgressStyle());
                Home.progressDialogApp.setMessage(context.getString(R.string.adapter_loading) + " " + listItem.getTitle() + "...");
                Home.progressDialogApp.setCancelable(false);
                Home.progressDialogApp.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        if (!apps.containsKey(listItem.getUrl())) {
                            App app = new App(listItem.getUrl(), listItem.getIcon(), context);
                            app.getData(new App.callback() {
                                @Override
                                public void callback(App app) {
                                    apps.put(listItem.getUrl(), app);
                                    context.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            context.startActivity(new Intent(context, MainActivity.class).putExtra("url", listItem.getUrl()).putExtra("icon", listItem.getIcon()));
                                        }
                                    });
                                }
                            });
                        }else {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    context.startActivity(new Intent(context, MainActivity.class).putExtra("url", listItem.getUrl()).putExtra("icon", listItem.getIcon()));
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}