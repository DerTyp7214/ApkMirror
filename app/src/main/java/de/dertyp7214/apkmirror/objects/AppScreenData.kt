/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package de.dertyp7214.apkmirror.objects

import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_VIEW
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
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_ON
import com.fede987.statusbaralert.StatusBarAlert
import de.dertyp7214.apkmirror.R
import saschpe.android.customtabs.CustomTabsHelper
import saschpe.android.customtabs.WebViewFallback
import java.net.HttpURLConnection
import java.net.URL

class AppScreenData(
    val app: App,
    private val description: String,
    val versions: ArrayList<App>,
    val variants: ArrayList<AppVariant>
) {

    fun applyDescriptionToTextView(context: Activity, textView: TextView, color: Int) {
        setTextViewHTML(textView, description, context, color)
    }

    private fun makeLinkClickable(
        strBuilder: SpannableStringBuilder,
        span: URLSpan,
        context: Activity,
        color: Int
    ) {
        val start = strBuilder.getSpanStart(span)
        val end = strBuilder.getSpanEnd(span)
        val flags = strBuilder.getSpanFlags(span)
        val clickable = object : ClickableSpan() {
            override fun onClick(view: View) {
                try {
                    StatusBarAlert.Builder(context)
                        .showProgress(true)
                        .withText("Loading")
                        .withAlertColor(color, true)
                        .autoHide(false)
                        .build()
                    val url = getDestinationUrl(span.url)
                    StatusBarAlert.hide(context) {}
                    if (url.contains("play.google.com")) {
                        context.startActivity(Intent(ACTION_VIEW, Uri.parse(url)))
                    } else {
                        val customTabsIntent = CustomTabsIntent.Builder()
                            .setInstantAppsEnabled(true)
                            .setShareState(SHARE_STATE_ON)
                            .setDefaultColorSchemeParams(
                                CustomTabColorSchemeParams
                                    .Builder()
                                    .setToolbarColor(color)
                                    .build()
                            )
                            .setShowTitle(true)
                            .setStartAnimations(context, R.anim.swipe_in, R.anim.swipe_out)
                            .setExitAnimations(context, R.anim.swipe_in, R.anim.swipe_out)
                            .build()
                        CustomTabsHelper.addKeepAliveExtra(context, customTabsIntent.intent)
                        CustomTabsHelper.openCustomTab(
                            context, customTabsIntent,
                            Uri.parse(span.url.toString()),
                            WebViewFallback()
                        )
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open this link!", Toast.LENGTH_LONG).show()
                }
            }
        }
        strBuilder.setSpan(clickable, start, end, flags)
        strBuilder.removeSpan(span)
    }

    private fun getDestinationUrl(url: String): String {
        return try {
            val conn = URL(url).openConnection() as HttpURLConnection
            HttpURLConnection.setFollowRedirects(true)
            conn.instanceFollowRedirects = true
            conn.readTimeout = 5000
            conn.connect()
            conn.getHeaderField("Location")
        } catch (e: Exception) {
            ""
        }
    }

    private fun setTextViewHTML(text: TextView, html: String, context: Activity, color: Int) {
        @Suppress("DEPRECATION") val sequence = Html.fromHtml(html)
        val strBuilder = SpannableStringBuilder(sequence)
        val urls = strBuilder.getSpans(0, sequence.length, URLSpan::class.java)
        for (span: URLSpan in urls) {
            makeLinkClickable(strBuilder, span, context, color)
        }
        text.text = strBuilder
        text.movementMethod = LinkMovementMethod.getInstance()
    }
}