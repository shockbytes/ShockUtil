package at.shockbytes.util.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View

/**
 * Author:  Martin Macheiner
 * Date:    05.03.2017
 */
abstract class BaseAdapter<T : Any>(
    protected val context: Context
) : RecyclerView.Adapter<BaseAdapter<T>.ViewHolder>() {

    interface OnItemClickListener<T> {

        fun onItemClick(t: T, v: View)
    }

    interface OnItemLongClickListener<T> {

        fun onItemLongClick(t: T, v: View)
    }

    interface OnItemMoveListener<T> {

        fun onItemMove(t: T, from: Int, to: Int)

        fun onItemMoveFinished()

        fun onItemDismissed(t: T, position: Int)
    }

    open var data: MutableList<T> = mutableListOf()
        set(value) {

            // Remove all deleted items
            (field.size - 1 downTo 0)
                    .filter { getLocation(value, field[it]) < 0 }
                    .forEach { deleteEntity(it) }

            // Add and move items
            for (index in value.indices) {
                val entity = value[index]
                val location = getLocation(field, entity)
                if (location < 0) {
                    addEntity(index, entity)
                } else if (location != index && location < data.size) {
                    moveEntity(index, location)
                }
            }
        }

    protected val inflater: LayoutInflater = LayoutInflater.from(context)

    var onItemMoveListener: OnItemMoveListener<T>? = null
    var onItemClickListener: OnItemClickListener<T>? = null
    var onItemLongClickListener: OnItemLongClickListener<T>? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    // ----------------------------- Public API -----------------------------

    fun addEntity(i: Int, entity: T) {

        val index = getIndexInRange(i)

        data.add(index, entity)
        notifyItemInserted(index)
    }

    fun deleteEntity(entity: T) {
        val location = getLocation(data, entity)
        if (location >= 0) {
            deleteEntity(location)
        }
    }

    fun deleteEntity(i: Int) {

        val index = getIndexInRange(i)

        data.removeAt(index)
        notifyItemRemoved(index)
    }

    fun addEntityAtLast(entity: T) {
        addEntity(data.size, entity)
    }

    fun addEntityAtFirst(entity: T) {
        addEntity(0, entity)
    }

    fun updateEntity(entity: T) {
        val location = getLocation(data, entity)
        if (location >= 0) {
            data[location] = entity
            notifyItemChanged(location)
        }
    }

    fun replace(changed: T, arrayIdx: Int) {

        val idx = getIndexInRange(arrayIdx)

        data[idx] = changed
        notifyItemChanged(idx)
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    fun moveEntity(index: Int, location: Int) {
        if (index < data.size) {
            val temp = data.removeAt(index)
            data.add(location, temp)
            notifyItemMoved(index, location)
            notifyDataSetChanged()
        }
    }

    fun getLocation(searching: T): Int {
        return getLocation(data, searching)
    }

    // ----------------------------------------------------------------------

    private fun getIndexInRange(index: Int): Int {
        return if (data.size == 0) {
            0
        } else {
            index.coerceIn(0 until data.size)
        }
    }

    private fun getLocation(data: List<T>, searching: T): Int {
        return data.indexOf(searching)
    }

    abstract inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        protected lateinit var content: T

        fun bind(t: T) {
            content = t
            bindToView(t)
            setClickListener()
        }

        abstract fun bindToView(t: T)

        private fun setClickListener() {
            itemView.setOnClickListener {
                onItemClickListener?.onItemClick(content, itemView)
            }
            itemView.setOnLongClickListener {
                onItemLongClickListener?.onItemLongClick(content, itemView)
                true
            }
        }
    }
}
