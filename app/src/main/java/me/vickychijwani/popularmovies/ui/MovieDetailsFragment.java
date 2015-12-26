package me.vickychijwani.popularmovies.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.vickychijwani.popularmovies.BuildConfig;
import me.vickychijwani.popularmovies.R;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.entity.Review;
import me.vickychijwani.popularmovies.entity.Video;
import me.vickychijwani.popularmovies.event.events.MovieLoadedEvent;
import me.vickychijwani.popularmovies.util.DeviceUtil;
import me.vickychijwani.popularmovies.util.TMDbUtil;

public class MovieDetailsFragment extends BaseFragment {

    private static final String TAG = "MovieDetailsFragment";

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
    @Bind(R.id.trailers)            ViewGroup mTrailersView;
    @Bind(R.id.reviews_header)      TextView mReviewsHeader;
    @Bind(R.id.reviews)             ViewGroup mReviewsView;

    private Movie mMovie;

    public MovieDetailsFragment() {}

    public static MovieDetailsFragment newInstance(Movie movie) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(BundleKeys.MOVIE, Movie.toParcelable(movie));
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

        Picasso picasso = Picasso.with(getActivity());

        int backdropWidth = DeviceUtil.getScreenWidth(getActivity());
        int backdropHeight = getResources().getDimensionPixelSize(R.dimen.details_backdrop_height);
        picasso.load(TMDbUtil.buildBackdropUrl(mMovie.getBackdropPath(), backdropWidth))
                .resize(backdropWidth, backdropHeight)
                .centerCrop()
                .transform(PaletteTransformation.instance())
                .into(mBackdrop, new ActivityPaletteTransformation(mBackdrop));

        int posterWidth = getResources().getDimensionPixelSize(R.dimen.details_poster_width);
        int posterHeight = getResources().getDimensionPixelSize(R.dimen.details_poster_height);
        picasso.load(TMDbUtil.buildPosterUrl(mMovie.getPosterPath(), posterWidth))
                .resize(posterWidth, posterHeight)
                .centerCrop()
                .into(mPoster);

        mTitle.setText(mMovie.getTitle());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mMovie.getReleaseDate());
        mReleaseDate.setText(String.valueOf(calendar.get(Calendar.YEAR)));

        mRating.setText(String.format("%1$2.1f", mMovie.getRating()));
        mSynopsis.setText(mMovie.getSynopsis());

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
                animateContent();
                return true;
            }
        });

        return view;
    }

    @Subscribe
    public void onMovieLoadedEvent(MovieLoadedEvent event) {
        mMovie = event.movie;
        List<Review> reviews = mMovie.getReviews();
        List<Video> trailers = Movie.getTrailers(mMovie);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, String.format("%1$s: fav=%2$s reviews=%3$d trailers=%4$d",
                    mMovie.getTitle(), String.valueOf(mMovie.isFavorite()),
                    reviews.size(), trailers.size()));
        }
        addTrailers(trailers);
        addReviews(reviews);
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
            thumbView.setOnClickListener((View.OnClickListener) getActivity());
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
            reviewContainer.setOnClickListener((View.OnClickListener) getActivity());
            reviewContainer.setTag(review);
            mReviewsView.addView(reviewContainer);
        }
    }

    private void animateContent() {
        View[] animatedViews = new View[] {
                mTitle, mReleaseDate, mRatingContainer, mSynopsis,
                mTrailersHeader, mTrailersView, mReviewsHeader, mReviewsView
        };
        Interpolator interpolator = new DecelerateInterpolator();
        for (int i = 0; i < animatedViews.length; ++i) {
            View v = animatedViews[i];
            v.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            v.setAlpha(0f);
            v.setTranslationY(75);
            v.animate()
                    .setInterpolator(interpolator)
                    .alpha(1.0f)
                    .translationY(0)
                    .setStartDelay(100 + 75 * i)
                    .start();
        }
    }

    class ActivityPaletteTransformation extends PaletteTransformation.Callback {
        public ActivityPaletteTransformation(@NonNull ImageView imageView) {
            super(imageView);
        }

        @Override
        protected void onSuccess(Palette palette) {
            Activity activity = getActivity();
            if (activity instanceof PaletteCallback) {
                PaletteCallback callback = (PaletteCallback) activity;
                callback.setPrimaryColor(palette);
            }
        }

        @Override
        public void onError() {}
    }

    public interface PaletteCallback {
        void setPrimaryColor(Palette palette);
    }

}
