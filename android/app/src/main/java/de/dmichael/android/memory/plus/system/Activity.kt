package de.dmichael.android.memory.plus.system

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import de.dmichael.android.memory.plus.R

abstract class Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(Game.TAG, "${javaClass.simpleName}: onCreate")
        // set the animation for the transition from this activity to another activity
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(Game.TAG, "${javaClass.simpleName}: onDestroy")
    }

    override fun onStart() {
        super.onStart()
        Log.v(Game.TAG, "${javaClass.simpleName}: onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.v(Game.TAG, "${javaClass.simpleName}: onStop")
    }

    override fun onPause() {
        super.onPause()
        Log.v(Game.TAG, "${javaClass.simpleName}: onPause")
        // set the animation for the transition to the next or previous activity
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
    }

    override fun onResume() {
        super.onResume()
        Log.v(Game.TAG, "${javaClass.simpleName}: onResume")
    }

    protected fun setUpAppearance() {
        // hide the navigation bar
        val rootView = findViewById<View>(android.R.id.content).rootView
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, rootView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    protected fun <T : View> onButtonClick(buttonId: Int, clicked: () -> Unit) {
        val button = findViewById<T>(buttonId)
        button.setOnClickListener {
            clicked()
        }
    }
}