package de.dmichael.android.memory.plus.cardsets

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.dmichael.android.memory.plus.ImageCropActivity
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.system.Activity

class CardSetActivity : Activity() {

    private lateinit var tvErrorMessage: TextView
    private lateinit var rvCards: RecyclerView
    private lateinit var btOkay: Button
    private lateinit var cardSet: CardSet
    private lateinit var callback: (card: Card?, longClick: Boolean) -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_set)
        setUpAppearance()

        tvErrorMessage = findViewById(R.id.card_set_name_error_message)
        btOkay = findViewById(R.id.card_set_button_okay)

        // Initializing card set data
        val cardSetId = intent.getStringExtra(CardSetsActivity.CARD_SET_ID)
        cardSet = if (cardSetId == null) {
            // Create new card set
            CardSet()
        } else {
            // Edit card set
            CardSet(CardSetManager.getCardSet(cardSetId))
        }

        // Card Set Name
        val etCardSetName = findViewById<EditText>(R.id.card_set_name)
        etCardSetName.setText(cardSet.displayName)
        etCardSetName.addTextChangedListener {
            cardSet.displayName = etCardSetName.text.toString()
            validate()
        }
        validate()

        // Gallery Launcher
        val launcher = ImageCropActivity.getGalleryLauncher(this) { uri ->
            if (cardSet.addCard(this, uri)) {
                refreshCardView()
            } else {
                Toast.makeText(this, R.string.card_set_image_already_exists, Toast.LENGTH_LONG)
                    .show()
            }
        }

        // Card Callback
        callback = { card, longClick ->
            if (card == null && !longClick) {
                // New Card
                ImageCropActivity.launchGallery(launcher)
            } else if (card != null && longClick) {
                // Delete Card
                card.delete()
                refreshCardView()
            }
        }

        // Card Set View
        rvCards = findViewById(R.id.card_set_view)
        rvCards.layoutManager = GridLayoutManager(this, 3)
        refreshCardView()

        // Cancel Button
        onButtonClick<Button>(R.id.card_set_button_cancel) {
            cardSet.rollback(this)
            finish()
        }

        // Okay Button
        onButtonClick<Button>(R.id.card_set_button_okay) {
            CardSetManager.addOrUpdateCardSet(cardSet)
            CardSetManager.serialize(this)
            finish()
        }
    }

    private fun validate(): Boolean {
        if (cardSet.displayName.isBlank()) {
            tvErrorMessage.setText(R.string.card_set_error_name_empty)
            btOkay.isEnabled = false
            return false
        } else if (CardSetManager.hasCardSetName(cardSet.displayName, cardSet)) {
            tvErrorMessage.setText(R.string.card_set_error_name_not_unique)
            btOkay.isEnabled = false
            return false
        } else if (cardSet.size(true) < 2) {
            tvErrorMessage.setText(R.string.card_set_error_too_few_cards)
            btOkay.isEnabled = false
            return false
        }
        tvErrorMessage.text = ""
        btOkay.isEnabled = true
        return true
    }

    private fun refreshCardView() {
        rvCards.adapter = CardSetAdapter(cardSet, callback)
        validate()
    }
}