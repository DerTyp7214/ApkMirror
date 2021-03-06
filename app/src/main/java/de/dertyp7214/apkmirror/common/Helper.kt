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
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

class Helper {
    companion object {
        fun changeLogs(context: Context, closeListener: () -> Unit = {}): ChangeLog {
            return ChangeLog.Builder(context)
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.7")
                        .setVersionCode("17000")
                        .addChange(
                            Version.Change(
                                Version.Change.ChangeType.ADD,
                                "networkstate Handling"
                            )
                        )
                        .addChange(
                            Version.Change(
                                Version.Change.ChangeType.FIX,
                                "crash without network"
                            )
                        )
                        .addChange(
                            Version.Change(
                                Version.Change.ChangeType.IMPROVEMENT,
                                "style fixes"
                            )
                        )
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.6")
                        .setVersionCode("16000")
                        .addChange(
                            Version.Change(
                                Version.Change.ChangeType.ADD,
                                "Read Darkmode from System"
                            )
                        )
                        .addChange(
                            Version.Change(
                                Version.Change.ChangeType.IMPROVEMENT,
                                "Performance"
                            )
                        )
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.5")
                        .setVersionCode("15000")
                        .addChange(
                            Version.Change(
                                Version.Change.ChangeType.ADD,
                                "Read Accentcolor from System"
                            )
                        )
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.4.1")
                        .setVersionCode("14100")
                        .addChange(
                            Version.Change(
                                Version.Change.ChangeType.FIX,
                                "Version handling"
                            )
                        )
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.4")
                        .setVersionCode("14000")
                        .addChange(
                            Version.Change(
                                Version.Change.ChangeType.FIX,
                                "Updater for apps"
                            )
                        )
                        .addChange(
                            Version.Change(
                                Version.Change.ChangeType.ADD,
                                "New version handling"
                            )
                        )
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.3")
                        .setVersionCode("1")
                        .addChange(
                            Version.Change(
                                Version.Change.ChangeType.ADD,
                                "Updater for apps"
                            )
                        )
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.2")
                        .setVersionCode("1")
                        .addChange(
                            Version.Change(
                                Version.Change.ChangeType.FIX,
                                "Updater for ApkMirror"
                            )
                        )
                        .build()
                )
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.1")
                        .setVersionCode("1")
                        .addChange(
                            Version.Change(
                                Version.Change.ChangeType.ADD,
                                "Updater for ApkMirror"
                            )
                        )
                        .addChange(
                            Version.Change(
                                Version.Change.ChangeType.IMPROVEMENT,
                                "Improved some animations"
                            )
                        )
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
                for (file in (dir.listFiles() ?: arrayOf())) {
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
            val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
            val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + (if (si) "" else "i")
            return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
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
            val r = (Color.red(color) * factor).roundToInt()
            val g = (Color.green(color) * factor).roundToInt()
            val b = (Color.blue(color) * factor).roundToInt()
            return Color.argb(
                a,
                r.coerceAtMost(255),
                g.coerceAtMost(255),
                b.coerceAtMost(255)
            )
        }

    }
}