package de.dmichael.android.memory.plus.system

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.net.toFile
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object Game {

    const val TAG = "Memory"

    fun getDrawable(context: Context, uri: Uri): Drawable {
        val bitmap = BitmapUtil.deserialize(uri)
        return BitmapDrawable(context.resources, bitmap)
    }
}