/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror.notify;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dertyp7214.apkmirror.R;
import com.dertyp7214.apkmirror.ThemeManager;

import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.MyViewHolder> {
    private Activity activity;
    private List<Notifications> notificationsList;
    private View root_layout;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, content, time;
        public RelativeLayout viewBackground, viewForeground;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.txt_title);
            content = view.findViewById(R.id.txt_content);
            time = view.findViewById(R.id.txt_time);
            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);
        }
    }


    public NotificationsAdapter(Activity activity, List<Notifications> notificationsList, View root_layout) {
        this.activity = activity;
        this.notificationsList = notificationsList;
        this.root_layout = root_layout;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_tile, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final Notifications item = notificationsList.get(position);
        final ThemeManager themeManager = ThemeManager.getInstance(activity);
        themeManager.isDarkTheme();
        holder.title.setTextColor(themeManager.getTitleTextColor());
        holder.content.setTextColor(themeManager.getSubTitleTextColor());
        holder.time.setTextColor(themeManager.getSubTitleTextColor());
        holder.viewForeground.setBackgroundColor(themeManager.getElementColor());
        holder.title.setText(item.TITLE);
        holder.content.setText(item.SUBTITLE);
        holder.time.setText(item.TIME);

        holder.viewForeground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.onClick(activity.getResources().getColor(R.color.colorPrimaryDark), activity, holder.viewForeground, root_layout);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public void removeItem(int position) {
        notificationsList.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    public void restoreItem(Notifications item, int position) {
        notificationsList.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }
}