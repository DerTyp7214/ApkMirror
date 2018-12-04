package com.dertyp7214.apkmirror.common

import android.content.Context
import android.graphics.Color
import com.dertyp7214.changelogs.ChangeLog
import com.dertyp7214.changelogs.Version

class Helper {
    companion object {
        fun changeLogs(context: Context): ChangeLog {
            return ChangeLog.Builder(context)
                .addVersion(
                    Version.Builder(context)
                        .setVersionName("1.0")
                        .setVersionCode("1")
                        .addChange(Version.Change(Version.Change.ChangeType.ADD, "Created the App"))
                        .build()
                )
                .setLinkColor(Color.GREEN)
                .build().buildDialog("Changes")
        }
    }
}