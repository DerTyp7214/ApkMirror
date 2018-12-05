package com.dertyp7214.apkmirror.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
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
import com.dertyp7214.apkmirror.R
import com.dertyp7214.apkmirror.common.NetworkTools.Companion.drawableFromUrl
import com.dertyp7214.apkmirror.common.NetworkTools.Companion.fastLoadImage
import com.dertyp7214.apkmirror.objects.App
import com.dertyp7214.apkmirror.objects.AppScreenData
import com.dertyp7214.apkmirror.screens.AppDataScreen
import com.dertyp7214.themeablecomponents.components.ThemeableProgressBar

class Adapter(private val activity: Activity, private var items: ArrayList<App>) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(activity).inflate(R.layout.app_list_item, null, false)
        return ViewHolder(v)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var progressDialog: ProgressDialog? = null
        val apps = HashMap<String, AppScreenData>()
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = items[position]

        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

        holder.card.layoutParams =
                ViewGroup.LayoutParams(displayMetrics.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        holder.title.text = app.title
        holder.dev.text = app.dev

        val img = fastLoadImage(app.imageUrl)
        holder.icon.setImageDrawable(img)
        if (img != null) {
            holder.progressBar.visibility = View.INVISIBLE
        } else {
            holder.icon.visibility = View.INVISIBLE
            Thread {
                val image = drawableFromUrl(activity, app.imageUrl)
                activity.runOnUiThread {
                    holder.icon.setImageDrawable(image)
                    holder.icon.visibility = View.VISIBLE
                    holder.progressBar.visibility = View.INVISIBLE
                }
            }.start()
        }

        holder.ly.setOnClickListener {
            progressDialog = ProgressDialog.show(activity, "", "Loading data...")
            progressDialog!!.setIndeterminateDrawable(ThemeableProgressBar(activity).indeterminateDrawable)
            Thread {
                apps[app.url] = HtmlParser(activity).getAppScreenData(app)
                activity.runOnUiThread {
                    val intent = Intent(activity, AppDataScreen::class.java)
                    intent.putExtra("url", app.url)
                    activity.startActivity(intent)
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
}