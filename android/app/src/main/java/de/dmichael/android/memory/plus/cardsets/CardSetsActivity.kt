package de.dmichael.android.memory.plus.cardsets

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import de.dmichael.android.memory.plus.DialogActivity
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.system.Activity

class CardSetsActivity : Activity() {

    companion object {
        const val CARD_SET_ID = "cardSetId"
    }

    private lateinit var rvCardSets: RecyclerView
    private lateinit var callback: (cardSet: CardSet?, longClick: Boolean) -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_sets)
        setUpAppearance()

        // Delete launcher
        val launcher = DialogActivity.getLauncher(this) { dialogOption, data ->
            if (dialogOption == 1) {
                val cardSet = data as CardSet
                CardSetManager.removeCardSet(this, cardSet)
                CardSetManager.serialize(this)
                refreshCardSetsView()
            }
        }

        // Callback Implementation
        callback = { cardSet, longClick ->
            if (cardSet == null && !longClick) {
                // Create a new card set
                startActivity(Intent(this, CardSetActivity::class.java))
            } else if (cardSet != null && !longClick) {
                // Edit card set
                val intent = Intent(this, CardSetActivity::class.java)
                intent.putExtra(CARD_SET_ID, cardSet.id)
                startActivity(intent)
            } else if (cardSet != null && longClick) {
                // Delete card set
                DialogActivity.launch(
                    this,
                    R.string.card_sets_dialog_remove_title,
                    R.string.card_sets_dialog_remove_message,
                    R.string.card_sets_dialog_remove_option_yes,
                    R.string.card_sets_dialog_remove_option_no,
                    -1,
                    cardSet,
                    launcher
                )
            }
        }

        // Card Set Recycler View
        rvCardSets = findViewById(R.id.card_sets_view)
        refreshCardSetsView()

        // Back Button
        onButtonClick<Button>(R.id.card_sets_button_back) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshCardSetsView()
    }

    private fun refreshCardSetsView() {
        rvCardSets.adapter = CardSetsAdapter(callback)
    }
}