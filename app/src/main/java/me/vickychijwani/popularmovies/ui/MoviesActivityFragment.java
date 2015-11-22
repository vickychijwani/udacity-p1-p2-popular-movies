package me.vickychijwani.popularmovies.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.vickychijwani.popularmovies.R;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.event.events.ApiErrorEvent;
import me.vickychijwani.popularmovies.event.events.CancelAllEvent;
import me.vickychijwani.popularmovies.event.events.LoadMostPopularMoviesEvent;
import me.vickychijwani.popularmovies.event.events.MostPopularMoviesLoadedEvent;
import me.vickychijwani.popularmovies.util.Util;
public class MoviesActivityFragment extends BaseFragment {

    // this is a good ratio for TMDb posters
    private static final double TMDB_POSTER_SIZE_RATIO = 185.0 / 277.0;
    private static final int DESIRED_GRID_COLUMN_WIDTH_DP = 300;

    @Bind(R.id.movies_list)             RecyclerView mMoviesListView;

    private RecyclerView.Adapter mMoviesAdapter;
    private List<Movie> mMovies = new ArrayList<>();

    public MoviesActivityFragment() {}

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

        mMoviesAdapter = new MoviesAdapter(activity, mMovies, actualPosterViewWidth);
        mMoviesListView.setAdapter(mMoviesAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDataBus().post(new LoadMostPopularMoviesEvent());
    }

    @Override
    public void onStop() {
        // cancel all pending and in-flight requests, if any, to conserve resources
        getDataBus().post(new CancelAllEvent());
        super.onStop();
    }

    @Subscribe
    public void onMostPopularMoviesLoadedEvent(MostPopularMoviesLoadedEvent event) {
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

    private static final class MoviesAdapter extends RecyclerView.Adapter<MovieViewHolder> {

        private final List<Movie> mMovies;
        private final LayoutInflater mInflater;
        private final Picasso mPicasso;
        private final int mPosterWidth;
        private final int mPosterHeight;

        public MoviesAdapter(Context context, List<Movie> movies, int posterWidth) {
            mMovies = movies;
            mInflater = LayoutInflater.from(context);
            mPicasso = Picasso.with(context);
            mPosterWidth = posterWidth;
            mPosterHeight = (int) (posterWidth / TMDB_POSTER_SIZE_RATIO);
        }

        @Override
        public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.movie_list_item, parent, false);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            lp.width = mPosterWidth;
            lp.height = mPosterHeight;
            view.setLayoutParams(lp);
            MovieViewHolder viewHolder = new MovieViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MovieViewHolder holder, int position) {
            Movie movie = mMovies.get(position);
            mPicasso
                    .load(Util.buildPosterUrl(movie.getPosterPath(), mPosterWidth))
                    .resize(mPosterWidth, mPosterHeight)
                    .centerCrop()
                    .into(holder.mPoster);
        }

        @Override
        public int getItemCount() {
            return mMovies.size();
        }

    }

    static final class MovieViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.movie_poster)            ImageView mPoster;

        public MovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}
