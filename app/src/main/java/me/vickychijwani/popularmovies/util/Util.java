package me.vickychijwani.popularmovies.util;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import me.vickychijwani.popularmovies.BuildConfig;

public class Util {

    private static final Uri TMDB_IMAGE_BASE_URI = Uri.parse("http://image.tmdb.org/t/p/");

    private enum TMDbPosterWidth {
        W92(92), W154(154), W185(185), W342(342), W500(500), W780(780), ORIGINAL(Integer.MAX_VALUE);

        public final int maxWidth;
        TMDbPosterWidth(int maxWidth) {
            this.maxWidth = maxWidth;
        }
        public String getWidthString() {
            return (this == ORIGINAL) ? "original" : "w" + this.maxWidth;
        }
    }

    public static String buildPosterUrl(String posterPath, int posterWidth) {
        TMDbPosterWidth tmdbPosterWidth = computeNextLowestPosterWidth(posterWidth);
        if (BuildConfig.DEBUG) {
            Log.d("Picasso", "Loading poster of size " + tmdbPosterWidth.maxWidth + "x"
                    + (tmdbPosterWidth.maxWidth * 277.0 / 185.0));
        }
        String relativePath = tmdbPosterWidth.getWidthString() + "/" + posterPath;
        return Uri.withAppendedPath(TMDB_IMAGE_BASE_URI, relativePath).toString();
    }

    // 50 => W92, 92 => W92, 93 => W185, 999 => ORIGINAL
    private static TMDbPosterWidth computeNextLowestPosterWidth(int posterWidth) {
        for (TMDbPosterWidth enumWidth : TMDbPosterWidth.values()) {
            if (0.8 * posterWidth <= enumWidth.maxWidth) {
                return enumWidth;
            }
        }
        return TMDbPosterWidth.ORIGINAL;
    }

    public static int getScreenWidth(@NonNull Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

}
