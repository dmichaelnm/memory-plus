package de.dmichael.android.memory.plus.leaderboard

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.system.Adapter

class LeaderboardCategoryAdapter(
    private val categories: List<Int>,
    private val selectionCallback: (category: Int) -> Unit
) :
    Adapter<LeaderboardCategoryAdapter.ViewHolder>(R.layout.view_item_leaderboard_category) {

    class ViewHolder(
        val view: View,
        val tvCount: TextView,
        val tvCards: TextView
    ) : Adapter.ViewHolder(view) {
        var category: Int = -1
    }

    private var oldSelection: ViewHolder? = null

    override fun createViewHolder(view: View): ViewHolder {
        val holder = ViewHolder(
            view,
            tvCount = view.findViewById(R.id.view_item_category_count),
            tvCards = view.findViewById(R.id.view_item_category_cards)
        )

        view.setOnClickListener {
            if (oldSelection != null) {
                oldSelection!!.view.setBackgroundColor(Color.TRANSPARENT)
            }
            selectionCallback(holder.category)
            view.background = AppCompatResources.getDrawable(
                view.context,
                R.drawable.shape_item_background_checked
            )
            oldSelection = holder
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.tvCount.setText(R.string.total)
            holder.tvCards.visibility = View.GONE
            holder.category = -1
            holder.view.background = AppCompatResources.getDrawable(
                holder.view.context,
                R.drawable.shape_item_background_checked
            )
            oldSelection = holder
        } else {
            val category = categories[position - 1]
            holder.tvCount.text = category.toString()
            holder.tvCards.visibility = View.VISIBLE
            holder.category = category
        }
    }

    override fun getItemCount(): Int {
        return categories.size + 1
    }
}