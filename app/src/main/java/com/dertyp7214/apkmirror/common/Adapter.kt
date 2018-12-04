package com.dertyp7214.apkmirror.common

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.apkmirror.R
import com.dertyp7214.apkmirror.common.NetworkTools.Companion.drawableFromUrl
import com.dertyp7214.apkmirror.common.NetworkTools.Companion.fastLoadImage
import com.dertyp7214.apkmirror.objects.App

class Adapter(private val activity: Activity, private var items: ArrayList<App>) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(activity).inflate(R.layout.app_list_item, null, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = items[position]

        holder.title.text = app.title
        holder.dev.text = app.dev

        val img = fastLoadImage(app.imageUrl)
        holder.icon.setImageDrawable(img)
        if (img != null) {
            holder.progressBar.visibility = View.INVISIBLE
        } else {
            Thread {
                val image = drawableFromUrl(activity, app.imageUrl)
                activity.runOnUiThread {
                    holder.icon.setImageDrawable(image)
                    holder.progressBar.visibility = View.INVISIBLE
                }
            }.start()
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.img_icon)
        val title: TextView = v.findViewById(R.id.txt_title)
        val dev: TextView = v.findViewById(R.id.txt_dev)
        val progressBar: ProgressBar = v.findViewById(R.id.progressBar)
    }
}