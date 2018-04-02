/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

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
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class Utils {

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
}
