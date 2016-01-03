package me.vickychijwani.popularmovies.model;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.realm.RealmList;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.entity.MovieResults;
import me.vickychijwani.popularmovies.entity.Review;
import me.vickychijwani.popularmovies.entity.Video;
import me.vickychijwani.popularmovies.event.DataBusProvider;
import me.vickychijwani.popularmovies.event.events.ApiErrorEvent;
import me.vickychijwani.popularmovies.event.events.CancelAllEvent;
import me.vickychijwani.popularmovies.event.events.LoadMovieEvent;
import me.vickychijwani.popularmovies.event.events.LoadMoviesEvent;
import me.vickychijwani.popularmovies.event.events.MovieLoadedEvent;
import me.vickychijwani.popularmovies.event.events.MovieUpdatedEvent;
import me.vickychijwani.popularmovies.event.events.MoviesLoadedEvent;
import me.vickychijwani.popularmovies.event.events.UpdateMovieEvent;
import me.vickychijwani.popularmovies.util.AppUtil;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class Model {

    private static final String TAG = "Model";
    private static final String BASE_URL = "http://api.themoviedb.org/3/";

    private final Database mDatabase;
    private final MovieDBApiService mApiService;
    private final String mApiKey;
    private final Map<String, Call> mPendingCalls = new ConcurrentHashMap<>();

    public Model() {
        mDatabase = new Database();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .registerTypeAdapter(new TypeToken<RealmList<Video>>() {}.getType(), new VideoRealmListDeserializer())
                .registerTypeAdapter(new TypeToken<RealmList<Review>>() {}.getType(), new ReviewRealmListDeserializer())
                .setExclusionStrategies(new RealmExclusionStrategy())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        mApiService = retrofit.create(MovieDBApiService.class);
        mApiKey = "075c3ac2845f0a71e38797ec6f57cdfb";
        getDataBus().register(this);
    }

    @Subscribe
    public void onLoadMoviesEvent(final LoadMoviesEvent event) {
        if (event.sortCriteria == MovieResults.SortCriteria.FAVORITES) {
            mDatabase.loadFavoriteMovies(new Database.ReadCallback<List<Movie>>() {
                @Override
                public void done(List<Movie> favorites) {
                    getDataBus().post(new MoviesLoadedEvent(favorites, event.sortCriteria));
                }
            });
            return;
        }
        Call<MovieResults> call = mApiService.fetchMovies(mApiKey, event.sortCriteria.str);
        enqueue(event.getClass().getSimpleName(), call, new ApiCallback<MovieResults>() {
            @Override
            public void onApiResponse(MovieResults movieResults, Retrofit retrofit) {
                updateMoviesInDb(movieResults.getResults(), event.sortCriteria);
            }

            @Override
            public void onApiFailure(Throwable throwable) {
                getDataBus().post(new ApiErrorEvent(event, throwable));
            }
        });
    }

    private void updateMoviesInDb(final List<Movie> movies, final MovieResults.SortCriteria sortCriteria) {
        mDatabase.loadFavoriteMovies(new Database.ReadCallback<List<Movie>>() {
            @Override
            public void done(List<Movie> favorites) {
                for (Movie m : movies)
                    for (Movie f : favorites)
                        if (m.getId() == f.getId())
                            m.setFavorite(true);
                mDatabase.createOrUpdateEntity(movies, new Database.WriteCallback() {
                    @Override
                    public void done() {
                        getDataBus().post(new MoviesLoadedEvent(movies, sortCriteria));
                    }
                });
            }
        });
    }

    @Subscribe
    public void onLoadMovieEvent(final LoadMovieEvent event) {
        mDatabase.loadMovie(event.id, new Database.ReadCallback<Movie>() {
            @Override
            public void done(final Movie localMovie) {
                getDataBus().post(new MovieLoadedEvent(localMovie));
                if (localMovie.getReviews().isEmpty() && localMovie.getVideos().isEmpty()) {
                    fetchMovie(localMovie, event);
                }
            }
        });
    }

    private void fetchMovie(final Movie localMovie, final LoadMovieEvent event) {
        Call<Movie> call = mApiService.fetchMovie(event.id, mApiKey);
        enqueue(event.getClass().getSimpleName(), call, new ApiCallback<Movie>() {
            @Override
            public void onApiResponse(Movie fetchedMovie, Retrofit retrofit) {
                fetchedMovie.setFavorite(localMovie.isFavorite());
                mDatabase.createOrUpdateEntity(fetchedMovie, new Database.WriteCallback() {
                    @Override
                    public void done() {
                        readMovieFromDb(event.id);
                    }
                });
            }

            @Override
            public void onApiFailure(Throwable throwable) {
                getDataBus().post(new ApiErrorEvent(event, throwable));
            }
        });
    }

    private void readMovieFromDb(int movieId) {
        mDatabase.loadMovie(movieId, new Database.ReadCallback<Movie>() {
            @Override
            public void done(Movie movie) {
                getDataBus().post(new MovieLoadedEvent(movie));
            }
        });
    }

    @Subscribe
    public void onUpdateMovieEvent(final UpdateMovieEvent event) {
        final Movie movieCopy = AppUtil.copy(event.movie, Movie.class);
        if (movieCopy != null) {
            mDatabase.createOrUpdateEntity(movieCopy, new Database.WriteCallback() {
                @Override
                public void done() {
                    // send over a copy of the updated movie
                    getDataBus().post(new MovieLoadedEvent(movieCopy));
                    getDataBus().post(new MovieUpdatedEvent(movieCopy));
                }
            });
        }
    }

    @Subscribe
    public void onCancelAllEvent(CancelAllEvent event) {
        for (Call call : mPendingCalls.values()) {
            call.cancel();
        }
        mPendingCalls.clear();
    }

    private <T> void enqueue(String tag, Call<T> call, ApiCallback<T> apiCallback) {
        // cancel any outstanding request with the same tag
        Call pendingRequest = mPendingCalls.remove(tag);
        if (pendingRequest != null) {
            pendingRequest.cancel();
        }
        // send a new request
        mPendingCalls.put(tag, call);
        apiCallback.setCall(call);
        call.enqueue(apiCallback);
    }

    private void removePendingCall(Call call) {
        if (call == null) {
            return;
        }
        for (Map.Entry<String, Call> entry : mPendingCalls.entrySet()) {
            if (call == entry.getValue()) {
                mPendingCalls.remove(entry.getKey());
            }
        }
    }

    private abstract class ApiCallback<T> implements Callback<T> {
        private Call<T> mCall;

        public void setCall(Call<T> call) {
            mCall = call;
        }

        @Override
        public void onResponse(Response<T> response, Retrofit retrofit) {
            removePendingCall(mCall);
            if (response.body() != null) {
                onApiResponse(response.body(), retrofit);
            } else if (response.errorBody() != null) {
                try { Log.e(TAG, response.errorBody().string()); } catch (IOException ignored) {}
            } else {
                Log.e(TAG, "response.body() and response.errorBody() are both null!");
            }
        }

        @Override
        public void onFailure(Throwable throwable) {
            removePendingCall(mCall);
            Log.e(TAG, "API error: " + throwable.getMessage());
            Log.e(TAG, Log.getStackTraceString(throwable));
            onApiFailure(throwable);
        }

        public abstract void onApiResponse(T response, Retrofit retrofit);

        public void onApiFailure(Throwable throwable) {}
    }

    private Bus getDataBus() {
        return DataBusProvider.getBus();
    }

}
