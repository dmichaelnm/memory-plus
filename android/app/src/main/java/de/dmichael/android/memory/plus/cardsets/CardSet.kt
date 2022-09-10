package de.dmichael.android.memory.plus.cardsets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.system.BitmapUtil
import de.dmichael.android.memory.plus.system.Game
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*

class CardSet : Iterable<Card> {

    constructor() {
        this.id = UUID.randomUUID().toString()
    }

    constructor(cardSet: CardSet) {
        this.id = cardSet.id
        this.displayName = cardSet.displayName
        this.cards.addAll(cardSet.cards)
    }

    constructor(reader: JsonReader) {
        reader.beginObject()
        assert(reader.nextName() == "id")
        this.id = reader.nextString()
        assert(reader.nextName() == "displayName")
        this.displayName = reader.nextString()
        assert(reader.nextName() == "cards")
        reader.beginArray()
        while (reader.hasNext()) {
            cards.add(Card(reader))
        }
        reader.endArray()
        reader.endObject()
    }

    private val cards = mutableListOf<Card>()
    private var drawable: Drawable? = null
    val id: String
    var displayName: String = ""

    fun addCard(context: Context, bitmap: Bitmap): Boolean {
        val hash = BitmapUtil.hash(bitmap)

        for (card in cards) {
            if (card.getHash() == hash && card.getState() != Card.State.Deleted) {
                // Image already exists with another card
                return false
            }
        }

        val card = Card(context, this, hash, bitmap)
        cards.add(card)

        return true
    }

    fun addCard(context: Context, uri: Uri): Boolean {
        val bitmap = BitmapUtil.deserialize(uri)
        return addCard(context, bitmap!!)
    }

    fun getCard(index: Int, excludeDeletedCards: Boolean): Card {
        if (!excludeDeletedCards) {
            return cards[index]
        }
        var i = 0
        for (card in cards) {
            if (card.getState() != Card.State.Deleted) {
                if (i == index) {
                    return card
                }
                i++
            }
        }
        throw IllegalArgumentException("Invalid index: $index")
    }

    fun getDrawable(context: Context): Drawable {
        if (size(false) == 0) {
            return AppCompatResources.getDrawable(context, R.drawable.image_card_set_empty)!!
        }
        if (drawable == null) {
            val base = AppCompatResources.getDrawable(context, R.drawable.image_card_set)
            val image = cards[0].getDrawable(context)
            drawable = BitmapUtil.combine(context, base!!, image, 56, 56, 144, 144)
        }
        return drawable!!
    }

    fun getDirectory(context: Context): File {
        val directory = File(CardSetManager.getDirectory(context), id)
        directory.mkdirs()
        return directory
    }

    fun size(excludeDeletedCards: Boolean): Int {
        if (!excludeDeletedCards) {
            return cards.size
        }
        var size = 0
        for (card in cards) {
            if (card.getState() != Card.State.Deleted) {
                size++
            }
        }
        return size
    }

    fun serialize(writer: JsonWriter) {
        writer.beginObject()
        writer.name("id").value(id)
        writer.name("displayName").value(displayName)
        writer.name("cards")
        writer.beginArray()
        for (card in cards) {
            card.serialize(writer)
        }
        writer.endArray()
        writer.endObject()
    }

    fun commit() {
        Log.v(Game.TAG, "Card Set '$displayName': commit")
        for (i in size(false) - 1 downTo 0) {
            val card = cards[i]
            if (card.getState() == Card.State.Deleted) {
                card.release()
                cards.removeAt(i)
                Log.v(Game.TAG, "Card ${card.id} removed from card set '$displayName' ($id)")
            } else if (card.getState() == Card.State.New) {
                card.setUnchanged()
                Log.v(Game.TAG, "Card ${card.id} added from card set '$displayName' ($id)")
            }
        }
    }

    fun rollback(context: Context) {
        Log.v(Game.TAG, "Card Set '$displayName': rollback")
        for (card in cards) {
            if (card.getState() == Card.State.New) {
                card.release()
            } else if (card.getState() == Card.State.Deleted) {
                card.setUnchanged()
            }
        }
        val directory = getDirectory(context)
        val files = directory.listFiles()
        if (files == null || files.isEmpty()) {
            directory.deleteRecursively()
            Log.v(Game.TAG, "Directory '${directory.absolutePath} deleted")
        }
    }

    override fun iterator(): Iterator<Card> {
        return cards.iterator()
    }
}