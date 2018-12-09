package de.dertyp7214.apkmirror.screens

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.dertyp7214.themeablecomponents.components.ThemeableFloatingActionButtonProgressBar
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import com.downloader.PRDownloader
import de.dertyp7214.apkmirror.BuildConfig
import de.dertyp7214.apkmirror.R
import de.dertyp7214.apkmirror.common.Adapter
import de.dertyp7214.apkmirror.common.HtmlParser
import kotlinx.android.synthetic.main.activity_about.*
import java.io.File

class About : AppCompatActivity() {

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
        val htmlParser = HtmlParser(this)
        val themeManager = ThemeManager.getInstance(this)
        themeManager.enableStatusAndNavBar(this)
        themeManager.changeAccentColor(Color.RED)
        themeManager.changePrimaryColor(resources.getColor(R.color.ic_launcher_background))

        fab.isFinished = true
        checkUpdate {
            if (update) {
                fab.isFinished = false
                fab.setOnClickListener {
                    fab.isLoading = true
                    fab.setOnClickListener { }
                    downloadSessions.add(
                        htmlParser.openInstaller(
                            apkUrl,
                            object : HtmlParser.Listener {
                                override fun run(progress: Int) {
                                    fab.setOnClickListener { }
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
        }
    }
    private fun checkUpdate(unit: () -> Unit) {
        if (version == "") {
            Thread {
                val htmlParser = HtmlParser(this@About)
                val json = htmlParser.getJson(latestRelease)
                version = json.getString("tag_name")
                apkUrl = json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")
                update = version != BuildConfig.VERSION_NAME
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
