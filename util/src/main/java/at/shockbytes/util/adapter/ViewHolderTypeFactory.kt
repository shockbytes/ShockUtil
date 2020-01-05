package at.shockbytes.util.adapter

import android.view.View

interface ViewHolderTypeFactory<T : Any> {

    fun type(item: T): Int

    fun create(parent: View, viewType: Int): BaseAdapter.ViewHolder<T>
}