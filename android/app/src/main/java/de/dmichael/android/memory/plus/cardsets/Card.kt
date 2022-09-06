package de.dmichael.android.memory.plus.cardsets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import de.dmichael.android.memory.plus.system.BitmapUtil
import de.dmichael.android.memory.plus.system.Game
import java.io.File
import java.util.*

class Card {

    enum class State {
        New,
        Deleted,
        Unchanged
    }

    constructor(context: Context, cardSet: CardSet, hash: String, bitmap: Bitmap) {
        this.id = UUID.randomUUID().toString()
        val file = File(cardSet.getDirectory(context), "$hash.png")
        BitmapUtil.serialize(bitmap, file)
        bitmap.recycle()
        this.imageUri = file.toUri()
        this.state = State.New
    }

    constructor(reader: JsonReader) {
        reader.beginObject()
        assert(reader.nextName() == "id")
        this.id = reader.nextString()
        assert(reader.nextName() == "imageUri")
        this.imageUri = Uri.parse(reader.nextString())
        reader.endObject()
        this.state = State.Unchanged
    }

    val id: String
    private val imageUri: Uri
    private var drawable: Drawable? = null
    private var state: State
    private var hash: String? = null

    fun getState(): State = state

    fun getHash(): String {
        if (hash == null) {
            val filename = imageUri.lastPathSegment
            hash = filename!!.substring(0, filename.length - 4)
        }
        return hash!!
    }

    fun getDrawable(context: Context): Drawable {
        if (drawable == null) {
            val bitmap = BitmapUtil.deserialize(imageUri)
            drawable = BitmapDrawable(context.resources, bitmap)
        }
        return drawable!!
    }

    fun serialize(writer: JsonWriter) {
        writer.beginObject()
        writer.name("id").value(id)
        writer.name("imageUri").value(imageUri.toString())
        writer.endObject()
    }

    fun delete() {
        state = State.Deleted
        Log.v(Game.TAG, "Card $id marked as deleted")
    }

    fun setUnchanged() {
        state = State.Unchanged
    }

    fun release() {
        imageUri.toFile().delete()
        Log.v(Game.TAG, "Image '$imageUri' deleted")
    }
}