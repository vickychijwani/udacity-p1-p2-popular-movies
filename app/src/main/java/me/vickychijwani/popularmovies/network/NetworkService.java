package me.vickychijwani.popularmovies.network;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import me.vickychijwani.popularmovies.event.events.MoviesLoadedEvent;
import me.vickychijwani.popularmovies.model.Database;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class NetworkService {

    private static final String TAG = "NetworkService";
    private static final String BASE_URL = "http://api.themoviedb.org/3/";

    private final Database mDatabase;
    private final MovieDBApiService mApiService;
    private final String mApiKey;
    private final Map<String, Call> mPendingCalls = new HashMap<>();

    public NetworkService(@NonNull Database database) {
        mDatabase = database;
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
        mApiKey = "YOUR_API_KEY_HERE";
    }

    public void start() {
        getDataBus().register(this);
    }

    public void stop() {
        getDataBus().unregister(this);
    }

    @Subscribe
    public void onLoadMoviesEvent(final LoadMoviesEvent event) {
        Call<MovieResults> call = mApiService.fetchMovies(mApiKey, event.sortCriteria.str);
        enqueue(event.getClass().getSimpleName(), call, new ApiCallback<MovieResults>() {
            @Override
            public void onApiResponse(MovieResults movieResults, Retrofit retrofit) {
                List<Movie> movies = movieResults.getResults();
                getDataBus().post(new MoviesLoadedEvent(movies));
            }

            @Override
            public void onApiFailure(Throwable throwable) {
                getDataBus().post(new ApiErrorEvent(event, throwable));
            }
        });
    }

    @Subscribe
    public void onLoadMovieEvent(final LoadMovieEvent event) {
        mDatabase.loadMovie(event.id, new Database.ReadCallback<Movie>() {
            @Override
            public void done(Movie movie) {
                getDataBus().post(new MovieLoadedEvent(movie));
            }

            @Override
            public void failed() {
                Call<Movie> call = mApiService.fetchMovie(event.id, mApiKey);
                enqueue(event.getClass().getSimpleName(), call, new ApiCallback<Movie>() {
                    @Override
                    public void onApiResponse(Movie movie, Retrofit retrofit) {
                        mDatabase.createOrUpdateEntity(movie, new Database.WriteCallback() {
                            @Override
                            public void done() {
                                onLoadMovieEvent(event);
                            }
                        });
                    }

                    @Override
                    public void onApiFailure(Throwable throwable) {
                        getDataBus().post(new ApiErrorEvent(event, throwable));
                    }
                });
            }
        });
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
        public abstract void onApiFailure(Throwable throwable);
    }

    private Bus getDataBus() {
        return DataBusProvider.getBus();
    }

}
