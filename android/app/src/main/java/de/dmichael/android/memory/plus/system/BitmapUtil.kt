package de.dmichael.android.memory.plus.system

import android.graphics.*
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import java.io.*
import java.security.MessageDigest

object BitmapUtil {

    private const val hexChars = "0123456789abcdef"

    fun cropCircle(bitmap: Bitmap): Bitmap {
        val size = bitmap.width
        val croppedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(croppedBitmap)
        val paintColor = Paint()
        paintColor.flags = Paint.ANTI_ALIAS_FLAG
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paintColor)
        val paintImage = Paint()
        paintImage.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        canvas.drawBitmap(bitmap, 0f, 0f, paintImage)
        bitmap.recycle()
        return croppedBitmap
    }

    fun deserialize(uri: Uri): Bitmap? {
        val file = uri.toFile()
        if (file.exists()) {
            BufferedInputStream(FileInputStream(file)).use { inStream ->
                val bitmap = BitmapFactory.decodeStream(inStream)
                Log.v(
                    Game.TAG,
                    "Bitmap (${toString(bitmap)}) deserialized from '${file.absolutePath}'"
                )
                return bitmap
            }
        }
        Log.w(Game.TAG, "No bitmap found at location '${file.absolutePath}'")
        return null
    }

    fun serialize(bitmap: Bitmap, file: File) {
        BufferedOutputStream(FileOutputStream(file)).use { outStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
        }
        Log.v(Game.TAG, "Bitmap (${toString(bitmap)}) serialized to '${file.absolutePath}'")
    }

    fun hash(bitmap: Bitmap): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        val hashed = digest.digest(out.toByteArray())
        return toHex(hashed)
    }

    private fun toString(bitmap: Bitmap): String {
        val with = bitmap.width
        val height = bitmap.height
        val config = bitmap.config
        return "$config ($with x $height)"
    }

    private fun toHex(array: ByteArray): String {
        val hex = CharArray(2 * array.size)
        array.forEachIndexed { index, byte ->
            val unsigned = 0xff and byte.toInt()
            hex[2 * index] = hexChars[unsigned / 16]
            hex[2 * index + 1] = hexChars[unsigned % 16]
        }
        return hex.joinToString("")
    }
}