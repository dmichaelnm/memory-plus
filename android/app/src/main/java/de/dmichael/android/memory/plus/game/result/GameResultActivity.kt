package de.dmichael.android.memory.plus.game.result

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import de.dmichael.android.memory.plus.MainMenuActivity
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.game.GameActivity
import de.dmichael.android.memory.plus.game.GameContext
import de.dmichael.android.memory.plus.system.Activity

class GameResultActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_result)
        setUpAppearance()

        // Back Button
        onButtonClick<Button>(R.id.game_result_button_back) {
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish()
        }

        // Again Button
        onButtonClick<Button>(R.id.game_result_button_again) {
            GameContext.reset()
            startActivity(Intent(this, GameActivity::class.java))
            finish()
        }

        // Result View
        val rvResult = findViewById<RecyclerView>(R.id.game_result_view)
        rvResult.adapter = GameResultAdapter(GameContext.sortedPlayerList())
    }
}