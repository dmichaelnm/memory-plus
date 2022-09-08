package de.dmichael.android.memory.plus.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.appcompat.content.res.AppCompatResources
import de.dmichael.android.memory.plus.R
import de.dmichael.android.memory.plus.system.AnimationAdapter
import de.dmichael.android.memory.plus.system.Game

@SuppressLint("ViewConstructor")
class GameCardView(
    context: Context,
    private val attributes: GameBoardView.Attributes,
    private val card: GameCard
) :
    View(context) {

    private val bitmapBack: Bitmap
    private val bitmapFront: Bitmap
    private val bitmapPaint = Paint()
    private val bitmapRect = Rect()
    private val frameRect = Rect()
    private val framePaint = Paint()
    private val shadowRect = Rect()
    private val shadowPaint = Paint()
    private val cardRect = Rect()

    init {
        bitmapBack = (AppCompatResources.getDrawable(
            context,
            R.drawable.image_card_back
        ) as BitmapDrawable).bitmap
        bitmapFront = card.getBitmap(context)

        framePaint.color = attributes.frameColor
        shadowPaint.color = attributes.shadowColor

        rotation = card.rotation.toFloat()

        setOnClickListener {
            if (selectionCallback != null) {
                selectionCallback?.invoke(card, this)
            }
        }

        if (card.state == GameCard.State.Discovered) {
            alpha = attributes.discoveredAlpha
        }

        card.view = this
    }

    var selectionCallback: ((card: GameCard, view: GameCardView) -> Unit)? = null

    fun discovered(callback: () -> Unit) {
        val animation = AlphaAnimation(1f, attributes.discoveredAlpha)
        animation.duration = attributes.duration
        animation.fillAfter = true
        animation.setAnimationListener(object : AnimationAdapter() {
            override fun onAnimationEnd(animation: Animation?) {
                callback()
            }
        })
        startAnimation(animation)
    }

    fun switchCard(callback: () -> Unit) {
        val scaleDownAnimation = ScaleAnimation(1f, 0f, 1f, 1f, width / 2f, 0f)
        scaleDownAnimation.duration = attributes.duration
        scaleDownAnimation.fillAfter = true
        scaleDownAnimation.setAnimationListener(object : AnimationAdapter() {
            override fun onAnimationEnd(animation: Animation?) {
                if (card.state == GameCard.State.Hidden) {
                    card.state = GameCard.State.Visible
                } else {
                    card.state = GameCard.State.Hidden
                }
                val scaleUpAnimation = ScaleAnimation(0f, 1f, 1f, 1f, width / 2f, 0f)
                scaleUpAnimation.duration = attributes.duration
                scaleUpAnimation.fillAfter = true
                scaleUpAnimation.setAnimationListener(object : AnimationAdapter() {
                    override fun onAnimationEnd(animation: Animation?) {
                        callback()
                    }
                })
                startAnimation(scaleUpAnimation)
            }
        })
        startAnimation(scaleDownAnimation)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        Log.v(Game.TAG, "GameCardView: onLayout($changed, $left, $top, $right, $bottom")
    }

    override fun onDraw(canvas: Canvas?) {
        Log.v(Game.TAG, "GameCardView: onDraw($canvas)")
        super.onDraw(canvas)
        if (canvas != null) {
            canvas.save()

            val bitmap = if (card.state == GameCard.State.Hidden) {
                bitmapBack
            } else {
                bitmapFront
            }

            // Draw Shadow
            shadowRect.top = attributes.shadowSize
            shadowRect.left = attributes.shadowSize
            shadowRect.bottom = height
            shadowRect.right = width
            canvas.drawRect(shadowRect, shadowPaint)

            // Draw Frame
            frameRect.right = width - attributes.shadowSize
            frameRect.bottom = height - attributes.shadowSize
            canvas.drawRect(frameRect, framePaint)

            // Draw bitmap
            val frameSize = ((width - attributes.shadowSize) * attributes.frameSize).toInt()
            bitmapRect.right = bitmap.width
            bitmapRect.bottom = bitmap.height
            cardRect.top = frameSize
            cardRect.left = frameSize
            cardRect.bottom = height - frameSize - attributes.shadowSize
            cardRect.right = width - frameSize - attributes.shadowSize
            canvas.drawBitmap(bitmap, bitmapRect, cardRect, bitmapPaint)

            canvas.restore()
        }
    }
}