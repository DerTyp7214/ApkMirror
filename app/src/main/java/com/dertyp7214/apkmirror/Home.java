/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import com.dertyp7214.apkmirror.components.BottomPopup;
import com.dertyp7214.apkmirror.components.Input;
import com.dertyp7214.apkmirror.components.InputDialog;
import com.dertyp7214.apkmirror.notify.Notifications;
import com.dertyp7214.apkmirror.notify.NotificationsAdapter;
import com.dertyp7214.apkmirror.notify.RecyclerItemTouchHelper;
import com.dertyp7214.apkmirror.settings.Setting;
import com.dertyp7214.apkmirror.settings.SettingCheckBox;
import com.dertyp7214.apkmirror.settings.SettingPlaceholder;
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
import java.util.Random;

public class Home extends AppCompatActivity implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    public static ProgressDialog progressDialog, progressDialogApp;
    public static Home instance;

    public NotificationsAdapter notificationsAdapter;

    private Input input;
    private RecyclerView recyclerView, settingList, notiRecyclerView;
    private AppListAdapter appListAdapter;
    private SettingsAdapter settingsAdapter;
    private int site = 1;
    private String querry;
    private boolean loading = false;
    private boolean cancled = false;
    private ProgressDialog updateDialog;
    private View home, dashboard, notifications;
    private ThemeManager themeManager;

    private String API_TOKEN;

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

        API_TOKEN=getSharedPreferences("settings", MODE_PRIVATE).getString("API_KEY", "NULL");

        home = findViewById(R.id.view_home);
        dashboard = findViewById(R.id.view_dashboard);
        notifications = findViewById(R.id.view_notification);



        setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name), BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher), getResources().getColor(R.color.colorPrimaryDark)));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Utils.isColorDark(getResources().getColor(R.color.colorPrimaryDark, null))) {
                findViewById(R.id.navigation).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                findViewById(R.id.navigation).setSystemUiVisibility(View.VISIBLE);
            }
        }

        setUpNavigationBar();

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
        setUpTheme();

    }

    private void setUpTheme(){
        themeManager = ThemeManager.getInstance(this);
        themeManager.isDarkTheme();
        ((BottomNavigationView) findViewById(R.id.navigation)).setItemTextColor(themeManager.getNavigationColors());
        ((BottomNavigationView) findViewById(R.id.navigation)).setItemIconTintList(themeManager.getNavigationColors());
        List<View> views = new ArrayList<>();
        views.add(findViewById(R.id.container));

        for(View v : views)
            v.setBackgroundColor(themeManager.getBackgroundColor());

        refreshRecyclerView(recyclerView);
        refreshRecyclerView(settingList);
        refreshRecyclerView(notiRecyclerView);
    }

    private void refreshRecyclerView(RecyclerView recyclerView){
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scrollBy(1, 1);
        recyclerView.scrollBy(-1, -1);
    }

    private void setUpNavigationBar() {
        int colorFrom = getWindow().getNavigationBarColor();
        int colorTo = getResources().getColor(R.color.colorPrimary);
        if (!getSharedPreferences("settings", MODE_PRIVATE).getBoolean("colored_navbar", false))
            colorTo = Color.BLACK;
        ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(),colorFrom, colorTo);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getWindow().setNavigationBarColor((int) animation.getAnimatedValue());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!Utils.isColorDark((int) animation.getAnimatedValue())) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                    } else {
                        getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
                    }
                }
            }
        });
        animator.start();
    }

    private void setUpNotifications(){
        notiRecyclerView = findViewById(R.id.noti_rv);
        notificationsAdapter = new NotificationsAdapter(this, Notifications.getNotificationsList(this), findViewById(R.id.container));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        notiRecyclerView.setLayoutManager(layoutManager);
        notiRecyclerView.setItemAnimator(new DefaultItemAnimator());
        notiRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notiRecyclerView.setAdapter(notificationsAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    private List<Setting> getSettings() {
        return new ArrayList<>(Arrays.asList(
                new SettingPlaceholder("appdetails", getString(R.string.text_appdetails), this),
                new Setting("version", getString(R.string.text_version), this).setSubTitle(BuildConfig.VERSION_NAME),
                new Setting("check_update", getString(R.string.text_check_update), this).setSubTitle(getString(R.string.text_click_check)).addSettingsOnClick(new Setting.settingsOnClickListener() {
                    @Override
                    public void onClick(String name, Setting instance, TextView subTitle, ProgressBar imageRight) {
                        checkForUpdate(instance, subTitle, imageRight);
                    }
                }),
                new Setting("sourcecode", "Sourcecode", this).setSubTitle(getString(R.string.text_sourcecode)).addSettingsOnClick(new Setting.settingsOnClickListener() {
                    @Override
                    public void onClick(String name, Setting setting, TextView subTitle, ProgressBar imageRight) {
                        openUrl("http://github.com/DerTyp7214/ApkMirror");
                    }
                }),
                new SettingPlaceholder("preferences", getString(R.string.text_prefs), this),
                new SettingCheckBox("search_at_start", getString(R.string.text_search_at_start), this, false),
                new SettingSwitch("colored_navbar", getString(R.string.text_colored_navbar), this, false).setCheckedChangeListener(new SettingSwitch.CheckedChangeListener() {
                    @Override
                    public void onChangeChecked(boolean value) {
                        Home.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setUpNavigationBar();
                            }
                        });
                    }
                }),
                new SettingSwitch("dark_theme", getString(R.string.text_dark_theme), this, false).setCheckedChangeListener(new SettingSwitch.CheckedChangeListener() {
                    @Override
                    public void onChangeChecked(final boolean value) {
                        Home.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
                                preferences.edit().putBoolean("dark_theme", value).apply();
                                setUpTheme();
                            }
                        });
                    }
                }),
                new SettingSwitch("blur_dialog", getString(R.string.text_blur_dialog), this, false),
                new Setting("api_key", getString(R.string.text_api_key), this).setSubTitle(Utils.cutString(getSharedPreferences("settings", MODE_PRIVATE).getString("API_KEY", getString(R.string.text_not_set)), 30)).addSettingsOnClick(new Setting.settingsOnClickListener() {
                    @Override
                    public void onClick(String name, Setting setting, final TextView subTitle, ProgressBar imageRight) {
                        InputDialog dialog = new InputDialog(getString(R.string.text_api_key), getSharedPreferences("settings", MODE_PRIVATE).getString("API_KEY", ""), getString(R.string.text_api_key), Home.this);
                        dialog.setListener(new InputDialog.Listener() {
                            @Override
                            public void onSubmit(String text) {
                                getSharedPreferences("settings", MODE_PRIVATE).edit().putString("API_KEY", text).apply();
                                subTitle.setText(Utils.cutString(getSharedPreferences("settings", MODE_PRIVATE).getString("API_KEY", getString(R.string.text_not_set)), 30));
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                        dialog.show();
                    }
                }),
                new SettingPlaceholder("social", getString(R.string.text_social), this),
                new Setting("github", "GitHub", this).setSubTitle(getString(R.string.text_github)).addSettingsOnClick(new Setting.settingsOnClickListener() {
                    @Override
                    public void onClick(String name, Setting setting, TextView subTitle, ProgressBar imageRight) {
                        openUrl("http://github.com/DerTyp7214");
                    }
                }),
                new Setting("googleplus", "Google+", this).setSubTitle(getString(R.string.text_googleplus)).addSettingsOnClick(new Setting.settingsOnClickListener() {
                    @Override
                    public void onClick(String name, Setting setting, TextView subTitle, ProgressBar imageRight) {
                        openUrl("http://plus.google.com/u/0/116183493734176118582");
                    }
                }),
                new Setting("donate", getString(R.string.text_donate), this).setSubTitle(getString(R.string.text_donate_sub)).addSettingsOnClick(new Setting.settingsOnClickListener() {
                    @Override
                    public void onClick(String name, Setting setting, TextView subTitle, ProgressBar imageRight) {
                        openUrl("https://paypal.me/JosuaLengwenath");
                    }
                })
        ));
    }

    private void openUrl(String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
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
                                                updateDialog = new ProgressDialog(Home.this, themeManager.getProgressStyle());
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
                                progressDialog = new ProgressDialog(Home.this, themeManager.getProgressStyle());
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
            BufferedReader in;
            if(!API_TOKEN.equals("NULL"))
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            else
                in = new BufferedReader(new InputStreamReader(web.openStream()));

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