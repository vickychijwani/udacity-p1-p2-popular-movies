package me.vickychijwani.popularmovies.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.vickychijwani.popularmovies.BuildConfig;
import me.vickychijwani.popularmovies.R;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.entity.MovieResults;
import me.vickychijwani.popularmovies.event.events.ApiErrorEvent;
import me.vickychijwani.popularmovies.event.events.CancelAllEvent;
import me.vickychijwani.popularmovies.event.events.LoadMoviesEvent;
import me.vickychijwani.popularmovies.event.events.MoviesLoadedEvent;
import me.vickychijwani.popularmovies.util.Util;

public class MoviesFragment extends BaseFragment implements
        MoviesAdapter.MovieViewHolder.ClickListener {

    public static final String TAG = "MoviesFragment";
    private static final int DESIRED_GRID_COLUMN_WIDTH_DP = 300;

    @Bind(R.id.movies_list)             RecyclerView mMoviesListView;

    private MoviesAdapter mMoviesAdapter;
    private List<Movie> mMovies = new ArrayList<>();

    public MoviesFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_movies, container, false);
        ButterKnife.bind(this, view);
        Activity activity = getActivity();

        mMoviesListView.setHasFixedSize(true);

        // compute optimal number of columns based on available width
        int screenWidth = Util.getScreenWidth(activity);
        int optimalColumnCount = Math.round(screenWidth / DESIRED_GRID_COLUMN_WIDTH_DP);
        int actualPosterViewWidth = screenWidth / optimalColumnCount;

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(activity, optimalColumnCount);
        mMoviesListView.setLayoutManager(layoutManager);

        mMoviesAdapter = new MoviesAdapter(activity, mMovies, actualPosterViewWidth, this);
        mMoviesListView.setAdapter(mMoviesAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDataBus().post(new LoadMoviesEvent(MovieResults.SortCriteria.POPULARITY));
    }

    @Override
    public void onStop() {
        // cancel all pending and in-flight requests, if any, to conserve resources
        getDataBus().post(new CancelAllEvent());
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_movies, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_popularity:
                getDataBus().post(new LoadMoviesEvent(MovieResults.SortCriteria.POPULARITY));
                return true;
            case R.id.action_sort_rating:
                getDataBus().post(new LoadMoviesEvent(MovieResults.SortCriteria.RATING));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Subscribe
    public void onMostPopularMoviesLoadedEvent(MoviesLoadedEvent event) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Loaded " + event.movies.size() + " movies:");
            for (Movie movie : event.movies) {
                Log.d(TAG, movie.getTitle() + " (poster: " + movie.getPosterPath() + ")");
            }
        }
        mMovies.clear();
        mMovies.addAll(event.movies);
        mMoviesAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onApiErrorEvent(ApiErrorEvent event) {
        if (getView() != null) {
            Snackbar.make(getView(), R.string.api_error, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPosterClick(View itemView) {
        int pos = mMoviesListView.getChildLayoutPosition(itemView);
        if (pos == RecyclerView.NO_POSITION) return;
        Movie movie = mMoviesAdapter.getItem(pos);
        Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
        intent.putExtra(BundleKeys.MOVIE, movie);
        startActivity(intent);
    }

}
