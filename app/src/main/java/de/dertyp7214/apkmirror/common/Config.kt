/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package de.dertyp7214.apkmirror.common

import android.annotation.SuppressLint
import android.app.Application

@SuppressLint("StaticFieldLeak")
object Config {
    var application: Application? = null
    val knownNames = ArrayList<String>()
}