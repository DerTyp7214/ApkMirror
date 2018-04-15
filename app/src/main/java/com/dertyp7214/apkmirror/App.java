/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class App {

    private String url;
    private String title, publisher, version, packageName, size, apkUrl;
    private Spanned description;
    private String content;
    private Bitmap background, appIcon;
    private Context context;
    private AppConfig appConfig;
    private int appColor;
    private String baseUrl = "https://www.apkmirror.com";
    private static List<Integer> download_id = new ArrayList<>();

    public App(String url, Context context){
        App(url, null, context);
    }

    public App(String url, Bitmap icon, Context context){
        App(url, icon, context);
    }

    private void App(String url, Bitmap icon, Context context){
        this.url=url;
        this.background=icon;
        this.appConfig=AppConfig.getInstance(context);
        this.context=context;
        if(download_id.size()<appConfig.getInteger(AppConfig.MAX_DOWNLOADS)){
            for(int i=0;i<appConfig.getInteger(AppConfig.MAX_DOWNLOADS);i++)
                download_id.add(0);
        }
        try {
            content = getContent(url);
            if(content.contains("class=\"widgetHeader\">Download")||(!content.contains("class=\"apk-detail-table\""))){
                String u = "";
                try {
                    String[] versions = content
                            .split("class=\"widgetHeader\">Download")[1]
                            .split("class=\"table topmargin variants-table\"")[1]
                            .split("class=\"table-row headerFont\"");
                    for (String ver : versions) {
                        if ((ver.contains("nodpi") || ver.contains("480dpi")) && (ver.contains("arm") || ver.contains("universal") ||ver.contains("noarch"))) {
                            u = baseUrl + ver.split("<a")[1].split("href=\"")[1].split("\"")[0];
                            break;
                        }
                    }
                }catch (Exception e){
                    u = baseUrl+content
                            .split("class=\"widgetHeader\">All versions")[1]
                            .split("<h5")[1]
                            .split("href=\"")[1]
                            .split("\"")[0];
                }
                if(u.length()>1)
                    content = getContent(u);
            }
        }catch (WrongUriException exeption){
            content="";
            Log.d("WrongUriException", exeption.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getData() {
        try {
            Bundle ApkDetails = getApkDetails();
            Bundle SiteTitleBar = getSiteTitleBar();
            this.title = SiteTitleBar.getString("title");
            this.publisher = SiteTitleBar.getString("publisher");
            this.version = ApkDetails.getString("version");
            //this.size=ApkDetails.getString("size");
            this.description = getDescriptionText();
            this.appIcon = getBitmap(SiteTitleBar.getString("iconUrl"));
            this.apkUrl=getApkPath();
            this.packageName=ApkDetails.getString("package");
            this.appColor = isColorDark(getDominantColor(getAppIcon()))?getDominantColor(getAppIcon()):manipulateColor(getDominantColor(getAppIcon()), 0.8F);
        } catch (Exception | WrongUriException e) {
            e.printStackTrace();
        }
    }

    public void getData(callback callback){
        getData();
        callback.callback(this);
    }

    interface callback{
        void callback(App app);
    }

    private String getApkPath() throws Exception, WrongUriException {
        String url = content.split("btn btn-flat downloadButton")[1].split("href=\"")[1].split("\"")[0];
        if(url.startsWith("/wp-content"))
            return baseUrl+url;
        String content = getContent(baseUrl+url);
        String apkUrl = content.split("nofollow")[1].split("href=\"")[1].split("\"")[0];
        return baseUrl+apkUrl;
    }

    private Spanned getDescriptionText() throws Exception{
        return fromHtml(content.split("id=\"description\">")[1]
                .split("notes\">")[1]
                .split("</div")[0]);
    }

    private Spanned fromHtml(String string){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
            return Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY);
        else
            return Html.fromHtml(string);
    }

    private Bundle getApkDetails()throws Exception{
        Bundle bundle = new Bundle();

        String apk_detail_table = content.split("class=\"apk-detail-table\"")[1];

        String appspec = apk_detail_table.split("appspec-value")[1];
        String version = appspec.split("Version: ")[1].split("<")[0].split("\"")[0];
        String packageName = appspec.split("Package: ")[1].split("</")[0];
        //String size = apk_detail_table.split("APK file size")[1].split("appspec-value\">")[1].split(" \\(")[0];

        bundle.putString("version", version);
        bundle.putString("package", packageName);
        //bundle.putString("size", size);

        return bundle;
    }

    private boolean isColorDark(int color){
        double darkness = 1-(0.299* Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        if(darkness<0.5){
            return false; // It's a light color
        }else{
            return true; // It's a dark color
        }
    }

    private int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }

    private Bundle getSiteTitleBar()throws Exception{
        Bundle bundle = new Bundle();

        String table_cell = content.split("class=\"siteTitleBar\"")[1];
        String appIconUrl = table_cell.split("<img")[1].split("src=\"")[1].split("\"")[0];
        String title = table_cell.split("<h1 title=\"")[1].split("\">")[1].split("</")[0];
        String publisher = table_cell.split("<h3 title=\"")[1].split("<a")[1].split(">")[1].split("</")[0];

        bundle.putString("iconUrl", baseUrl+appIconUrl);
        bundle.putString("title", title);
        bundle.putString("publisher", publisher);

        return bundle;
    }

    public App(App app){
        this.url=app.url;
        this.context=app.context;
        this.content=app.content;
        this.title=app.title;
        this.background=app.background;
    }

    public static void cancel(int id){
        try {
            Download.cancelDownload(download_id.get(id));
            download_id.remove(id);
        }catch (Exception ignored){}
    }

    public void removeDownload(int id) {
        try {
            List<Integer> tmp = new ArrayList<>();
            for(int i=0;i<tmp.size();i++)
                if(i!=id)
                    tmp.add(download_id.get(i));
            download_id.clear();
            download_id.addAll(tmp);

        } catch (Exception ignored) {}
    }

    public String getUrl() {
        return this.url;
    }

    public String getSize() {
        return this.size;
    }

    public String getVersion() {
        return StringEscapeUtils.unescapeHtml4(this.version);
    }

    public String getPackageName() {
        return this.packageName;
    }

    public Spanned getDescription(){
        return this.description;
    }

    public String getTitle(){
        return StringEscapeUtils.unescapeHtml4(this.title);
    }

    public String getPublisher(){
        return StringEscapeUtils.unescapeHtml4(this.publisher);
    }

    public Bitmap getAppIcon(){
        return this.appIcon;
    }

    public int getAppColor(){
        return this.appColor;
    }

    public void setAppColor(int color){
        this.appColor=color;
    }

    public void getVersions(final LoadListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Version> list = new ArrayList<>();

                String listContent;
                try {
                    listContent = content.split("All versions")[1].split("class=\"listWidget\"")[0];
                } catch (Exception e) {
                    listContent = content.split("Previous APKs")[1].split("class=\"listWidget\"")[0];
                }

                String[] listEntrys = listContent.split("class=\"appRow\"");

                for (String entry : listEntrys) {
                    try {
                        String url = entry.split("<h5")[1].split("<a")[1].split("href=\"")[1].split("\"")[0];
                        String title = entry.split("<h5")[1].split("title=\"")[1].split("\"")[0];
                        String date = entry.split("class=\"dateyear_utc\"")[1].split(">")[1].split("</")[0];
                        list.add(new Version(title, date, appIcon, baseUrl + url));
                    } catch (Exception ignored) {
                    }
                }

                listener.onLoadFinish(list);
            }
        }).start();
    }

    public void download(Activity context, int id, Listener listener){
        if(download_id.size()<=appConfig.getInteger(AppConfig.MAX_DOWNLOADS)&&id<download_id.size()) {
            Download download = new Download(this, context, listener);
            download_id.set(id, download.startDownload(id));
            Log.d("DOWNLOAD_ID", download_id.get(id)+"");
        } else {
            listener.onCancel(context.getString(R.string.notification_max_downloads));
        }
    }

    public interface Listener{
        void onUpdate(int progress);
        void onFinish();
        void onStart();
        void onCancel(String message);
    }

    private String getContent(String url) throws WrongUriException{
        try {
            String data = new GetContentFromUrl().execute(url).get();
            if(data==null)
                throw new WrongUriException();
            return data;
        } catch (Exception e) {
            throw new WrongUriException();
        }
    }

    private int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    private Bitmap getBitmap(String url){
        if(this.background!=null)
            return this.background;
        if(url==null)
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.header);
        try {
            Bitmap data = new GetBitmapFromUrl().execute(url).get();
            if(data==null)
                return null;
            this.background=data;
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    public String getApkUrl(){
        return this.apkUrl;
    }

    static class GetContentFromUrl extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(urls[0]);
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null)
                    sb.append(line);

                String resString = sb.toString();

                is.close();
                return  resString;
            }catch (Exception e){
                return null;
            }
        }

        protected void onPostExecute(String string){

        }
    }

    static class GetBitmapFromUrl extends AsyncTask<String, Void, Bitmap> {

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

    private static class WrongUriException extends Throwable {
        public String getMessage() {
            return "Wrong Uri used!";
        }
    }

    public interface LoadListener{
        void onLoadFinish(List<Version> versions);
    }
}
