package de.dertyp7214.apkmirror.common

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.view.ContextThemeWrapper
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import de.psdev.licensesdialog.NoticesHtmlBuilder
import de.psdev.licensesdialog.NoticesXmlParser
import de.psdev.licensesdialog.R
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices

class LicensesDialog

private constructor(
    private val mContext: Context,
    private val mLicensesText: String,
    private val mTitleText: String,
    private val mCloseText: String,
    private val mThemeResourceId: Int,
    private val mDividerColor: Int
) {

    private var mOnDismissListener: DialogInterface.OnDismissListener? = null

    fun setOnDismissListener(onDismissListener: DialogInterface.OnDismissListener): LicensesDialog {
        mOnDismissListener = onDismissListener
        return this
    }

    private fun create(): AlertDialog {
        val webView = createWebView(mContext)
        webView.loadDataWithBaseURL(null, mLicensesText, "text/html", "utf-8", null)
        val builder: AlertDialog.Builder = if (mThemeResourceId != 0) {
            AlertDialog.Builder(ContextThemeWrapper(mContext, mThemeResourceId))
        } else {
            AlertDialog.Builder(mContext)
        }
        builder.setTitle(mTitleText)
            .setView(webView)
            .setPositiveButton(mCloseText) { dialogInterface, i -> dialogInterface.dismiss() }
        val dialog = builder.create()
        dialog.setOnDismissListener { dialog1 ->
            if (mOnDismissListener != null) {
                mOnDismissListener!!.onDismiss(dialog1)
            }
        }
        dialog.setOnShowListener {
            if (mDividerColor != 0) {
                val titleDividerId = mContext.resources.getIdentifier("titleDivider", "id", "android")
                val titleDivider = dialog.findViewById<View>(titleDividerId)
                titleDivider?.setBackgroundColor(mDividerColor)
            }
        }
        return dialog
    }

    fun show(): AlertDialog {
        val dialog = create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemeManager.getInstance(mContext).colorAccent)
        return dialog
    }

    class Builder(private val mContext: Context) {

        private var mTitleText: String? = null
        private var mCloseText: String? = null
        private var mRawNoticesId: Int? = null
        private var mNotices: Notices? = null
        private var mNoticesText: String? = null
        private var mNoticesStyle: String? = null
        private var mShowFullLicenseText: Boolean = false
        private var mIncludeOwnLicense: Boolean = false
        private var mThemeResourceId: Int = 0
        private var mDividerColor: Int = 0

        init {
            mTitleText = mContext.getString(R.string.notices_title)
            mCloseText = mContext.getString(R.string.notices_close)
            mNoticesStyle = mContext.getString(R.string.notices_default_style)
            mShowFullLicenseText = false
            mIncludeOwnLicense = false
            mThemeResourceId = 0
            mDividerColor = 0
        }

        private fun getNotices(context: Context, rawNoticesResourceId: Int): Notices {
            try {
                val resources = context.resources
                return if ("raw" == resources.getResourceTypeName(rawNoticesResourceId)) {
                    NoticesXmlParser.parse(resources.openRawResource(rawNoticesResourceId))
                } else {
                    throw IllegalStateException("not a raw resource")
                }
            } catch (e: Exception) {
                throw IllegalStateException(e)
            }

        }

        private fun getLicensesText(
            context: Context, notices: Notices, showFullLicenseText: Boolean,
            includeOwnLicense: Boolean, style: String?
        ): String {
            try {
                if (includeOwnLicense) {
                    val noticeList = notices.notices
                    noticeList.add(LICENSES_DIALOG_NOTICE)
                }
                return NoticesHtmlBuilder.create(context).setShowFullLicenseText(showFullLicenseText)
                    .setNotices(notices).setStyle(style).build()
            } catch (e: Exception) {
                throw IllegalStateException(e)
            }

        }

        private fun getSingleNoticeNotices(notice: Notice): Notices {
            val notices = Notices()
            notices.addNotice(notice)
            return notices
        }

        fun setTitle(titleId: Int): Builder {
            mTitleText = mContext.getString(titleId)
            return this
        }

        fun setTitle(title: String): Builder {
            mTitleText = title
            return this
        }

        fun setCloseText(closeId: Int): Builder {
            mCloseText = mContext.getString(closeId)
            return this
        }

        fun setCloseText(closeText: String): Builder {
            mCloseText = closeText
            return this
        }

        fun setNotices(rawNoticesId: Int): Builder {
            mRawNoticesId = rawNoticesId
            mNotices = null
            return this
        }

        fun setNotices(notices: Notices): Builder {
            mNotices = notices
            mRawNoticesId = null
            return this
        }

        fun setNotices(notice: Notice): Builder {
            return setNotices(getSingleNoticeNotices(notice))
        }

        internal fun setNotices(notices: String): Builder {
            mNotices = null
            mRawNoticesId = null
            mNoticesText = notices
            return this
        }

        fun setNoticesCssStyle(cssStyleTextId: Int): Builder {
            mNoticesStyle = mContext.getString(cssStyleTextId)
            return this
        }

        fun setNoticesCssStyle(cssStyleText: String): Builder {
            mNoticesStyle = cssStyleText
            return this
        }

        fun setShowFullLicenseText(showFullLicenseText: Boolean): Builder {
            mShowFullLicenseText = showFullLicenseText
            return this
        }

        fun setIncludeOwnLicense(includeOwnLicense: Boolean): Builder {
            mIncludeOwnLicense = includeOwnLicense
            return this
        }

        fun setThemeResourceId(themeResourceId: Int): Builder {
            mThemeResourceId = themeResourceId
            return this
        }

        fun setDividerColor(dividerColor: Int): Builder {
            mDividerColor = dividerColor
            return this
        }

        fun setDividerColorId(dividerColorId: Int): Builder {
            mDividerColor = mContext.resources.getColor(dividerColorId)
            return this
        }

        fun build(): LicensesDialog {
            val licensesText: String = when {
                mNotices != null -> getLicensesText(
                    mContext,
                    mNotices!!, mShowFullLicenseText, mIncludeOwnLicense, mNoticesStyle
                )
                mRawNoticesId != null -> getLicensesText(
                    mContext, getNotices(mContext, mRawNoticesId!!), mShowFullLicenseText, mIncludeOwnLicense,
                    mNoticesStyle
                )
                mNoticesText != null -> mNoticesText!!
                else -> throw IllegalStateException("Notices have to be provided, see setNotices")
            }

            return LicensesDialog(mContext, licensesText, mTitleText!!, mCloseText!!, mThemeResourceId, mDividerColor)
        }

    }

    companion object {
        val LICENSES_DIALOG_NOTICE = Notice(
            "LicensesDialog", "http://psdev.de/LicensesDialog",
            "Copyright 2013-2016 Philip Schiffer",
            ApacheSoftwareLicense20()
        )

        private fun createWebView(context: Context): WebView {
            val webView = WebView(context)
            webView.settings.setSupportMultipleWindows(true)
            webView.webChromeClient = object : WebChromeClient() {
                override fun onCreateWindow(
                    view: WebView,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message
                ): Boolean {
                    val result = view.hitTestResult
                    val data = result.extra
                    if (data != null) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(data))
                        context.startActivity(browserIntent)
                    }
                    return false
                }
            }
            return webView
        }
    }
}