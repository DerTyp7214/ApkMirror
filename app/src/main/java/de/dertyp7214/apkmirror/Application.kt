/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package de.dertyp7214.apkmirror

import android.app.Activity
import android.app.ProgressDialog
import android.app.UiModeManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.dertyp7214.themeablecomponents.components.ThemeableProgressBar
import com.dertyp7214.themeablecomponents.controller.AppController
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import de.dertyp7214.apkmirror.common.Adapter
import de.dertyp7214.apkmirror.common.Helper
import de.dertyp7214.apkmirror.common.HtmlParser
import de.dertyp7214.apkmirror.screens.AppDataScreen

class Application : AppController() {

    private var thread: Thread? = null

    override fun onCreate() {
        super.onCreate()
        val accentColor = Helper.getAttrColor(this, android.R.attr.colorAccent)
        themeManager = ThemeManager.getInstance(this)
        themeManager.setDefaultAccent(accentColor)
        themeManager.changeAccentColor(accentColor)
        themeManager.darkMode = (getSystemService(Context.UI_MODE_SERVICE) as UiModeManager).nightMode ==
                UiModeManager.MODE_NIGHT_YES

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity?) {
            }

            override fun onActivityResumed(activity: Activity?) {
                handleNetwork(activity)
            }

            override fun onActivityStarted(activity: Activity?) {
                handleNetwork(activity)
            }

            override fun onActivityDestroyed(activity: Activity?) {
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            }

            override fun onActivityStopped(activity: Activity?) {
            }

            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                handleNetwork(activity)
            }
        })
    }

    private fun handleNetwork(activity: Activity?, loop: Boolean = false) {
        try {
            if (thread != null)
                if (thread!!.isAlive)
                    thread!!.interrupt()
            thread = Thread {
                if (!isNetworkAvailable()) {
                    try {
                        activity!!.runOnUiThread {
                            activity.findViewById<TextView>(R.id.noConnection).visibility = VISIBLE
                            activity.window.statusBarColor = activity.resources.getColor(android.R.color.holo_red_light)
                        }
                    } catch (e: Exception) {
                    }
                } else {
                    try {
                        activity!!.runOnUiThread {
                            activity.findViewById<TextView>(R.id.noConnection).visibility = GONE
                            if (activity is AppDataScreen) {
                                activity.window.statusBarColor = activity.color
                                if (loop) {
                                    if (Adapter.currentApp != null && !activity.loaded) {
                                        val app = Adapter.currentApp!!
                                        Adapter.progressDialog = ProgressDialog.show(activity, "", "Loading data...")
                                        Adapter.progressDialog!!.setIndeterminateDrawable(ThemeableProgressBar(activity).indeterminateDrawable)
                                        Thread {
                                            Adapter.apps[app.url] = HtmlParser(activity).getAppScreenData(app, true)
                                            activity.runOnUiThread {
                                                activity.startActivity(activity.intent)
                                                activity.finish()
                                            }
                                        }.start()
                                    }
                                }
                            } else
                                activity.window.statusBarColor =
                                        activity.resources.getColor(R.color.ic_launcher_background)
                        }
                    } catch (e: Exception) {
                    }
                }
                try {
                    var last = isNetworkAvailable()
                    while (!thread!!.isInterrupted) {
                        Thread.sleep(500)
                        if (last != isNetworkAvailable()) {
                            last = isNetworkAvailable()
                            handleNetwork(activity, true)
                        }
                    }
                } catch (e: Exception) {
                }
            }
            thread!!.start()
        } catch (e: Exception) {
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun getManager(): ThemeManager {
        return themeManager
    }
}
