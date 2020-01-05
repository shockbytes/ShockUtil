package at.shockbytes.util.adapter

import android.content.Context
import android.view.ViewGroup

abstract class MultiViewHolderBaseAdapter<T : Any>(
    context: Context,
    onItemClickListener: OnItemClickListener<T>? = null,
    onItemLongClickListener: OnItemLongClickListener<T>? = null,
    onItemMoveListener: OnItemMoveListener<T>? = null
) : BaseAdapter<T>(context, onItemClickListener, onItemLongClickListener, onItemMoveListener) {

    abstract val vhFactory: ViewHolderTypeFactory<T>

    override fun getItemViewType(position: Int): Int {
        return vhFactory.type(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        return vhFactory.create(parent, viewType)
    }
}