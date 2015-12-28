package me.vickychijwani.popularmovies.util;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import java.lang.reflect.InvocationTargetException;

import me.vickychijwani.popularmovies.R;

public class AppUtil {

    private static final String TAG = "AppUtil";

    public enum ToolbarNavIcon {
        UP,
        NONE
    }

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

    private static void tintMenuItems(@Nullable Menu menu, @ColorInt int color) {
        if (menu == null) {
            return;
        }
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem != null && menuItem.getIcon() != null) {
                tintDrawable(menuItem.getIcon(), color);
            }
        }
    }

    public static void setupToolbar(final Activity activity, Toolbar toolbar, ToolbarNavIcon icon,
                                    String title) {
        if (icon == ToolbarNavIcon.UP) {
            Drawable upArrow = ContextCompat.getDrawable(activity, R.drawable.arrow_left);
            AppUtil.tintDrawable(upArrow, ContextCompat.getColor(activity, android.R.color.white));
            toolbar.setNavigationIcon(upArrow);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onBackPressed();
                }
            });
        }
        toolbar.setTitle(title);
    }

    public static void setColorTheme(final Activity activity, final Toolbar mToolbar,
                                     int primaryColor, int primaryDarkColor,
                                     final int titleTextColor, boolean animate) {
        int currentTitleTextColor = ContextCompat.getColor(activity, android.R.color.white);
        setColorTheme(activity, mToolbar, primaryColor, primaryDarkColor, titleTextColor,
                currentTitleTextColor, animate);
    }

    public static void setColorTheme(final Activity activity, final Toolbar mToolbar,
                                     int primaryColor, int primaryDarkColor,
                                     final int titleTextColor, int currentTitleTextColor,
                                     boolean animate) {
        if (animate) {
            int currentPrimaryColor;
            if (mToolbar.getBackground() instanceof ColorDrawable) {
                currentPrimaryColor = ((ColorDrawable) mToolbar.getBackground()).getColor();
            } else {
                currentPrimaryColor = ContextCompat.getColor(activity, R.color.colorPrimary);
            }
            startColorAnimation(currentPrimaryColor, primaryColor, new ColorUpdateListener() {
                @Override
                public void onColorUpdate(int color) {
                    mToolbar.setBackgroundColor(color);
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int statusBarColor = activity.getWindow().getStatusBarColor();
                startColorAnimation(statusBarColor, primaryDarkColor, new ColorUpdateListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onColorUpdate(int color) {
                        activity.getWindow().setStatusBarColor(color);
                    }
                });
            }

            startColorAnimation(currentTitleTextColor, titleTextColor, new ColorUpdateListener() {
                @Override
                public void onColorUpdate(int color) {
                    mToolbar.setTitleTextColor(titleTextColor);
                    tintDrawable(mToolbar.getNavigationIcon(), titleTextColor);
                    tintMenuItems(mToolbar.getMenu(), titleTextColor);
                }
            });
        } else {
            mToolbar.setBackgroundColor(primaryColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.getWindow().setStatusBarColor(primaryDarkColor);
            }
            mToolbar.setTitleTextColor(titleTextColor);
            tintDrawable(mToolbar.getNavigationIcon(), titleTextColor);
            tintMenuItems(mToolbar.getMenu(), titleTextColor);
        }
    }

    private static void startColorAnimation(int fromColor, int toColor,
                                     final ColorUpdateListener listener) {
        // credits: http://stackoverflow.com/a/14467625/504611
        ValueAnimator colorAnimation = ValueAnimator
                .ofObject(new ArgbEvaluator(), fromColor, toColor)
                .setDuration(500);
        colorAnimation.setInterpolator(new AccelerateInterpolator());
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                listener.onColorUpdate((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    private interface ColorUpdateListener {
        void onColorUpdate(int color);
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
