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
            @Suppress("DEPRECATION")
            return (if (Build.VERSION.SDK_INT >= 24) Html.fromHtml(
                field,
                Html.FROM_HTML_MODE_LEGACY
            )
            else Html.fromHtml(field)).toString()
        }

    override fun equals(other: Any?): Boolean = try {
        other is App && title == other.title
                && dev == other.dev
                && version == version
                && date == other.date
                && url == other.url
    } catch (e: Exception) {
        false
    }

    override fun hashCode(): Int {
        var result = dev.hashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + imageUrl.hashCode()
        result = 31 * result + packageName.hashCode()
        return result
    }
}