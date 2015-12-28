package me.vickychijwani.popularmovies.ui;

import android.animation.Animator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.vickychijwani.popularmovies.BuildConfig;
import me.vickychijwani.popularmovies.R;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.entity.Review;
import me.vickychijwani.popularmovies.entity.Video;
import me.vickychijwani.popularmovies.event.events.LoadMovieEvent;
import me.vickychijwani.popularmovies.event.events.MovieLoadedEvent;
import me.vickychijwani.popularmovies.event.events.UpdateMovieEvent;
import me.vickychijwani.popularmovies.util.AppUtil;
import me.vickychijwani.popularmovies.util.TMDbUtil;

public class MovieDetailsFragment extends BaseFragment implements
        View.OnClickListener,
        Toolbar.OnMenuItemClickListener {

    private static final String TAG = "MovieDetailsFragment";


    @Bind(R.id.toolbar)             Toolbar mToolbar;
    @Bind(R.id.scroll_view)         ScrollView mScrollView;
    @Bind(R.id.scroll_view_layout)  ViewGroup mScrollViewLayout;
    @Bind(R.id.backdrop)            ImageView mBackdrop;
    @Bind(R.id.poster)              ImageView mPoster;
    @Bind(R.id.title)               TextView mTitle;
    @Bind(R.id.release_date)        TextView mReleaseDate;
    @Bind(R.id.rating)              TextView mRating;
    @Bind(R.id.rating_container)    ViewGroup mRatingContainer;
    @Bind(R.id.synopsis)            TextView mSynopsis;
    @Bind(R.id.trailers_header)     TextView mTrailersHeader;
    @Bind(R.id.trailers_container)  HorizontalScrollView mTrailersScrollView;
    @Bind(R.id.trailers)            ViewGroup mTrailersView;
    @Bind(R.id.reviews_header)      TextView mReviewsHeader;
    @Bind(R.id.reviews)             ViewGroup mReviewsView;
    @Bind(R.id.favorite)            FloatingActionButton mFavoriteBtn;

    @BindDrawable(R.drawable.star_outline)  Drawable mStarOutline;
    @BindDrawable(R.drawable.star)          Drawable mStarFilled;

    @ColorInt private int mPrimaryColor = -1;
    @ColorInt private int mPrimaryDarkColor = -1;
    @ColorInt private int mTitleTextColor = -1;

    private Movie mMovie;
    private List<View> mEnterAnimationViews;
    private List<View> mExitAnimationViews;

    public MovieDetailsFragment() {}

    public static MovieDetailsFragment newInstance(Movie movie, boolean twoPane) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(BundleKeys.MOVIE, Movie.toParcelable(movie));
        args.putBoolean(BundleKeys.TWO_PANE, twoPane);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_movie_details, container, false);
        ButterKnife.bind(this, view);

        mMovie = Movie.fromParcelable(getArguments().getParcelable(BundleKeys.MOVIE));
        if (mMovie == null) {
            throw new IllegalStateException("No movie given!");
        }

        AppUtil.ToolbarNavIcon navIcon = getArguments().getBoolean(BundleKeys.TWO_PANE)
                ? AppUtil.ToolbarNavIcon.NONE
                : AppUtil.ToolbarNavIcon.UP;
        AppUtil.setupToolbar(getActivity(), mToolbar, navIcon, mMovie.getTitle());
        mToolbar.inflateMenu(R.menu.menu_movie_details);
        mToolbar.setOnMenuItemClickListener(this);

        mEnterAnimationViews = Arrays.asList(
                mTitle, mReleaseDate, mRatingContainer, mSynopsis,
                mTrailersHeader, mTrailersView, mReviewsHeader, mReviewsView);
        mExitAnimationViews = new ArrayList<>();
        mExitAnimationViews.add(mBackdrop);
        mExitAnimationViews.add(mPoster);
        mExitAnimationViews.addAll(mEnterAnimationViews);

        // credits for onPreDraw technique: http://frogermcs.github.io/Instagram-with-Material-Design-concept-part-2-Comments-transition/
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                if (mScrollViewLayout.getHeight() < mScrollView.getHeight()) {
                    ViewGroup.LayoutParams lp = mScrollViewLayout.getLayoutParams();
                    lp.height = mScrollView.getHeight();
                    mScrollViewLayout.setLayoutParams(lp);
                }
                updateMovieDetails();
                startEnterAnimation(100);
                return true;
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDataBus().post(new LoadMovieEvent(mMovie.getId()));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share_trailer:
                Video firstTrailer = Movie.getTrailers(mMovie).get(0);
                String subject = mMovie.getTitle() + " - " + firstTrailer.getName();
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    //noinspection deprecation
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                } else {
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                }
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, Video.getUrl(firstTrailer));
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_trailer)));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            Intent reviewIntent = new Intent(getActivity(), ReviewActivity.class);
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
            getActivity().startActivity(reviewIntent, opts.toBundle());
        }
    }

    private void updateMovieDetails() {
        Picasso picasso = Picasso.with(getActivity());

        mToolbar.setTitle(mMovie.getTitle());

        mBackdrop.setAlpha(0f); // wait for enter animation
        mBackdrop.setTranslationY(0);
        int backdropWidth = mBackdrop.getWidth();   // this will be correct because this function is
                                                    // only called after layout is complete
        int backdropHeight = getResources().getDimensionPixelSize(R.dimen.details_backdrop_height);
        picasso.load(TMDbUtil.buildBackdropUrl(mMovie.getBackdropPath(), backdropWidth))
                .resize(backdropWidth, backdropHeight)
                .centerCrop()
                .transform(PaletteTransformation.instance())
                .noFade()
                .into(mBackdrop, new PaletteTransformationCallback(mBackdrop));

        mPoster.setAlpha(0f); // wait for enter animation
        mPoster.setTranslationY(0);
        int posterWidth = getResources().getDimensionPixelSize(R.dimen.details_poster_width);
        int posterHeight = getResources().getDimensionPixelSize(R.dimen.details_poster_height);
        picasso.load(TMDbUtil.buildPosterUrl(mMovie.getPosterPath(), posterWidth))
                .resize(posterWidth, posterHeight)
                .centerCrop()
                .noFade()
                .into(mPoster);

        mTitle.setText(mMovie.getTitle());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mMovie.getReleaseDate());
        mReleaseDate.setText(String.valueOf(calendar.get(Calendar.YEAR)));

        mRating.setText(String.format("%1$2.1f", mMovie.getRating()));
        mSynopsis.setText(mMovie.getSynopsis());
    }

    @Subscribe
    public void onMovieLoadedEvent(MovieLoadedEvent event) {
        mMovie = event.movie;

        updateFavoriteBtn();
        List<Review> reviews = mMovie.getReviews();
        List<Video> trailers = Movie.getTrailers(mMovie);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("%1$s: fav=%2$s reviews=%3$d trailers=%4$d",
                    mMovie.getTitle(), String.valueOf(mMovie.isFavorite()),
                    reviews.size(), trailers.size()));
        }

        boolean hasTrailers = !trailers.isEmpty();
        mToolbar.getMenu().findItem(R.id.share_trailer).setVisible(hasTrailers);
        mTrailersHeader.setVisibility(hasTrailers ? View.VISIBLE : View.GONE);
        mTrailersScrollView.setVisibility(hasTrailers ? View.VISIBLE : View.GONE);
        if (hasTrailers) {
            addTrailers(trailers);
        }

        boolean hasReviews = !reviews.isEmpty();
        mReviewsHeader.setVisibility(hasReviews ? View.VISIBLE : View.GONE);
        mReviewsView.setVisibility(hasReviews ? View.VISIBLE : View.GONE);
        if (hasReviews) {
            addReviews(reviews);
        }
    }

    public void setMovie(final Movie movie) {
        if (movie.getId() == mMovie.getId()) {
            return;
        }
        startExitAnimation(new Runnable() {
            @Override
            public void run() {
                mMovie = movie;
                getDataBus().post(new LoadMovieEvent(mMovie.getId()));
                updateMovieDetails();
                startEnterAnimation(0);
            }
        });
    }

    private void updateFavoriteBtn() {
        mFavoriteBtn.setImageDrawable(mMovie.isFavorite() ? mStarFilled : mStarOutline);
        if (mFavoriteBtn.getScaleX() == 0) {
            // credits for onPreDraw technique: http://frogermcs.github.io/Instagram-with-Material-Design-concept-part-2-Comments-transition/
            mFavoriteBtn.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mFavoriteBtn.getViewTreeObserver().removeOnPreDrawListener(this);
                    mFavoriteBtn.animate()
                            .withLayer()
                            .setInterpolator(new DecelerateInterpolator())
                            .scaleX(1f)
                            .scaleY(1f)
                            .setStartDelay(100)
                            .start();
                    return true;
                }
            });
        }
    }

    private void addTrailers(List<Video> trailers) {
        mTrailersView.removeAllViews();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        Picasso picasso = Picasso.with(getActivity());
        for (Video trailer : trailers) {
            ViewGroup thumbContainer = (ViewGroup) inflater.inflate(R.layout.video, mTrailersView,
                    false);
            ImageView thumbView = (ImageView) thumbContainer.findViewById(R.id.video_thumb);
            thumbView.setTag(Video.getUrl(trailer));
            thumbView.setOnClickListener(this);
            picasso
                    .load(Video.getThumbnailUrl(trailer))
                    .resizeDimen(R.dimen.video_width, R.dimen.video_height)
                    .centerCrop()
                    .into(thumbView);
            mTrailersView.addView(thumbContainer);
        }
    }

    private void addReviews(List<Review> reviews) {
        mReviewsView.removeAllViews();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        for (Review review : reviews) {
            ViewGroup reviewContainer = (ViewGroup) inflater.inflate(R.layout.review, mReviewsView,
                    false);
            TextView reviewAuthor = (TextView) reviewContainer.findViewById(R.id.review_author);
            TextView reviewContent = (TextView) reviewContainer.findViewById(R.id.review_content);
            reviewAuthor.setText(review.getAuthor());
            reviewContent.setText(review.getContent().replace("\n\n", " ").replace("\n", " "));
            reviewContainer.setOnClickListener(this);
            reviewContainer.setTag(review);
            mReviewsView.addView(reviewContainer);
        }
    }

    private void startEnterAnimation(int startDelay) {
        Interpolator interpolator = new DecelerateInterpolator();
        mBackdrop.setAlpha(0f);
        mBackdrop.animate().setInterpolator(interpolator).alpha(1f).start();
        mPoster.setAlpha(0f);
        mPoster.animate().setInterpolator(interpolator).alpha(1f).start();
        for (int i = 0; i < mEnterAnimationViews.size(); ++i) {
            final View v = mEnterAnimationViews.get(i);
            v.setAlpha(0f);
            v.setTranslationY(75);
            v.animate()
                    .withLayer()
                    .setInterpolator(interpolator)
                    .alpha(1.0f)
                    .translationY(0)
                    .setStartDelay(startDelay + 75 * i)
                    .setListener(null)      // http://stackoverflow.com/a/22934588/504611
                    .start();
        }
    }

    private void startExitAnimation(final Runnable onAnimationNearlyEnded) {
        Interpolator interpolator = new AccelerateInterpolator();
        final View viewForAnimationNearlyEnded = mExitAnimationViews.get(5);
        for (int i = 0; i < mExitAnimationViews.size(); ++i) {
            final View v = mExitAnimationViews.get(i);
            v.setAlpha(1f);
            v.setTranslationY(0);
            ViewPropertyAnimator animator = v.animate();
            if (v == viewForAnimationNearlyEnded) {
                animator.setListener(new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onAnimationNearlyEnded.run();
                    }
                });
            }
            animator
                    .withLayer()
                    .setInterpolator(interpolator)
                    .alpha(0.0f)
                    .translationY(-75)
                    .setStartDelay(75 * i)
                    .start();
        }
        mFavoriteBtn.animate()
                .withLayer()
                .setInterpolator(interpolator)
                .scaleX(0f)
                .scaleY(0f)
                .start();
    }

    class PaletteTransformationCallback extends PaletteTransformation.Callback {
        public PaletteTransformationCallback(@NonNull ImageView imageView) {
            super(imageView);
        }

        @Override
        protected void onSuccess(Palette palette) {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            if (palette != null && palette.getVibrantSwatch() != null) {
                Palette.Swatch vibrant = palette.getVibrantSwatch();
                mPrimaryColor = vibrant.getRgb();
                mPrimaryDarkColor = AppUtil.multiplyColor(mPrimaryColor, 0.8f);
                mTitleTextColor = vibrant.getTitleTextColor();
            } else {
                mPrimaryColor = ContextCompat.getColor(activity, R.color.colorPrimary);
                mPrimaryDarkColor = ContextCompat.getColor(activity, R.color.colorPrimaryDark);
                mTitleTextColor = ContextCompat.getColor(activity, android.R.color.white);
            }
            AppUtil.setColorTheme(activity, mToolbar, mPrimaryColor, mPrimaryDarkColor,
                    mTitleTextColor, true);

            if (activity instanceof PaletteCallback) {
                PaletteCallback callback = (PaletteCallback) activity;
                callback.setPalette(mPrimaryColor, mPrimaryDarkColor, mTitleTextColor);
            }
        }

        @Override
        public void onError() {}
    }

    private static abstract class AnimatorEndListener implements Animator.AnimatorListener {
        @Override public void onAnimationStart(Animator animation) {}
        @Override public abstract void onAnimationEnd(Animator animation);
        @Override public void onAnimationCancel(Animator animation) {}
        @Override public void onAnimationRepeat(Animator animation) {}
    }

    public interface PaletteCallback {
        void setPalette(int primaryColor, int primaryDarkColor, int titleTextColor);
    }

}
