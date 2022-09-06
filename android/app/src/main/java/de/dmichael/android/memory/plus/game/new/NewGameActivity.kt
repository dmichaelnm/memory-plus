package de.dmichael.android.memory.plus.game.new

import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.dmichael.android.memory.plus.R
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
        cardSetAdapter = NewGameCardSetAdapter() {
            validate()
        }
        val rvCardSets = findViewById<RecyclerView>(R.id.new_game_which_card_set_view)
        rvCardSets.layoutManager = GridLayoutManager(this, 3)
        rvCardSets.adapter = cardSetAdapter

        // Profiles View
        profilesAdapter = NewGameProfilesAdapter() { _, _ ->
            validate()
        }
        val rvProfiles = findViewById<RecyclerView>(R.id.new_game_which_profiles_view)
        rvProfiles.layoutManager = GridLayoutManager(this, 3)
        rvProfiles.adapter = profilesAdapter

        // Okay Button
        btOkay = findViewById(R.id.new_game_button_okay)
        validate()
        onButtonClick<Button>(R.id.new_game_button_okay) {
            //val cardSet = cardSetAdapter.getSelectedCardSet()
            //val profiles = profilesAdapter.getSelectedProfiles()
        }

        // Back Button
        onButtonClick<Button>(R.id.new_game_button_back) {
            finish()
        }
    }

    private fun validate() {
        btOkay.isEnabled =
            cardSetAdapter.getSelectedCardSet() != null && profilesAdapter.getSelectedProfiles()
                .isNotEmpty()
    }
}