package com.dertyp7214.apkmirror;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.dertyp7214.apkmirror.components.BottomPopup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private App app;
    private TextView publisher, description, version;
    private ImageView appIcon, appBackground;
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;
    private Menu collapsedMenu;
    private Button open, uninstall;
    private String packageName;
    private PackageManager packageManager;
    private List<Version> versions = new ArrayList<>();
    private RecyclerView recyclerView;
    private AppVersionAdapter appVersionAdapter;
    private FloatingActionButton fab;
    private BottomPopup description_popup;
    private boolean appBarExpanded;
    private boolean launchable;
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
                    app.setAppColor(vibrantColor);
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

        fab = findViewById(R.id.fab);

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

        description.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                description_popup = new BottomPopup(app.getAppColor(), v, MainActivity.this);
                description_popup.setText(app.getDescription());
                description_popup.setUp(findViewById(R.id.main_layout), R.layout.description_popup);
                description_popup.show();
            }
        });

        setUp();
    }

    private void setUp(){
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download(MainActivity.this, app);
            }
        });

        uninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    uninstall();
                }catch (Exception e){
                    setUp();
                }
            }
        });

        final Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(app.getPackageName());
        if(LaunchIntent!=null)
            launchable=true;
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(LaunchIntent);
                }catch (Exception e){
                    setUp();
                }
            }
        });

        if(appInstalled(app.getPackageName())){
            fab.setVisibility(View.INVISIBLE);
            if(launchable)
                open.setVisibility(View.VISIBLE);
            if(!isSystem(app.getPackageName()))
                uninstall.setVisibility(View.VISIBLE);
        }
    }

    private void download(Activity context, App app) {
        if(appInstalled(app.getPackageName())) {
            setUp();
            return;
        }
        Toast.makeText(context, "Downloading "+app.getTitle(), Toast.LENGTH_LONG).show();
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
                setUp();
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

    private void uninstall() {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:"+app.getPackageName()));
        startActivityForResult(intent, 1);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (collapsedMenu != null && (!appBarExpanded || collapsedMenu.size() != 1) && !appInstalled(app.getPackageName())) {
            collapsedMenu.add("Download")
                    .setIcon(R.drawable.ic_file_download_white_24dp)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return super.onPrepareOptionsMenu(collapsedMenu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1){
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            setUp();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        collapsedMenu = menu;
        return true;
    }

    @Override
    public void onBackPressed() {
        if(description_popup!=null) {
            if (!description_popup.isShowing())
                super.onBackPressed();
            else
                description_popup.dismiss();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case android.R.id.home:
                if (description_popup == null)
                    return true;
                else if (!description_popup.isShowing()) {
                    fab.setVisibility(View.INVISIBLE);
                    finishAfterTransition();
                    return true;
                } else
                    description_popup.dismiss();
        }

        if (item.getTitle() == "Download") {
            download(this, app);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean appInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isSystem(String packageName){
        PackageManager pm = getPackageManager();
        try {
            if((pm.getApplicationInfo(packageName, 0).flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                return true;
            else
                return false;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
