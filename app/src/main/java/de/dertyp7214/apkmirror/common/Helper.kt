/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package de.dertyp7214.apkmirror.common

import android.content.Context
import android.graphics.Color
import com.dertyp7214.changelogs.ChangeLog
import com.dertyp7214.changelogs.Version
import java.io.File

class Helper {
    companion object {
        fun changeLogs(context: Context, closeListener: () -> Unit = {}): ChangeLog {
            return ChangeLog.Builder(context)
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.7")
                        .setVersionCode("17000")
                        .addChange(Version.Change(Version.Change.ChangeType.ADD, "networkstate Handling"))
                        .addChange(Version.Change(Version.Change.ChangeType.FIX, "crash without network"))
                        .addChange(Version.Change(Version.Change.ChangeType.IMPROVEMENT, "style fixes"))
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.6")
                        .setVersionCode("16000")
                        .addChange(Version.Change(Version.Change.ChangeType.ADD, "Read Darkmode from System"))
                        .addChange(Version.Change(Version.Change.ChangeType.IMPROVEMENT, "Performance"))
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.5")
                        .setVersionCode("15000")
                        .addChange(Version.Change(Version.Change.ChangeType.ADD, "Read Accentcolor from System"))
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.4.1")
                        .setVersionCode("14100")
                        .addChange(Version.Change(Version.Change.ChangeType.FIX, "Version handling"))
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.4")
                        .setVersionCode("14000")
                        .addChange(Version.Change(Version.Change.ChangeType.FIX, "Updater for apps"))
                        .addChange(Version.Change(Version.Change.ChangeType.ADD, "New version handling"))
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.3")
                        .setVersionCode("1")
                        .addChange(Version.Change(Version.Change.ChangeType.ADD, "Updater for apps"))
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.2")
                        .setVersionCode("1")
                        .addChange(Version.Change(Version.Change.ChangeType.FIX, "Updater for ApkMirror"))
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.1")
                        .setVersionCode("1")
                        .addChange(Version.Change(Version.Change.ChangeType.ADD, "Updater for ApkMirror"))
                        .addChange(Version.Change(Version.Change.ChangeType.IMPROVEMENT, "Improved some animations"))
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.0")
                        .setVersionCode("1")
                        .addChange(Version.Change(Version.Change.ChangeType.ADD, "Created the App"))
                        .build()
                )
                .addCloseListener(closeListener)
                .setLinkColor(Color.GREEN)
                .build().buildDialog("Changes")
        }

        fun showChangeDialog(context: Context, function: () -> Unit) {
            if (!changeLogs(context, function).showDialogOnVersionChange()) function()
        }

        fun folderSize(dir: File): Long {
            var length = 0L
            try {
                if (dir.isFile) return dir.length()
                for (file in dir.listFiles()) {
                    length += if (file.isFile) file.length()
                    else folderSize(file)
                }
            } catch (e: Exception) {
            }
            return length
        }

        fun humanReadableByteCount(bytes: Long, si: Boolean = true): String {
            val unit = if (si) 1000 else 1024
            if (bytes < unit) return "$bytes B"
            val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
            val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + (if (si) "" else "i")
            return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
        }

        fun getAttrColor(context: Context, attr: Int): Int {
            return try {
                val ta = context.obtainStyledAttributes(intArrayOf(attr))
                val colorAccent = ta.getColor(0, 0)
                ta.recycle()
                colorAccent
            } catch (e: Exception) {
                Color.RED
            }
        }

        fun manipulateColor(color: Int, factor: Float): Int {
            val a = Color.alpha(color)
            val r = Math.round(Color.red(color) * factor)
            val g = Math.round(Color.green(color) * factor)
            val b = Math.round(Color.blue(color) * factor)
            return Color.argb(
                a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255)
            )
        }

        fun isColorDark(color: Int): Boolean {
            val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
            return darkness >= 0.5
        }
    }
}