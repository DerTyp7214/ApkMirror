package com.dertyp7214.apkmirror.objects

class App(
    val title: String,
    val dev: String,
    val version: String,
    val date: String,
    val size: String,
    val url: String,
    val imageUrl: String
) {
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