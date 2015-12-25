package me.vickychijwani.popularmovies.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
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
import me.vickychijwani.popularmovies.event.events.LoadMoviesEvent;
import me.vickychijwani.popularmovies.event.events.MoviesLoadedEvent;
import me.vickychijwani.popularmovies.util.DeviceUtil;

public class MoviesFragment extends BaseFragment implements
        MoviesAdapter.MovieViewHolder.ClickListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MoviesFragment";
    private static final int DESIRED_GRID_COLUMN_WIDTH_DP = 300;

    private static final String KEY_MOVIES = "movies";
    private static final String KEY_SORT_ORDER = MovieResults.SortCriteria.class.getSimpleName();

    @Bind(R.id.movies_list)             RecyclerView mMoviesListView;
    @Bind(R.id.swipe_refresh_layout)    SwipeRefreshLayout mSwipeRefreshLayout;

    private MoviesAdapter mMoviesAdapter;
    private ArrayList<Movie> mMovies = new ArrayList<>();
    private MovieResults.SortCriteria mCurrentSortCriteria = MovieResults.SortCriteria.POPULARITY;

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
        int screenWidth = DeviceUtil.getScreenWidth(activity);
        int optimalColumnCount = Math.round(screenWidth / DESIRED_GRID_COLUMN_WIDTH_DP);
        int actualPosterViewWidth = screenWidth / optimalColumnCount;

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(activity, optimalColumnCount);
        mMoviesListView.setLayoutManager(layoutManager);

        mMoviesAdapter = new MoviesAdapter(activity, mMovies, actualPosterViewWidth, this);
        mMoviesListView.setAdapter(mMoviesAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);

        if (savedInstanceState != null) {
            String enumName = savedInstanceState.getString(KEY_SORT_ORDER);
            mCurrentSortCriteria = MovieResults.SortCriteria.valueOf(enumName);
            List<Parcelable> parcelables = savedInstanceState.getParcelableArrayList(KEY_MOVIES);
            if (parcelables != null) {
                showMovies(Movie.fromParcelable(parcelables));
            }
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMovies.isEmpty()) {
            onRefresh();
        }
    }

    @Override
    public void onStop() {
        stopRefreshing();
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
                setSortCriteria(MovieResults.SortCriteria.POPULARITY);
                return true;
            case R.id.action_sort_rating:
                setSortCriteria(MovieResults.SortCriteria.RATING);
                return true;
            case R.id.action_filter_favorites:
                setSortCriteria(MovieResults.SortCriteria.FAVORITES);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SORT_ORDER, mCurrentSortCriteria.name());
        outState.putParcelableArrayList(KEY_MOVIES, Movie.toParcelable(mMovies));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        String enumName = savedInstanceState.getString(MovieResults.SortCriteria.class.getSimpleName());
        mCurrentSortCriteria = MovieResults.SortCriteria.valueOf(enumName);
    }

    public void setSortCriteria(MovieResults.SortCriteria criteria) {
        if (mCurrentSortCriteria != criteria) {
            mSwipeRefreshLayout.setRefreshing(true);
            getDataBus().post(new LoadMoviesEvent(criteria));
        }
    }

    @Override
    public void onRefresh() {
        getDataBus().post(new LoadMoviesEvent(mCurrentSortCriteria));
    }

    public void stopRefreshing() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Subscribe
    public void onMostPopularMoviesLoadedEvent(MoviesLoadedEvent event) {
        mCurrentSortCriteria = event.sortCriteria;
        showMovies(event.movies);
    }

    private void showMovies(@NonNull List<Movie> movies) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Loaded " + movies.size() + " movies:");
            for (Movie movie : movies) {
                Log.d(TAG, movie.getTitle() + " (poster: " + movie.getPosterPath() + ")");
            }
        }
        mMovies.clear();
        mMovies.addAll(movies);
        mMoviesAdapter.notifyDataSetChanged();
        stopRefreshing();
    }

    @Subscribe
    public void onApiErrorEvent(ApiErrorEvent event) {
        if (event.sourceEvent instanceof LoadMoviesEvent && getView() != null) {
            Snackbar.make(getView(), R.string.api_error, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPosterClick(View itemView) {
        int pos = mMoviesListView.getChildLayoutPosition(itemView);
        if (pos == RecyclerView.NO_POSITION) return;
        Movie movie = mMoviesAdapter.getItem(pos);
        Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
        intent.putExtra(BundleKeys.MOVIE, Movie.toParcelable(movie));
        ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(itemView, 0, 0,
                itemView.getWidth(), itemView.getHeight());
        getActivity().startActivity(intent, opts.toBundle());
    }

}
