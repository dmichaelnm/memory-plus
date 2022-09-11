package de.dmichael.android.memory.plus.profiles

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.system.Adapter
import de.dmichael.android.memory.plus.system.Game

class ProfilesAdapter(private val callback: (profile: Profile?, longClick: Boolean) -> Unit) :
    Adapter<ProfilesAdapter.ViewHolder>(R.layout.view_item_profile_horizontal) {

    class ViewHolder(
        view: View,
        val ivProfileImage: ImageView,
        val tvProfileName: TextView,
        val tvProfileIdentifier: TextView
    ) : Adapter.ViewHolder(view) {
        var profile: Profile? = null
    }

    override fun createViewHolder(view: View): ViewHolder {
        val holder = ViewHolder(
            view,
            view.findViewById(R.id.view_item_profile_horizontal_image),
            view.findViewById(R.id.view_item_profile_horizontal_name),
            view.findViewById(R.id.view_item_profile_horizontal_identifier)
        )

        view.setOnClickListener {
            Log.v(Game.TAG, "Short click on profile ${holder.profile}")
            callback(holder.profile, false)
        }
        view.setOnLongClickListener {
            Log.v(Game.TAG, "Long click on profile ${holder.profile}")
            callback(holder.profile, true)
            true
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == ProfileManager.size()) {
            holder.profile = null
            holder.ivProfileImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    holder.context,
                    R.drawable.image_item_new
                )
            )
            holder.ivProfileImage.contentDescription =
                holder.context.getString(R.string.desc_image_profile_new)
            holder.tvProfileName.setText(R.string.profiles_new_profile)
            holder.tvProfileIdentifier.visibility = View.GONE
        } else {
            val profile = ProfileManager.getProfile(position)
            holder.profile = profile
            holder.ivProfileImage.setImageDrawable(profile.getProfileImage(holder.context))
            holder.ivProfileImage.contentDescription = String.format(
                holder.context.getString(R.string.desc_image_profile_with_name),
                profile.displayName
            )
            holder.tvProfileName.text = profile.displayName
            holder.tvProfileIdentifier.text = profile.id
        }
    }

    override fun getItemCount(): Int {
        return ProfileManager.size() + 1
    }
}