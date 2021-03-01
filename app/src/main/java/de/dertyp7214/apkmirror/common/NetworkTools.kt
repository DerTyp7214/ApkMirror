/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package de.dertyp7214.apkmirror.common

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import de.dertyp7214.apkmirror.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class NetworkTools {
    companion object {
        private val contentMap = HashMap<String, Any>()
        fun getWebContent(url: String, forced: Boolean = false): String? {
            if (!contentMap.containsKey(url) || forced)
                contentMap[url] = try {
                    val web = URL(url)
                    val reader = BufferedReader(InputStreamReader(web.openStream()))

                    val ret = StringBuilder()
                    var line: String?

                    while (run {
                            line = reader.readLine()
                            line
                        } != null)
                        ret.append(line!!)

                    if (url == "http://api.github.com/repos/DerTyp7214/ApkMirror/releases/latest") Log.d(
                        "RET",
                        ret.toString()
                    )
                    reader.close()
                    ret.toString()
                } catch (e: Exception) {
                    Log.d("ERROR", e.message ?: "")
                    ""
                }
            return try {
                contentMap[url] as String
            } catch (e: Exception) {
                ""
            }
        }

        fun fastLoadImage(url: String): Drawable? {
            return if (url == "self:launcher_icon" && Config.application != null) ContextCompat.getDrawable(
                Config.application!!,
                R.mipmap.ic_launcher
            )
            else if (contentMap.containsKey(url)) contentMap[url] as Drawable else null
        }

        fun drawableFromUrl(context: Context, url: String): Drawable {
            return try {
                if (!contentMap.containsKey(url)) {
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.connect()
                    val input = connection.inputStream
                    contentMap[url] =
                        BitmapDrawable(context.resources, BitmapFactory.decodeStream(input))
                }
                contentMap[url] as Drawable
            } catch (e: Exception) {
                e.printStackTrace()
                ContextCompat.getDrawable(context, R.mipmap.ic_launcher)!!
            }
        }
    }
}