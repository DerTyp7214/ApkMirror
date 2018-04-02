package com.dertyp7214.apkmirror;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private App app;
    private TextView publisher, description, version;
    private ImageView appIcon, appBackground;
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;
    private boolean appBarExpanded;
    private Menu collapsedMenu;
    private Button open, uninstall;
    private String packageName;
    private PackageManager packageManager;
    private List<Version> versions = new ArrayList<>();
    private RecyclerView recyclerView;
    private AppVersionAdapter appVersionAdapter;
    private static int notifyId;
    private static HashMap<String, App> apps = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        collapsingToolbar = findViewById(R.id.toolbar_layout);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appBackground = findViewById(R.id.app_background);
        appIcon = findViewById(R.id.app_icon);
        open = findViewById(R.id.btn_open);
        uninstall = findViewById(R.id.btn_remove);
        publisher = findViewById(R.id.txt_publisher);
        description = findViewById(R.id.txt_description);
        version = findViewById(R.id.txt_ver);

        Bundle data = getIntent().getExtras();

        if(notifyId<1&&notifyId!=0)
            notifyId=0;

        assert data != null;
        if(data.isEmpty()){
            super.onBackPressed();
        }

        if(apps.containsKey(data.getString("url"))){
            app=apps.get(data.getString("url"));
        } else {
            app = new App(data.getString("url"), (Bitmap) data.getParcelable("icon"), this);
            app.getData();
            apps.put(data.getString("url"), app);
        }

        Home.progressDialog.cancel();

        packageManager = getPackageManager();
        setTitle(app.getTitle());
        packageName=app.getPackageName();

        if(appInstalled(packageName)){
            open.setVisibility(View.INVISIBLE); //VISIBLE
            uninstall.setVisibility(View.INVISIBLE);
            open.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = packageManager.getLaunchIntentForPackage(packageName);
                    startActivity(intent);
                }
            });
            uninstall.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:"+packageName));
                    startActivity(intent);
                }
            });
        } else {
            open.setVisibility(View.INVISIBLE);
            uninstall.setVisibility(View.INVISIBLE);
        }

        try {
            int vibrantColor = app.getAppColor();
            collapsingToolbar.setContentScrimColor(vibrantColor);
            collapsingToolbar.setStatusBarScrimColor(vibrantColor);
            appBackground.setBackgroundColor(vibrantColor);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(vibrantColor);
            setTaskDescription(new ActivityManager.TaskDescription("Apkmirror - "+app.getTitle(), app.getAppIcon(), vibrantColor));
            if(getSharedPreferences("settings", MODE_PRIVATE).getBoolean("colored_navbar", false))
                window.setNavigationBarColor(vibrantColor);
        }catch (Exception e) {
            Palette.from(app.getAppIcon()).generate(new Palette.PaletteAsyncListener() {
                @SuppressWarnings("ResourceType")
                @Override
                public void onGenerated(@NonNull Palette palette) {
                    int vibrantColor = palette.getDarkVibrantColor(R.color.colorPrimary);
                    collapsingToolbar.setContentScrimColor(vibrantColor);
                    collapsingToolbar.setStatusBarScrimColor(vibrantColor);
                    appBackground.setBackgroundColor(vibrantColor);
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(vibrantColor);
                    setTaskDescription(new ActivityManager.TaskDescription("Apkmirror - "+app.getTitle(), app.getAppIcon(), vibrantColor));
                    if(getSharedPreferences("settings", MODE_PRIVATE).getBoolean("colored_navbar", false))
                        window.setNavigationBarColor(vibrantColor);
                }
            });
        }

        int color = getResources().getColor(android.R.color.background_dark);

        appIcon.setImageBitmap(app.getAppIcon());
        publisher.setText(app.getPublisher());
        description.setText(app.getDescription());
        version.setText(app.getVersion());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download(MainActivity.this, app);
            }
        });

        uninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uninstall();
            }
        });

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open();
            }
        });

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                if (Math.abs(verticalOffset) > 200) {
                    appBarExpanded = false;
                    invalidateOptionsMenu();
                } else {
                    appBarExpanded = true;
                    invalidateOptionsMenu();
                }
            }
        });

        recyclerView = findViewById(R.id.rv_versions);

        appVersionAdapter = new AppVersionAdapter(versions, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(appVersionAdapter);
        recyclerView.setFocusable(false);

        app.getVersions(new App.LoadListener() {
            @Override
            public void onLoadFinish(List<Version> vers) {
                versions.clear();
                versions.addAll(vers);
                appVersionAdapter.notifyDataSetChanged();
            }
        });
    }

    private void download(Context context, App app) {
        notifyId++;
        Log.d("NOTIFYID", notifyId+"");
        final Notifications notifications = new Notifications(context, notifyId, app.getTitle(), "Download", "", app.getAppIcon(), true);
        notifications.addButton(R.drawable.ic_file_download_white_24dp, "Cancel", new Intent(context, Reciever.class).putExtra(Reciever.ACTION, Reciever.CANCEL_DOWNLOAD).putExtra(Reciever.ID, notifyId));
        notifications.showNotification();
        app.download(context, notifyId, new App.Listener() {
            @Override
            public void onUpdate(int progress) {
                notifications.setProgress(progress);
            }

            @Override
            public void onFinish() {
                notifications.setFinished();
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onCancel(String message){
                notifications.setCanceled(message);
            }
        });
    }

    private void open() {
        app.open(this);
    }

    private void uninstall() {
        app.uninstall(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (collapsedMenu != null
                && (!appBarExpanded || collapsedMenu.size() != 1)) {
            collapsedMenu.add("Download")
                    .setIcon(R.drawable.ic_file_download_white_24dp)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return super.onPrepareOptionsMenu(collapsedMenu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        collapsedMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                finishAfterTransition();
                return true;
        }

        if (item.getTitle() == "Download") {
            download(this, app);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean appInstalled(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }
}
