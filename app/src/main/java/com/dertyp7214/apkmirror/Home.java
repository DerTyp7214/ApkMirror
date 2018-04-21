/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dertyp7214.apkmirror.components.Input;
import com.dertyp7214.apkmirror.notify.Notifications;
import com.dertyp7214.apkmirror.notify.NotificationsAdapter;
import com.dertyp7214.apkmirror.notify.RecyclerItemTouchHelper;
import com.dertyp7214.apkmirror.settings.Setting;
import com.dertyp7214.apkmirror.settings.SettingCheckBox;
import com.dertyp7214.apkmirror.settings.SettingSwitch;
import com.dertyp7214.apkmirror.settings.SettingsAdapter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Home extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    public static ProgressDialog progressDialog, progressDialogApp;
    public static Home instance;

    public NotificationsAdapter notificationsAdapter;

    private Input input;
    private RecyclerView recyclerView, settingList;
    private AppListAdapter appListAdapter;
    private SettingsAdapter settingsAdapter;
    private int site = 1;
    private String querry;
    private boolean loading = false;
    private boolean cancled = false;
    private ProgressDialog updateDialog;
    private View home, dashboard, notifications;

    private String API_TOKEN = "a9fac492a7729222d63c231279c7de3e642a9390";

    private List<Thread> threads = new ArrayList<>();

    private final List<AppListItem> appListItems = new ArrayList<>();
    private final String searchUrl = "https://www.apkmirror.com/?post_type=app_release&searchtype=app&s=";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    home.setVisibility(View.VISIBLE);
                    dashboard.setVisibility(View.INVISIBLE);
                    notifications.setVisibility(View.INVISIBLE);
                    return true;
                case R.id.navigation_dashboard:
                    dashboard.setVisibility(View.VISIBLE);
                    home.setVisibility(View.INVISIBLE);
                    notifications.setVisibility(View.INVISIBLE);
                    return true;
                case R.id.navigation_notifications:
                    notifications.setVisibility(View.VISIBLE);
                    home.setVisibility(View.INVISIBLE);
                    dashboard.setVisibility(View.INVISIBLE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        instance=this;

        home = findViewById(R.id.view_home);
        dashboard = findViewById(R.id.view_dashboard);
        notifications = findViewById(R.id.view_notification);

        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher), getResources().getColor(R.color.colorPrimaryDark)));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if(!Utils.isColorDark(getResources().getColor(R.color.colorPrimaryDark, null)))
                findViewById(R.id.navigation).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            if(!Utils.isColorDark(getResources().getColor(R.color.colorPrimaryDark, null)))
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        askPermissions();
        input = findViewById(R.id.input);

        recyclerView = findViewById(R.id.rv);

        appListAdapter = new AppListAdapter(appListItems, this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(appListAdapter);
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (querry != null && !cancled && !loading)
                    if (querry.length() > 0)
                        loadMore();
            }
        });

        if(getSharedPreferences("settings", MODE_PRIVATE).getBoolean("search_at_start", false)) {
            querry = "google";
            search(querry);
        }

        input.setSubmitListener(new Input.SubmitListener() {
            @Override
            public void onSubmit(String text) {
                querry=text;
                search(querry);
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                assert inputManager != null;
                inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        if(getSharedPreferences("settings", MODE_PRIVATE).getBoolean("colored_navbar", false))
               getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));

        settingList = findViewById(R.id.setting_rv);

        settingsAdapter = new SettingsAdapter(getSettings(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        settingList.setLayoutManager(layoutManager);
        settingList.setItemAnimator(new DefaultItemAnimator());
        settingList.setAdapter(settingsAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(settingList.getContext(), layoutManager.getOrientation());
        settingList.addItemDecoration(dividerItemDecoration);

        setUpNotifications();

    }

    private void setUpNotifications(){
        RecyclerView recyclerView = findViewById(R.id.noti_rv);
        notificationsAdapter = new NotificationsAdapter(this, Notifications.getNotificationsList(this), findViewById(R.id.container));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(notificationsAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    private List<Setting> getSettings() {
        return new ArrayList<>(Arrays.asList(
                new Setting("version", getString(R.string.text_version), this).setSubTitle(BuildConfig.VERSION_NAME),
                new Setting("check_update", getString(R.string.text_check_update), this).setSubTitle(getString(R.string.text_click_check)).addSettingsOnClick(new Setting.settingsOnClickListener() {
                    @Override
                    public void onClick(String name, Setting instance, TextView subTitle, ProgressBar imageRight) {
                        checkForUpdate(instance, subTitle, imageRight);
                    }
                }),
                new SettingCheckBox("search_at_start", getString(R.string.text_search_at_start), this, false),
                new SettingSwitch("colored_navbar", getString(R.string.text_colored_navbar), this, false),
                new SettingSwitch("blur_dialog", getString(R.string.text_blur_dialog), this, false)
        ));
    }

    private void checkForUpdate(final Setting setting, final TextView subTitle, final ProgressBar imageRight) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Home.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageRight.setVisibility(View.VISIBLE);
                            imageRight.setIndeterminate(true);
                        }
                    });
                    JSONObject jsonObject = new JSONObject(getWebContent("https://api.github.com/repos/DerTyp7214/ApkMirror/releases/latest"));
                    String version = jsonObject.getString("tag_name");
                    final String downloadUrl = jsonObject.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
                    Home.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageRight.setVisibility(View.INVISIBLE);
                            imageRight.setIndeterminate(false);
                        }
                    });
                    if(!version.replace("v", "").equals(BuildConfig.VERSION_NAME)){
                        setting.addSettingsOnClick(new Setting.settingsOnClickListener() {
                            @Override
                            public void onClick(String name, Setting setting, TextView subTitle, ProgressBar imageRight) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Looper.prepare();
                                        Home.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                updateDialog = new ProgressDialog(Home.this);
                                                updateDialog.setMessage(getString(R.string.notification_downloading) + " update");
                                                updateDialog.setCancelable(false);
                                                updateDialog.show();
                                            }
                                        });
                                        final File apk = DownloadFile(downloadUrl);
                                        updateDialog.cancel();
                                        Utils.install_apk(apk, Home.this);
                                    }
                                }).start();
                            }
                        });
                        Home.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                subTitle.setText(getString(R.string.text_touch_to_update));
                            }
                        });
                    } else {
                        Home.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                subTitle.setText(getString(R.string.text_latest_version));
                            }
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        }).start();
    }

    private Drawable getProgressBarIndeterminate() {
        final int[] attrs = {android.R.attr.indeterminateDrawable};
        final int attrs_indeterminateDrawable_index = 0;
        TypedArray a = this.obtainStyledAttributes(android.R.style.Widget_ProgressBar, attrs);
        try {
            return a.getDrawable(attrs_indeterminateDrawable_index);
        } finally {
            a.recycle();
        }
    }

    private File DownloadFile(String url){

        try {
            URL u = new URL(url);
            InputStream is = u.openStream();

            DataInputStream dis = new DataInputStream(is);

            byte[] buffer = new byte[1024];
            int length;

            FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/" + ".apkmirror/update.apk"));
            while ((length = dis.read(buffer))>0) {
                fos.write(buffer, 0, length);
            }

            dis.close();
            fos.close();
            is.close();

            return new File(Environment.getExternalStorageDirectory() + "/" + ".apkmirror/update.apk");

        } catch (MalformedURLException mue) {
            Log.e("SYNC getUpdate", "malformed url error", mue);
        } catch (IOException ioe) {
            Log.e("SYNC getUpdate", "io error", ioe);
        } catch (SecurityException se) {
            Log.e("SYNC getUpdate", "security error", se);
        }
        return null;
    }

    private void search(final String querry){
        site=1;
        cancled=true;
        for (Thread thread : threads)
            thread.interrupt();
        threads.clear();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                appListItems.clear();
                if(querry!=null) {
                    if (querry.length() > 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog = new ProgressDialog(Home.this);
                                progressDialog.setMessage(getString(R.string.adapter_loading)+"...");
                                progressDialog.setCancelable(false);
                                try {
                                    progressDialog.show();
                                }catch (Exception ingored){}
                            }
                        });
                        cancled=false;
                        getAppList(querry, site, appListAdapter);
                    }
                }
            }
        });
        thread.start();
        threads.add(thread);
    }

    private void loadMore() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                site++;
                if(site>1)
                    getAppList(querry, site, appListAdapter);
            }
        });
        thread.start();
        threads.add(thread);
    }

    @Nullable
    private String getWebContent(String url) {
        try {
            URL web = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) web.openConnection();
            connection.setRequestProperty("Authorization", "token "+API_TOKEN);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));

            String inputLine;
            StringBuilder ret = new StringBuilder();

            while ((inputLine = in.readLine()) != null)
                ret.append(inputLine);

            in.close();
            return ret.toString();
        } catch (Exception ignored) {
        }
        return null;
    }

    private void askPermissions() {
        ArrayList<String> arrPerm = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            arrPerm.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            arrPerm.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!arrPerm.isEmpty()) {
            String[] permissions = new String[arrPerm.size()];
            permissions = arrPerm.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, 1);
        }
    }

    private void getAppList(String querry, int site, AppListAdapter adapter) {
        //while (loading){}
        loading=true;
        String content = getWebContent(searchUrl + querry + "&page=" + site);
        try {
            String[] elements = content.split("class=\"col-md-8 content-area search-area\"")[1].split("class=\"listWidget\"")[1].split("class=\"OUTBRAIN\"")[0].split("class=\"appRow\"");
            for (String element : elements) {
                try {
                    String url = getWebContent("https://www.apkmirror.com" + element
                            .split("<h5")[1]
                            .split("<a")[1]
                            .split("href=\"")[1]
                            .split("\"")[0])
                            .split("All versions")[1]
                            .split("<h5")[1]
                            .split("<a")[1]
                            .split("href=\"")[1]
                            .split("\"")[0];

                    String title = element
                            .split("<h5")[1]
                            .split("title=\"")[1]
                            .split("\"")[0];

                    String publisher = element
                            .split("class=\"byDeveloper block-on-mobile wrapText\"")[1]
                            .split(">by ")[1]
                            .split("</")[0];

                    URL iconUrl = new URL("https://www.apkmirror.com" + element
                            .split("<img")[1]
                            .split("src=\"")[1]
                            .split("\"")[0]
                            .replace("&w=32&h=32&q=100", ""));

                    if (!cancled)
                        appListItems.add(new AppListItem(url, title, publisher, iconUrl));
                    else
                        Thread.currentThread().interrupt();
                    synchronized (adapter) {
                        adapter.notifyDataSetChanged();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.scrollBy(1, 1);
                            recyclerView.scrollBy(-1, -1);
                            progressDialog.cancel();
                        }
                    });
                } catch (Exception ignored) {
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.cancel();
            }
        });
        loading=false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof NotificationsAdapter.MyViewHolder) {
            String name = Notifications.getNotificationsList(this).get(viewHolder.getAdapterPosition()).TITLE;

            final Notifications deletedItem = Notifications.getNotificationsList(this).get(viewHolder.getAdapterPosition());

            notificationsAdapter.removeItem(viewHolder.getAdapterPosition());
            Notifications.removeNotification(deletedItem, this);
            Snackbar.make(findViewById(R.id.container), name + " "+getString(R.string.text_swipe_snackbar), Snackbar.LENGTH_LONG).show();
        }
    }
}