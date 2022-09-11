package de.dmichael.android.memory.plus.profiles

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import de.dmichael.android.memory.plus.ImageCropActivity
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.system.Activity
import de.dmichael.android.memory.plus.system.BitmapUtil
import de.dmichael.android.memory.plus.system.FirebaseUtil
import de.dmichael.android.memory.plus.system.Game
import java.io.File

class ProfileActivity : Activity() {

    companion object {
        private var currentProfileImage: BitmapDrawable? = null
        private var currentProfileName: String = ""

        fun reset() {
            recycleCurrentProfileImage()
            currentProfileName = ""
        }

        private fun recycleCurrentProfileImage() {
            if (currentProfileImage != null) {
                currentProfileImage!!.bitmap.recycle()
                currentProfileImage = null
            }
        }
    }

    private lateinit var btOkay: Button
    private lateinit var etProfileName: EditText
    private lateinit var tvErrorMessage: TextView
    private lateinit var vwLoadingCircle: View
    private var profile: Profile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setUpAppearance()

        val tvTitle = findViewById<TextView>(R.id.profile_title)
        val tvMessage = findViewById<TextView>(R.id.profile_message)
        val ivProfileImage = findViewById<ImageButton>(R.id.profile_image)
        val ibRemoveImage = findViewById<ImageButton>(R.id.profile_button_image_remove)
        val tvIdentifierHint = findViewById<TextView>(R.id.profile_hint_identifier)
        etProfileName = findViewById(R.id.profile_name)
        tvErrorMessage = findViewById(R.id.profile_error_message)
        btOkay = findViewById(R.id.profile_button_okay)
        vwLoadingCircle = findViewById(R.id.profile_loading_circle)

        // Initialize profile data
        val profileId = intent.getStringExtra(ProfilesActivity.PROFILE_ID)
        if (profileId == null) {
            // Create Profile
            tvTitle.setText(R.string.profile_title_create)
            tvMessage.setText(R.string.profile_message_create)
            tvIdentifierHint.visibility = View.GONE
            validate()
        } else {
            // Edit Profile
            profile = ProfileManager.getProfile(profileId)
            tvTitle.setText(R.string.profile_title_edit)
            tvMessage.setText(R.string.profile_message_edit)
            currentProfileImage = profile!!.getProfileImage(this, false) as BitmapDrawable?
            currentProfileName = profile!!.displayName
            tvIdentifierHint.visibility = View.VISIBLE
            val spannableString = SpannableString(
                String.format(
                    getString(R.string.profile_hint_identifier),
                    profile!!.id
                )
            )
            val startIndex = resources.getInteger(R.integer.profile_hint_identifier_start_index)
            spannableString.setSpan(
                ForegroundColorSpan(getColor(R.color.text_result)),
                startIndex,
                startIndex + profile!!.id.length,
                SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
            )
            tvIdentifierHint.text = spannableString

        }

        // Set profile image
        if (currentProfileImage != null) {
            ivProfileImage.setImageDrawable(currentProfileImage)
            ibRemoveImage.visibility = View.VISIBLE
        } else {
            ibRemoveImage.visibility = View.INVISIBLE
        }

        // Set profile name
        etProfileName.setText(currentProfileName)

        // Gallery Launcher
        val galleryLauncher = ImageCropActivity.getGalleryLauncher(this) { uri ->
            // Recycle old profile image
            recycleCurrentProfileImage()
            // Set new profile image
            val bitmap = BitmapUtil.deserialize(uri)
            if (bitmap != null) {
                val croppedBitmap = BitmapUtil.cropCircle(bitmap, 10f)
                currentProfileImage = BitmapDrawable(resources, croppedBitmap)
                ivProfileImage.setImageDrawable(currentProfileImage)
                // remove temporary file
                uri.toFile().delete()
                ibRemoveImage.visibility = View.VISIBLE
            } else {
                // Set default profile image
                ivProfileImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        this,
                        R.drawable.image_profile_empty
                    )
                )
                ibRemoveImage.visibility = View.INVISIBLE
            }
        }

        // Choose Profile Image
        ivProfileImage.setOnClickListener {
            ImageCropActivity.launchGallery(galleryLauncher)
        }

        // Check and set profile name
        etProfileName.addTextChangedListener {
            validate()
            currentProfileName = etProfileName.text.toString()
        }

        // Remove Profile Image Button
        onButtonClick<Button>(R.id.profile_button_image_remove) {
            recycleCurrentProfileImage()
            ivProfileImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    this,
                    R.drawable.image_profile_empty
                )
            )
            ibRemoveImage.visibility = View.INVISIBLE
        }

        // Cancel Button
        onButtonClick<Button>(R.id.profile_button_cancel) {
            recycleCurrentProfileImage()
            finish()
        }

        // Okay Button
        onButtonClick<Button>(R.id.profile_button_okay) {
            vwLoadingCircle.visibility = View.VISIBLE

            val imageUri = getProfileImageUri()
            if (profile == null) {
                FirebaseUtil.createUniqueIdentifier(
                    currentProfileName,
                    { identifier ->
                        profile =
                            ProfileManager.addProfile(
                                this,
                                identifier,
                                currentProfileName,
                                imageUri
                            )
                        storeProfile(profile!!, true)
                    },
                    { ex -> Game.showUnexpectedError(this, ex) }
                )
            } else {
                profile!!.setProfileImage(this, imageUri)
                profile!!.displayName = currentProfileName
                storeProfile(profile!!, false)
            }
        }
    }

    private fun storeProfile(profile: Profile, newProfile: Boolean) {
        // Store profile image in firebase
        FirebaseUtil.uploadFile(
            profile.getProfileImageUri(),
            "profiles/${profile.id}",
            {
                // Write profile to firestore
                val properties = mapOf(
                    "identifier" to profile.id,
                    "displayName" to profile.displayName,
                    "imageFile" to profile.getProfileImageUri()?.lastPathSegment
                )
                FirebaseUtil.createOrUpdateDocument(
                    "profiles",
                    profile.id,
                    properties,
                    !newProfile,
                    {
                        finish()
                    },
                    { ex ->
                        vwLoadingCircle.visibility = View.INVISIBLE
                        Game.showUnexpectedError(this, ex)
                    }
                )
            }, { ex ->
                vwLoadingCircle.visibility = View.INVISIBLE
                Game.showUnexpectedError(this, ex)
            })
    }

    private fun getProfileImageUri(): Uri? {
        if (currentProfileImage != null) {
            val tempFile = File.createTempFile("bmp", "tmp", cacheDir)
            BitmapUtil.serialize(currentProfileImage!!.bitmap, tempFile)
            return tempFile.toUri()
        }
        return null
    }

    private fun validate(): Boolean {
        return if (etProfileName.text.isBlank()) {
            tvErrorMessage.setText(R.string.profile_error_empty_name)
            btOkay.isEnabled = false
            false
        } else if (ProfileManager.hasDisplayName(etProfileName.text.toString(), profile)) {
            tvErrorMessage.setText(R.string.profile_error_name_not_unique)
            btOkay.isEnabled = false
            false
        } else {
            btOkay.isEnabled = true
            tvErrorMessage.text = ""
            true
        }
    }
}