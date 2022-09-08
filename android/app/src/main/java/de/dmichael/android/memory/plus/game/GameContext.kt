package de.dmichael.android.memory.plus.game

import android.content.Intent
import android.os.SystemClock
import android.util.Log
import de.dmichael.android.memory.plus.cardsets.CardSet
import de.dmichael.android.memory.plus.game.result.GameResultActivity
import de.dmichael.android.memory.plus.profiles.ProfileManager
import de.dmichael.android.memory.plus.system.Game
import kotlin.random.Random

object GameContext {

    enum class State {
        FirstCard,
        SecondCard
    }

    private var cardSet: CardSet? = null
    private var onStateChanged: ((locked: Boolean, state: State) -> Unit)? = null
    private var initialized = false
    private var players: List<GamePlayer>? = null
    private var cards: List<GameCard>? = null
    private var firstCard: GameCard? = null
    private var secondCard: GameCard? = null
    private var currentPlayerIndex: Int = 0
    private var locked = false
        set(value) {
            field = value
            onStateChanged?.invoke(locked, state)
        }
    private var state: State = State.FirstCard
        set(value) {
            field = value
            onStateChanged?.invoke(locked, state)
        }

    fun initialize(
        profileIds: Array<String>,
        cardSet: CardSet,
        onStateChanged: (locked: Boolean, state: State) -> Unit
    ) {
        val random = Random(SystemClock.elapsedRealtime())

        this.cardSet = cardSet
        this.onStateChanged = onStateChanged
        this.firstCard = null
        this.secondCard = null
        this.players =
            List(profileIds.size) { index -> GamePlayer(ProfileManager.getProfile(profileIds[index])) }
        this.cards = shuffleCards(cardSet)
        this.currentPlayerIndex = random.nextInt(players!!.size)
        this.state = State.FirstCard
        this.locked = false
        this.initialized = true
    }

    fun cardCount(): Int {
        return cardSet!!.size(false)
    }

    fun clear() {
        initialized = false
    }

    fun reset() {
        val random = Random(SystemClock.elapsedRealtime())

        this.firstCard = null
        this.secondCard = null
        for (player in players!!) {
            player.reset()
        }
        this.cards = shuffleCards(cardSet!!)
        this.currentPlayerIndex = random.nextInt(players!!.size)
        this.state = State.FirstCard
        this.locked = false
    }

    fun cards(): List<GameCard> {
        return cards!!
    }

    fun isCancelable(): Boolean {
        return !locked && state == State.FirstCard
    }

    fun isInitialized(): Boolean {
        return initialized
    }

    fun isSinglePlayer(): Boolean {
        return players!!.size == 1
    }

    fun currentPlayer(): GamePlayer {
        return players!![currentPlayerIndex]
    }

    fun nextPlayer() {
        do {
            currentPlayerIndex++
            if (currentPlayerIndex == players!!.size) {
                currentPlayerIndex = 0
            }
        } while (currentPlayer().removed)
    }

    fun removePlayer(activity: GameActivity) {
        currentPlayer().removed = true
        activity.showNextPlayer(true) {
            state = State.FirstCard
            locked = false
        }
    }

    fun switchCards(card: GameCard, view: GameCardView, activity: GameActivity) {
        if (initialized && !locked) {
            if (state == State.FirstCard && card.state == GameCard.State.Hidden) {
                locked = true
                view.switchCard {
                    firstCard = card
                    state = State.SecondCard
                    locked = false
                }
            } else if (state == State.SecondCard && card.state == GameCard.State.Hidden) {
                locked = true
                view.switchCard {
                    secondCard = card
                    checkMatch(activity)
                }
            }
        }
    }

    fun getRealPlayerCount(): Int {
        var count = 0
        for (player in players!!) {
            if (!player.removed) {
                count++
            }
        }
        return count
    }

    fun sortedPlayerList(): List<GamePlayer> {
        return players!!.sortedWith { p1, p2 ->
            if (p1.hitCount > p2.hitCount) {
                -1
            } else if (p1.hitCount < p2.hitCount) {
                1
            } else {
                if (!p1.removed && p2.removed) {
                    -1
                } else if (p1.removed && !p2.removed) {
                    1
                } else {
                    0
                }
            }
        }
    }

    private fun shuffleCards(cardSet: CardSet): List<GameCard> {
        val random = Random(SystemClock.elapsedRealtime())
        val cardCount = cardSet.size(false) * 2
        val gameCards = List<GameCard?>(cardCount) { null }.toMutableList()
        val positions = List(cardCount) { index -> index }.toMutableList()
        for (card in cardSet) {
            for (i in 0 until 2) {
                val index = random.nextInt(positions.size)
                val position = positions[index]
                gameCards[position] = GameCard(card)
                positions.removeAt(index)
            }
        }
        return List(cardCount) { index -> gameCards[index]!! }
    }

    private fun checkMatch(activity: GameActivity) {
        val player = currentPlayer()
        player.turnCount++
        if (firstCard!!.match(secondCard!!)) {
            Log.v(Game.TAG, "Cards has a match")
            player.hitCount++
            activity.showCurrentPlayer(player)

            firstCard!!.state = GameCard.State.Discovered
            secondCard!!.state = GameCard.State.Discovered

            firstCard!!.view!!.discovered {
                secondCard!!.view!!.discovered {
                    // Check if hidden cards are left
                    var finished = true
                    for (card in cards!!) {
                        if (card.state != GameCard.State.Discovered) {
                            finished = false
                            break
                        }
                    }
                    if (finished) {
                        Log.v(Game.TAG, "Match has ended")
                        activity.startActivity(Intent(activity, GameResultActivity::class.java))
                    } else {
                        state = State.FirstCard
                        locked = false
                    }
                }
            }
        } else {
            Log.v(Game.TAG, "Cards has no match")
            firstCard!!.view!!.switchCard {
                secondCard!!.view!!.switchCard {
                    activity.showNextPlayer(false) {
                        state = State.FirstCard
                        locked = false
                    }
                }
            }
        }
    }
}