package com.dertyp7214.apkmirror

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.dertyp7214.apkmirror.common.Adapter
import com.dertyp7214.apkmirror.common.Helper
import com.dertyp7214.apkmirror.common.HtmlParser
import com.dertyp7214.apkmirror.objects.App
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var appList: ArrayList<App>
    private lateinit var adapter: Adapter
    private lateinit var htmlParser: HtmlParser
    private var thread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val themeManager = ThemeManager.getInstance(this)
        themeManager.enableStatusAndNavBar(this)
        themeManager.darkMode = true
        themeManager.changeAccentColor(Color.RED)
        themeManager.changePrimaryColor(Color.parseColor("#66AD21"))

        Helper.changeLogs(this).showDialogOnVersionChange()

        htmlParser = HtmlParser(this)
        appList = ArrayList()
        adapter = Adapter(this, appList)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    private fun search(query: String) {
        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0
        appList.clear()
        adapter.notifyDataSetChanged()
        if (thread != null) thread!!.interrupt()
        thread = Thread {
            var loading = true
            var i = 0
            while (loading) {
                i++
                htmlParser.page = i
                val list = htmlParser.getAppList(query)
                loading = list.size == 10
                appList.addAll(list)
                runOnUiThread {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                        progressBar.setProgress((i * 1.5).toInt(), true)
                    else progressBar.progress = i
                    adapter.notifyDataSetChanged()
                }
            }
            runOnUiThread {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    progressBar.setProgress(100, true)
                else progressBar.progress = 100
                Handler().postDelayed({
                    progressBar.visibility = View.GONE
                }, 100)
            }
        }
        thread!!.start()
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
