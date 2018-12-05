package com.dertyp7214.apkmirror.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.dertyp7214.apkmirror.R
import com.dertyp7214.apkmirror.common.NetworkTools.Companion.drawableFromUrl
import com.dertyp7214.apkmirror.objects.AppVariant
import com.dertyp7214.themeablecomponents.components.ThemeableProgressBar
import java.io.File


class VariantAdapter(private var context: Activity, private var items: ArrayList<AppVariant>) :
    RecyclerView.Adapter<VariantAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_variant, null, false)

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
        holder.title.text = app.app.title
        holder.version.text = app.version
        holder.dpi.text = app.dpi
        holder.androidVersion.text = app.androidVersion
        holder.architecture.text = app.arch
        holder.icon.visibility = View.INVISIBLE

        Thread {
            val image = drawableFromUrl(context, app.app.imageUrl)
            context.runOnUiThread {
                holder.icon.setImageDrawable(image)
                holder.icon.visibility = View.VISIBLE
                holder.progressBar.visibility = View.INVISIBLE
            }
        }.start()

        holder.ly.setOnClickListener {
            MaterialDialog.Builder(context)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.no)
                .title("Download")
                .content("Do you want to download \"${app.app.title}\"")
                .onPositive { dialog, _ ->
                    dialog.dismiss()
                    progressDialog = ProgressDialog.show(context, "", "Download (0%)")
                    progressDialog!!.setIndeterminateDrawable(ThemeableProgressBar(context).indeterminateDrawable)
                    Thread {
                        val htmlParser = HtmlParser(context)
                        val url = htmlParser.getApkUrl(htmlParser.getDownloadPage(app).variants[0].url)
                        context.runOnUiThread {
                            progressDialog!!.setMessage("Download (1%)")
                        }
                        htmlParser.openInstaller(url, object : HtmlParser.Listener {
                            override fun run(progress: Int) {
                                context.runOnUiThread {
                                    if (progress > 0) progressDialog!!.setMessage("Download ($progress%)")
                                }
                            }

                            override fun cancel() {
                                context.runOnUiThread {
                                    progressDialog!!.dismiss()
                                }
                            }

                            override fun stop(file: File) {
                                context.runOnUiThread {
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

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var card: CardView = v.findViewById(R.id.card)
        var ly: LinearLayout = v.findViewById(R.id.ly)
        val progressBar: ProgressBar = v.findViewById(R.id.progressBar)
        val icon: ImageView = v.findViewById(R.id.img_icon)
        val title: TextView = v.findViewById(R.id.txt_title)
        val version: TextView = v.findViewById(R.id.txt_version)
        val dpi: TextView = v.findViewById(R.id.txt_dpi)
        val androidVersion: TextView = v.findViewById(R.id.txt_androidversion)
        val architecture: TextView = v.findViewById(R.id.txt_architecture)
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
