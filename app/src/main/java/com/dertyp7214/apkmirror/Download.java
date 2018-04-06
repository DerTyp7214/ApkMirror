/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Download {
    private Activity context;
    private App app;
    private App.Listener listener;
    private static List<Thread> threads = new ArrayList<>();
    private static List<App.Listener> listeners = new ArrayList<>();

    public Download(App app, Activity context, App.Listener listener) {
        this.context = context;
        this.app = app;
        this.listener = listener;
    }

    public int startDownload(final int id) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Utils.BooleanUri uri = Utils.download(threads.size() != 0 ? threads.size() - 1 : 0, app.getApkUrl(), new File(Environment.getExternalStorageDirectory(), ".apkmirror").getAbsolutePath(), listener);
                if (uri.getBoolean()) {
                    listener.onFinish();
                    app.removeDownload(id);
                    install_apk(uri.getUri());
                } else {
                    app.removeDownload(id);
                    listener.onCancel("Connection Error");
                }
            }
        });
        thread.start();
        threads.add(thread);
        listeners.add(listener);
        return threads.size() - 1;
    }

    public static void cancelDownload(int id) {
        threads.get(id).interrupt();
        listeners.get(id).onCancel("Canceled");
    }

    private void install_apk(File file) {
        try {
            if (file.exists()) {
                String[] fileNameArray = file.getName().split(Pattern.quote("."));
                if (fileNameArray[fileNameArray.length - 1].equals("apk")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri downloaded_apk = getFileUri(context, file);
                        Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(downloaded_apk,
                                "application/vnd.android.package-archive");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivityForResult(intent, 1);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file),
                                "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivityForResult(intent, 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Uri getFileUri(Context context, File file) {
        return FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".GenericFileProvider", file);
    }
}