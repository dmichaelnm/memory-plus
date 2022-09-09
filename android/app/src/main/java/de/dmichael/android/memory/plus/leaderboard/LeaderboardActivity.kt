package de.dmichael.android.memory.plus.leaderboard

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.dmichael.android.memory.plus.DialogActivity
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.profiles.ProfileManager
import de.dmichael.android.memory.plus.system.Activity

class LeaderboardActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val singleLayout = ProfileManager.getCategories().size <= 1
        val panelId = if (singleLayout) {
            setContentView(R.layout.activity_leaderboard_single)
            R.id.leaderboard_result_view
        } else {
            setContentView(R.layout.activity_leaderboard)
            R.id.leaderboard_panel
        }
        setUpAppearance()

        val vwEmpty = findViewById<View>(R.id.leaderboard_empty)
        val vwPanel = findViewById<View>(panelId)
        val btClear = findViewById<Button>(R.id.leaderboard_button_clear)

        val results = ProfileManager.getResults(-1)
        if (results.isEmpty()) {
            vwEmpty.visibility = View.VISIBLE
            vwPanel.visibility = View.GONE
            btClear.visibility = View.GONE
        } else {
            vwEmpty.visibility = View.GONE
            vwPanel.visibility = View.VISIBLE
            btClear.visibility = View.VISIBLE
        }

        // Back Button
        onButtonClick<Button>(R.id.leaderboard_button_back) {
            finish()
        }

        // Clear Button
        val launcher = DialogActivity.getLauncher(this) { dialogOption, _ ->
            if (dialogOption == 1) {
                ProfileManager.clearResults(this)
                vwEmpty.visibility = View.VISIBLE
                vwPanel.visibility = View.GONE
                btClear.visibility = View.GONE
            }
        }

        onButtonClick<Button>(R.id.leaderboard_button_clear) {
            DialogActivity.launch(
                this,
                R.string.leaderboard_dialog_clear_title,
                R.string.leaderboard_dialog_clear_message,
                R.string.leaderboard_dialog_clear_option_yes,
                R.string.leaderboard_dialog_clear_option_no,
                -1,
                null,
                launcher
            )
        }

        // Result View
        val rvResults = findViewById<RecyclerView>(R.id.leaderboard_result_view)
        rvResults.adapter = LeaderboardResultAdapter(ProfileManager.getResults(-1))

        if (!singleLayout) {
            // Category View
            val rvCategories = findViewById<RecyclerView>(R.id.leaderboard_category_view)
            rvCategories.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            rvCategories.adapter =
                LeaderboardCategoryAdapter(ProfileManager.getCategories()) { category ->
                    rvResults.adapter =
                        LeaderboardResultAdapter(ProfileManager.getResults(category))
                }
        }
    }
}