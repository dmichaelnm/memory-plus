package de.dmichael.android.memory.plus.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import de.dmichael.android.memory.plus.cardsets.Card

class GameCard(private val setCard: Card) {

    enum class State {
        Hidden,
        Visible,
        Discovered
    }

    var state: State = State.Hidden
    var rotation: Int = 0
    var view: GameCardView? = null
    var leftOffset: Int = 0
    var topOffset: Int = 0

    fun getBitmap(context: Context): Bitmap {
        val drawable = setCard.getDrawable(context)
        return (drawable as BitmapDrawable).bitmap
    }

    fun match(other: GameCard): Boolean {
        return setCard.id == other.setCard.id
    }
}