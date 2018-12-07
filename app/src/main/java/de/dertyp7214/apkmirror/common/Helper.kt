package de.dertyp7214.apkmirror.common

import android.content.Context
import android.graphics.Color
import com.dertyp7214.changelogs.ChangeLog
import com.dertyp7214.changelogs.Version

class Helper {
    companion object {
        fun changeLogs(context: Context, closeListener: () -> Unit = {}): ChangeLog {
            return ChangeLog.Builder(context)
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
    }
}