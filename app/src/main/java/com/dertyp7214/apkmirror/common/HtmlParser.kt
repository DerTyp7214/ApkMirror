@file:Suppress("NAME_SHADOWING")

package com.dertyp7214.apkmirror.common

import android.content.Context
import android.os.StrictMode
import android.util.Log
import com.dertyp7214.apkmirror.common.NetworkTools.Companion.getWebContent
import com.dertyp7214.apkmirror.objects.App
import com.dertyp7214.apkmirror.objects.AppScreenData
import com.dertyp7214.apkmirror.objects.AppVariant

class HtmlParser(private val context: Context) {

    var page: Int = 1
        set(value) {
            field = value
            baseQueryUrl = "$baseUrl/?post_type=app_release&searchtype=app&page=$value&s="
        }

    private val baseUrl = "https://www.apkmirror.com"
    private var baseQueryUrl = "$baseUrl/?post_type=app_release&searchtype=app&page=$page&s="

    companion object {
        private val appMap = HashMap<String, AppScreenData>()
    }

    init {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    fun getAppList(query: String): ArrayList<App> {
        val searchContent = getWebContent("$baseQueryUrl$query") ?: ""
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
                    val app = App(title, dev, version, "", size, "$baseUrl$url", "$baseUrl$imageUrl")
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
                                                variants.add(AppVariant(version, androidVersion, arch, dpi))
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
}