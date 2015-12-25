package me.vickychijwani.popularmovies.util;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;

public class AppUtil {

    private static final String TAG = "AppUtil";

    public static int multiplyColor(int srcColor, float factor) {
        int alpha = Color.alpha(srcColor);
        int red = (int) (Color.red(srcColor) * factor);
        int green = (int) (Color.green(srcColor) * factor);
        int blue = (int) (Color.blue(srcColor) * factor);
        return Color.argb(alpha, red, green, blue);
    }

    public static void tintDrawable(@Nullable Drawable drawable, int color) {
        if (drawable != null) {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    /**
     * Tries to copy an object using a copy constructor.
     */
    public static <T> T copy(T obj, Class<T> clazz) {
        try {
            return clazz.getConstructor(clazz).newInstance(obj);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return null;
        } catch (IllegalAccessException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return null;
        } catch (InvocationTargetException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return null;
        } catch (InstantiationException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            return null;
        }
    }

}
