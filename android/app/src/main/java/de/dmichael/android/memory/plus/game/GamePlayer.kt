package de.dmichael.android.memory.plus.game

import de.dmichael.android.memory.plus.profiles.Profile

class GamePlayer(val profile: Profile) {

    var removed = false

    var turnCount: Int = 0
    var hitCount: Int = 0

    fun reset() {
        removed = false
        turnCount = 0
        hitCount = 0
    }
}