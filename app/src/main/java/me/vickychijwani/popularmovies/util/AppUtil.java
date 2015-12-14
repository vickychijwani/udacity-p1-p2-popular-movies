package me.vickychijwani.popularmovies.util;

import android.graphics.Color;

import org.parceler.Parcels;

public class AppUtil {

    public static int multiplyColor(int srcColor, float factor) {
        int alpha = Color.alpha(srcColor);
        int red = (int) (Color.red(srcColor) * factor);
        int green = (int) (Color.green(srcColor) * factor);
        int blue = (int) (Color.blue(srcColor) * factor);
        return Color.argb(alpha, red, green, blue);
    }

    public static <T> T makeCopyByParcelling(T obj, Class<T> clazz) {
        // make a copy by serializing and deserializing the object
        return Parcels.unwrap(Parcels.wrap(clazz, obj));
    }

}
