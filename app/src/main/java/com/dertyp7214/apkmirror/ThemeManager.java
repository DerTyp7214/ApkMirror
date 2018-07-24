package com.dertyp7214.apkmirror;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

public class ThemeManager {

    private static ThemeManager instance;

    private static String DARK_THEME = "dark_theme";
    private static String AUTO_DARK_THEME = "auto_dark_theme";

    private Context context;
    private SharedPreferences preferences;
    private boolean darkTheme;

    private ThemeManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        this.darkTheme = preferences.getBoolean(DARK_THEME, false);
        instance = this;
    }

    public static ThemeManager getInstance(Context context) {
        if (instance == null)
            new ThemeManager(context);
        return instance;
    }

    public boolean isDarkTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
        if (preferences.getBoolean(AUTO_DARK_THEME, false))
            this.darkTheme = context.getResources().getBoolean(R.bool.night_mode);
        else
            this.darkTheme = preferences.getBoolean(DARK_THEME, false);
        return darkTheme;
    }

    public int getBackgroundColor() {
        return darkTheme ? context.getResources()
                .getColor(R.color.cardview_dark_background) : Color.WHITE;
    }

    public int getPlaceHolderBackgroundColor() {
        return darkTheme ? context.getResources()
                .getColor(R.color.placeholder_background_dark) : context.getResources()
                .getColor(R.color.placeholder_background_light);
    }

    public int getElementColor() {
        return darkTheme ? context.getResources()
                .getColor(R.color.element_color_dark) : Color.WHITE;
    }

    public int getTitleTextColor() {
        return darkTheme ? Color.WHITE : Color.BLACK;
    }

    public int getSubTitleTextColor() {
        return darkTheme ? Color.WHITE : Color.parseColor("#808080");
    }

    public int getProgressStyle() {
        return darkTheme ? R.style.ProgressStyleDark : 0;
    }

    public ColorStateList getNavigationColors() {
        return darkTheme ? context.getResources()
                .getColorStateList(R.color.menu_text_color_dark) : context.getResources()
                .getColorStateList(R.color.menu_text_color);
    }
}
