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
        holder.title.setText(listItem.getTitle());
        holder.date.setText(listItem.getDate());
        holder.icon.setImageBitmap(listItem.getIcon());
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Home.progressDialog = new ProgressDialog(context);
                Home.progressDialog.setMessage("Loading "+listItem.getTitle()+"...");
                Home.progressDialog.setCancelable(false);
                Home.progressDialog.show();
                context.startActivity(new Intent(context, MainActivity.class).putExtra("url", listItem.getUrl()).putExtra("icon", listItem.getIcon()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}