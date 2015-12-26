package me.vickychijwani.popularmovies.ui;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.vickychijwani.popularmovies.R;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.entity.Review;
import me.vickychijwani.popularmovies.event.events.LoadMovieEvent;
import me.vickychijwani.popularmovies.event.events.MovieLoadedEvent;
import me.vickychijwani.popularmovies.event.events.UpdateMovieEvent;
import me.vickychijwani.popularmovies.util.AppUtil;

public class MovieDetailsActivity extends BaseActivity implements
        MovieDetailsFragment.PaletteCallback, View.OnClickListener {

    private static final String TAG = "MovieDetailsActivity";

    @Bind(R.id.toolbar)                     Toolbar mToolbar;
    @Bind(R.id.favorite)                    FloatingActionButton mFavoriteBtn;

    @BindDrawable(R.drawable.arrow_left)    Drawable mUpArrow;
    @BindDrawable(R.drawable.star_outline)  Drawable mStarOutline;
    @BindDrawable(R.drawable.star)          Drawable mStarFilled;

    private Movie mMovie;
    @ColorInt private int mPrimaryColor = -1;
    @ColorInt private int mPrimaryDarkColor = -1;
    @ColorInt private int mTitleTextColor = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // credits for up arrow color tinting: http://stackoverflow.com/a/26837072/504611
        int upArrowColor = getResources().getColor(android.R.color.white);
        AppUtil.tintDrawable(mUpArrow, upArrowColor);
        getSupportActionBar().setHomeAsUpIndicator(mUpArrow);

        mMovie = Movie.fromParcelable(getIntent().getExtras().getParcelable(BundleKeys.MOVIE));
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(mMovie.getTitle());

        if (savedInstanceState == null) {
            Fragment detailsFragment = MovieDetailsFragment.newInstance(mMovie);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, detailsFragment,
                            MovieDetailsFragment.class.getSimpleName())
                    .commit();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getDataBus().post(new LoadMovieEvent(mMovie.getId()));
    }

    @OnClick(R.id.favorite)
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.favorite && mMovie != null) {
            Movie movieCopy = AppUtil.copy(mMovie, Movie.class);
            if (movieCopy != null) {
                movieCopy.setFavorite(!movieCopy.isFavorite());
                getDataBus().post(new UpdateMovieEvent(movieCopy));
            }
        } else if (v.getId() == R.id.video_thumb) {
            String videoUrl = (String) v.getTag();
            Intent playVideoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            startActivity(playVideoIntent);
        } else if (v.getId() == R.id.review) {
            Review review = (Review) v.getTag();
            Intent reviewIntent = new Intent(this, ReviewActivity.class);
            reviewIntent.putExtra(BundleKeys.REVIEW, Review.toParcelable(review));
            boolean validColors = (mPrimaryColor != -1 && mPrimaryDarkColor != -1
                    && mTitleTextColor != -1);
            if (validColors) {
                reviewIntent.putExtra(BundleKeys.COLOR_PRIMARY, mPrimaryColor);
                reviewIntent.putExtra(BundleKeys.COLOR_PRIMARY_DARK, mPrimaryDarkColor);
                reviewIntent.putExtra(BundleKeys.COLOR_TEXT_TITLE, mTitleTextColor);
            }
            ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(),
                    v.getHeight());
            startActivity(reviewIntent, opts.toBundle());
        }
    }

    @Subscribe
    public void onMovieLoadedEvent(MovieLoadedEvent event) {
        mMovie = event.movie;
        updateFavoriteBtn();
    }

    private void updateFavoriteBtn() {
        mFavoriteBtn.setImageDrawable(mMovie.isFavorite() ? mStarFilled : mStarOutline);
    }

    @Override
    public void setPrimaryColor(Palette palette) {
        Palette.Swatch vibrant = palette.getVibrantSwatch();

        if (vibrant == null) {
            return;
        }

        mPrimaryColor = vibrant.getRgb();
        mTitleTextColor = vibrant.getTitleTextColor();
        mPrimaryDarkColor = AppUtil.multiplyColor(mPrimaryColor, 0.8f);

        int currentPrimaryColor = getResources().getColor(R.color.colorPrimary);
        startColorAnimation(currentPrimaryColor, mPrimaryColor, new ColorUpdateListener() {
            @Override
            public void onColorUpdate(int color) {
                mToolbar.setBackgroundColor(color);
            }
        });

        int currentTitleTextColor = getResources().getColor(android.R.color.white);
        startColorAnimation(currentTitleTextColor, mTitleTextColor, new ColorUpdateListener() {
            @Override
            public void onColorUpdate(int color) {
                mToolbar.setTitleTextColor(mTitleTextColor);
                AppUtil.tintDrawable(mUpArrow, mTitleTextColor);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int statusBarColor = getWindow().getStatusBarColor();
            startColorAnimation(statusBarColor, mPrimaryDarkColor, new ColorUpdateListener() {
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

}
