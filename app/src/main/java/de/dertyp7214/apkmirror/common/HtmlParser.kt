/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

@file:Suppress("NAME_SHADOWING", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package de.dertyp7214.apkmirror.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import androidx.core.content.FileProvider
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import de.dertyp7214.apkmirror.BuildConfig
import de.dertyp7214.apkmirror.common.NetworkTools.Companion.getWebContent
import de.dertyp7214.apkmirror.objects.App
import de.dertyp7214.apkmirror.objects.AppScreenData
import de.dertyp7214.apkmirror.objects.AppVariant
import de.dertyp7214.apkmirror.objects.DownloadData
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern


class HtmlParser(private val context: Context) {

    var page: Int = 1
        set(value) {
            field = value
            baseQueryUrl = "$baseUrl/?post_type=app_release&searchtype={TYPE}&page=$value&s="
        }

    private val baseUrl = "https://www.apkmirror.com"
    private var baseQueryUrl = "$baseUrl/?post_type=app_release&searchtype={TYPE}&page=$page&s="

    companion object {
        private val appMap = HashMap<String, AppScreenData>()
    }

    init {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    fun getAppList(query: String, type: String = "app"): ArrayList<App> {
        val searchContent = getWebContent("${baseQueryUrl.replace("{TYPE}", type)}$query") ?: ""
        val splitOne = searchContent.split("id=\"content\"")
        val content = if (splitOne.size > 1) splitOne[1] else ""
        val splitTwo = content.split("class=\"listWidget\"")
        val listWidget = if (splitTwo.size > 1) splitTwo[1] else ""
        val appList = ArrayList<App>()
        listWidget.split("class=\"appRow\"").forEachIndexed { index, s ->
            if (index > 0) {
                try {
                    val title = s.split("class=\"fontBlack\"")[1]
                        .split(">")[1]
                        .split("</a")[0]
                    val dev = s.split("class=\"byDeveloper")[1]
                        .split(">by")[1]
                        .split("</a")[0]
                    val version =
                        s.split("class=\"infoslide-name\">Version")[1]
                            .split("class=\"infoslide-value\">")[1]
                            .split("</span")[0]
                    val size =
                        s.split("class=\"infoslide-name\">File")[1]
                            .split("class=\"infoslide-value\">")[1]
                            .split("</span")[0]
                    val url = s.split("class=\"fontBlack\"")[1]
                        .split("href=\"")[1]
                        .split("\">")[0]
                    val imageUrl = s.split("<img")[1]
                        .split("src=\"")[1]
                        .split("\">")[0]
                        .replace("w=32&h=32", "w=128&h=128")
                    val app = App(
                        title,
                        dev,
                        version,
                        "",
                        size,
                        "$baseUrl${if (type == "apk") url.split("/").dropLast(2).joinToString("/") else url}",
                        "$baseUrl$imageUrl"
                    )
                    appList.add(app)
                } catch (e: Exception) {
                    Log.wtf("ERROR", "Error at: $s\n\nMessage: ${e.localizedMessage}")
                }
            }
        }
        return appList
    }

    fun getAppScreenData(app: App): AppScreenData {
        if (!appMap.containsKey(app.url)) {
            val pageSource = getWebContent(app.url) ?: ""
            var description = ""
            val versions = ArrayList<App>()
            val variants = ArrayList<AppVariant>()
            pageSource.split("class=\"tab-pane fade").forEachIndexed { index, s ->
                if (index > 0) {
                    try {
                        when {
                            s.substring(0, 50).contains("id=\"description\"") -> {
                                description =
                                        s.split("id=\"description\"")[1]
                                            .split("class=\"notes\">")[1]
                                            .split("role=\"tabpanel")[0]
                                            .replace("</div></div></div><div ", "")
                            }
                            s.substring(0, 50).contains("id=\"variants\"") -> {
                                s.split("id=\"variants\"")[1]
                                    .split("class=\"table topmargin\">")[1]
                                    .split("class=\"table-row headerFont\"")
                                    .forEachIndexed { index, s ->
                                        if (index > 0) {
                                            try {
                                                val url = s.split("class=\"table-cell")[1]
                                                    .split("class=\"colorLightBlack\"")[1]
                                                    .split("<a")[1]
                                                    .split("href=\"")[1]
                                                    .split("\">")[0]
                                                val version = s.split("class=\"table-cell")[1]
                                                    .split("class=\"colorLightBlack\"")[1]
                                                    .split("<a")[1]
                                                    .split("\">")[1]
                                                    .split("</a")[0]
                                                val androidVersion = s.split("class=\"table-cell")[3]
                                                    .split("<a")[1]
                                                    .split("\">")[1]
                                                    .split("</a")[0]
                                                val arch = s.split("class=\"table-cell")[2]
                                                    .split("<a")[1]
                                                    .split("\">")[1]
                                                    .split("</a")[0]
                                                val dpi = s.split("class=\"table-cell")[4]
                                                    .split("<a")[1]
                                                    .split("\">")[1]
                                                    .split("</a")[0]
                                                variants.add(
                                                    AppVariant(
                                                        app,
                                                        "$baseUrl$url",
                                                        version,
                                                        androidVersion,
                                                        arch,
                                                        dpi
                                                    )
                                                )
                                            } catch (e: Exception) {
                                                Log.wtf("Error", "$index: $s\n\n${e.message}")
                                            }
                                        }
                                    }
                            }
                        }
                    } catch (e: Exception) {
                        Log.wtf("Error", e.localizedMessage)
                    }
                }
            }
            val widget = pageSource.split("class=\"widgetHeader\">All")
            if (widget.size > 1) widget[1]
                .split("class=\"OUTBRAIN\"")[0]
                .split("class=\"appRow\"").forEachIndexed { index, s ->
                    if (index > 0) {
                        try {
                            val title = s.split("class=\"fontBlack\"")[1]
                                .split(">")[1]
                                .split("</a")[0]
                            val dev = app.dev
                            val date = s.split("class=\"dateyear_utc\"")[1]
                                .split(">")[1]
                                .split("</span")[0]
                            val version =
                                s.split("class=\"infoslide-name\">Version")[1]
                                    .split("class=\"infoslide-value\">")[1]
                                    .split("</span")[0]
                            val size =
                                s.split("class=\"infoslide-name\">File")[1]
                                    .split("class=\"infoslide-value\">")[1]
                                    .split("</span")[0]
                            val url = s.split("class=\"fontBlack\"")[1]
                                .split("href=\"")[1]
                                .split("\">")[0]
                            val imageUrl = s.split("<img")[1]
                                .split("src=\"")[1]
                                .split("\">")[0]
                                .replace("w=32&h=32", "w=128&h=128")
                            versions.add(App(title, dev, version, date, size, "$baseUrl$url", "$baseUrl$imageUrl"))
                        } catch (e: Exception) {
                            Log.wtf("ERROR", "Error at: $s\n\nMessage: ${e.localizedMessage}")
                        }
                    }
                }
            appMap[app.url] = AppScreenData(app, description, versions, variants)
        }
        return appMap[app.url]!!
    }

    fun getDownloadPage(app: AppVariant): DownloadData {
        return getDownloadPage(app.app, app.url)
    }

    fun getDownloadPage(app: App, url: String): DownloadData {

        val content = getWebContent(url) ?: ""
        val variants = ArrayList<AppVariant>()

        try {
            content.split("class=\"table topmargin")[1]
                .split("class=\"table-row headerFont\"")
                .forEachIndexed { index, s ->
                    if (index > 0) {
                        try {
                            val url = s.split("class=\"table-cell")[1]
                                .split("<a")[1]
                                .split("href=\"")[1]
                                .split("\">")[0]
                            val version = s.split("class=\"table-cell")[1]
                                .split("<a")[1]
                                .split("svg>")[1]
                                .split("</a")[0]
                            val androidVersion = s.split("class=\"table-cell")[3]
                                .split(">")[1]
                                .split("</div")[0]
                            val arch = s.split("class=\"table-cell")[2]
                                .split(">")[1]
                                .split("</div")[0]
                            val dpi = s.split("class=\"table-cell")[4]
                                .split(">")[1]
                                .split("</div")[0]
                            variants.add(AppVariant(app, "$baseUrl$url", version, androidVersion, arch, dpi))
                        } catch (e: Exception) {
                            Log.wtf("Error", "$index: $s\n\n${e.message}")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.wtf("ERROR", "${content.split("class=\"table topmargin\">").size} ${e.message}")
        }

        return DownloadData(app, variants)
    }

    fun getApkUrl(url: String): String {
        val content = getWebContent(url) ?: ""

        return baseUrl + try {
            val url = content.split("class=\"btn btn-flat downloadButton\"")[1]
                .split("href=\"")[1]
                .split("\"")[0]
            if (url.contains("download.php")) url
            else {
                val apkPage = getWebContent(baseUrl + url) ?: ""
                apkPage.split("<h3>Your download")[1]
                    .split("<a")[1]
                    .split("href=\"")[1]
                    .split("\"")[0]
            }
        } catch (e: Exception) {
            Log.wtf("Error", e.message)
            ""
        }
    }

    fun openInstaller(url: String, listener: Listener): Int {
        val folder = File(Environment.getExternalStorageDirectory(), ".apkmirror")
        if (!folder.exists()) folder.mkdirs()
        val path = folder.absolutePath
        return PRDownloader.download(url, path, "app.apk")
            .build()
            .setOnProgressListener {
                listener.run(((it.currentBytes * 100L) / it.totalBytes).toInt())
            }
            .setOnCancelListener {
                listener.cancel()
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    listener.stop(File(path, "app.apk"))
                }

                override fun onError(error: Error?) {
                    listener.cancel()
                }
            })
    }

    fun getJson(url: String): JSONObject {
        return try {
            JSONObject(getWebContent(url))
        } catch (e: Exception) {
            e.printStackTrace()
            JSONObject("{\"tag_name\": \"null\", \"assets\": [{\"browser_download_url\": \"https://github.com/DerTyp7214/ApkMirror/releases/download/${BuildConfig.VERSION_NAME}/app-release.apk\"}]}")
        }
    }

    fun humanReadableByteCount(bytes: Long, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + (if (si) "" else "i")
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }

    private fun isRootAvailable(): Boolean {
        for (pathDir in System.getenv("PATH").split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (File(pathDir, "su").exists()) {
                return true
            }
        }
        return false
    }

    private fun isRootGiven(): Boolean {
        if (isRootAvailable()) {
            var process: Process? = null
            try {
                process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
                val `in` = BufferedReader(InputStreamReader(process!!.inputStream))
                val output = `in`.readLine()
                if (output != null && output.toLowerCase().contains("uid=0"))
                    return true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                process?.destroy()
            }
        }

        return false
    }

    private fun Try(unit: () -> Unit): String {
        return try {
            unit()
            "Error: none"
        } catch (e: Exception) {
            e.message ?: "Error: null"
        }
    }

    fun installApk(file: File?) {
        if (isRootGiven()) {
            executeCommand("cp ${file?.absolutePath} /data/local/tmp/\npm install -r /data/local/tmp/${file?.name}\n")
        } else
            try {
                Log.d("Dismiss Dialog", Try { VariantAdapter.progressDialog!!.dismiss() })
                Log.d("Dismiss Dialog", Try { VersionAdapter.progressDialog!!.dismiss() })
                if (file!!.exists()) {
                    val fileNameArray =
                        file.name.split(Pattern.quote(".").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (fileNameArray[fileNameArray.size - 1] == "apk") {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            val downloadedApk = getFileUri(context, file)
                            val intent = Intent(Intent.ACTION_VIEW).setDataAndType(
                                downloadedApk,
                                "application/vnd.android.package-archive"
                            )
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            context.startActivity(intent)
                        } else {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(
                                Uri.fromFile(file),
                                "application/vnd.android.package-archive"
                            )
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
    }

    private fun executeCommand(cmds: String): Boolean {
        return try {
            Log.d("executeCommand", cmds)
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)

            os.writeBytes(cmds + "\n")

            os.writeBytes("exit\n")
            os.flush()
            os.close()

            process.waitFor()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            context.applicationContext
                .packageName + ".GenericFileProvider", file
        )
    }

    interface Listener {
        fun run(progress: Int)
        fun cancel()
        fun stop(file: File)
    }
}