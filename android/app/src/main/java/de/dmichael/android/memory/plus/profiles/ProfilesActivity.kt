package de.dmichael.android.memory.plus.profiles

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import de.dmichael.android.memory.plus.DialogActivity
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.system.Activity
import de.dmichael.android.memory.plus.system.FirebaseUtil
import de.dmichael.android.memory.plus.system.Game

class ProfilesActivity : Activity() {

    companion object {
        const val PROFILE_ID = "profileId"
    }

    private lateinit var rvProfiles: RecyclerView
    private lateinit var callback: (profile: Profile?, longClick: Boolean) -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profiles)
        setUpAppearance()

        // Remove Dialog Launcher
        val launcher = DialogActivity.getLauncher(this) { option, data ->
            if (option == 1) {
                val profile = data as Profile
                // Remove remote profile
                FirebaseUtil.removeDocument(
                    "profiles",
                    profile.id,
                    {},
                    { ex -> Game.showUnexpectedError(this, ex) })
                FirebaseUtil.removeDirectory(
                    "profiles/${profile.id}",
                    {},
                    { ex -> Game.showUnexpectedError(this, ex) })
                // Remove local profile
                removeLocalProfile(profile)
            } else if (option == 2) {
                // Remove only local profile
                removeLocalProfile(data as Profile)
            }
        }

        // Back Button
        onButtonClick<Button>(R.id.profiles_button_back) {
            finish()
        }

        // Callback method to process the selected profile
        callback = { profile, longClick ->
            if (profile == null && !longClick) {
                // Create a new profile
                ProfileActivity.reset()
                startActivity(Intent(this, ProfileActivity::class.java))
            } else if (profile != null && !longClick) {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra(PROFILE_ID, profile.id)
                startActivity(intent)
            } else if (profile != null && longClick) {
                // Remove selected profile
                DialogActivity.launch(
                    this,
                    R.string.profiles_dialog_remove_title,
                    R.string.profiles_dialog_remove_message,
                    R.string.profiles_dialog_remove_option_yes_both,
                    R.string.profiles_dialog_remove_option_yes_local,
                    R.string.profiles_dialog_remove_option_no,
                    profile,
                    launcher
                )
            }
        }

        // Profiles Recycler View
        rvProfiles = findViewById(R.id.profiles_list_view)
        refreshProfilesListView()
    }

    override fun onResume() {
        super.onResume()
        refreshProfilesListView()
    }

    private fun refreshProfilesListView() {
        rvProfiles.adapter = ProfilesAdapter(callback)
    }

    private fun removeLocalProfile(profile: Profile) {
        ProfileManager.removeProfile(this, profile)
        refreshProfilesListView()
    }
}