package at.shockbytes.util.view

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View

/**
 * Apply equal margin to a RecyclerViewItem
 */
class EqualSpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = space
        outRect.top = space
        outRect.left = space
        outRect.right = space
    }
}