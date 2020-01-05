package at.shockbytes.util.adapter

import android.view.ViewGroup

interface ViewHolderTypeFactory<T : Any> {

    fun type(item: T): Int

    fun create(parent: ViewGroup, viewType: Int): BaseAdapter.ViewHolder<T>
}