package de.dmichael.android.memory.plus.game.new

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.cardsets.CardSetManager
import de.dmichael.android.memory.plus.game.GameActivity
import de.dmichael.android.memory.plus.profiles.ProfileManager
import de.dmichael.android.memory.plus.system.Activity

class NewGameActivity : Activity() {

    private lateinit var btOkay: Button
    private lateinit var cardSetAdapter: NewGameCardSetAdapter
    private lateinit var profilesAdapter: NewGameProfilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_new)
        setUpAppearance()

        // Card Set View
        val vwCardSetPanel = findViewById<View>(R.id.new_game_which_card_set_panel)
        if (CardSetManager.size() == 1) {
            vwCardSetPanel.visibility = View.GONE
        }

        cardSetAdapter = NewGameCardSetAdapter {
            validate()
        }
        val rvCardSets = findViewById<RecyclerView>(R.id.new_game_which_card_set_view)
        rvCardSets.layoutManager = GridLayoutManager(this, 3)
        rvCardSets.adapter = cardSetAdapter

        // Profiles View
        val vwProfilesPanel = findViewById<View>(R.id.new_game_which_profiles_panel)
        if (ProfileManager.size() == 1) {
            vwProfilesPanel.visibility = View.GONE
        }

        profilesAdapter = NewGameProfilesAdapter { _, _ ->
            validate()
        }
        val rvProfiles = findViewById<RecyclerView>(R.id.new_game_which_profiles_view)
        rvProfiles.layoutManager = GridLayoutManager(this, 3)
        rvProfiles.adapter = profilesAdapter

        // Okay Button
        btOkay = findViewById(R.id.new_game_button_start)
        validate()
        onButtonClick<Button>(R.id.new_game_button_start) {
            val cardSet = if (CardSetManager.size() > 1) {
                cardSetAdapter.getSelectedCardSet()!!
            } else {
                CardSetManager.getCardSet(0)
            }
            val profiles = if (ProfileManager.size() > 1) {
                profilesAdapter.getSelectedProfiles()
            } else {
                listOf(ProfileManager.getProfile(0))
            }
            GameActivity.launchGame(this, cardSet, profiles)
        }

        // Back Button
        onButtonClick<Button>(R.id.new_game_button_back) {
            finish()
        }
    }

    private fun validate() {
        btOkay.isEnabled =
            (cardSetAdapter.getSelectedCardSet() != null || CardSetManager.size() == 1) &&
                    (profilesAdapter.getSelectedProfiles()
                        .isNotEmpty() || ProfileManager.size() == 1)
    }
}