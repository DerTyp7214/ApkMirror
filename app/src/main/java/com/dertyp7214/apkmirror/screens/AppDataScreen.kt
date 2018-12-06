@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.dertyp7214.apkmirror.screens

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.dertyp7214.apkmirror.R
import com.dertyp7214.apkmirror.common.*
import com.dertyp7214.apkmirror.common.NetworkTools.Companion.drawableFromUrl
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import kotlinx.android.synthetic.main.activity_app_data_screen.*
import java.util.*


class AppDataScreen : AppCompatActivity() {

    private lateinit var htmlParser: HtmlParser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_data_screen)
        setSupportActionBar(toolbar)

        val themeManager = ThemeManager.getInstance(this)
        themeManager.enableStatusAndNavBar(this)

        if (intent == null || intent.extras == null || !intent.extras.containsKey("url")) finish()
        val extras = intent.extras
        htmlParser = HtmlParser(this)
        val appData = Adapter.apps[extras["url"]]
        Adapter.progressDialog?.dismiss()
        title = ""
        txt_title.text = appData!!.app.title
        txt_title.setTextColor(if (themeManager.darkMode) Color.WHITE else Color.BLACK)

        val color = getDominantColor((drawableFromUrl(this, appData.app.imageUrl) as BitmapDrawable).bitmap)
        themeManager.getComponents(this).forEach {
            Log.d("COMPONENT", it.getId())
            it.changeColor(
                if (it.isAccent) Color.RED else color
            )
        }
        changeNavColor(color)
        changeStatusColor(color)

        icon.setImageDrawable(drawableFromUrl(this, appData.app.imageUrl))

        txt_description.setLinkTextColor(themeManager.colorAccent)
        appData.applyDescriptionToTextView(txt_description)

        val variantAdapter = VariantAdapter(this, appData.variants)
        val variantBottomSheet = BottomSheet(getString(R.string.titleVariants), variantAdapter)
        btn_vars.visibility = if (appData.variants.size > 0) View.VISIBLE else View.INVISIBLE
        btn_vars.setOnClickListener {
            variantBottomSheet.show(supportFragmentManager, "Variants")
        }

        val versionAdapter = VersionAdapter(this, appData.versions)
        val versionBottomSheet = BottomSheet(getString(R.string.titleVersions), versionAdapter)
        btn_vers.visibility = if (appData.versions.size > 0) View.VISIBLE else View.INVISIBLE
        btn_vers.setOnClickListener {
            versionBottomSheet.show(supportFragmentManager, "Versions")
        }
    }

    private fun getDominantColor(bitmap: Bitmap): Int {
        val swatchesTemp = Palette.generate(bitmap).swatches
        val swatches = ArrayList<Palette.Swatch>(swatchesTemp)
        swatches.sortWith(Comparator { swatch1, swatch2 -> swatch2.population - swatch1.population })
        return if (swatches.size > 0) swatches[0].rgb else Color.GRAY
    }

    private fun changeStatusColor(color: Int) {
        window.statusBarColor = color
        var tmp = window.decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tmp = if (ColorUtils.calculateLuminance(color) > 0.5F)
                tmp or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            else
                tmp and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        window.decorView.systemUiVisibility = tmp
    }

    private fun changeNavColor(color: Int) {
        window.navigationBarColor = color
        var tmp = window.decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tmp = if (ColorUtils.calculateLuminance(color) > 0.5F)
                tmp or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            else
                tmp and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
        window.decorView.systemUiVisibility = tmp
    }
}
