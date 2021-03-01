/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

@file:Suppress("DEPRECATION")

package de.dertyp7214.apkmirror.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.app.ProgressDialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.DisplayMetrics
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.content.edit
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.themeablecomponents.components.ThemeableProgressBar
import de.dertyp7214.apkmirror.R
import de.dertyp7214.apkmirror.common.NetworkTools.Companion.drawableFromUrl
import de.dertyp7214.apkmirror.common.NetworkTools.Companion.fastLoadImage
import de.dertyp7214.apkmirror.objects.App
import de.dertyp7214.apkmirror.objects.AppScreenData
import de.dertyp7214.apkmirror.screens.About
import de.dertyp7214.apkmirror.screens.AppDataScreen
import de.dertyp7214.apkmirror.screens.MainActivity
import de.dertyp7214.apkmirror.screens.MainActivity.Companion.showHiddenApps
import org.json.JSONArray

class Adapter(private val activity: Activity, private var items: ArrayList<App>) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(activity).inflate(R.layout.app_list_item, parent, false)
        return ViewHolder(v)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var progressDialog: ProgressDialog? = null
        val apps = HashMap<String, AppScreenData>()
        var clicked = false
        var currentApp: App? = null
    }

    private fun JSONArray.forEach(unit: (item: Any, index: Int) -> Unit) {
        for (i in 0 until length())
            unit(this[i], i)
    }

    private fun JSONArray.clone(): JSONArray {
        return JSONArray(toString())
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

        if (app.packageName.isNotBlank()) {
            holder.ly.setOnLongClickListener {
                val popup = PopupMenu(activity, it)
                val inflater = popup.menuInflater
                inflater.inflate(R.menu.app_menu, popup.menu)
                val hideApp = popup.menu.findItem(R.id.action_hide_app)
                if (showHiddenApps) hideApp.setTitle(R.string.show_app)
                hideApp.setOnMenuItemClickListener {
                    val sharedPreferences = activity.getSharedPreferences("check_updates", MODE_PRIVATE)
                    val jsonArray = JSONArray(sharedPreferences.getString("hidden_apps", "[]"))
                    if (showHiddenApps) {
                        jsonArray.clone().forEach { item, index ->
                            if (app.packageName == item) jsonArray.remove(index)
                        }
                        MainActivity.loadHiddenApps = true
                    } else {
                        jsonArray.put(app.packageName)
                        MainActivity.checkUpdates = true
                    }
                    sharedPreferences.edit {
                        putString("hidden_apps", jsonArray.toString())
                    }
                    activity.finish()
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                    true
                }
                popup.show()
                true
            }
        } else {
            holder.ly.setOnLongClickListener { true }
        }
        holder.ly.setOnClickListener {
            if (!clicked) {
                clicked = true
                if (app.imageUrl == "self:launcher_icon") {
                    val icon = Pair.create<View, String>(holder.icon, "icon")
                    val options = ActivityOptions.makeSceneTransitionAnimation(activity, icon)
                    activity.startActivity(Intent(activity, About::class.java), options.toBundle())
                } else {
                    if (!apps.containsKey(app.url)) {
                        progressDialog = ProgressDialog.show(activity, "", "Loading data...")
                        progressDialog!!.setIndeterminateDrawable(ThemeableProgressBar(activity).indeterminateDrawable)
                    }
                    Thread {
                        if (!apps.containsKey(app.url)) apps[app.url] = HtmlParser(activity).getAppScreenData(app)
                        activity.runOnUiThread {
                            val intent = Intent(activity, AppDataScreen::class.java)
                            intent.putExtra("url", app.url)
                            activity.startActivity(intent)
                            currentApp = app
                        }
                    }.start()
                }
            }
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