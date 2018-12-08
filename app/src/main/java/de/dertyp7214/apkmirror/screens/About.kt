@file:Suppress("DEPRECATION")

package de.dertyp7214.apkmirror.screens

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity.CENTER
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import de.dertyp7214.apkmirror.R
import kotlinx.android.synthetic.main.activity_about.*

class About : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val foreground = Bitmap.createScaledBitmap(
            getBitmapFromVectorDrawable(R.drawable.about_background),
            3000,
            3000,
            false
        )
        val background = createImage(3000, 3000, resources.getColor(R.color.ic_launcher_background))

        val image = BitmapDrawable(resources, overlay(background, foreground))
        image.gravity = CENTER
        root.background = image

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        iconColors()
    }

    private fun iconColors() {
        var tmp = window.decorView.systemUiVisibility
        val color = resources.getColor(R.color.ic_launcher_background)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tmp = if (ColorUtils.calculateLuminance(color) > 0.5F)
                tmp or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            else
                tmp and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tmp = if (ColorUtils.calculateLuminance(color) > 0.5F)
                tmp or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            else
                tmp and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
        window.decorView.systemUiVisibility = tmp
    }

    private fun overlay(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
        val bmOverlay = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(bmp1, Matrix(), null)
        canvas.drawBitmap(bmp2, Matrix(), null)
        return bmOverlay
    }

    private fun createImage(width: Int, height: Int, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = color
        canvas.drawRect(0F, 0F, width.toFloat(), height.toFloat(), paint)
        return bitmap
    }

    private fun getBitmapFromVectorDrawable(drawableId: Int): Bitmap {
        var drawable = ContextCompat.getDrawable(this, drawableId)
        drawable = DrawableCompat.wrap(drawable!!).mutate()

        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }
}
