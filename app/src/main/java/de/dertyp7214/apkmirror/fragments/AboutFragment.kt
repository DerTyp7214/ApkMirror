package de.dertyp7214.apkmirror.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.util.DisplayMetrics
import android.view.ViewGroup
import com.danielstone.materialaboutlibrary.MaterialAboutFragment
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic
import de.dertyp7214.apkmirror.BuildConfig
import de.dertyp7214.apkmirror.R
import de.dertyp7214.apkmirror.common.Helper
import de.dertyp7214.apkmirror.common.NetworkTools.Companion.drawableFromUrl
import java.io.File

class AboutFragment : MaterialAboutFragment() {
    override fun onResume() {
        super.onResume()

        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)

        Handler().postDelayed({
            val animator = ValueAnimator.ofInt(displayMetrics.heightPixels, 0)
            animator.duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong() * 2
            animator.addUpdateListener {
                (view as ViewGroup).setPadding(0, it.animatedValue as Int, 0, 0)
            }
            animator.start()
        }, resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
    }

    override fun getMaterialAboutList(activityContext: Context): MaterialAboutList {

        val themeManager = ThemeManager.getInstance(context!!)
        val iconColor = if (themeManager.darkMode) Color.WHITE else Color.BLACK
        val icon = { iconId: IIcon -> IconicsDrawable(context!!).icon(iconId).color(iconColor) }

        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)

        (view as ViewGroup).getChildAt(0).setPadding(0, convertDpToPixel(130F).toInt(), 0, convertDpToPixel(7F).toInt())
        (view as ViewGroup).setPadding(0, displayMetrics.heightPixels, 0, 0)

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
            .build()

        val developers = MaterialAboutCard.Builder()
            .title(R.string.title_developers)
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text(R.string.key_leaddev)
                    .subText(R.string.dev)
                    .icon(drawableFromUrl(context!!, getString(R.string.dev_github_userimage)))
                    .build()
            )
            .addItem(MaterialAboutActionItem.Builder()
                .text(R.string.key_leaddev_github)
                .subText(R.string.dev_url)
                .icon(icon(MaterialDesignIconic.Icon.gmi_github))
                .setOnClickAction {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.dev_url))))
                }
                .build())
            .build()

        val licenses = MaterialAboutCard.Builder()
            .title(R.string.title_licenses)
            .addItem(
                MaterialAboutActionItem.Builder()
                    .text("PLACEHOLDER")
                    .subText("PLACEHOLDER")
                    .icon(icon(MaterialDesignIconic.Icon.gmi_file_text))
                    .setOnClickAction {
                        Snackbar.make(view!!, "BottomSheet", Snackbar.LENGTH_LONG).show()
                    }
                    .build()
            )
            .build()

        return MaterialAboutList.Builder()
            .addCard(applicationInfo)
            .addCard(developers)
            .addCard(licenses)
            .build()
    }

    override fun getTheme(): Int {
        return com.danielstone.materialaboutlibrary.R.style.Theme_Mal_Dark
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
