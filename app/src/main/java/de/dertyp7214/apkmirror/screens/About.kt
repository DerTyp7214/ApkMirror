/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

@file:Suppress("DEPRECATION")

package de.dertyp7214.apkmirror.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.dertyp7214.themeablecomponents.components.ThemeableFloatingActionButtonProgressBar
import com.dertyp7214.themeablecomponents.screens.ThemeableActivity
import com.dertyp7214.themeablecomponents.utils.OnThemeChangeListener
import com.dertyp7214.themeablecomponents.utils.Theme
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import com.downloader.PRDownloader
import de.dertyp7214.apkmirror.Application
import de.dertyp7214.apkmirror.BuildConfig
import de.dertyp7214.apkmirror.R
import de.dertyp7214.apkmirror.common.Adapter
import de.dertyp7214.apkmirror.common.Comparators
import de.dertyp7214.apkmirror.common.HtmlParser
import de.dertyp7214.apkmirror.fragments.AboutFragment.Companion.disableCheckUpdates
import kotlinx.android.synthetic.main.activity_about.*
import java.io.File

class About : ThemeableActivity() {

    private var update = false
    private var apkUrl =
        "https://github.com/DerTyp7214/ApkMirror/releases/download/${BuildConfig.VERSION_NAME}/app-release.apk"
    private val latestRelease = "https://api.github.com/repos/DerTyp7214/ApkMirror/releases/latest"

    companion object {
        private var version: String = ""
        private val downloadSessions = ArrayList<Int>()
        @SuppressLint("StaticFieldLeak")
        private lateinit var fab: ThemeableFloatingActionButtonProgressBar
    }

    override fun onDestroy() {
        super.onDestroy()
        Adapter.clicked = false
    }

    override fun onBackPressed() {
        fab.hide()
        Handler().postDelayed({
            super.onBackPressed()
            disableCheckUpdates = false
            downloadSessions.forEach {
                PRDownloader.cancel(it)
                downloadSessions.remove(it)
            }
        }, 80)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbar)

        title = getString(R.string.app_name)
        fab = themeableFloatingActionButtonProgressBar
        val themeManager = (application as Application).getManager()
        themeManager.enableStatusAndNavBar(this)

        val themeChangeListener = toolbar.onThemeChangeListener
        toolbar.onThemeChangeListener = object: OnThemeChangeListener {
            override val id: String
                get() = "aboutToolbar"
            override val type: ThemeManager.Component.TYPE
                get() = ThemeManager.Component.TYPE.TOOLBAR

            override fun accent(): Boolean {
                return false
            }

            override fun onThemeChanged(theme: Theme, animated: Boolean) {
                themeChangeListener.onThemeChanged(theme, animated)
            }
        }

        toolbar.onThemeChangeListener.onThemeChanged(Theme(resources.getColor(R.color.ic_launcher_background)), false)

        fab.isFinished = true
        checkUpdate {
            if (update) {
                fab.isFinished = false
                fab.setOnClickListener(getClickListener())
            }
        }
    }

    private fun getClickListener(): (v: View) -> Unit {
        val htmlParser = HtmlParser(this)
        return {
            fab.isLoading = true
            fab.setOnClickListener {
                PRDownloader.cancel(downloadSessions.last())
                fab.isLoading = false
                fab.setOnClickListener(getClickListener())
            }
            downloadSessions.add(
                htmlParser.openInstaller(
                    apkUrl,
                    object : HtmlParser.Listener {
                        override fun run(progress: Int) {
                            fab.setOnClickListener {
                                PRDownloader.cancel(downloadSessions.last())
                                fab.isLoading = false
                                fab.setOnClickListener(getClickListener())
                            }
                            fab.progress = progress
                        }

                        override fun cancel() {
                            fab.isLoading = false
                        }

                        override fun stop(file: File) {
                            fab.setOnClickListener { htmlParser.installApk(file) }
                            fab.progress = 0
                            fab.isFinished = true
                            htmlParser.installApk(file)
                        }
                    })
            )
        }
    }

    private fun checkUpdate(unit: () -> Unit) {
        if (version == "") {
            Thread {
                val htmlParser = HtmlParser(this@About)
                val json = htmlParser.getJson(latestRelease)
                version = json.getString("tag_name")
                apkUrl = json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")
                update = Comparators.compareVersion(version, BuildConfig.VERSION_NAME) == 1
                runOnUiThread {
                    unit()
                }
            }.start()
        } else {
            update = version != BuildConfig.VERSION_NAME
            unit()
        }
    }
}
