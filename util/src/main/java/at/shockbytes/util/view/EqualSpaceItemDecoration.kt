package at.shockbytes.util.view

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Apply equal margin to a RecyclerViewItem
 */
class EqualSpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View,
                                parent: RecyclerView, state: RecyclerView.State) {
        outRect.bottom = space
        outRect.top = space
        outRect.left = space
        outRect.right = space
    }
}