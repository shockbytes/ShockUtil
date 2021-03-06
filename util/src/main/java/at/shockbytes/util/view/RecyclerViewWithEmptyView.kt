package at.shockbytes.util.view

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

/**
 * Author:  Martin Macheiner
 * Date:    15.12.2015
 */
class RecyclerViewWithEmptyView : RecyclerView {

    private var mEmptyView: View? = null

    private val emptyObserver = object : RecyclerView.AdapterDataObserver() {

        override fun onChanged() {

            if (adapter?.itemCount == 0 && mEmptyView?.alpha == 0f) {
                mEmptyView?.animate()?.alpha(1f)?.start()
                isNestedScrollingEnabled = false
            } else if (adapter?.itemCount != 0 && mEmptyView?.alpha == 1f) {
                mEmptyView?.animate()?.alpha(0f)?.start()
                isNestedScrollingEnabled = true
            }
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)

        adapter?.registerAdapterDataObserver(emptyObserver)
        emptyObserver.onChanged()
    }

    fun setEmptyView(emptyView: View) {
        mEmptyView = emptyView
    }
}
