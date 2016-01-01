package me.vickychijwani.popularmovies.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import me.vickychijwani.popularmovies.BuildConfig;
import me.vickychijwani.popularmovies.R;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.entity.MovieResults;
import me.vickychijwani.popularmovies.event.events.ApiErrorEvent;
import me.vickychijwani.popularmovies.event.events.LoadMoviesEvent;
import me.vickychijwani.popularmovies.event.events.MovieUpdatedEvent;
import me.vickychijwani.popularmovies.event.events.MoviesLoadedEvent;
import me.vickychijwani.popularmovies.util.AppUtil;

public class MoviesFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener, Toolbar.OnMenuItemClickListener {

    private static final String TAG = "MoviesFragment";

    private static final String KEY_MOVIES = "movies";
    private static final String KEY_SORT_ORDER = MovieResults.SortCriteria.class.getSimpleName();

    public static final MovieResults.SortCriteria DEFAULT_SORT_CRITERIA =
            MovieResults.SortCriteria.POPULARITY;

    @Bind(R.id.toolbar)                 Toolbar mToolbar;
    @Bind(R.id.movies_list)             RecyclerView mMoviesListView;
    @Bind(R.id.swipe_refresh_layout)    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindColor(android.R.color.white)           int mCurrentTitleTextColor;
    @BindDimen(R.dimen.desired_column_width)    int desiredColumnWidth;

    private MoviesAdapter mMoviesAdapter;
    private ArrayList<Movie> mMovies = new ArrayList<>();
    private MovieResults.SortCriteria mCurrentSortCriteria = DEFAULT_SORT_CRITERIA;
    private boolean mDetailsUpdatePending = false;

    public MoviesFragment() {}

    @SuppressWarnings("unused")
    public static MoviesFragment newInstance() {
        return new MoviesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_movies, container, false);
        ButterKnife.bind(this, view);

        final Activity activity = getActivity();
        String title = getString(R.string.app_name);
        AppUtil.setupToolbar(activity, mToolbar, AppUtil.ToolbarNavIcon.NONE, title);
        mToolbar.inflateMenu(R.menu.menu_movies);
        mToolbar.setOnMenuItemClickListener(this);

        mMoviesListView.setHasFixedSize(true);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // compute optimal number of columns based on available width
                int gridWidth = mMoviesListView.getWidth();
                int optimalColumnCount = Math.max(Math.round((1f*gridWidth) / desiredColumnWidth), 1);
                int actualPosterViewWidth = gridWidth / optimalColumnCount;

                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(activity, optimalColumnCount);
                mMoviesListView.setLayoutManager(layoutManager);

                mMoviesAdapter = new MoviesAdapter(activity, mMovies, actualPosterViewWidth,
                        (MoviesAdapter.ClickListener) activity);
                mMoviesListView.setAdapter(mMoviesAdapter);

                if (savedInstanceState != null) {
                    String enumName = savedInstanceState.getString(KEY_SORT_ORDER);
                    mCurrentSortCriteria = MovieResults.SortCriteria.valueOf(enumName);
                    List<Parcelable> parcelables = savedInstanceState.getParcelableArrayList(KEY_MOVIES);
                    if (parcelables != null) {
                        showMovies(Movie.fromParcelable(parcelables));
                    }
                }
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMovies.isEmpty() || mCurrentSortCriteria == MovieResults.SortCriteria.FAVORITES) {
            onRefresh();
        }
    }

    @Override
    public void onStop() {
        stopRefreshing();
        super.onStop();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
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
    public void onMoviesLoadedEvent(MoviesLoadedEvent event) {
        mCurrentSortCriteria = event.sortCriteria;
        showMovies(event.movies);
        if (mDetailsUpdatePending && mCurrentSortCriteria == MovieResults.SortCriteria.FAVORITES) {
            Movie firstMovie = mMovies.isEmpty() ? null : mMovies.get(0);
            ((MoviesActivity) getActivity()).showMovieDetails(firstMovie);
            mDetailsUpdatePending = false;
        }
    }

    @Subscribe
    public void onMovieUpdatedEvent(MovieUpdatedEvent event) {
        // if a movie was updated and we're currently in the favorites list, that means
        // the two-pane layout is visible and the favorites list itself needs to be updated
        // also the current movie was un-favorited, so tell the activity to update the details pane
        if (mCurrentSortCriteria == MovieResults.SortCriteria.FAVORITES) {
            getDataBus().post(new LoadMoviesEvent(mCurrentSortCriteria));
            mDetailsUpdatePending = true;
        }
    }

    private void showMovies(@NonNull List<Movie> movies) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Loaded " + movies.size() + " movies:");
            for (Movie movie : movies) {
                Log.v(TAG, movie.getTitle() + " (poster: " + movie.getPosterPath() + ")");
            }
        }
        mMovies.clear();
        mMovies.addAll(movies);
        mMoviesAdapter.notifyDataSetChanged();
        stopRefreshing();
    }

    public void setPalette(int primaryColor, int primaryDarkColor, int titleTextColor,
                           boolean animate) {
        AppUtil.setColorTheme(getActivity(), mToolbar, primaryColor, primaryDarkColor,
                titleTextColor, mCurrentTitleTextColor, animate);
        mCurrentTitleTextColor = titleTextColor;
    }

    @Subscribe
    public void onApiErrorEvent(ApiErrorEvent event) {
        if (event.sourceEvent instanceof LoadMoviesEvent && getView() != null) {
            Snackbar.make(getView(), R.string.api_error, Snackbar.LENGTH_LONG).show();
        }
    }

}
