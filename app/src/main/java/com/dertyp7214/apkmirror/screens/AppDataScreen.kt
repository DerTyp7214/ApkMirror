@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.dertyp7214.apkmirror.screens

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dertyp7214.apkmirror.R
import com.dertyp7214.apkmirror.common.Adapter
import com.dertyp7214.apkmirror.common.HtmlParser

class AppDataScreen : AppCompatActivity() {

    private lateinit var htmlParser: HtmlParser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_data_screen)

        if (intent == null || intent.extras == null || !intent.extras.containsKey("url")) finish()
        val extras = intent.extras
        htmlParser = HtmlParser(this)
        val appData = Adapter.apps[extras["url"]]
        Adapter.progressDialog?.dismiss()


    }
}
