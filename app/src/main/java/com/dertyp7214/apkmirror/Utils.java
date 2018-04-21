/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class Utils {

    public static HashMap<String, App> apps = new HashMap<>();

    public static BooleanUri download(int id, String url, String path, App.Listener listener){

        try {

            File dir = new File(path);

            URL fileurl = new URL(url);
            URLConnection urlConnection = fileurl.openConnection();
            urlConnection.connect();

            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream(), 8192);

            if (!dir.exists()) {

                dir.mkdir();

            }

            File downloadedfile = new File(dir, "download_app_"+id+".apk");

            OutputStream outputStream = new FileOutputStream(downloadedfile);

            byte[] buffer = new byte[4096];

            int fileSize = urlConnection.getContentLength();
            int read;
            long total = 0;

            listener.onStart();

            while ((read = inputStream.read(buffer)) != -1) {

                total += read;

                if(fileSize > 0) {
                    listener.onUpdate((int) (total * 100 / fileSize));
                }

                outputStream.write(buffer, 0, read);

            }

            listener.onFinish();

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            listener.onFinish();

            return new BooleanUri(true, downloadedfile);

        } catch (IOException e) {

            e.printStackTrace();

            listener.onCancel("Connection error");

            return new BooleanUri(false, null);
        }
    }

    public static class BooleanUri{
        private boolean value;
        private File uri;
        public BooleanUri(boolean value, File uri){
            this.value=value;
            this.uri=uri;
        }
        public File getUri() {
            return uri;
        }
        public boolean getBoolean(){
            return value;
        }
    }

    public static void saveHashMap(String fileName, HashMap<String, App> map){
        try {
            File dir = new File(Environment.getExternalStorageDirectory(), ".apkmirror");
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir, fileName);
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(map);
            outputStream.flush();
            outputStream.close();
        }catch (Exception ignored){}
    }

    public static HashMap<String, App> loadHashMap(String fileName, HashMap hashMap){
        try{
            File dir = new File(Environment.getExternalStorageDirectory(), ".apkmirror");
            File file = new File(dir, fileName);
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            HashMap<String, App> map = (HashMap<String, App>) inputStream.readObject();
            inputStream.close();
            return map;
        }catch (Exception ignored){
            return hashMap;
        }
    }

    public static Bitmap getQrCode(String data, Context context){
        String url = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data="+data;
        try {
            return new GetBitmapFromUrl().execute(url).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.apkmirror_qr);
        }
    }

    public static class GetBitmapFromUrl extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                return BitmapFactory.decodeStream(url.openConnection().getInputStream());
            }catch (Exception e){
                return null;
            }
        }

        protected void onPostExecute(String string){

        }
    }

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }

    public static void SlideToAbove(final RelativeLayout footer) {
        Animation slide = null;
        slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, -5.0f);

        slide.setDuration(400);
        slide.setFillAfter(true);
        slide.setFillEnabled(true);
        footer.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                footer.clearAnimation();

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        footer.getWidth(), footer.getHeight());
                // lp.setMargins(0, 0, 0, 0);
                lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                footer.setLayoutParams(lp);

            }

        });

    }

    public static void SlideToDown(final RelativeLayout footer) {
        Animation slide = null;
        slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 5.2f);

        slide.setDuration(400);
        slide.setFillAfter(true);
        slide.setFillEnabled(true);
        footer.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                footer.clearAnimation();

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        footer.getWidth(), footer.getHeight());
                lp.setMargins(0, footer.getWidth(), 0, 0);
                lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                footer.setLayoutParams(lp);

            }

        });

    }

    public static boolean isColorDark(int color){
        double darkness = 1-(0.299* Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        if(darkness<0.5){
            return false; // It's a light color
        }else{
            return true; // It's a dark color
        }
    }

    public static boolean hasNavBar (Resources resources) {
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && resources.getBoolean(id);
    }

    public static int MergeColors(int backgroundColor, int foregroundColor) {
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

    private static void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span, final Context context){
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

    public static void setTextViewHTML(TextView text, Spanned html, Context context){
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

    public static String getCurrentTimeStamp(String format){
        try {

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);

            return dateFormat.format(new Date());
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public static void install_apk(File file, Context context) {
        try {
            if (file.exists()) {
                String[] fileNameArray = file.getName().split(Pattern.quote("."));
                if (fileNameArray[fileNameArray.length - 1].equals("apk")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri downloaded_apk = getFileUri(context, file);
                        Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(downloaded_apk,
                                "application/vnd.android.package-archive");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file),
                                "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Uri getFileUri(Context context, File file) {
        return FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".GenericFileProvider", file);
    }
}
