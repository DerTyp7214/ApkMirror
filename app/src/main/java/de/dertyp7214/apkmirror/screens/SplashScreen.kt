/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

@file:Suppress("DEPRECATION")

package de.dertyp7214.apkmirror.screens

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import de.dertyp7214.apkmirror.R
import de.dertyp7214.apkmirror.common.Config
import de.dertyp7214.apkmirror.common.NetworkTools
import kotlinx.android.synthetic.main.activity_splash_screen.*
import java.util.*

class SplashScreen : AppCompatActivity() {

    private val PERMISSIONS = 10
    private var width = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        changeNavColor(resources.getColor(R.color.ic_launcher_background))
        changeStatusColor(resources.getColor(R.color.ic_launcher_background))

        Config.application = application

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        width = size.x - convertDpToPixel(60F)

        progressView.background = resources.getDrawable(R.drawable.progress_shape)
        progressView.layoutParams.width = 0

        val animator = ValueAnimator.ofFloat(0F, 1F)
        animator.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        animator.addUpdateListener {
            logo.alpha = it.animatedValue as Float
            progressView.elevation = it.animatedValue as Float * 10
            if (it.animatedValue as Float == 1F) {
                Thread {NetworkTools.drawableFromUrl(this, getString(R.string.dev_github_userimage))}.start()
                Handler().postDelayed({
                    ActivityCompat.requestPermissions(
                        this,
                        Arrays.asList(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ).toTypedArray(),
                        PERMISSIONS
                    )
                }, resources.getInteger(android.R.integer.config_longAnimTime).toLong())
            }
        }
        animator.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        var close = false
        if (requestCode == PERMISSIONS) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    close = true
                    finish()
                }
            }
            if (!close) startAnimations()
        }
    }

    private fun startAnimations() {
        val maxValue = 60
        Handler().postDelayed({
            setUpNames()
            val animator = ValueAnimator.ofInt(0, maxValue)
            animator.duration = (resources.getInteger(android.R.integer.config_longAnimTime) * 2).toLong()
            animator.addUpdateListener {
                icon.elevation = it.animatedValue as Int + 0F
                progressView.layoutParams.width = ((width / 100) * it.animatedValue as Int).toInt()
                progressView.requestLayout()
            }
            animator.start()
            val animator2 = ValueAnimator.ofInt(maxValue, 100)
            animator2.duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
            animator2.addUpdateListener {
                progressView.layoutParams.width = (width / 100 * it.animatedValue as Int).toInt()
                progressView.requestLayout()
                if (it.animatedValue as Int == 100) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            animator2.startDelay = (resources.getInteger(android.R.integer.config_longAnimTime) * 2).toLong()
            animator2.start()
        }, resources.getInteger(android.R.integer.config_mediumAnimTime).toLong())
    }

    private fun setUpNames() {
        val name = getString(R.string.app_name).toLowerCase()

        Config.knownNames.add(name)
        for (i in 0 until name.length) {
            var n = ""
            name.toCharArray().forEachIndexed { index, c ->
                if (index == i) n += " "
                n += c.toString()
            }
            Config.knownNames.add(n)
        }
    }

    private fun changeStatusColor(color: Int) {
        window.statusBarColor = color
        var tmp = window.decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tmp = if (ColorUtils.calculateLuminance(color) > 0.5F)
                tmp or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            else
                tmp and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        window.decorView.systemUiVisibility = tmp
    }

    private fun changeNavColor(color: Int) {
        window.navigationBarColor = color
        var tmp = window.decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tmp = if (ColorUtils.calculateLuminance(color) > 0.5F)
                tmp or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            else
                tmp and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
        window.decorView.systemUiVisibility = tmp
    }

    private fun convertDpToPixel(dp: Float): Float {
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}
