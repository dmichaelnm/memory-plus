package de.dmichael.android.memory.plus.game

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import de.dmichael.android.memory.plus.DialogActivity
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.cardsets.CardSet
import de.dmichael.android.memory.plus.cardsets.CardSetManager
import de.dmichael.android.memory.plus.game.result.GameResultActivity
import de.dmichael.android.memory.plus.profiles.Profile
import de.dmichael.android.memory.plus.system.Activity
import de.dmichael.android.memory.plus.system.AnimationAdapter

class GameActivity : Activity() {

    companion object {

        private const val CARD_SET_ID = "cardSetId"
        private const val PROFILE_IDS = "profileIds"
        private const val PLAYER_FADING_DURATION = 500L

        fun launchGame(activity: Activity, cardSet: CardSet, profiles: List<Profile>) {
            GameContext.clear()
            val intent = Intent(activity, GameActivity::class.java)
            intent.putExtra(CARD_SET_ID, cardSet.id)
            val profileIds = Array(profiles.size) { index -> profiles[index].id }
            intent.putExtra(PROFILE_IDS, profileIds)
            activity.startActivity(intent)
        }
    }

    private lateinit var btnCancel: Button
    private lateinit var ivPlayerImage: ImageView
    private lateinit var tvPlayerName: TextView
    private lateinit var gbvGameBoard: GameBoardView
    private lateinit var vwInfoPanel: View
    private lateinit var tvTurns: TextView
    private lateinit var tvHits: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setUpAppearance()

        // Get Components
        ivPlayerImage = findViewById(R.id.game_board_player_profile_image)
        tvPlayerName = findViewById(R.id.game_board_player_profile_name)
        vwInfoPanel = findViewById(R.id.game_board_info_panel)
        btnCancel = findViewById(R.id.game_board_button_cancel)
        tvTurns = findViewById(R.id.game_board_result_turns)
        tvHits = findViewById(R.id.game_board_result_hits)
        gbvGameBoard = findViewById(R.id.game_board_view)

        if (!GameContext.isInitialized()) {
            // Card Set
            val cardSetId = intent.getStringExtra(CARD_SET_ID)
            val cardSet = CardSetManager.getCardSet(cardSetId!!)

            // Player
            val profiles = intent.getStringArrayExtra(PROFILE_IDS)

            // Initialize Game Context
            GameContext.initialize(profiles!!, cardSet) { locked, state ->
                btnCancel.isEnabled = !locked && state == GameContext.State.FirstCard
            }
        }

        // Game Board
        gbvGameBoard.cards = GameContext.cards()
        gbvGameBoard.selectionCallback = { card, view ->
            GameContext.switchCards(card, view, this)
        }

        val dialogLauncher = DialogActivity.getLauncher(this) { dialogOption, _ ->
            val playerCount = GameContext.getRealPlayerCount()
            if ((playerCount == 1 && dialogOption == 1) || (playerCount > 1 && dialogOption == 2)) {
                // Cancel the game
                startActivity(Intent(this, GameResultActivity::class.java))
            } else if (playerCount > 1 && dialogOption == 1) {
                // Leave the game
                GameContext.removePlayer(this)
            }
        }

        // Cancel Button
        onButtonClick<Button>(R.id.game_board_button_cancel) {
            if (GameContext.isCancelable()) {
                if (GameContext.getRealPlayerCount() == 1) {
                    // Single Player
                    DialogActivity.launch(
                        this,
                        R.string.game_dialog_cancel_single_title,
                        R.string.game_dialog_cancel_single_message,
                        R.string.game_dialog_cancel_single_option_yes,
                        R.string.game_dialog_cancel_single_option_no,
                        -1,
                        null,
                        dialogLauncher
                    )
                } else {
                    // Multi Player
                    DialogActivity.launch(
                        this,
                        R.string.game_dialog_cancel_multi_title,
                        R.string.game_dialog_cancel_multi_message,
                        R.string.game_dialog_cancel_multi_option_leave,
                        R.string.game_dialog_cancel_multi_option_cancel,
                        R.string.game_dialog_cancel_multi_option_no,
                        null,
                        dialogLauncher
                    )
                }
            }
        }

        showCurrentPlayer(GameContext.currentPlayer())
    }

    fun showCurrentPlayer(player: GamePlayer) {
        ivPlayerImage.setImageDrawable(player.profile.getProfileImage(this))
        tvPlayerName.text = player.profile.displayName
        tvTurns.text = String.format(getString(R.string.game_turns), player.turnCount + 1)
        tvHits.text = String.format(getString(R.string.game_hits), player.hitCount)
    }

    fun showNextPlayer(skipSinglePlayerCheck: Boolean, callback: () -> Unit) {
        if (!skipSinglePlayerCheck && GameContext.getRealPlayerCount() == 1) {
            // When single player then return directly without any animation
            showCurrentPlayer(GameContext.currentPlayer())
            callback()
            return
        }

        // Fading out old player
        val fadeOutAnimation = AlphaAnimation(1f, 0f)
        fadeOutAnimation.duration = PLAYER_FADING_DURATION
        fadeOutAnimation.fillAfter = true
        fadeOutAnimation.setAnimationListener(object : AnimationAdapter() {
            override fun onAnimationEnd(animation: Animation?) {
                // Next player
                GameContext.nextPlayer()
                showCurrentPlayer(GameContext.currentPlayer())
                // Fading in new player
                val fadeInAnimation = AlphaAnimation(0f, 1f)
                fadeInAnimation.duration = PLAYER_FADING_DURATION
                fadeInAnimation.fillAfter = true
                fadeInAnimation.setAnimationListener(object : AnimationAdapter() {
                    override fun onAnimationEnd(animation: Animation?) {
                        callback()
                    }
                })
                vwInfoPanel.startAnimation(fadeInAnimation)
            }
        })
        vwInfoPanel.startAnimation(fadeOutAnimation)
    }
}