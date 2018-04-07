/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror.components;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.dertyp7214.apkmirror.R;

import static android.content.Context.MODE_PRIVATE;

public class BottomPopup {

    private View parent;
    private Activity activity;
    private String text;
    private int currentStatusBarColor;
    private PopupWindow popup;

    public BottomPopup(int currentStatusBarColor, View parent, Activity activity){
        this.parent=parent;
        this.activity=activity;
        this.currentStatusBarColor=currentStatusBarColor;
    }

    public void setText(String text){
        this.text=text;
    }

    public void setUp(View root_layout){
        final CoordinatorLayout rootLayout = (CoordinatorLayout) root_layout;

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.description_popup, (ViewGroup) activity.findViewById(R.id.root_layout));

        if(activity.getSharedPreferences("settings", MODE_PRIVATE).getBoolean("colored_navbar", false)) {
            activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.white));
            if (!isColorDark(activity.getResources().getColor(R.color.white)) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                parent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), 0x00000000, 0x80000000);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                activity.getWindow().setStatusBarColor(MergeColors(currentStatusBarColor, (int) animation.getAnimatedValue()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    rootLayout.setForeground(new ColorDrawable((int) animation.getAnimatedValue()));
            }
        });
        animator.start();

        popup = new PopupWindow(layout);
        popup.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        popup.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        popup.setAnimationStyle(R.style.Animation);
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), 0x80000000, 0x00000000);
                animator.setDuration(500);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        activity.getWindow().setStatusBarColor(MergeColors(currentStatusBarColor, (int) animation.getAnimatedValue()));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            rootLayout.setForeground(new ColorDrawable((int) animation.getAnimatedValue()));
                    }
                });
                animator.start();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
                if(activity.getSharedPreferences("settings", MODE_PRIVATE).getBoolean("colored_navbar", false)) {
                    activity.getWindow().setNavigationBarColor(currentStatusBarColor);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        parent.setSystemUiVisibility(View.VISIBLE);
                }
            }
        });

        Button close = layout.findViewById(R.id.btn_close);
        TextView description = layout.findViewById(R.id.txt_big_description);

        description.setText(text);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
    }

    public void show(){
        popup.showAtLocation(parent, Gravity.CENTER, 0, 0);
    }

    public void dismiss(){
        popup.dismiss();
    }

    public boolean isShowing(){
        return popup != null && popup.isShowing();
    }

    private int MergeColors(int backgroundColor, int foregroundColor) {
        final byte ALPHA_CHANNEL = 24;
        final byte RED_CHANNEL   = 16;
        final byte GREEN_CHANNEL =  8;
        final byte BLUE_CHANNEL  =  0;

        final double ap1 = (double)(backgroundColor >> ALPHA_CHANNEL & 0xff) / 255d;
        final double ap2 = (double)(foregroundColor >> ALPHA_CHANNEL & 0xff) / 255d;
        final double ap = ap2 + (ap1 * (1 - ap2));

        final double amount1 = (ap1 * (1 - ap2)) / ap;
        final double amount2 = amount1 / ap;

        int a = ((int)(ap * 255d)) & 0xff;

        int r = ((int)(((float)(backgroundColor >> RED_CHANNEL & 0xff )*amount1) +
                ((float)(foregroundColor >> RED_CHANNEL & 0xff )*amount2))) & 0xff;
        int g = ((int)(((float)(backgroundColor >> GREEN_CHANNEL & 0xff )*amount1) +
                ((float)(foregroundColor >> GREEN_CHANNEL & 0xff )*amount2))) & 0xff;
        int b = ((int)(((float)(backgroundColor & 0xff )*amount1) +
                ((float)(foregroundColor & 0xff )*amount2))) & 0xff;

        return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL | b << BLUE_CHANNEL;
    }

    private boolean isColorDark(int color){
        double darkness = 1-(0.299* Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        return !(darkness < 0.5);
    }

}
