package de.dmichael.android.memory.plus.cardsets

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.system.Adapter

class CardSetAdapter(
    private val cardSet: CardSet,
    private val callback: (card: Card?, longClick: Boolean) -> Unit
) :
    Adapter<CardSetAdapter.ViewHolder>(R.layout.view_item_card) {

    class ViewHolder(
        view: View,
        val vwCard: View,
        val ivCard: ImageView
    ) : Adapter.ViewHolder(view) {
        var card: Card? = null
    }

    override fun createViewHolder(view: View): ViewHolder {
        val holder = ViewHolder(
            view,
            view.findViewById(R.id.view_item_card),
            view.findViewById(R.id.view_item_card_image)
        )

        view.setOnClickListener {
            callback(holder.card, false)
        }
        view.setOnLongClickListener {
            callback(holder.card, true)
            true
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == cardSet.size(true)) {
            holder.ivCard.setImageDrawable(
                AppCompatResources.getDrawable(
                    holder.context,
                    R.drawable.image_item_new
                )
            )
            holder.vwCard.setBackgroundColor(Color.TRANSPARENT)
            holder.ivCard.contentDescription =
                holder.context.getString(R.string.desc_image_card_new)
            holder.card = null
        } else {
            val card = cardSet.getCard(position, true)
            holder.ivCard.setImageDrawable(card.getDrawable(holder.context))
            holder.vwCard.setBackgroundColor(Color.WHITE)
            holder.ivCard.contentDescription =
                String.format(holder.context.getString(R.string.desc_image_card), position)
            holder.card = card
            holder.showClick = false
        }
    }

    override fun getItemCount(): Int {
        return cardSet.size(true) + 1
    }
}