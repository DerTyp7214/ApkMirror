/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static com.dertyp7214.apkmirror.Utils.apps;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.MyViewHolder> {

    private List<AppListItem> itemList;
    private Activity context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, publisher;
        public ImageView icon;
        public CardView card;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.txt_title);
            publisher = view.findViewById(R.id.txt_publisher);
            icon = view.findViewById(R.id.img_icon);
            card = view.findViewById(R.id.card);
        }
    }


    public AppListAdapter(List<AppListItem> itemList, Activity context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final AppListItem listItem = itemList.get(position);
        final ThemeManager themeManager = ThemeManager.getInstance(context);
        themeManager.isDarkTheme();
        holder.title.setTextColor(themeManager.getTitleTextColor());
        holder.publisher.setTextColor(themeManager.getSubTitleTextColor());
        holder.card.setCardBackgroundColor(themeManager.getElementColor());
        holder.title.setText(listItem.getTitle());
        holder.publisher.setText(listItem.getPublisher());
        new Thread(() -> {
            final Bitmap bmp = listItem.getIcon(context);
            context.runOnUiThread(() -> holder.icon.setImageBitmap(bmp));
        }).start();
        holder.card.setOnClickListener(v -> {
            Home.progressDialogApp = new ProgressDialog(context, themeManager.getProgressStyle());
            Home.progressDialogApp.setMessage(
                    context.getString(R.string.adapter_loading) + " " + listItem.getTitle()
                            + "...");
            Home.progressDialogApp.setCancelable(false);
            Home.progressDialogApp.show();
            new Thread(() -> {
                Looper.prepare();
                if (! apps.containsKey(listItem.getUrl())) {
                    App app = new App(listItem.getUrl(), listItem.getIcon(context), context);
                    app.getData(app1 -> {
                        apps.put(listItem.getUrl(), app1);
                        context.runOnUiThread(() -> {
                            ActivityOptions options = ActivityOptions
                                    .makeSceneTransitionAnimation(context, holder.icon, "icon");
                            context.startActivity(new Intent(context, MainActivity.class)
                                            .putExtra("url", listItem.getUrl())
                                            .putExtra("icon", listItem.getIcon(context)),
                                    options.toBundle());
                        });
                    });
                } else {
                    context.runOnUiThread(() -> {
                        ActivityOptions options = ActivityOptions
                                .makeSceneTransitionAnimation(context, holder.icon, "icon");
                        context.startActivity(new Intent(context, MainActivity.class)
                                .putExtra("url", listItem.getUrl())
                                .putExtra("icon", listItem.getIcon(context)), options.toBundle());
                    });
                }
            }).start();
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}