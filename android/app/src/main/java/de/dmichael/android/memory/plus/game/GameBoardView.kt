package de.dmichael.android.memory.plus.game

import android.content.Context
import android.graphics.Color
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import androidx.core.view.children
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.system.Game
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

class GameBoardView(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

    internal class Dimension(val columns: Int, val rows: Int)

    class Attributes {
        var spacing: Int = 0
        var frameSize: Float = 0f
        var frameColor: Int = Color.WHITE
        var shadowSize: Int = 0
        var shadowColor: Int = Color.BLACK
        var maxRotationAngle = 0
        var maxReplacement = 0
        var duration = 0L
        var discoveredAlpha = 0f
    }

    private val attributes: Attributes = Attributes()

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.GameBoardView, 0, 0).apply {
            attributes.spacing = getDimensionPixelSize(R.styleable.GameBoardView_spacing, 0)
            attributes.frameSize = getFloat(R.styleable.GameBoardView_frameSize, 0f)
            attributes.frameColor = getColor(R.styleable.GameBoardView_frameColor, Color.WHITE)
            attributes.shadowSize = getDimensionPixelSize(R.styleable.GameBoardView_shadowSize, 0)
            attributes.shadowColor = getColor(R.styleable.GameBoardView_shadowColor, Color.BLACK)
            attributes.maxRotationAngle = getInt(R.styleable.GameBoardView_maxRotationAngle, 0)
            attributes.maxReplacement =
                getDimensionPixelSize(R.styleable.GameBoardView_maxReplacement, 0)
            attributes.duration = getInt(R.styleable.GameBoardView_duration, 0).toLong()
            attributes.discoveredAlpha = getFloat(R.styleable.GameBoardView_discoveredAlpha, 0f)
        }
    }

    var selectionCallback: ((card: GameCard, view: GameCardView) -> Unit)? = null
        set(value) {
            for (view in children) {
                (view as GameCardView).selectionCallback = value
            }
            field = value
        }

    var cards: List<GameCard>? = null
        set(value) {
            val random = Random(SystemClock.elapsedRealtime())
            if (value != null) {
                removeAllViews()
                for (card in value) {
                    card.rotation = random.nextInt(
                        attributes.maxRotationAngle * -1,
                        attributes.maxRotationAngle + 1
                    )
                    card.leftOffset = random.nextInt(
                        attributes.maxReplacement * -1,
                        attributes.maxReplacement + 1
                    )
                    card.topOffset = random.nextInt(
                        attributes.maxReplacement * -1,
                        attributes.maxReplacement + 1
                    )
                    addView(GameCardView(context, attributes, card))
                }
            }
            field = value
        }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (cards != null) {
            Log.v(Game.TAG, "GameBoard: onLayout($changed, $left, $top, $right, $bottom")

            val width = (right - left) - (attributes.maxReplacement * 2)
            val height = (bottom - top) - (attributes.maxReplacement * 2)
            Log.v(Game.TAG, "GameBoard: onLayout: width = $width, height = $height")
            val cardCount = cards!!.size
            val dimension = getCardsDimension(cardCount, width, height)
            Log.v(
                Game.TAG,
                "GameBoard: onLayout: dimension = ${dimension.columns} x ${dimension.rows}"
            )

            val spacing = attributes.spacing
            val totalHorizontalSpace = spacing * (dimension.columns - 1)
            val totalVerticalSpace = spacing * (dimension.rows - 1)
            val tempCardWidth = (width - totalHorizontalSpace) / dimension.columns
            val tempCardHeight = (height - totalVerticalSpace) / dimension.rows
            val cardSize = min(tempCardWidth, tempCardHeight)
            val usedWidth = dimension.columns * cardSize + totalHorizontalSpace
            val leftOffset = (width - usedWidth) / 2
            val usedHeight = dimension.rows * cardSize + totalVerticalSpace
            val topOffset = (height - usedHeight) / 2
            Log.v(
                Game.TAG,
                "GameBoard: onLayout: cardSpace=$spacing, cardSize=$cardSize, left=$leftOffset, top=$topOffset"
            )

            var idx = 0
            var column = 0
            var row = 0
            for (view in children) {
                val card = cards!![idx]

                val l =
                    (spacing + cardSize) * column + leftOffset + card.leftOffset + attributes.maxReplacement
                val t =
                    (spacing + cardSize) * row + topOffset + card.topOffset + attributes.maxReplacement

                view.layout(l, t, l + cardSize, t + cardSize)

                column++
                if (column == dimension.columns) {
                    column = 0
                    row++
                }

                idx++
            }
        }
    }

    private fun getCardsDimension(cardCount: Int, width: Int, height: Int): Dimension {
        val screenRatio = width.toFloat() / height.toFloat()
        var columns = sqrt(cardCount.toDouble()).toInt()
        var rows = columns
        var maxDifference = Float.MAX_VALUE
        var oldColumns = columns
        var oldRows = rows

        while (true) {
            val size = rows * columns
            if (size < cardCount) {
                if (screenRatio < 1) {
                    rows++
                } else {
                    columns++
                }
            } else {
                val cardRatio = columns.toFloat() / rows.toFloat()
                val difference = abs(cardRatio - screenRatio)
                if (difference < maxDifference) {
                    oldColumns = columns
                    oldRows = rows
                    maxDifference = difference
                    if (screenRatio < 1) {
                        columns--
                    } else {
                        rows--
                    }
                    if (columns == 0 || rows == 0) {
                        return Dimension(oldColumns, oldRows)
                    }
                } else {
                    return Dimension(oldColumns, oldRows)
                }
            }
        }
    }
}