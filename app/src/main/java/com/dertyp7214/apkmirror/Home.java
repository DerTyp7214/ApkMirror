/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.apkmirror;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.dertyp7214.apkmirror.settings.Setting;
import com.dertyp7214.apkmirror.settings.SettingCheckBox;
import com.dertyp7214.apkmirror.settings.SettingColor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Home extends AppCompatActivity {

    public static ProgressDialog progressDialog;

    private RecyclerView recyclerView, settingList;
    private AppListAdapter appListAdapter;
    private SettingsAdapter settingsAdapter;
    private int site = 1;
    private String querry;
    private boolean loading = false;
    private boolean cancled = false;

    private View home, dashboard;

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
                    return true;
                case R.id.navigation_dashboard:
                    dashboard.setVisibility(View.VISIBLE);
                    home.setVisibility(View.INVISIBLE);
                    return true;
                case R.id.navigation_notifications:
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

        home = findViewById(R.id.view_home);
        dashboard = findViewById(R.id.view_dashboard);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        askPermissions();

        final TextInputEditText inputEditText = findViewById(R.id.input_querry);
        final Button search = findViewById(R.id.btn_send);

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

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                querry=inputEditText.getText().toString();
                search(querry);
            }
        });

        inputEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_SEARCH){
                    querry=v.getText().toString();
                    search(querry);
                }
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                assert inputManager != null;
                inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                inputEditText.clearFocus();
                return true;
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
    }

    private List<Setting> getSettings() {
        return new ArrayList<>(Arrays.asList(
                new Setting("version", "Version: " + BuildConfig.VERSION_NAME, this),
                new SettingCheckBox("search_at_start", "Show Googleapps on start", this, false),
                new SettingCheckBox("colored_navbar", "Colored Navigationbar", this, false)
        ));
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
                                progressDialog.setMessage("Loading...");
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

    private String getWebContent(String url) {
        try {
            URL web = new URL(url);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            web.openStream()));

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

                    Bitmap icon = BitmapFactory.decodeStream(iconUrl.openConnection().getInputStream());
                    if (!cancled)
                        appListItems.add(new AppListItem(url, title, publisher, icon));
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
}