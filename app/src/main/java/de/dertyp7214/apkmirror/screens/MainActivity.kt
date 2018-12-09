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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val themeManager = ThemeManager.getInstance(this)
        themeManager.enableStatusAndNavBar(this)
        themeManager.darkMode = true
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
            search("ApkMirror")
        }
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - lastTimeStamp > 4000) {
            Toast.makeText(this, "Press back again to close the app.", Toast.LENGTH_LONG).show()
            lastTimeStamp = System.currentTimeMillis()
        } else super.onBackPressed()
    }

    @SuppressLint("SimpleDateFormat")
    private fun search(query: String, callBack: () -> Unit = {}) {
        title = query
        if (thread != null) thread!!.interrupt()
        appList.clear()
        adapter.notifyDataSetChanged()
        if (Config.knownNames.contains(query.toLowerCase())) {
            title = getString(R.string.app_name)
            progressDialog?.dismiss()
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
        } else {
            progressBar.visibility = View.VISIBLE
            progressBar.progress = 0
            title = query
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
