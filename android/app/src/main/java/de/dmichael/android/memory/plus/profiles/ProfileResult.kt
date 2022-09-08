package de.dmichael.android.memory.plus.profiles

import android.util.JsonReader
import android.util.JsonWriter
import kotlin.math.floor

class ProfileResult {

    var cardCount: Int
    var totalGameCount: Int
    var competitiveGameCount: Int
    var wonGameCount: Int
    var turnCount: Int
    var hitCount: Int

    val averageHitCount: Float
        get() {
            if (totalGameCount > 0 ) {
                return floor(hitCount.toFloat() / totalGameCount.toFloat() * 100f) / 100f
            }
            return 0f
        }
    val hitRate: Int
        get() {
            if (turnCount > 0) {
                return floor(hitCount.toFloat() / turnCount.toFloat() * 100).toInt()
            }
            return 0
        }

    constructor(cardCount: Int) {
        this.cardCount = cardCount
        this.totalGameCount = 0
        this.competitiveGameCount = 0
        this.wonGameCount = 0
        this.turnCount = 0
        this.hitCount = 0
    }

    constructor(reader: JsonReader) {
        reader.beginObject()
        assert(reader.nextName() == "cardCount")
        cardCount = reader.nextInt()
        assert(reader.nextName() == "totalGameCount")
        totalGameCount = reader.nextInt()
        assert(reader.nextName() == "competitiveGameCount")
        competitiveGameCount = reader.nextInt()
        assert(reader.nextName() == "wonGameCount")
        wonGameCount = reader.nextInt()
        assert(reader.nextName() == "turnCount")
        turnCount = reader.nextInt()
        assert(reader.nextName() == "hitCount")
        hitCount = reader.nextInt()
        reader.endObject()
    }

    fun serialize(writer: JsonWriter) {
        writer.beginObject()
        writer.name("cardCount").value(cardCount)
        writer.name("totalGameCount").value(totalGameCount)
        writer.name("competitiveGameCount").value(competitiveGameCount)
        writer.name("wonGameCount").value(wonGameCount)
        writer.name("turnCount").value(turnCount)
        writer.name("hitCount").value(hitCount)
        writer.endObject()
    }
}