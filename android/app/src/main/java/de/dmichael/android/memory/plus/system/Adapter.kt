package de.dmichael.android.memory.plus.system

import android.annotation.SuppressLint
import android.view.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import de.dmichael.android.memory.plus.R

abstract class Adapter<VH : RecyclerView.ViewHolder>(private val viewId: Int) :
    RecyclerView.Adapter<VH>() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(viewId, parent, false)
        val holder = createViewHolder(view)

        view.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == KeyEvent.ACTION_DOWN) {
                view.background = AppCompatResources.getDrawable(
                    context,
                    R.drawable.shape_item_background_selected
                )
            } else if (motionEvent.action == KeyEvent.ACTION_UP
                || motionEvent.action == MotionEvent.ACTION_CANCEL
            ) {
                view.background =
                    AppCompatResources.getDrawable(context, R.drawable.shape_item_background)
            }
            false
        }

        return holder
    }

    protected abstract fun createViewHolder(view: View): VH
}