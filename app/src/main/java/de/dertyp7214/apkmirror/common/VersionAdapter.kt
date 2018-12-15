/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package de.dertyp7214.apkmirror.common

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.dertyp7214.themeablecomponents.components.ThemeableProgressBar
import de.dertyp7214.apkmirror.R
import de.dertyp7214.apkmirror.common.NetworkTools.Companion.drawableFromUrl
import de.dertyp7214.apkmirror.objects.App
import java.io.File

class VersionAdapter(private var context: AppCompatActivity, private var items: ArrayList<App>) :
    RecyclerView.Adapter<VersionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_version, null, false)

        return ViewHolder(v)
    }

    override fun getItemCount(): Int = items.size

    var height: Int = 0
        get() {
            return convertDpToPixel(((if (itemCount > 5) 5.5F else itemCount - 0.5F) * 65)).toInt()
        }
        private set(value) {
            field = value
        }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var progressDialog: ProgressDialog? = null
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = items[position]

        val displayMetrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(displayMetrics)

        holder.card.layoutParams =
                ViewGroup.LayoutParams(displayMetrics.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        holder.title.text = app.title
        holder.dev.text = app.dev
        holder.icon.visibility = View.INVISIBLE

        Thread {
            val image = drawableFromUrl(context, app.imageUrl)
            context.runOnUiThread {
                holder.icon.setImageDrawable(image)
                holder.icon.visibility = View.VISIBLE
                holder.progressBar.visibility = View.INVISIBLE
            }
        }.start()

        holder.ly.setOnClickListener {
            progressDialog = ProgressDialog.show(context, "", "Loading data...")
            progressDialog!!.setIndeterminateDrawable(ThemeableProgressBar(context).indeterminateDrawable)
            Thread {
                val htmlParser = HtmlParser(context)
                val variantAdapter = VariantAdapter(context, htmlParser.getDownloadPage(app, app.url).variants)
                val variantBottomSheet = BottomSheet(context.getString(R.string.titleVariants), variantAdapter)
                if (htmlParser.getDownloadPage(app, app.url).variants.size > 0)
                    context.runOnUiThread {
                        progressDialog!!.dismiss()
                        BottomSheet.close("Versions")
                        variantBottomSheet.show(context.supportFragmentManager, "Variants")
                    }
                else {
                    context.runOnUiThread {
                        progressDialog!!.dismiss()
                        MaterialDialog.Builder(context)
                            .positiveText(android.R.string.ok)
                            .negativeText(android.R.string.no)
                            .title("Download")
                            .content("Do you want to download \"${app.title}\"")
                            .onPositive { dialog, _ ->
                                dialog.dismiss()
                                Thread {
                                    this@VersionAdapter.context.runOnUiThread {
                                        progressDialog = ProgressDialog.show(context, "", "Download (0%)")
                                        progressDialog!!.setIndeterminateDrawable(ThemeableProgressBar(context).indeterminateDrawable)
                                    }
                                    val url = htmlParser.getApkUrl(app.url)
                                    this@VersionAdapter.context.runOnUiThread {
                                        progressDialog!!.setMessage("Download (1%)")
                                    }
                                    htmlParser.openInstaller(url, object : HtmlParser.Listener {
                                        override fun run(progress: Int) {
                                            this@VersionAdapter.context.runOnUiThread {
                                                if (progress > 0) progressDialog!!.setMessage("Download ($progress%)")
                                            }
                                        }

                                        override fun cancel() {
                                            this@VersionAdapter.context.runOnUiThread {
                                                progressDialog!!.dismiss()
                                            }
                                        }

                                        override fun stop(file: File) {
                                            this@VersionAdapter.context.runOnUiThread {
                                                progressDialog!!.dismiss()
                                                htmlParser.installApk(file)
                                            }
                                        }
                                    })
                                }.start()
                            }
                            .onNegative { dialog, _ ->
                                dialog.dismiss()
                            }
                            .build()
                            .show()
                    }
                }
            }.start()
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.img_icon)
        val title: TextView = v.findViewById(R.id.txt_title)
        val dev: TextView = v.findViewById(R.id.txt_dev)
        val ly: LinearLayout = v.findViewById(R.id.ly)
        val card: CardView = v.findViewById(R.id.card)
        val progressBar: ProgressBar = v.findViewById(R.id.progressBar)
    }

    private fun convertDpToPixel(dp: Float): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    private fun convertPixelsToDp(px: Float): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}
