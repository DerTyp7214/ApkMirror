/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package de.dertyp7214.apkmirror

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import com.dertyp7214.themeablecomponents.controller.AppController
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import de.dertyp7214.apkmirror.common.Config
import de.dertyp7214.apkmirror.common.Helper

class Application : AppController() {

    @SuppressLint("MissingSuperCall")
    override fun onCreate() {
        super.onCreate()
        Config.application = this
        val accentColor = Helper.getAttrColor(this, android.R.attr.colorAccent)
        themeManager = ThemeManager.getInstance(this)
        themeManager.setDefaultAccent(accentColor)
        themeManager.changeAccentColor(accentColor)
        themeManager.darkMode =
            (getSystemService(Context.UI_MODE_SERVICE) as UiModeManager).nightMode ==
                    UiModeManager.MODE_NIGHT_YES
    }

    fun getManager(): ThemeManager {
        return themeManager
    }
}
