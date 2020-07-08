/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

@file:Suppress("DEPRECATION")

package de.dertyp7214.apkmirror.screens

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertyp7214.themeablecomponents.components.ThemeableProgressBar
import com.fede987.statusbaralert.StatusBarAlert
import de.dertyp7214.apkmirror.Application
import de.dertyp7214.apkmirror.BuildConfig
import de.dertyp7214.apkmirror.R
import de.dertyp7214.apkmirror.common.Adapter
import de.dertyp7214.apkmirror.common.Comparators
import de.dertyp7214.apkmirror.common.Config
import de.dertyp7214.apkmirror.common.Helper.Companion.showChangeDialog
import de.dertyp7214.apkmirror.common.HtmlParser
import de.dertyp7214.apkmirror.objects.App
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var appList: ArrayList<App>
    private lateinit var adapter: Adapter
    private lateinit var htmlParser: HtmlParser
    private var mainAction: MenuItem? = null
    private var thread: Thread? = null
    private var progressDialog: ProgressDialog? = null
    private var lastTimeStamp = 0L
    private val parallelTasks = 3

    companion object {
        var checkUpdates = false
        var showHiddenApps = false
        var loadHiddenApps = false
        var homePressed = false
        var search = ""
        var lastSearch = ""
    }

    private fun JSONArray.map(unit: (Any) -> Any): JSONArray {
        val tmp = JSONArray()
        for (i in 0 until length()) {
            tmp.put(unit(this[i]))
        }
        return tmp
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val themeManager = (application as Application).getManager()
        themeManager.enableStatusAndNavBar(this)
        themeManager.setDefaultPrimary(resources.getColor(R.color.ic_launcher_background))
        themeManager.changePrimaryColor(resources.getColor(R.color.ic_launcher_background))

        htmlParser = HtmlParser(this)
        appList = ArrayList()
        adapter = Adapter(this, appList)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        showChangeDialog(this) {
            search("ApkMirror", false)
        }
    }

    override fun onBackPressed() {
        when {
            homePressed -> {
                search(lastSearch)
                homePressed = false
            }
            System.currentTimeMillis() - lastTimeStamp > 4000 -> {
                Toast.makeText(this, "Press back again to close the app.", Toast.LENGTH_LONG).show()
                lastTimeStamp = System.currentTimeMillis()
            }
            else -> super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        when {
            checkUpdates -> {
                checkUpdates()
                checkUpdates = false
            }
            loadHiddenApps -> {
                loadHiddenApps()
                loadHiddenApps = false
            }
            search.isNotBlank() -> {
                search(search)
                search = ""
            }
        }
    }

    private fun runParallel(units: ArrayList<() -> Unit>, parallelTasks: Int, run: () -> Unit) {
        units.forEachIndexed { i, _ ->
            if (i % parallelTasks == 0) {
                val threads = ArrayList<Thread>()
                for (index in 0 until parallelTasks) {
                    if (units.size >= i + index + 1) {
                        val th = Thread {
                            try {
                                units[index + i]()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        th.start()
                        threads.add(th)
                    }
                }
                while (threads.any { it.isAlive }) Thread.sleep(10)
            }
        }
        run()
    }

    private fun getApps(array: JSONArray): List<String> {
        val list = ArrayList<String>()
        for (i in 0 until array.length())
            list.add(array.getString(i))
        return list
    }

    private fun isHidden(packageName: String): Boolean {
        val jsonArray =
            JSONArray(getSharedPreferences("check_updates", Context.MODE_PRIVATE).getString("hidden_apps", "[]"))
        for (i in 0 until jsonArray.length()) {
            if (packageName == jsonArray.getString(i)) return true
        }
        return false
    }

    @SuppressLint("SimpleDateFormat")
    private fun loadPackages(
        startTime: Long,
        packages: List<String>,
        availablePackages: ArrayList<String>,
        unit: () -> Unit
    ) {
        val units = ArrayList<() -> Unit>()
        val loadList = if (availablePackages.size != packages.size) packages.filter {
            !isHidden(it)
        } else packages
        loadList.forEachIndexed { index, info ->
            units.add {
                val list = htmlParser.getAppList(info, "apk")
                if (list.size > 0 && availablePackages.size != packages.size) availablePackages.add(info)
                try {
                    appList.add(list.first {
                        it.packageName = info
                        !it.title.contains("Wear OS")
                                && !it.title.contains("Android TV")
                                && !it.title.contains("Daydream")
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                this@MainActivity.runOnUiThread {
                    if (index % parallelTasks == 0) {
                        val percentage = ((index.toFloat() / loadList.size.toFloat()) * 1000).toInt().toFloat() / 10
                        val currentTime = System.currentTimeMillis()
                        val time = ((currentTime - startTime) / (index + 1) * (loadList.size - index))
                        val date = Date(time)
                        val formatter = SimpleDateFormat("mm:ss")
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        progressDialog?.setMessage("Reading packages ($percentage%)\n${formatter.format(date)} Minutes left")
                    }
                }
            }
        }
        runParallel(units, parallelTasks, unit)
    }

    private fun loadHiddenApps() {
        title = "Hidden apps"
        if (mainAction != null) mainAction!!.isVisible = true
        appList.clear()
        adapter.notifyDataSetChanged()
        if (thread != null) thread!!.interrupt()
        progressDialog = ProgressDialog.show(this@MainActivity, "", "Reading packages (0%)")
        progressDialog!!.setIndeterminateDrawable(ThemeableProgressBar(this@MainActivity).indeterminateDrawable)
        thread = Thread {
            val startTime = System.currentTimeMillis()
            val packages = packageManager.getInstalledApplications(0)
                .map { it.packageName }
                .filter {
                    isHidden(it)
                } as ArrayList
            loadPackages(startTime, packages, packages) {
                showHiddenApps = true
                appList.sortWith(Comparator { o1, o2 ->
                    when {
                        o1.title < o2.title -> -1
                        o1.title >= o2.title -> 1
                        else -> 0
                    }
                })
                this@MainActivity.runOnUiThread {
                    adapter.notifyDataSetChanged()
                    progressDialog?.dismiss()
                }
            }
        }
        thread?.start()
    }

    private fun checkUpdates() {
        title = "Updates"
        if (mainAction != null) mainAction!!.isVisible = true
        appList.clear()
        adapter.notifyDataSetChanged()
        if (thread != null) thread!!.interrupt()
        progressDialog = ProgressDialog.show(this@MainActivity, "", "Reading packages (0%)")
        progressDialog!!.setIndeterminateDrawable(ThemeableProgressBar(this@MainActivity).indeterminateDrawable)
        thread = Thread {
            val startTime = System.currentTimeMillis()
            var packages = packageManager.getInstalledApplications(0)
                .map { it.packageName }
            val packageCount = packages.size
            val availablePackages = ArrayList<String>()
            val sharedPreferences = getSharedPreferences("packages", Context.MODE_PRIVATE)
            val loadAll = if (sharedPreferences.getInt("packageCount", 0) == packageCount) {
                var ret = false
                val array = JSONArray(sharedPreferences.getString("packages", "[]"))
                for (i in 0 until array.length())
                    if (!ret) ret = !packages.contains(array.getString(i))
                ret
            } else true
            if (!loadAll) packages = getApps(JSONArray(sharedPreferences.getString("availablePackages", "[]")))
            loadPackages(startTime, packages, availablePackages) {
                showHiddenApps = false
                sharedPreferences.edit {
                    putString("availablePackages", JSONArray(availablePackages).toString())
                    putString("packages", JSONArray(packages).toString())
                    putInt("packageCount", packageCount)
                }
                this@MainActivity.runOnUiThread {
                    val tmpList: List<App?> = appList.clone() as List<App>
                    appList.clear()
                    appList.addAll(tmpList.filter {
                        if (it == null) false
                        else
                            Comparators.compareVersion(
                                it.version.trim(),
                                packageManager.getPackageInfo(it.packageName, 0).versionName.trim()
                            ) == 1
                    } as List<App>)
                    appList.sortWith(Comparator { o1, o2 ->
                        when {
                            o1.title < o2.title -> -1
                            o1.title >= o2.title -> 1
                            else -> 0
                        }
                    })
                    adapter.notifyDataSetChanged()
                    progressDialog?.dismiss()
                }
            }
        }
        thread?.start()
    }

    @SuppressLint("SimpleDateFormat")
    private fun search(query: String, search: Boolean = true, callBack: () -> Unit = {}) {
        title = query
        if (thread != null) thread!!.interrupt()
        appList.clear()
        adapter.notifyDataSetChanged()
        if (Config.knownNames.contains(query.toLowerCase())) {
            title = getString(R.string.app_name)
            if (!search) progressDialog?.dismiss()
            appList.add(
                App(
                    getString(R.string.app_name),
                    getString(R.string.dev),
                    BuildConfig.VERSION_NAME,
                    SimpleDateFormat("yyyy-MM-dd").format(Date()),
                    "NaN",
                    getString(R.string.dev_url),
                    "self:launcher_icon"
                )
            )
            adapter.notifyDataSetChanged()
        }
        if (search) {
            if (mainAction != null) mainAction!!.isVisible = true
            lastSearch = query
            progressBar.visibility = View.VISIBLE
            progressBar.progress = 0
            thread = Thread {
                var loading = true
                var i = 0
                while (loading) {
                    i++
                    htmlParser.page = i
                    val list = htmlParser.getAppList(query)
                    loading = list.size == 10
                    appList.addAll(list)
                    this@MainActivity.runOnUiThread {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                                progressBar.setProgress((i * 1.5).toInt(), true)
                            else progressBar.progress = i
                            adapter.notifyDataSetChanged()
                            progressDialog?.dismiss()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                showHiddenApps = false
                this@MainActivity.runOnUiThread {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        progressBar.setProgress(100, true)
                    else progressBar.progress = 100
                    Handler().postDelayed({
                        progressBar.visibility = View.GONE
                        callBack()
                    }, 100)
                }
            }
            thread?.start()
        } else if (Config.knownNames.contains(query.toLowerCase()) && mainAction != null)
            mainAction!!.isVisible = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val myActionMenuItem: MenuItem = menu!!.findItem(R.id.action_search)
        myActionMenuItem.icon.setTintList(ColorStateList.valueOf(Color.WHITE))
        val searchView = myActionMenuItem.actionView as SearchView
        mainAction = menu.findItem(R.id.action_main)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!searchView.isIconified) {
                    searchView.isIconified = true
                }
                myActionMenuItem.collapseActionView()
                progressDialog = ProgressDialog.show(this@MainActivity, "", "Loading data...")
                progressDialog!!.setIndeterminateDrawable(ThemeableProgressBar(this@MainActivity).indeterminateDrawable)
                search(query ?: "")
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        mainAction!!.isVisible = false
        mainAction!!.setOnMenuItemClickListener {
            homePressed = true
            search(getString(R.string.app_name), false)
            true
        }
        return true
    }
}
