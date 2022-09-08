package de.dmichael.android.memory.plus.leaderboard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.icu.text.NumberFormat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.system.Adapter

class LeaderboardResultAdapter(private val results: List<LeaderboardResult>) :
    Adapter<LeaderboardResultAdapter.ViewHolder>(R.layout.view_item_leaderboard_result) {

    class ViewHolder(
        view: View,
        val tvPlayerName: TextView,
        val ivPlayerImage: ImageView,
        val ivMedal: ImageView,
        val tvAverageHitPoints: TextView,
        val tvHitPoints: TextView,
        val tvHitRate: TextView,
        val tvTotalGames: TextView,
        val tvWonGames: TextView
    ) : Adapter.ViewHolder(view)

    override fun createViewHolder(view: View): ViewHolder {
        val holder = ViewHolder(
            view,
            view.findViewById(R.id.view_item_leaderboard_result_player_name),
            view.findViewById(R.id.view_item_leaderboard_result_player_image),
            view.findViewById(R.id.view_item_leaderboard_result_medal),
            view.findViewById(R.id.view_item_leaderboard_result_average_hit_points),
            view.findViewById(R.id.view_item_leaderboard_result_hit_points),
            view.findViewById(R.id.view_item_leaderboard_result_hit_rate),
            view.findViewById(R.id.view_item_leaderboard_result_total_games),
            view.findViewById(R.id.view_item_leaderboard_result_won_games)
        )
        holder.showClick = false
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[position]
        holder.tvPlayerName.text = result.profile.displayName
        holder.ivPlayerImage.setImageDrawable(result.profile.getProfileImage(holder.context))
        val drawable: Drawable? = when (position) {
            0 -> AppCompatResources.getDrawable(holder.context, R.drawable.image_medal_gold)
            1 -> AppCompatResources.getDrawable(holder.context, R.drawable.image_medal_silver)
            2 -> AppCompatResources.getDrawable(holder.context, R.drawable.image_medal_bronze)
            else -> null
        }
        holder.ivMedal.setImageDrawable(drawable)

        holder.tvAverageHitPoints.text =
            createSpannable(
                holder.context,
                R.string.leaderboard_average_hit_count,
                NumberFormat.getInstance().format(result.result.averageHitCount)
            )

        holder.tvHitPoints.text = createSpannable(
            holder.context,
            R.string.leaderboard_hit_count,
            result.result.hitCount.toString()
        )

        holder.tvHitRate.text = createSpannable(
            holder.context,
            R.string.leaderboard_hit_rate,
            result.result.hitRate.toString() + "%"
        )

        holder.tvTotalGames.text = createSpannable(
            holder.context,
            R.string.leaderboard_total_games,
            result.result.totalGameCount.toString()
        )

        holder.tvWonGames.text = createSpannable(
            holder.context,
            R.string.leaderboard_won_games,
            result.result.wonGameCount.toString()
        )
    }

    override fun getItemCount(): Int {
        return results.size
    }

    private fun createSpannable(
        context: Context,
        stringResId: Int,
        value: String
    ): SpannableString {
        val str = String.format(context.getString(stringResId), value)
        val span = SpannableString(str)
        span.setSpan(
            ForegroundColorSpan(context.getColor(R.color.text_result)),
            0,
            value.length,
            SpannableString.SPAN_INCLUSIVE_EXCLUSIVE
        )
        return span
    }
}