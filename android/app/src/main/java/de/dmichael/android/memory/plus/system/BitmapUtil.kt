package de.dmichael.android.memory.plus.system

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import java.io.*
import java.nio.channels.FileChannel
import java.security.MessageDigest

object BitmapUtil {

    private const val hexChars = "0123456789abcdef"

    fun cropCircle(bitmap: Bitmap, frameSize: Float): Bitmap {
        val size = bitmap.width
        val croppedBitmap = Bitmap.createBitmap(
            size,
            size,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(croppedBitmap)
        val paintColor = Paint()
        paintColor.flags = Paint.ANTI_ALIAS_FLAG
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - frameSize, paintColor)
        val paintImage = Paint()
        paintImage.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        paintImage.color = Color.WHITE
        canvas.drawBitmap(bitmap, 0f, 0f, paintImage)
        val paintStroke = Paint()
        paintStroke.flags = Paint.ANTI_ALIAS_FLAG
        paintStroke.color = Color.WHITE
        paintStroke.style = Paint.Style.STROKE
        paintStroke.strokeWidth = frameSize
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - frameSize / 2f, paintStroke)
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

    fun combine(
        context: Context,
        source: Drawable,
        target: Drawable,
        x: Int,
        y: Int,
        w: Int,
        h: Int
    ): Drawable {
        val sourceBitmap = (source as BitmapDrawable).bitmap
        val metrics = context.resources.displayMetrics
        val with = (sourceBitmap.width * metrics.density).toInt()
        val height = (sourceBitmap.height * metrics.density).toInt()
        val scaledSourceBitmap = Bitmap.createScaledBitmap(sourceBitmap, with, height, false)
        val targetBitmap = (target as BitmapDrawable).bitmap
        val newBitmap = toMutableBitmap(context, scaledSourceBitmap)
        scaledSourceBitmap.recycle()

        val dx = (x * metrics.density).toInt()
        val dy = (y * metrics.density).toInt()
        val dw = (w * metrics.density).toInt()
        val dh = (h * metrics.density).toInt()
        val canvas = Canvas(newBitmap)
        canvas.drawBitmap(
            targetBitmap,
            Rect(0, 0, targetBitmap.width, targetBitmap.height),
            Rect(dx, dy, dx + dw, dy + dh),
            null
        )

        return BitmapDrawable(context.resources, newBitmap)
    }

    private fun toMutableBitmap(context: Context, bitmap: Bitmap): Bitmap {
        val tmpFile = File.createTempFile("bmp", "tmp", context.cacheDir)
        val accessFile = RandomAccessFile(tmpFile, "rw")
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config
        val channel = accessFile.channel
        val map =
            channel.map(FileChannel.MapMode.READ_WRITE, 0L, (bitmap.rowBytes * height).toLong())
        bitmap.copyPixelsToBuffer(map)
        val result = Bitmap.createBitmap(width, height, config)
        map.position(0)
        result.copyPixelsFromBuffer(map)
        channel.close()
        accessFile.close()
        tmpFile.delete()

        return result
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