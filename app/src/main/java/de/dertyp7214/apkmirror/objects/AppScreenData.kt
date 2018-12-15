/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package de.dertyp7214.apkmirror.objects

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView

class AppScreenData(val app: App, private val description: String, val versions: ArrayList<App>, val variants: ArrayList<AppVariant>) {

    fun getDescription(): Spanned? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT)
        else
            Html.fromHtml(description)
    }

    fun applyDescriptionToTextView(textView: TextView) {
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.text = getDescription()
    }
}