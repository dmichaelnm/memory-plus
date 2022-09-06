package de.dmichael.android.memory.plus.game.new

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.profiles.Profile
import de.dmichael.android.memory.plus.profiles.ProfileManager
import de.dmichael.android.memory.plus.system.Adapter

class NewGameProfilesAdapter(private val callback: (profile: Profile, selected: Boolean) -> Unit) :
    Adapter<NewGameProfilesAdapter.ViewHolder>(R.layout.view_item_profile_vertical) {

    companion object {
        private const val ALPHA_SELECTED = 1f
        private const val ALPHA_UNSELECTED = 0.5f
    }

    class ViewHolder(
        val view: View,
        val ivImage: ImageView,
        val tvName: TextView
    ) : Adapter.ViewHolder(view) {
        var profile: Profile? = null
        var selected: Boolean = false
    }

    private val selectedProfiles =  mutableListOf<Profile>()

    fun getSelectedProfiles(): List<Profile> {
        return selectedProfiles
    }

    override fun createViewHolder(view: View): ViewHolder {
        val holder = ViewHolder(
            view,
            view.findViewById(R.id.view_item_profile_vertical_image),
            view.findViewById(R.id.view_item_profile_vertical_name)
        )

        view.alpha = ALPHA_UNSELECTED
        view.setOnClickListener {
            holder.selected = !holder.selected
            if (holder.selected) {
                holder.view.alpha = ALPHA_SELECTED
                selectedProfiles.add(holder.profile!!)
            } else {
                holder.view.alpha = ALPHA_UNSELECTED
                selectedProfiles.remove(holder.profile!!)
            }
            callback(holder.profile!!, holder.selected)
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val profile = ProfileManager.getProfile(position)
        holder.ivImage.setImageDrawable(profile.getProfileImage(holder.context))
        holder.tvName.text = profile.displayName
        holder.profile = profile
    }

    override fun getItemCount(): Int {
        return ProfileManager.size()
    }
}