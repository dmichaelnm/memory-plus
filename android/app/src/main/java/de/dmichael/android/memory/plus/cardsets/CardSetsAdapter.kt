package de.dmichael.android.memory.plus.cardsets

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.system.Adapter
import de.dmichael.android.memory.plus.system.Game
import org.w3c.dom.Text

class CardSetsAdapter(private val callback: (cardSet: CardSet?, longClick: Boolean) -> Unit) :
    Adapter<CardSetsAdapter.ViewHolder>(R.layout.view_item_card_set_horizontal) {

    class ViewHolder(
        view: View,
        val ivCardSetImage: ImageView,
        val tvCardSetName: TextView,
        val tvCardSetCardCount: TextView
    ) : Adapter.ViewHolder(view) {
        var cardSet: CardSet? = null
    }

    override fun createViewHolder(view: View): ViewHolder {
        val holder = ViewHolder(
            view,
            view.findViewById(R.id.view_item_card_set_horizontal_image),
            view.findViewById(R.id.view_item_card_set_horizontal_name),
            view.findViewById(R.id.view_item_card_set_horizontal_card_count)
        )

        view.setOnClickListener {
            Log.v(Game.TAG, "Short click on card set ${holder.cardSet}")
            callback(holder.cardSet, false)
        }

        view.setOnLongClickListener {
            Log.v(Game.TAG, "Long click on card set ${holder.cardSet}")
            callback(holder.cardSet, true)
            true
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.ivCardSetImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    holder.context,
                    R.drawable.image_card_set_new
                )
            )
            holder.ivCardSetImage.contentDescription =
                holder.context.getString(R.string.desc_image_card_set_new)
            holder.tvCardSetName.setText(R.string.card_sets_new_card_set)
            holder.tvCardSetCardCount.visibility = View.GONE
            holder.cardSet = null
        } else {
            val cardSet = CardSetManager.getCardSet(position - 1)
            holder.ivCardSetImage.setImageDrawable(cardSet.getDrawable(holder.context))
            holder.ivCardSetImage.contentDescription = String.format(
                holder.context.getString(R.string.desc_image_card_set_with_name),
                cardSet.displayName
            )
            holder.tvCardSetName.text = cardSet.displayName
            holder.tvCardSetCardCount.text = String.format(
                holder.context.getString(R.string.card_sets_card_count),
                cardSet.size(false)
            )
            holder.cardSet = cardSet
        }
    }

    override fun getItemCount(): Int {
        return CardSetManager.size() + 1
    }
}
