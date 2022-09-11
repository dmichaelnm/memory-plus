package de.dmichael.android.memory.plus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import de.dmichael.android.memory.plus.cardsets.CardSetManager
import de.dmichael.android.memory.plus.cardsets.CardSetsActivity
import de.dmichael.android.memory.plus.game.GameActivity
import de.dmichael.android.memory.plus.game.new.NewGameActivity
import de.dmichael.android.memory.plus.leaderboard.LeaderboardActivity
import de.dmichael.android.memory.plus.profiles.ProfileManager
import de.dmichael.android.memory.plus.profiles.ProfilesActivity
import de.dmichael.android.memory.plus.system.Activity
import de.dmichael.android.memory.plus.system.FirebaseUtil
import de.dmichael.android.memory.plus.system.Game

class MainMenuActivity : Activity() {

    private lateinit var btNewGame: Button

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

        // New Game Button
        btNewGame = findViewById(R.id.main_menu_button_new_game)
        checkButtonNewGame()
        onButtonClick<Button>(R.id.main_menu_button_new_game) {
            // If just one profile and one card set is configured, start directly with the game
            if (ProfileManager.size() == 1 && CardSetManager.size() == 1) {
                val cardSet = CardSetManager.getCardSet(0)
                val profiles = listOf(ProfileManager.getProfile(0))
                GameActivity.launchGame(this, cardSet, profiles)
            } else {
                startActivity(Intent(this, NewGameActivity::class.java))
            }
        }

        // Leaderboard Button
        onButtonClick<Button>(R.id.main_menu_button_leaderboard) {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        // Initialize Firebase
        FirebaseUtil.initialize(this)
    }

    override fun onResume() {
        super.onResume()
        checkButtonNewGame()
    }

    private fun checkButtonNewGame() {
        btNewGame.isEnabled = ProfileManager.size() > 0 && CardSetManager.size() > 0
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