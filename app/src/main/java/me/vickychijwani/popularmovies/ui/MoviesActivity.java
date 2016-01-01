package me.vickychijwani.popularmovies.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.squareup.otto.Subscribe;

import me.vickychijwani.popularmovies.R;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.entity.MovieResults;
import me.vickychijwani.popularmovies.event.events.MoviesLoadedEvent;

public class MoviesActivity extends BaseActivity implements
        MoviesAdapter.ClickListener,
        MovieDetailsFragment.PaletteCallback {

    private static final String KEY_SELECTED_MOVIE = "selected_movie";
    private static final String KEY_SORT_ORDER = "sort_order";

    private MoviesFragment mMoviesFragment = null;

    // only used in two-pane layout
    private MovieDetailsFragment mDetailsFragment = null;
    private boolean mTwoPane = false;
    private Movie mSelectedMovie = null;
    private MovieResults.SortCriteria mSelectedSortCriteria = MoviesFragment.DEFAULT_SORT_CRITERIA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        mMoviesFragment = (MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.movies_fragment);
        if (findViewById(R.id.details_fragment_container) != null) {
            mTwoPane = true;
            if (savedInstanceState != null) {
                String enumName = savedInstanceState.getString(KEY_SORT_ORDER);
                mSelectedSortCriteria = MovieResults.SortCriteria.valueOf(enumName);
                Parcelable movieParcelable = savedInstanceState.getParcelable(KEY_SELECTED_MOVIE);
                Movie selectedMovie = Movie.fromParcelable(movieParcelable);
                showMovieDetails(selectedMovie);
            }
        } else {
            // reset colors if the orientation was just changed
            int primaryColor = ContextCompat.getColor(this, R.color.colorPrimary);
            int primaryDarkColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
            int titleTextColor = ContextCompat.getColor(this, android.R.color.white);
            mMoviesFragment.setPalette(primaryColor, primaryDarkColor, titleTextColor, false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SELECTED_MOVIE, Movie.toParcelable(mSelectedMovie));
        outState.putString(KEY_SORT_ORDER, mSelectedSortCriteria.name());
    }

    @Subscribe
    public void onMoviesLoadedEvent(MoviesLoadedEvent event) {
        if (mTwoPane && event.movies != null && ! event.movies.isEmpty()) {
            if (mSelectedSortCriteria == event.sortCriteria && mSelectedMovie != null) {
                return;
            }
            Movie movie = event.movies.get(0);
            mSelectedSortCriteria = event.sortCriteria;
            showMovieDetails(movie);
        }
    }

    @Override
    public void onMovieClick(View movieView, Movie movie) {
        if (! mTwoPane) {
            Intent intent = new Intent(this, MovieDetailsActivity.class);
            intent.putExtra(BundleKeys.MOVIE, Movie.toParcelable(movie));
            ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(movieView, 0, 0,
                    movieView.getWidth(), movieView.getHeight());
            startActivity(intent, opts.toBundle());
        } else {
            showMovieDetails(movie);
        }
    }

    private void showMovieDetails(@Nullable Movie movie) {
        if (! mTwoPane) {
            throw new UnsupportedOperationException("This is only supposed to be called in a " +
                    "2-pane layout!");
        }
        if (movie == null) {
            return;
        }
        mSelectedMovie = movie;
        if (mDetailsFragment == null) {
            mDetailsFragment = MovieDetailsFragment.newInstance(movie, true);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.details_fragment_container, mDetailsFragment,
                            MovieDetailsFragment.class.getSimpleName())
                    .commit();
        } else {
            mDetailsFragment.setMovie(movie);
        }
    }

    @Override
    public void setPalette(int primaryColor, int primaryDarkColor, int titleTextColor) {
        mMoviesFragment.setPalette(primaryColor, primaryDarkColor, titleTextColor, true);
    }

}
