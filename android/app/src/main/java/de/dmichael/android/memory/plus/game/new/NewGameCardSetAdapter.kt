package de.dmichael.android.memory.plus.game.new

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.cardsets.CardSet
import de.dmichael.android.memory.plus.cardsets.CardSetManager
import de.dmichael.android.memory.plus.system.Adapter

class NewGameCardSetAdapter(private val callback: (cardSet: CardSet) -> Unit) :
    Adapter<NewGameCardSetAdapter.ViewHolder>(R.layout.view_item_card_set_vertical) {

    companion object {
        private const val ALPHA_SELECTED = 1f
        private const val ALPHA_UNSELECTED = 0.5f
    }

    class ViewHolder(
        val view: View,
        val ivImage: ImageView,
        val tvName: TextView,
        val tvCardCount: TextView
    ) : Adapter.ViewHolder(view) {
        var selected: Boolean = false
        var cardSet: CardSet? = null
    }

    private var selectedHolder: ViewHolder? = null

    fun getSelectedCardSet(): CardSet? {
        if (selectedHolder != null) {
            return selectedHolder!!.cardSet
        }
        return null
    }

    override fun createViewHolder(view: View): ViewHolder {
        val holder = ViewHolder(
            view,
            view.findViewById(R.id.view_item_card_set_vertical_image),
            view.findViewById(R.id.view_item_card_set_vertical_name),
            view.findViewById(R.id.view_item_card_set_vertical_card_count)
        )

        view.alpha = ALPHA_UNSELECTED
        view.setOnClickListener {
            if (selectedHolder != null) {
                selectedHolder!!.view.alpha = ALPHA_UNSELECTED
                selectedHolder!!.selected = false
            }
            holder.selected = true
            view.alpha = ALPHA_SELECTED
            selectedHolder = holder
            callback(selectedHolder!!.cardSet!!)
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardSet = CardSetManager.getCardSet(position)
        holder.ivImage.setImageDrawable(cardSet.getDrawable(holder.context))
        holder.ivImage.contentDescription = String.format(
            holder.context.getString(R.string.desc_image_card_set_with_name),
            cardSet.displayName
        )
        holder.tvName.text = cardSet.displayName
        holder.tvCardCount.text = String.format(
            holder.context.getString(R.string.new_game_card_set_card_count),
            cardSet.size(false)
        )
        holder.cardSet = cardSet
    }

    override fun getItemCount(): Int {
        return CardSetManager.size()
    }
}