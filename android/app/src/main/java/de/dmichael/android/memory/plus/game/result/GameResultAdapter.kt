package de.dmichael.android.memory.plus.game.result

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.game.GamePlayer
import de.dmichael.android.memory.plus.system.Adapter
import kotlin.math.round

class GameResultAdapter(private val players: List<GamePlayer>) :
    Adapter<GameResultAdapter.ViewHolder>(R.layout.view_item_result) {

    class ViewHolder(
        val view: View,
        val tvPlayerName: TextView,
        val ivPlayerImage: ImageView,
        val ivMedal: ImageView,
        val tvPoints: TextView,
        val tvHitRate: TextView
    ) : Adapter.ViewHolder(view)

    override fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(
            view,
            view.findViewById(R.id.view_item_leaderboard_result_player_name),
            view.findViewById(R.id.view_item_leaderboard_result_player_image),
            view.findViewById(R.id.view_item_leaderboard_result_medal),
            view.findViewById(R.id.view_item_result_points_value),
            view.findViewById(R.id.view_item_result_hit_rate_value)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]
        holder.tvPlayerName.text = player.profile.displayName
        holder.ivPlayerImage.setImageDrawable(player.profile.getProfileImage(holder.context))
        holder.ivPlayerImage.contentDescription = String.format(
            holder.context.getString(R.string.desc_image_profile_with_name),
            player.profile.displayName
        )
        val medalDrawable: Drawable?
        val medalContentDesc: String?
        if (position == 0) {
            medalDrawable =
                AppCompatResources.getDrawable(holder.context, R.drawable.image_medal_gold)
            medalContentDesc = holder.context.getString(R.string.desc_image_medal_gold)
        } else if (position == 1) {
            medalDrawable =
                AppCompatResources.getDrawable(holder.context, R.drawable.image_medal_silver)
            medalContentDesc = holder.context.getString(R.string.desc_image_medal_silver)
        } else if (position == 2) {
            medalDrawable =
                AppCompatResources.getDrawable(holder.context, R.drawable.image_medal_bronze)
            medalContentDesc = holder.context.getString(R.string.desc_image_medal_bronze)
        } else {
            medalDrawable = null
            medalContentDesc = null
        }
        holder.ivMedal.setImageDrawable(medalDrawable)
        holder.ivMedal.contentDescription = medalContentDesc
        holder.tvPoints.text = player.hitCount.toString()
        val hitRate = round(player.hitCount.toFloat() / player.turnCount.toFloat() * 100f).toInt()
        holder.tvHitRate.text = String.format(
            holder.context.getString(R.string.game_result_label_hit_rate_value),
            hitRate
        )
        holder.showClick = false

        if (player.removed) {
            holder.view.alpha = 0.5f
        }
    }

    override fun getItemCount(): Int {
        return players.size
    }
}