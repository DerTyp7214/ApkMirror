/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package de.dertyp7214.apkmirror.objects

import android.os.Build
import android.text.Html

class App(
    title: String,
    val dev: String,
    val version: String,
    val date: String,
    val size: String,
    val url: String,
    val imageUrl: String,
    var packageName: String = ""
) {
    val title = title
        get() {
            return (if (Build.VERSION.SDK_INT >= 24) Html.fromHtml(field, Html.FROM_HTML_MODE_LEGACY)
            else Html.fromHtml(field)).toString()
        }

    fun equals(app: App): Boolean = try {
        title == app.title
                && dev == app.dev
                && version == version
                && date == app.date
                && url == app.url
    } catch (e: Exception) {
        false
    }
}