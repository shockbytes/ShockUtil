package at.shockbytes.util.view

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.Interpolator
import android.widget.Scroller

/**
 * Author:  Martin Macheiner
 * Date:    19.03.2015
 */
class NonSwipeableViewPager : ViewPager {

    private var mScroller: NonSwipeableScroller? = null

    var duration: Int = 1000
        set(value) {
            if (value <= 0) {
                throw IllegalArgumentException("Duration $duration cannot be < 0!")
            }
            field = value
        }

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize()
    }

    private fun initialize() {

        try {
            val scroller = androidx.viewpager.widget.ViewPager::class.java.getDeclaredField("mScroller")
            scroller.isAccessible = true
            val interpolator = androidx.viewpager.widget.ViewPager::class.java.getDeclaredField("sInterpolator")
            interpolator.isAccessible = true

            mScroller = NonSwipeableScroller(context, interpolator.get(null) as Interpolator)
            scroller.set(this, mScroller)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun makeFancyPageTransformation() {
        setPageTransformer(true, DepthPageTransformer())
    }

    override fun onInterceptHoverEvent(event: MotionEvent) = false

    override fun onTouchEvent(ev: MotionEvent) = false

    private inner class NonSwipeableScroller : Scroller {

        constructor(context: Context) : super(context) {}

        constructor(context: Context, interpolator: Interpolator) : super(context, interpolator) {}

        constructor(context: Context, interpolator: Interpolator, flywheel: Boolean) : super(context, interpolator, flywheel) {}

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, this@NonSwipeableViewPager.duration)
        }
    }
}
