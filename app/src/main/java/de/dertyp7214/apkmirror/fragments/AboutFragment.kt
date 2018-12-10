package de.dertyp7214.apkmirror.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.DisplayMetrics
import android.view.ViewGroup
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import de.dertyp7214.apkmirror.BuildConfig
import de.dertyp7214.apkmirror.R
import de.dertyp7214.apkmirror.common.Helper
import de.dertyp7214.apkmirror.common.LicensesDialog
import de.dertyp7214.apkmirror.common.NetworkTools.Companion.drawableFromUrl
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20
import de.psdev.licensesdialog.licenses.EclipsePublicLicense10
import de.psdev.licensesdialog.licenses.MITLicense
import de.psdev.licensesdialog.model.Notice
import de.psdev.licensesdialog.model.Notices
import java.io.File

class AboutFragment : MaterialAboutFragment() {
    override fun onResume() {
        super.onResume()

        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)

        /*Handler().postDelayed({
            val animator = ValueAnimator.ofInt(displayMetrics.heightPixels, 0)
            animator.duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong() * 2
            animator.addUpdateListener {
                (view as ViewGroup).setPadding(0, it.animatedValue as Int, 0, 0)
            }
            animator.start()
        }, resources.getInteger(android.R.integer.config_shortAnimTime).toLong())*/
    }

    override fun getMaterialAboutList(activityContext: Context): MaterialAboutList {

        val themeManager = ThemeManager.getInstance(activity!!)
        val iconColor = if (themeManager.darkMode) Color.WHITE else Color.BLACK
        val icon = { iconId: IIcon -> IconicsDrawable(context!!).icon(iconId).color(iconColor) }

        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)

        (view as ViewGroup).getChildAt(0).setPadding(0, convertDpToPixel(130F).toInt(), 0, convertDpToPixel(7F).toInt())
        //(view as ViewGroup).setPadding(0, displayMetrics.heightPixels, 0, 0)

        val notices = Notices()
        notices.addNotice(
            Notice(
                "Kotlin Standard Library JDK 7",
                "https://kotlinlang.org/",
                "JetBrains",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "Android AppCompat Library V7",
                "https://developer.android.com/topic/libraries/support-library/packages",
                "The Android Open Source Project",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "PRDownloader",
                "https://github.com/MindorksOpenSource/PRDownloader",
                "Mindorks",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "Android Support Palette V7",
                "https://developer.android.com/topic/libraries/support-library/packages",
                "The Android Open Source Project",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "Material Dialogs",
                "https://github.com/afollestad/material-dialogs",
                "Afollestad",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "Core Kotlin Extensions",
                "https://android.github.io/android-ktx/core-ktx/",
                "The Android Open Source Project",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "Android Support Library V4",
                "https://developer.android.com/topic/libraries/support-library/",
                "The Android Open Source Project",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "Material Components For Android",
                "https://material.io/develop/android/docs/getting-started/",
                "The Android Open Source Project",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "Android ConstraintLayout",
                "https://developer.android.com/training/constraint-layout/",
                "The Android Open Source Project",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "ThemeableComponents",
                "https://github.com/DerTyp7214/ThemeableComponents",
                "Josua Lengwenath",
                MITLicense()
            )
        )
        notices.addNotice(
            Notice(
                "ChangeLogLib",
                "https://github.com/DerTyp7214/ChangeLogLib",
                "Josua Lengwenath",
                MITLicense()
            )
        )
        notices.addNotice(
            Notice(
                "material-about-library",
                "https://github.com/daniel-stoneuk/material-about-library",
                "Daniel Stoneuk",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "Android Iconics Library",
                "https://github.com/mikepenz/Android-Iconics",
                "Mike Penz",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "Android Iconics Google Material Typeface Library",
                "https://github.com/mikepenz/Android-Iconics",
                "Mike Penz",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "Android Iconics Material Design Iconic Typeface Library",
                "https://github.com/mikepenz/Android-Iconics",
                "Mike Penz",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "Apache Commons Lang",
                "https://commons.apache.org/proper/commons-lang/",
                "Apache",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(Notice("JUnit", "https://junit.org/junit5/", "JUnit", EclipsePublicLicense10()))
        notices.addNotice(
            Notice(
                "AndroidX Test Library",
                "https://developer.android.com/training/testing/set-up-project",
                "The Android Open Source Project",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "Android Support RecyclerView V7",
                "https://developer.android.com/topic/libraries/support-library/packages",
                "The Android Open Source Project",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "Android Support CardView V7",
                "https://developer.android.com/topic/libraries/support-library/packages",
                "The Android Open Source Project",
                ApacheSoftwareLicense20()
            )
        )
        notices.addNotice(
            Notice(
                "LicensesDialog",
                "http://psdev.de/LicensesDialog",
                "Copyright 2013-2016 Philip Schiffer",
                ApacheSoftwareLicense20()
            )
        )

        val applicationInfo = MaterialAboutCard.Builder()
            .title(R.string.app_name)
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(R.string.key_version)
                    .subText(BuildConfig.VERSION_NAME)
                    .icon(icon(GoogleMaterial.Icon.gmd_info_outline))
                    .build()
            )
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(R.string.key_buildtype)
                    .subText(BuildConfig.BUILD_TYPE)
                    .icon(icon(GoogleMaterial.Icon.gmd_build))
                    .build()
            )
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(R.string.key_size)
                    .subText(getAppSize())
                    .icon(icon(GoogleMaterial.Icon.gmd_storage))
                    .build()
            )
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(R.string.key_packagename)
                    .subText(BuildConfig.APPLICATION_ID)
                    .icon(icon(GoogleMaterial.Icon.gmd_archive))
                    .build()
            )
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(R.string.title_licenses)
                    .icon(icon(MaterialDesignIconic.Icon.gmi_file_text))
                    .setOnClickAction {
                        LicensesDialog.Builder(activity!!)
                            .setNotices(notices)
                            .setNoticesCssStyle(
                                """
                                * {
                                    background: ${if (themeManager.darkMode) "#424242" else "#FFFFFF"};
                                    color: ${if (themeManager.darkMode) "#FFFFFF" else "#000000"}
                                }
                                p.license {
                                    background:grey;
                                }
                                body {
                                    font-family: sans-serif;
                                    overflow-wrap: break-word;
                                }
                                pre {
                                    background-color: ${if (themeManager.darkMode) "#FFFFFF" else "#000000"}18;
                                    padding: 1em;
                                    white-space: pre-wrap;
                                }
                                a {
                                    color: #${Integer.toHexString(themeManager.colorAccent).substring(2)};
                                    padding-left: 2px;
                                    padding-right: 2px;
                                }
                            """.trimIndent()
                            )
                            .build()
                            .show()
                    }
                    .build()
            )
            .addItem(MaterialAboutActionItem.Builder()
                .text("Toggle Darkmode")
                .subText(if (themeManager.darkMode) "enabled" else "disabled")
                .icon(icon(MaterialDesignIconic.Icon.gmi_invert_colors))
                .setOnClickAction {
                    themeManager.darkMode = !themeManager.darkMode
                    activity!!.recreate()
                }
                .build())
            .build()

        val developers = MaterialAboutCard.Builder()
            .title(R.string.title_developers)
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(R.string.dev)
                    .subText(R.string.dev_url)
                    .icon(drawableFromUrl(context!!, getString(R.string.dev_github_userimage)))
                    .setOnClickAction {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.dev_url))))
                    }
                    .build()
            ).build()

        return MaterialAboutList.Builder()
            .addCard(applicationInfo)
            .addCard(developers)
            .build()
    }

    override fun getTheme(): Int {
        return if (ThemeManager.getInstance(context!!).darkMode)
            com.danielstone.materialaboutlibrary.R.style.Theme_Mal_Dark
        else
            com.danielstone.materialaboutlibrary.R.style.Theme_Mal_Light
    }

    @SuppressLint("SdCardPath")
    private fun getAppSize(): String {
        val folder = File("/data/data/${BuildConfig.APPLICATION_ID}")
        return Helper.humanReadableByteCount(Helper.folderSize(getApkDir()) + Helper.folderSize(folder))
    }

    private fun getApkDir(): File {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = context!!.packageManager.queryIntentActivities(mainIntent, 0)
        apps.forEach {
            if (it.activityInfo.packageName == BuildConfig.APPLICATION_ID)
                return File(it.activityInfo.applicationInfo.publicSourceDir.replace("/base.apk", ""))
        }
        return File("")
    }

    private fun convertDpToPixel(dp: Float): Float {
        val resources = context!!.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}
