package de.dmichael.android.memory.plus.system

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log

object Game {

    const val TAG = "Memory"

    fun getDrawable(context: Context, uri: Uri): Drawable {
        val bitmap = BitmapUtil.deserialize(uri)
        return BitmapDrawable(context.resources, bitmap)
    }

    fun dumpScreenInfo(context: Context) {
        val metrics = context.resources.displayMetrics
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val dpiWidth = (screenWidth / metrics.density).toInt()
        val dpiHeight = (screenHeight / metrics.density).toInt()

        var layout = "Unknown"
        val layoutId = context.resources.configuration.screenLayout
        if (layoutId and Configuration.SCREENLAYOUT_SIZE_SMALL != 0) {
            layout = "Small"
        } else if (layoutId and Configuration.SCREENLAYOUT_SIZE_NORMAL != 0) {
            layout = "Normal"
        } else if (layoutId and Configuration.SCREENLAYOUT_SIZE_LARGE != 0) {
            layout = "Large"
        } else if (layoutId and Configuration.SCREENLAYOUT_SIZE_XLARGE != 0) {
            layout = "XLarge"
        }

        Log.v(
            TAG,
            "Screen Size $screenWidth x $screenHeight " +
                    "( dpi = ${metrics.densityDpi}, " +
                    "density = ${metrics.density}, $dpiWidth x $dpiHeight, layout = $layout  )"
        )
    }
}