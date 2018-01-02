package at.shockbytes.util.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import java.util.*

/**
 * @author Martin Macheiner
 * Date: 05.03.2017.
 */
abstract class BaseAdapter<T>(protected var context: Context,
                              extData: MutableList<T>) : RecyclerView.Adapter<BaseAdapter<T>.ViewHolder>() {

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

    open var data: MutableList<T> = ArrayList()
        set(value) {

            //Remove all deleted items
            (field.size - 1 downTo 0)
                    .filter { getLocation(value, field[it]) < 0 }
                    .forEach { deleteEntity(it) }

            //Add and move items
            for (i in value.indices) {
                val entity = value[i]
                val location = getLocation(field, entity)
                if (location < 0) {
                    addEntity(i, entity)
                } else if (location != i) {
                    moveEntity(i, location)
                }
            }
        }

    protected var inflater: LayoutInflater = LayoutInflater.from(context)

    var onItemMoveListener: OnItemMoveListener<T>? = null
    var onItemClickListener: OnItemClickListener<T>? = null
    var onItemLongClickListener: OnItemLongClickListener<T>? = null

    init {
        data = extData
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    //-----------------------------Data Section-----------------------------
    fun addEntity(i: Int, entity: T) {
        data.add(i, entity)
        notifyItemInserted(i)
    }

    fun deleteEntity(entity: T) {
        val location = getLocation(data, entity)
        if (location >= 0) {
            deleteEntity(location)
        }
    }

    fun deleteEntity(i: Int) {
        data.removeAt(i)
        notifyItemRemoved(i)
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
        data[arrayIdx] = changed
        notifyItemChanged(arrayIdx)
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    fun moveEntity(i: Int, dest: Int) {
        val temp = data.removeAt(i)
        data.add(dest, temp)
        notifyItemMoved(i, dest)
        notifyDataSetChanged()
    }

    fun getLocation(searching: T): Int {
        return getLocation(data, searching)
    }

    protected fun getLocation(data: List<T>, searching: T): Int {

        for (j in data.indices) {
            val newEntity = data[j]
            if (searching == newEntity) {
                return j
            }
        }
        return -1
    }
    //----------------------------------------------------------------------

    abstract inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        protected var content: T? = null

        init {
            itemView.setOnClickListener {
                if (content != null){
                    onItemClickListener?.onItemClick(content!!, itemView)
                }
            }
            itemView.setOnLongClickListener {
                if (content != null) {
                    onItemLongClickListener?.onItemLongClick(content!!, itemView)
                    true
                } else {
                    false
                }
            }
        }

        abstract fun bind(t: T)

    }

}
