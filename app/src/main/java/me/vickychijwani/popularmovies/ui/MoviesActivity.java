package me.vickychijwani.popularmovies.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.squareup.otto.Subscribe;

import me.vickychijwani.popularmovies.R;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.event.events.MoviesLoadedEvent;

public class MoviesActivity extends BaseActivity implements
        MoviesAdapter.ClickListener,
        MovieDetailsFragment.PaletteCallback {

    private MoviesFragment mMoviesFragment = null;
    private MovieDetailsFragment mDetailsFragment = null;
    private boolean mTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        mMoviesFragment = (MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.movies_fragment);
        if (findViewById(R.id.details_fragment_container) != null) {
            mTwoPane = true;
        } else {
            // reset colors if the orientation was just changed
            int primaryColor = ContextCompat.getColor(this, R.color.colorPrimary);
            int primaryDarkColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
            int titleTextColor = ContextCompat.getColor(this, android.R.color.white);
            mMoviesFragment.setPalette(primaryColor, primaryDarkColor, titleTextColor, false);
        }
    }

    @Subscribe
    public void onMoviesLoadedEvent(MoviesLoadedEvent event) {
        if (mTwoPane && event.movies != null && ! event.movies.isEmpty()) {
            Movie firstMovie = event.movies.get(0);
            if (mDetailsFragment == null) {
                mDetailsFragment = MovieDetailsFragment.newInstance(firstMovie, true);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.details_fragment_container, mDetailsFragment,
                                MovieDetailsFragment.class.getSimpleName())
                        .commit();
            } else {
                mDetailsFragment.setMovie(firstMovie);
            }
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
        } else if (mDetailsFragment != null) {
            mDetailsFragment.setMovie(movie);
        }
    }

    @Override
    public void setPalette(int primaryColor, int primaryDarkColor, int titleTextColor) {
        mMoviesFragment.setPalette(primaryColor, primaryDarkColor, titleTextColor, true);
    }

}
