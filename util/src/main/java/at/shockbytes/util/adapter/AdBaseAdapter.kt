package at.shockbytes.util.adapter

import android.content.Context
import android.support.annotation.IdRes
import android.view.View
import android.view.ViewGroup
import at.shockbytes.util.BuildConfig
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import java.util.*


/**
 * @author Martin Macheiner
 * Date: 26.12.2017.
 */
abstract class AdBaseAdapter<T>(c: Context, d: List<T>) : BaseAdapter<T>(c, d) {

    var adPosition = 2

    /**
     * This view must contain a AdView with id
     */
    abstract val adViewContainer: View

    abstract val  adViewViewId: Int

    override var data: MutableList<T> = ArrayList()
        set(value) {

            field = ArrayList()
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

            addAdvertisementEntity()
            notifyDataSetChanged()
        }

    abstract val testDeviceId: String

    override fun getItemViewType(position: Int): Int {
        return if (position == adPosition) adViewType else itemViewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        return when (viewType) {

            adViewType -> AdViewHolder(adViewContainer, adViewViewId)
            else -> getViewHolder(parent) // If nothing found, use default item
        }
    }

    abstract fun addAdvertisementEntity()

    abstract fun getViewHolder(parent: ViewGroup): ViewHolder

    inner class AdViewHolder(itemView: View, @IdRes adViewId: Int) : ViewHolder(itemView) {

        private val adView: AdView = itemView.findViewById(adViewId)

        private var isAdLoaded = false

        override fun bind(t: T) {

            if (!isAdLoaded) {
                isAdLoaded = true
                cleanAndRecycle()

                // Build requests different for release and debug config
                val request = if (BuildConfig.DEBUG) {
                    AdRequest.Builder().addTestDevice(testDeviceId).build()
                } else {
                    AdRequest.Builder().build()
                }
                adView.loadAd(request)
            }
        }

        private fun cleanAndRecycle() {
            // The NativeExpressAdViewHolder recycled by the RecyclerView may be a different
            // instance than the one used previously for this position. Clear the
            // NativeExpressAdViewHolder of any subviews in case it has a different
            // AdView associated with it, and make sure the AdView for this position doesn't
            // already have a parent of a different recycled NativeExpressAdViewHolder.
            itemView as ViewGroup
            if (itemView.childCount > 0) {
                itemView.removeAllViews()
            }
            if (adView.parent != null) {
                (adView.parent as ViewGroup).removeView(adView)
            }
            itemView.addView(adView)
        }

    }

    companion object {

        // Usual item
        protected val itemViewType = 0

        // Ad
        protected val adViewType = 1

    }

}