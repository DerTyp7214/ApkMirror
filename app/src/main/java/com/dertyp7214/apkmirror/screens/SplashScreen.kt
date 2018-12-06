package com.dertyp7214.apkmirror.screens

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import com.dertyp7214.apkmirror.R
import kotlinx.android.synthetic.main.activity_splash_screen.*
import java.util.*

class SplashScreen : AppCompatActivity() {

    private val PERMISSIONS = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        changeNavColor(resources.getColor(R.color.ic_launcher_background))
        changeStatusColor(resources.getColor(R.color.ic_launcher_background))

        ActivityCompat.requestPermissions(
            this,
            Arrays.asList(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).toTypedArray(),
            PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    finish()
                }
            }
            startAnimations()
        }
    }

    private fun startAnimations() {
        val maxValue = 60
        var animating = true
        Handler().postDelayed({
            val animator = ValueAnimator.ofInt(0, maxValue)
            animator.duration = 1000
            animator.interpolator = OvershootInterpolator()
            animator.addUpdateListener {
                icon.elevation = it.animatedValue as Int + 0F
                if (it.animatedValue as Int == maxValue) animating = false
            }
            animator.start()
        }, 1000)
        Thread {
            while (animating);
            runOnUiThread {
                Handler().postDelayed({
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }, 500)
            }
        }.start()
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

    private fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap {
        var drawable = ContextCompat.getDrawable(this, drawableId)
        drawable = DrawableCompat.wrap(drawable!!).mutate()

        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth + 20,
            drawable.intrinsicHeight + 20, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(10, 10, canvas.width - 10, canvas.height - 10)
        drawable.draw(canvas)

        return bitmap
    }
}
