package de.dmichael.android.memory.plus.profiles

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toFile
import androidx.core.net.toUri
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.system.BitmapUtil
import de.dmichael.android.memory.plus.system.Game
import java.util.*

class Profile {

    constructor(context: Context, displayName: String, imageUri: Uri?) {
        this.id = UUID.randomUUID().toString()
        this.displayName = displayName
        setProfileImage(context, imageUri)
    }

    constructor(reader: JsonReader) {
        reader.beginObject()
        assert(reader.nextName() == "id")
        this.id = reader.nextString()
        assert(reader.nextName() == "displayName")
        this.displayName = reader.nextString()
        assert(reader.nextName() == "imageUri")
        if (reader.peek() == JsonToken.STRING) {
            this.imageUri = Uri.parse(reader.nextString())
        } else {
            reader.nextNull()
            this.imageUri = null
        }
        reader.endObject()
    }

    val id: String
    var displayName: String

    private var imageUri: Uri? = null

    fun getProfileImageUri(): Uri? {
        return imageUri
    }

    fun getProfileImage(context: Context, defaultImage: Boolean = true): Drawable? {
        return if (imageUri == null && defaultImage) {
            // Return default profile image
            AppCompatResources.getDrawable(context, R.drawable.image_profile_empty)!!
        } else if (imageUri != null) {
            Game.getDrawable(context, imageUri!!)
        } else {
            return null
        }
    }

    fun setProfileImage(context: Context, uri: Uri?) {
        // Check if uri is the same, when true then do nothing
        if ((imageUri == null && uri == null) || (imageUri != null && imageUri == uri)) {
            return
        }
        // Remove old image file if exists
        if (imageUri != null) {
            imageUri!!.toFile().delete()
        }
        // Save new image file, if exists
        imageUri = if (uri != null) {
            // Load bitmap
            val bitmap = BitmapUtil.deserialize(uri)
            val file = ProfileManager.getBitmapFile(context, bitmap!!)
            if (!file.exists()) {
                BitmapUtil.serialize(bitmap, file)
            }
            bitmap.recycle()
            uri.toFile().delete()
            file.toUri()
        } else {
            null
        }
    }

    fun serialize(writer: JsonWriter) {
        writer.beginObject()
        writer.name("id").value(id)
        writer.name("displayName").value(displayName)
        writer.name("imageUri")
        if (imageUri != null) {
            writer.value(imageUri.toString())
        } else {
            writer.nullValue()
        }
        writer.endObject()
    }

    override fun toString(): String {
        return "$displayName ($id)"
    }
}