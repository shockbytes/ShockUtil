package at.shockbytes.util.adapter

/**
 * @author Martin Macheiner
 * Date: 09.09.2015.
 */
interface ItemTouchHelperAdapter {

    fun onItemMove(from: Int, to: Int): Boolean

    fun onItemMoveFinished()

    fun onItemDismiss(position: Int)

}
