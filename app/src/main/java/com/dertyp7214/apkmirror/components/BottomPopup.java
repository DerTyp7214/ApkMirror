/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror.components;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.dertyp7214.apkmirror.R;

import static android.content.Context.MODE_PRIVATE;

public class BottomPopup {

    private View parent, rootLayout;
    private Activity activity;
    private Spanned text;
    private int currentStatusBarColor;
    private PopupWindow popup;
    private Bitmap view, blurOverlay;
    private ScriptIntrinsicBlur script;
    private Allocation output, input;
    private RenderScript rs;
    private boolean blur;
    private View layout;
    private TextView textView;

    public BottomPopup(int currentStatusBarColor, View parent, Activity activity, boolean blur){
        this.parent=parent;
        this.activity=activity;
        this.currentStatusBarColor=currentStatusBarColor;
        this.blur=blur;
    }

    public void setText(String text){
        this.text=Build.VERSION.SDK_INT >= Build.VERSION_CODES.N?Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY):Html.fromHtml(text);
    }

    public void setText(Spanned text){
        this.text=text;
    }

    public void setUp(final View root_layout, int inflateLayout){
        rootLayout = root_layout;

        setUpBitmap();

        blurOverlay=view;

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = inflater.inflate(inflateLayout, (ViewGroup) activity.findViewById(R.id.root_layout));

        if(activity.getSharedPreferences("settings", MODE_PRIVATE).getBoolean("colored_navbar", false)) {
            activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.material_grey_850));
            if (!isColorDark(activity.getResources().getColor(R.color.material_grey_850)) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                parent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        ValueAnimator animator = ValueAnimator.ofInt(1, 80);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                activity.getWindow().setStatusBarColor(MergeColors(currentStatusBarColor, getColor((int) animation.getAnimatedValue())));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int color = getColor((int) animation.getAnimatedValue());
                    float radius = ((Number) animation.getAnimatedValue()).floatValue();
                    if (blur)
                        blurOverlay = blur(view, radius / 4);
                    rootLayout.setForeground(new BitmapDrawable(activity.getResources(),
                            overlay(blurOverlay, createImage(blurOverlay.getWidth(), blurOverlay.getHeight(), color))));
                }
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
                ValueAnimator animator = ValueAnimator.ofInt(80, 1);
                animator.setDuration(500);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        activity.getWindow().setStatusBarColor(MergeColors(currentStatusBarColor, getColor((int) animation.getAnimatedValue())));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if((int) animation.getAnimatedValue() == 1){
                                root_layout.setForeground(new ColorDrawable(0x00000000));
                            }else {
                                int color = getColor((int) animation.getAnimatedValue());
                                float radius = ((Number) animation.getAnimatedValue()).floatValue();
                                if (blur)
                                    blurOverlay = blur(view, radius / 8);
                                rootLayout.setForeground(new BitmapDrawable(activity.getResources(),
                                        overlay(blurOverlay, createImage(blurOverlay.getWidth(), blurOverlay.getHeight(), color))));
                            }
                        }
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
        textView = layout.findViewById(R.id.txt_text);

        setTextViewHTML(textView, text, activity);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });
    }

    public void setOnclick(int view, final ClickListener onClickListener){
        layout.findViewById(view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(v, textView.getText().toString());
            }
        });
    }

    public interface ClickListener{
        void onClick(View root, String text);
    }

    public void show(){
        setUpBitmap();
        popup.showAtLocation(parent, Gravity.CENTER, 0, 0);
    }

    public void dismiss(){
        popup.dismiss();
    }

    public boolean isShowing(){
        return popup != null && popup.isShowing();
    }

    private void setUpBitmap(){
        rootLayout.destroyDrawingCache();
        rootLayout.setDrawingCacheEnabled(true);
        rootLayout.buildDrawingCache();
        Bitmap rootView = rootLayout.getDrawingCache();
        Bitmap background = createImage(rootView.getWidth(), rootView.getHeight(), Color.WHITE);
        view = overlay(background, rootView);

        rs = RenderScript.create(activity);
        input = Allocation.createFromBitmap(rs, view);
        output = Allocation.createTyped(rs, input.getType());
        script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
    }

    private int getColor(int v){
        String value = v+"";
        if(!(value.length() >1))
            value="0"+value;
        return Color.parseColor("#"+value+"000000");
    }

    private Bitmap blur(Bitmap bitmapOriginal, float radius){
        input.copyFrom(bitmapOriginal);
        script.setRadius(radius);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(bitmapOriginal);
        return bitmapOriginal;
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

    private void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span, final Context context){
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(span.getURL()));
                    context.startActivity(browserIntent);
                }catch (Exception e){
                    Toast.makeText(context, context.getString(R.string.popup_error), Toast.LENGTH_LONG).show();
                }
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    private void setTextViewHTML(TextView text, Spanned html, Context context){
        try {
            SpannableStringBuilder strBuilder = new SpannableStringBuilder(html);
            URLSpan[] urls = strBuilder.getSpans(0, html.length(), URLSpan.class);
            for (URLSpan span : urls) {
                makeLinkClickable(strBuilder, span, context);
            }
            text.setText(strBuilder);
            text.setMovementMethod(LinkMovementMethod.getInstance());
        }catch (Exception ignored){}
    }

    private Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }

    private Bitmap createImage(int width, int height, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint);
        return bitmap;
    }

}
