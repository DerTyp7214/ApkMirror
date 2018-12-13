package de.dertyp7214.apkmirror

import androidx.core.content.edit
import com.dertyp7214.themeablecomponents.controller.AppController
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import de.dertyp7214.apkmirror.common.Helper

class Application : AppController() {
    override fun onCreate() {
        super.onCreate()
        val accentColor = Helper.getAttrColor(this, android.R.attr.colorAccent)
        val sharedPreferences = getSharedPreferences("application", MODE_PRIVATE)
        themeManager = ThemeManager.getInstance(this)
        themeManager.setDefaultAccent(accentColor)
        themeManager.changeAccentColor(accentColor)
        if (!sharedPreferences.getBoolean("ui_mode_set", false)) {
            sharedPreferences.edit {
                putBoolean("ui_mode_set", true)
            }
            themeManager.darkMode = !Helper.isColorDark(accentColor)
        }
    }

    fun getManager(): ThemeManager {
        return themeManager
    }
}
