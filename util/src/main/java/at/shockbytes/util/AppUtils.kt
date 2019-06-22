package at.shockbytes.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresPermission
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.appcompat.widget.AppCompatDrawableManager
import androidx.appcompat.widget.Toolbar
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.DatePicker
import java.io.IOException
import java.io.ObjectOutputStream
import java.io.Serializable
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

/**
 * Author:  Martin Macheiner
 * Date:    15.06.2017
 */
object AppUtils {

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    fun getProfileImage(context: Context): Uri? {

        val c = context.contentResolver.query(
                ContactsContract.Profile.CONTENT_URI, null,
                null, null, null) ?: return null

        val idxImgUri = c.getColumnIndex(ContactsContract.Profile.PHOTO_URI)
        val imgUri = if (c.moveToNext()) {
            val uriStr = c.getString(idxImgUri)
            if (uriStr != null) Uri.parse(uriStr) else null
        } else {
            null
        }
        c.close()
        return imgUri
    }

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    fun getProfileName(context: Context): String {

        val c = context.contentResolver.query(ContactsContract.Profile.CONTENT_URI, null,
                null, null, null) ?: return ""

        val idxName = c.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME)
        val name = if (c.moveToNext()) { c.getString(idxName) } else { "" }
        c.close()
        return name
    }

    fun convertDpInPixel(dp: Int, context: Context): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
                context.resources.displayMetrics).toInt()
    }

    private fun getBitmap(vectorDrawable: VectorDrawableCompat, padding: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(padding, padding, canvas.width - padding, canvas.height - padding)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    private fun getBitmap(vectorDrawable: VectorDrawable, padding: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(padding, padding, canvas.width - padding, canvas.height - padding)
        vectorDrawable.draw(canvas)
        return bitmap
    }

    @SuppressLint("RestrictedApi")
    private fun getBitmap(context: Context, drawableId: Int): Bitmap {

        val drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId)

        // Handle special case if drawable is vector drawable, which is only supported in API level 21
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && (drawable is VectorDrawable)) {
            getBitmap(drawable, convertDpInPixel(24, context))
        } else {
            when (drawable) {
                is BitmapDrawable -> BitmapFactory.decodeResource(context.resources, drawableId)
                is VectorDrawableCompat -> getBitmap(drawable, convertDpInPixel(24, context))
                else -> throw IllegalArgumentException("Unsupported drawable type")
            }
        }
    }

    fun createRoundedBitmapFromResource(
        context: Context,
        @DrawableRes resId: Int,
        @ColorRes bgRes: Int
    ): RoundedBitmapDrawable {

        val original = getBitmap(context, resId)
        val image = Bitmap.createBitmap(original.width, original.height,
                Bitmap.Config.ARGB_8888)
        image.eraseColor(ContextCompat.getColor(context, bgRes))

        val c = Canvas(image)
        c.drawBitmap(original, 0f, 0f, null)

        val rdb = RoundedBitmapDrawableFactory.create(context.resources, image)
        rdb.isCircular = true
        return rdb
    }

    fun createRoundedBitmap(context: Context, uri: Uri): RoundedBitmapDrawable? {

        return try {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            createRoundedBitmap(context, bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun createStringBitmap(width: Int, color: Int, text: String): Bitmap {

        val config = Bitmap.Config.ARGB_8888
        val bmp = Bitmap.createBitmap(width, width, config)

        // Text paint settings
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isSubpixelText = true
        paint.textSize = 20f
        paint.isDither = true
        paint.color = Color.WHITE
        paint.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = (width / 2).toFloat()

        val canvas = Canvas(bmp)
        canvas.drawColor(color)

        canvas.drawText(text, (width / 2).toFloat(),
                width / 2 - (paint.descent() + paint.ascent()) / 2, paint)

        return bmp
    }

    fun createRoundedBitmap(context: Context, bitmap: Bitmap): RoundedBitmapDrawable {
        val dr = RoundedBitmapDrawableFactory.create(context.resources, bitmap)
        dr.cornerRadius = Math.max(bitmap.width, bitmap.height) / 2.0f
        dr.setAntiAlias(true)
        return dr
    }

    fun roundDouble(value: Double, digits: Int): Double {

        if (value == 0.0 ||
                digits < 0 ||
                value == Double.POSITIVE_INFINITY ||
                value == Double.NaN ||
                value == Double.NEGATIVE_INFINITY
        ) {
            return 0.00
        }

        return BigDecimal(value).setScale(digits, RoundingMode.HALF_UP).toDouble()
    }

    fun getNavigationIcon(toolbar: Toolbar): View? {
        // check if contentDescription previously was set
        val hadContentDescription = TextUtils.isEmpty(toolbar.navigationContentDescription)
        val contentDescription = if (!hadContentDescription) toolbar.navigationContentDescription?.toString() else " navigationIcon "
        toolbar.navigationContentDescription = contentDescription
        val potentialViews = ArrayList<View>()
        // find the view based on it's content description, set programmatically or with android:contentDescription
        toolbar.findViewsWithText(potentialViews, contentDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
        // Nav icon is always instantiated at this point because calling setNavigationContentDescription ensures its existence
        var navIcon: View? = null
        if (potentialViews.size > 0) {
            navIcon = potentialViews[0] // navigation icon is ImageButton
        }
        // Clear content description if not previously present
        if (hadContentDescription)
            toolbar.navigationContentDescription = null
        return navIcon
    }

    fun formatDateOfYear(millis: Long): String {
        return SimpleDateFormat("dd. MMM yyyy", Locale.getDefault()).format(Date(millis))
    }

    fun formatDateOfYear(millis: Long, pattern: String): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
    }

    fun getDateFromDatePicker(datePicker: DatePicker): Long {
        val day = datePicker.dayOfMonth
        val month = datePicker.month
        val year = datePicker.year
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        return calendar.timeInMillis
    }

    /**
     * Saves generic content to a given file
     *
     * @param context Application context
     * @param obj Serializable object to store in file
     * @param filename Storage destination, file name
     * @throws IOException If file provides corrupt stream or unauthorized access
     */
    @Throws(IOException::class)
    fun <T : Serializable> save(context: Context, obj: T, filename: String) {

        val fos = context.openFileOutput(filename, Context.MODE_PRIVATE)
        val oos = ObjectOutputStream(fos)
        oos.writeObject(obj)
        oos.close()
        fos.close()
    }
}
