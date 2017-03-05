package at.shockbytes.util;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.TypedValue;

import org.joda.time.LocalDate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Martin Macheiner
 *         Date: 27.10.2015.
 */
public class ResourceManager {

    private static SimpleDateFormat SDF_DATE = new SimpleDateFormat("dd.MM.", Locale.getDefault());
    private static SimpleDateFormat SDF_DATE_W_YEAR = new SimpleDateFormat("MMM yy", Locale.getDefault());

    /**
     * Loads the profile image icon from system
     *
     * @param context Application context
     * @return The URI of the own profile image set in the contacts app
     */
    @Nullable
    public static Uri loadProfileImage(Context context) {

        //Cursor für Profildaten laden
        Cursor c = context.getContentResolver().query(
                ContactsContract.Profile.CONTENT_URI, null, null, null, null);

        if (c == null) {
            return null;
        }

        //Index von Foto-URI laden
        int index_photo_uri = c
                .getColumnIndex(ContactsContract.Profile.PHOTO_URI);

        //Über Cursor iterieren
        if (c.moveToNext()) {
            String uri_string = c.getString(index_photo_uri); //URI-String laden

            if (uri_string != null) {
                c.close();
                return Uri.parse(uri_string); //String in URI parsen
            }
        }

        c.close();
        return null; //default return null
    }

    /**
     * Loads the profile name from system
     *
     * @param context Application context
     * @return The name of the own profile set in the contacts app
     */
    public static String loadProfileName(Context context) {

        //Cursor für Profildaten laden
        Cursor c = context.getContentResolver().query(
                ContactsContract.Profile.CONTENT_URI, null, null, null, null);

        if (c == null) {
            return "";
        }

        //Index von Profilnamen laden
        int index_name = c
                .getColumnIndex(ContactsContract.Profile.DISPLAY_NAME);

        //Über Cursor iterieren
        if (c.moveToNext()) {

            String name = c.getString(index_name);
            c.close();

            return name; //Namen zurückgeben
        }

        c.close();
        return ""; //default return
    }

    /**
     * Returns the first two letters of the current day of week
     *
     * @param date The current date as String
     * @return First two letters of current day of week
     */
    @NonNull
    public static String getDayOfWeek(String date) {

        try {
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd",
                    Locale.getDefault());
            Date dt1 = format1.parse(date);

            SimpleDateFormat format2 = new SimpleDateFormat("EE",
                    Locale.getDefault());
            return format2.format(dt1).toUpperCase(Locale.getDefault());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static int getDayOfWeek() {
        return LocalDate.now().getDayOfWeek() - 1;
    }

    public static String formatDate(long date, boolean yearFormat) {
        return yearFormat ? SDF_DATE_W_YEAR.format(new Date(date)) : SDF_DATE.format(new Date(date));
    }

    public static int convertDpInPixel(int dp, Context context) {
        Resources res = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }

    private static Bitmap getBitmap(VectorDrawableCompat vectorDrawable, int padding) {

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(padding, padding, canvas.getWidth() - padding, canvas.getHeight() - padding);
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    private static Bitmap getBitmap(Context context, int drawableId) {

        Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return BitmapFactory.decodeResource(context.getResources(), drawableId);
        } else if (drawable instanceof VectorDrawableCompat) {
            int px = convertDpInPixel(24, context);
            return getBitmap((VectorDrawableCompat) drawable, px);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    public static RoundedBitmapDrawable createRoundedBitmapFromResource(Context context,
                                                                        @DrawableRes int resId,
                                                                        @ColorRes int bgRes) {

        Bitmap original = getBitmap(context, resId);
        Bitmap image = Bitmap.createBitmap(original.getWidth(), original.getHeight(),
                Bitmap.Config.ARGB_8888);
        image.eraseColor(ContextCompat.getColor(context, bgRes));

        Canvas c = new Canvas(image);
        c.drawBitmap(original, 0, 0, null);

        RoundedBitmapDrawable rdb = RoundedBitmapDrawableFactory.create(context.getResources(), image);
        rdb.setCircular(true);
        return rdb;
    }

    @Nullable
    public static RoundedBitmapDrawable createRoundedBitmap(Context context, Uri uri) {

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            return createRoundedBitmap(context, bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static RoundedBitmapDrawable createRoundedBitmap(Context context, Bitmap bitmap) {
        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        dr.setCornerRadius(Math.max(bitmap.getWidth(), bitmap.getHeight()) / 2.0f);
        dr.setAntiAlias(true);
        dr.setDither(false);
        return dr;
    }

    /**
     * Saves generic content to a given file
     *
     * @param context  Application context
     * @param obj      Serializable object to store in file
     * @param filename Storage destination, file name
     * @throws IOException If file provides corrupt stream or unauthorized access
     */
    public static <T extends Serializable> void save(Context context, T obj,
                                                     String filename) throws IOException {

        //Null pointer will be handled as an empty list
        if (obj == null || filename == null) {
            return;
        }

        //Create outputstreams
        FileOutputStream fos = context.openFileOutput(filename,
                Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        //Write object
        oos.writeObject(obj);
        //Close streams
        oos.close();
        fos.close();
    }

    public static double roundDoubleWithDigits(double value, int digits) {

        if (digits < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(digits, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
