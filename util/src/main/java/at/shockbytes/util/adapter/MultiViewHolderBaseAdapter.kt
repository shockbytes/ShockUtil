package at.shockbytes.util.adapter

import android.content.Context
import android.view.ViewGroup

abstract class MultiViewHolderBaseAdapter<T : Any>(context: Context) : BaseAdapter<T>(context) {

    abstract val vhFactory: ViewHolderTypeFactory<T>

    override fun getItemViewType(position: Int): Int {
        return vhFactory.type(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        return vhFactory.create(parent, viewType)
    }
}