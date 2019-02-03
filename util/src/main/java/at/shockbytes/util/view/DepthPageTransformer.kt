package at.shockbytes.util.view

import android.support.v4.view.ViewPager
import android.view.View

/**
 * Author:  Martin Macheiner
 * Date:    03.12.2015
 */
class DepthPageTransformer : ViewPager.PageTransformer {

    private var minScale = 0.75f
        set(value) {
            if (value < 0f) {
                throw IllegalArgumentException("Minimum scale $value cannot be < 0!")
            }
            field = value
        }

    override fun transformPage(page: View, position: Float) {

        val pageWidth = page.width

        when {

            position < -1 -> page.alpha = 0f
            position <= 0 -> {
                page.alpha = 1f
                page.translationX = 0f
                page.scaleX = 1f
                page.scaleY = 1f
            }
            position <= 1 -> {
                page.alpha = 1 - position
                page.translationX = pageWidth * -position
                val scaleFactor = minScale + (1 - minScale) * (1 - Math.abs(position))
                page.scaleY = scaleFactor
                page.scaleX = scaleFactor
            }
            else -> page.alpha = 0f
        }
    }

    companion object {

        fun withScale(scale: Float): DepthPageTransformer = DepthPageTransformer().apply {
            this.minScale = scale
        }
    }
}
