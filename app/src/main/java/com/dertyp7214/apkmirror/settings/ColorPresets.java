/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror.settings;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.hacker.module.colorpicker.Color.Color;
import com.hacker.module.colorpicker.ColorList;
import com.hacker.module.colorpicker.ColorUtil;
import com.hacker.module.colorpicker.R.id;
import com.hacker.module.colorpicker.R.layout;

import java.util.ArrayList;
import java.util.List;

public class ColorPresets extends Dialog {
    private Context c;
    private ColorPicker colorPicker;
    private List<ColorList> colorList;
    private Button btn_ok;
    private Button btn_cancel;
    private Button btn_back;
    private List<View> colorViews = new ArrayList<>();

    public ColorPresets(Context context, List<ColorList> colorList, ColorPicker colorPicker) {
        super(context);
        this.c = context;
        this.colorList = colorList;
        this.colorPicker = colorPicker;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(1);
        this.setContentView(layout.color_presets);
        this.btn_ok = this.findViewById(id.btn_ok);
        this.btn_cancel = this.findViewById(id.btn_cancel);
        this.btn_back = this.findViewById(id.btn_back);
        this.btn_ok.setOnClickListener(v -> {
            colorPicker.actionOK();
            cancel();
        });
        this.btn_cancel.setOnClickListener(v -> {
            colorPicker.actionCancel();
            cancel();
        });
        this.colorViews.add(this.getView(id.colorViewPlate1));
        this.colorViews.add(this.getView(id.colorViewPlate2));
        this.colorViews.add(this.getView(id.colorViewPlate3));
        this.colorViews.add(this.getView(id.colorViewPlate4));
        this.colorViews.add(this.getView(id.colorViewPlate5));
        this.colorViews.add(this.getView(id.colorViewPlate6));
        this.colorViews.add(this.getView(id.colorViewPlate7));
        this.colorViews.add(this.getView(id.colorViewPlate8));
        this.setViews();
    }

    private void setViews() {
        this.btn_back.setOnClickListener(v -> {
            colorPicker.show();
            hide();
        });

        for (int i = 0; i < this.colorList.size(); ++ i) {
            final int x = i;
            ((GradientDrawable) ((LayerDrawable) this.colorViews.get(x).getBackground())
                    .findDrawableByLayerId(id.plate_color)).setColor(
                    this.colorList.get(x).getPrevColor());
            this.colorViews.get(x).setOnClickListener(v -> setViews(colorList.get(x)));
        }

    }

    private void setViews(ColorList colorList) {
        this.btn_back.setOnClickListener(v -> setViews());

        for (int i = 0; i < colorList.getColors().size(); ++ i) {
            final int x = i;
            this.animateViewColor(this.colorViews.get(x), 400, colorList.getColors().get(x));
            this.colorViews.get(x).setOnClickListener(v -> colorPicker.setColor(ColorUtil
                    .getDominantColor(ColorUtil.drawableToBitmap(((LayerDrawable) colorViews.get(x)
                            .getBackground()).findDrawableByLayerId(id.plate_color)))));
        }

    }

    private void animateViewColor(final View view, int time, final int color2) {
        final int color1 = ColorUtil.getDominantColor(ColorUtil.drawableToBitmap(
                ((LayerDrawable) view.getBackground()).findDrawableByLayerId(id.plate_color)));
        ValueAnimator anim = ValueAnimator.ofInt(0, 100);
        anim.setDuration((long) time);
        anim.addUpdateListener(animation -> {
            int animProgress = (Integer) animation.getAnimatedValue();
            ((GradientDrawable) ((LayerDrawable) view.getBackground())
                    .findDrawableByLayerId(id.plate_color))
                    .setColor(ColorUtil.calculateColor(color1, color2, 100, animProgress));
        });
        anim.start();
    }

    public void setTheme(ColorPicker.Theme theme) {
        RelativeLayout ly = this.findViewById(id.ly);
        switch (theme) {
            case LIGHT:
                ly.setBackgroundColor(Color.MATERIAL_LIGHT);
                this.btn_ok.setTextColor(Color.MATERIAL_DARK);
                this.btn_cancel.setTextColor(Color.MATERIAL_DARK);
                this.btn_back.setTextColor(Color.MATERIAL_DARK);
                break;
            case DARK:
                ly.setBackgroundColor(Color.MATERIAL_DARK);
                this.btn_ok.setTextColor(Color.MATERIAL_LIGHT);
                this.btn_cancel.setTextColor(Color.MATERIAL_LIGHT);
                this.btn_back.setTextColor(Color.MATERIAL_LIGHT);
        }

    }

    private View getView(int id) {
        return this.findViewById(id);
    }
}
