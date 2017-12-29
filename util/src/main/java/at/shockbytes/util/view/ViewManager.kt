package at.shockbytes.util.view

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.*
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.animation.Animation
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.view.animation.Transformation

object ViewManager {

    private val animationScaleTime = 1.5f

    fun createStringBitmap(width: Int, color: Int, text: String): Bitmap {

        val config = Bitmap.Config.ARGB_8888
        val bmp = Bitmap.createBitmap(width, width, config)

        //Text paint settings
        val tPt = Paint()
        tPt.isAntiAlias = true
        tPt.isSubpixelText = true
        tPt.color = Color.WHITE
        tPt.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        tPt.textAlign = Paint.Align.CENTER
        tPt.textSize = (width / 2).toFloat()

        val canvas = Canvas(bmp)
        canvas.drawColor(color)
        canvas.drawText(text, (width / 2).toFloat(),
                width / 2 - (tPt.descent() + tPt.ascent()) / 2, tPt)
        return bmp
    }

    fun animateEmptyField(v: View) {
        val anim = ObjectAnimator.ofFloat<View>(v, View.ROTATION, 0f, 8f, -8f)
        anim.repeatCount = 1
        anim.repeatMode = ValueAnimator.REVERSE
        anim.duration = 400
        anim.start()
    }

    @JvmOverloads
    fun expand(v: View, duration: Long = 300L, interpolator: Interpolator = LinearInterpolator()) {

        v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        val targetHeight = v.measuredHeight

        v.layoutParams.height = 0
        v.visibility = View.VISIBLE

        val a = object : Animation() {

            override fun applyTransformation(interpolatedTime: Float,
                                             t: Transformation) {
                v.layoutParams.height = if (interpolatedTime == 1f)
                    LayoutParams.WRAP_CONTENT
                else
                    (targetHeight * interpolatedTime).toInt()
                v.requestLayout()
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        a.interpolator = interpolator
        if (duration <= 0) {
            a.duration = (animationScaleTime * targetHeight
                    / v.context.resources.displayMetrics.density).toLong()
        } else {
            a.duration = duration
        }
        v.startAnimation(a)
    }

    @JvmOverloads
    fun collapse(v: View, duration: Long = 300L, interpolator: Interpolator = LinearInterpolator()) {

        val initialHeight = v.measuredHeight
        val a = object : Animation() {

            override fun applyTransformation(interpolatedTime: Float,
                                             t: Transformation) {

                if (interpolatedTime == 1f) {
                    v.visibility = View.GONE
                } else {
                    v.layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }
        a.interpolator = interpolator
        if (duration <= 0) {
            a.duration = (animationScaleTime * initialHeight
                    / v.context.resources.displayMetrics.density).toLong()
        } else {
            a.duration = duration
        }
        v.startAnimation(a)
    }

}
