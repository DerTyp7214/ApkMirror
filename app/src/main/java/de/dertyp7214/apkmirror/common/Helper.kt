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
            if (dir.isFile) return dir.length()
            for (file in dir.listFiles()) {
                length += if (file.isFile) file.length()
                else folderSize(file)
            }
            return length
        }
        fun humanReadableByteCount(bytes:Long, si:Boolean = true):String {
            val unit = if (si) 1000 else 1024
            if (bytes < unit) return "$bytes B"
            val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
            val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + (if (si) "" else "i")
            return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
        }
    }
}