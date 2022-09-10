package de.dmichael.android.memory.plus.profiles

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import androidx.core.net.toFile
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.leaderboard.LeaderboardResult
import de.dmichael.android.memory.plus.system.BitmapUtil
import de.dmichael.android.memory.plus.system.Game
import java.io.*
import java.lang.IllegalArgumentException

object ProfileManager {

    private const val VERSION = 1

    private val profiles = mutableListOf<Profile>()
    private var intialized = false

    fun addProfile(context: Context, displayName: String, imageUri: Uri?): Profile {
        val profile = Profile(context, displayName, imageUri)
        profiles.add(profile)
        return profile
    }

    fun clearResults(context: Context) {
        for (profile in profiles) {
            profile.clearResults()
        }
        serialize(context)
    }

    fun getCategories(): List<Int> {
        val set = mutableSetOf<Int>()
        for (profile in profiles) {
            set.addAll(profile.getCategories())
        }
        return set.sortedWith { c1, c2 -> c2 - c1 }
    }

    fun getProfile(index: Int): Profile {
        return profiles[index]
    }

    fun getProfile(id: String): Profile {
        for (profile in profiles) {
            if (profile.id == id) {
                return profile
            }
        }
        throw IllegalArgumentException("No profile found for id $id")
    }

    fun getResults(category: Int): List<LeaderboardResult> {
        val list = mutableListOf<LeaderboardResult>()
        for (profile in profiles) {
            if (profile.hasResult(category)) {
                list.add(LeaderboardResult(profile, profile.getResult(category)))
            }
        }
        return list.sortedWith { r1, r2 ->
            if (r1.result.averageHitCount > r2.result.averageHitCount) {
                -1
            } else if (r1.result.averageHitCount < r2.result.averageHitCount) {
                1
            } else {
                0
            }
        }
    }

    fun hasDisplayName(displayName: String, excludeProfile: Profile?): Boolean {
        for (profile in profiles) {
            if (profile != excludeProfile && profile.displayName == displayName) {
                return true
            }
        }
        return false
    }

    fun getBitmapFile(context: Context, bitmap: Bitmap): File {
        val hash = BitmapUtil.hash(bitmap)
        return File(getDirectory(context), "$hash.png")
    }

    fun removeProfile(context: Context, profile: Profile) {
        if (profile.getProfileImageUri() != null) {
            // Look for other profiles with the same image as the profile to be removed
            var used = false
            for (p in profiles) {
                if (p != profile && p.getProfileImageUri() == profile.getProfileImageUri()) {
                    used = true
                    break
                }
            }
            // Delete image if no other profile uses this image
            if (!used) {
                profile.getProfileImageUri()!!.toFile().delete()
            }
        }
        profiles.remove(profile)
        serialize(context)
    }

    fun size(): Int {
        return profiles.size
    }

    fun initialize(context: Context) {
        if (!intialized) {
            val file = getProfilesFile(context)
            if (file.exists()) {
                try {
                    JsonReader(BufferedReader(FileReader(file))).use { reader ->
                        reader.beginObject()
                        assert(reader.nextName() == "profiles")
                        reader.beginArray()
                        while (reader.hasNext()) {
                            profiles.add(Profile(reader))
                        }
                        reader.endArray()
                        reader.endObject()
                    }
                } catch (ex: Exception) {
                    Log.e(Game.TAG, "Failed to initialize profile manager: $ex", ex)
                    getDirectory(context).deleteRecursively()
                    initialize(context)
                }
            } else {
                addProfile(
                    context,
                    context.getString(R.string.default_profile_name),
                    null
                )
                serialize(context)
            }
            Log.v(Game.TAG, "Profile Manager: ${size()} profiles deserialized")
            intialized = true
        }
    }

    fun serialize(context: Context) {
        val file = getProfilesFile(context)
        JsonWriter(BufferedWriter(FileWriter(file))).use { writer ->
            writer.beginObject()
            writer.name("profiles")
            writer.beginArray()
            for (profile in profiles) {
                profile.serialize(writer)
            }
            writer.endArray()
            writer.endObject()
        }
        Log.v(Game.TAG, "Profile Manager: ${size()} profiles serialized")
    }

    private fun getProfilesFile(context: Context): File {
        return File(getDirectory(context), "profiles_v$VERSION.json")
    }

    private fun getDirectory(context: Context): File {
        val directory = File(context.dataDir, "profiles")
        directory.mkdirs()
        return directory
    }
}