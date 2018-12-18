/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package de.dertyp7214.apkmirror.objects

import android.content.Context
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import saschpe.android.customtabs.CustomTabsHelper
import saschpe.android.customtabs.WebViewFallback

class AppScreenData(
    val app: App,
    private val description: String,
    val versions: ArrayList<App>,
    val variants: ArrayList<AppVariant>
) {

    fun getDescription(): Spanned? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT)
        else
            Html.fromHtml(description)
    }

    fun applyDescriptionToTextView(context: Context, textView: TextView, color: Int) {
        setTextViewHTML(textView, description, context, color)
    }

    private fun makeLinkClickable(strBuilder: SpannableStringBuilder, span: URLSpan, context: Context, color: Int) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)
        val clickable = object : ClickableSpan() {
            override fun onClick(view: View) {
                try {
                    val customTabsIntent = CustomTabsIntent.Builder()
                        .setInstantAppsEnabled(true)
                        .addDefaultShareMenuItem()
                        .setToolbarColor(color)
                        .setShowTitle(true)
                        .build()
                    CustomTabsHelper.addKeepAliveExtra(context, customTabsIntent.intent)
                    CustomTabsHelper.openCustomTab(
                        context, customTabsIntent,
                        Uri.parse(span.url.toString()),
                        WebViewFallback()
                    )
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open this link!", Toast.LENGTH_LONG).show()
                }
            }
        }
        strBuilder.setSpan(clickable, start, end, flags)
        strBuilder.removeSpan(span)
    }

    private fun setTextViewHTML(text: TextView, html: String, context: Context, color: Int) {
        val sequence = Html.fromHtml(html)
        val strBuilder = SpannableStringBuilder(sequence)
        val urls = strBuilder.getSpans(0, sequence.length, URLSpan::class.java)
        for (span: URLSpan in urls) {
            makeLinkClickable(strBuilder, span, context, color)
        }
        text.text = strBuilder
        text.movementMethod = LinkMovementMethod.getInstance()
    }
}