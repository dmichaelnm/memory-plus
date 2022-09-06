package de.dmichael.android.memory.plus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import de.dmichael.android.memory.plus.cardsets.CardSetManager
import de.dmichael.android.memory.plus.cardsets.CardSetsActivity
import de.dmichael.android.memory.plus.profiles.ProfileManager
import de.dmichael.android.memory.plus.profiles.ProfilesActivity
import de.dmichael.android.memory.plus.system.Activity
import de.dmichael.android.memory.plus.system.Game

class MainMenuActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        setUpAppearance()

        // Dump Screen Information
        Game.dumpScreenInfo(this)

        // Set Version Info
        val tvCopyright = findViewById<TextView>(R.id.main_menu_copyright_and_version)
        tvCopyright.text = String.format(
            getString(
                R.string.main_menu_copyright_and_version,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE
            )
        )

        // Clear Cache Directory
        clearCacheDirectory()

        // Initialize Profile Manager
        ProfileManager.initialize(this)

        // Initialize Card Set Manager
        CardSetManager.initialize(this)

        // Profiles Button
        onButtonClick<Button>(R.id.main_menu_button_profiles) {
            startActivity(Intent(this, ProfilesActivity::class.java))
        }

        // Card Sets Button
        onButtonClick<Button>(R.id.main_menu_button_card_sets) {
            startActivity(Intent(this, CardSetsActivity::class.java))
        }
    }

    private fun clearCacheDirectory() {
        val files = cacheDir.listFiles()
        if (files != null) {
            for (file in files) {
                file.delete()
            }
            Log.v(Game.TAG, "Cache directory cleared (${files.size} file removed)")
        }
    }
}