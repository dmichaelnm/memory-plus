package de.dmichael.android.memory.plus.cardsets

import android.content.Context
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import de.dmichael.android.memory.plus.system.Game
import java.io.*
import java.lang.IllegalArgumentException

object CardSetManager {

    private const val VERSION = 1

    private val cardSets = mutableListOf<CardSet>()
    private var initialized = false

    fun addOrUpdateCardSet(cardSet: CardSet) {
        var index = -1
        for (i in 0 until size()) {
            if (cardSets[i].id == cardSet.id) {
                index = i
                break
            }
        }
        if (index > -1) {
            // Update Card Set
            Log.v(Game.TAG, "Updating card set '${cardSet.displayName}' (${cardSet.id})")
            cardSets[index] = cardSet
            cardSet.commit()
        } else {
            // Add New Card Set
            Log.v(Game.TAG, "Adding new card set '${cardSet.displayName}' (${cardSet.id})")
            cardSets.add(cardSet)
        }
    }

    fun getCardSet(index: Int): CardSet {
        return cardSets[index]
    }

    fun getCardSet(id: String): CardSet {
        for (cardSet in cardSets) {
            if (cardSet.id == id) {
                return cardSet
            }
        }
        throw IllegalArgumentException("No card set found for id $id")
    }

    fun getDirectory(context: Context): File {
        val directory = File(context.dataDir, "card_sets")
        directory.mkdirs()
        return directory
    }

    fun hasCardSetName(displayName: String, excludeCardSet: CardSet?): Boolean {
        val id = excludeCardSet?.id
        for (cardSet in cardSets) {
            if (cardSet.id != id && cardSet.displayName == displayName) {
                return true
            }
        }
        return false
    }

    fun removeCardSet(context: Context, cardSet: CardSet) {
        cardSets.remove(cardSet)
        val directory = cardSet.getDirectory(context)
        directory.deleteRecursively()
    }

    fun size(): Int {
        return cardSets.size
    }

    fun initialize(context: Context) {
        if (!initialized) {
            val file = getCardDecksFile(context)
            if (file.exists()) {
                JsonReader(BufferedReader(FileReader(file))).use { reader ->
                    reader.beginObject()
                    assert(reader.nextName() == "cardSets")
                    reader.beginArray()
                    while (reader.hasNext()) {
                        cardSets.add(CardSet(reader))
                    }
                    reader.endArray()
                    reader.endObject()
                }
            }
            Log.v(Game.TAG, "Card Set Manager: ${size()} card sets deserialized")
            initialized = true
        }
    }

    fun serialize(context: Context) {
        val file = getCardDecksFile(context)
        JsonWriter(BufferedWriter(FileWriter(file))).use { writer ->
            writer.beginObject()
            writer.name("cardSets")
            writer.beginArray()
            for (cardSet in cardSets) {
                cardSet.serialize(writer)
            }
            writer.endArray()
            writer.endObject()
        }
        Log.v(Game.TAG, "Card Set Manager: ${size()} card sets serialized")
    }

    private fun getCardDecksFile(context: Context): File {
        return File(getDirectory(context), "card_sets_v$VERSION.json")
    }
}