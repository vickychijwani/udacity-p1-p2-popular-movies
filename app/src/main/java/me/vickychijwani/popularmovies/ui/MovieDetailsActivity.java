package me.vickychijwani.popularmovies.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import me.vickychijwani.popularmovies.R;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.event.events.MovieLoadedEvent;

public class MovieDetailsActivity extends BaseActivity {

    private static final String TAG = "MovieDetailsActivity";

    private Movie mMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        mMovie = Movie.fromParcelable(getIntent().getExtras().getParcelable(BundleKeys.MOVIE));

        if (savedInstanceState == null) {
            Fragment detailsFragment = MovieDetailsFragment.newInstance(mMovie, false);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, detailsFragment,
                            MovieDetailsFragment.class.getSimpleName())
                    .commit();
        }
    }

    @Subscribe
    public void onMovieLoadedEvent(MovieLoadedEvent event) {
        mMovie = event.movie;
    }

}
