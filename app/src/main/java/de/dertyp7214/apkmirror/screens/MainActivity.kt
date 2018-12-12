package de.dertyp7214.apkmirror.screens

import android.annotation.SuppressLint
import android.app.ProgressDialog
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertyp7214.themeablecomponents.components.ThemeableProgressBar
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import de.dertyp7214.apkmirror.BuildConfig
import de.dertyp7214.apkmirror.R
import de.dertyp7214.apkmirror.common.Adapter
import de.dertyp7214.apkmirror.common.Config
import de.dertyp7214.apkmirror.common.Helper.Companion.showChangeDialog
import de.dertyp7214.apkmirror.common.HtmlParser
import de.dertyp7214.apkmirror.objects.App
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appList: ArrayList<App>
    private lateinit var adapter: Adapter
    private lateinit var htmlParser: HtmlParser
    private var thread: Thread? = null
    private var progressDialog: ProgressDialog? = null
    private var lastTimeStamp = 0L

    companion object {
        var checkUpdates = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val themeManager = ThemeManager.getInstance(this)
        themeManager.enableStatusAndNavBar(this)
        themeManager.changeAccentColor(Color.RED)
        themeManager.changePrimaryColor(resources.getColor(R.color.ic_launcher_background))
        themeManager.setDefaultAccent(Color.RED)
        themeManager.setDefaultPrimary(resources.getColor(R.color.ic_launcher_background))

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
        if (System.currentTimeMillis() - lastTimeStamp > 4000) {
            Toast.makeText(this, "Press back again to close the app.", Toast.LENGTH_LONG).show()
            lastTimeStamp = System.currentTimeMillis()
        } else super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        if (checkUpdates) {
            checkUpdates()
            checkUpdates = false
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun checkUpdates(callBack: () -> Unit = {}) {
        title = "Updates"
        appList.clear()
        adapter.notifyDataSetChanged()
        if (thread != null) thread!!.interrupt()
        progressDialog = ProgressDialog.show(this@MainActivity, "", "Reading packages (0%)")
        progressDialog!!.setIndeterminateDrawable(ThemeableProgressBar(this@MainActivity).indeterminateDrawable)
        thread = Thread {
            val startTime = System.currentTimeMillis()
            val packages = packageManager.getInstalledApplications(0)
            packages.forEachIndexed { index, info ->
                val list = htmlParser.getAppList(info.packageName, "apk")
                try {
                    appList.addAll(Arrays.asList(list.first {
                        it.packageName = info.packageName
                        !it.title.contains("Wear OS")
                    }))
                } catch (e: Exception) {
                }
                this@MainActivity.runOnUiThread {
                    val percentage = ((index.toFloat() / packages.size.toFloat()) * 1000).toInt().toFloat() / 10
                    val currentTime = System.currentTimeMillis()
                    val time = ((currentTime - startTime) / (index + 1) * (packages.size - index))
                    val date = Date(time)
                    val formatter = SimpleDateFormat("mm:ss")
                    formatter.timeZone = TimeZone.getTimeZone("UTC")
                    progressDialog?.setMessage("Reading packages ($percentage%)\n${formatter.format(date)} Minutes left")
                }
            }
            this@MainActivity.runOnUiThread {
                val tmpList: List<App> = appList.clone() as List<App>
                appList.clear()
                appList.addAll(tmpList.filter {
                    it.version.trim() != packageManager.getPackageInfo(it.packageName, 0).versionName.trim()
                })
                appList.sortWith(Comparator { o1, o2 ->
                    when {
                        o1.title < o2.title -> -1
                        o1.title >= o2.title -> 1
                        else -> 0
                    }
                })
                adapter.notifyDataSetChanged()
                progressDialog?.dismiss()
                Handler().postDelayed({
                    callBack()
                }, 100)
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
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        val myActionMenuItem: MenuItem = menu!!.findItem(R.id.action_search)
        myActionMenuItem.icon.setTintList(ColorStateList.valueOf(Color.WHITE))
        val searchView = myActionMenuItem.actionView as SearchView
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
        return true
    }
}
