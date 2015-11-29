package me.vickychijwani.popularmovies.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.animation.AccelerateInterpolator;

import me.vickychijwani.popularmovies.R;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.util.Util;

public class MovieDetailsActivity extends AppCompatActivity implements
        MovieDetailsFragment.PaletteCallback {

    private Toolbar mToolbar;
    private Drawable mUpArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUpArrow = getResources().getDrawable(R.drawable.arrow_left);
        if (mUpArrow != null) {
            int upArrowColor = getResources().getColor(android.R.color.white);
            mUpArrow.setColorFilter(upArrowColor, PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(mUpArrow);
        }

        Movie movie = getIntent().getExtras().getParcelable(BundleKeys.MOVIE);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(movie.getTitle());

        if (savedInstanceState == null) {
            Fragment detailsFragment = MovieDetailsFragment.newInstance(movie);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, detailsFragment,
                            MovieDetailsFragment.class.getSimpleName())
                    .commit();
        }

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public void setPrimaryColor(Palette palette) {
        Palette.Swatch vibrant = palette.getVibrantSwatch();

        if (vibrant == null) {
            return;
        }

        int newPrimaryColor = vibrant.getRgb();
        final int newTitleTextColor = vibrant.getTitleTextColor();
        int newPrimaryDarkColor = Util.multiplyColor(newPrimaryColor, 0.8f);

        int currentPrimaryColor = getResources().getColor(R.color.colorPrimary);
        startColorAnimation(currentPrimaryColor, newPrimaryColor, new ColorUpdateListener() {
            @Override
            public void onColorUpdate(int color) {
                mToolbar.setBackgroundColor(color);
            }
        });

        int currentTitleTextColor = getResources().getColor(android.R.color.white);
        startColorAnimation(currentTitleTextColor, newTitleTextColor, new ColorUpdateListener() {
            @Override
            public void onColorUpdate(int color) {
                mToolbar.setTitleTextColor(newTitleTextColor);
                if (mUpArrow != null) {
                    mUpArrow.setColorFilter(newTitleTextColor, PorterDuff.Mode.SRC_IN);
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusBarColor = getWindow().getStatusBarColor();
            startColorAnimation(statusBarColor, newPrimaryDarkColor, new ColorUpdateListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onColorUpdate(int color) {
                    getWindow().setStatusBarColor(color);
                }
            });
        }
    }

    private void startColorAnimation(int fromColor, int toColor,
                                            final ColorUpdateListener listener) {
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

}
