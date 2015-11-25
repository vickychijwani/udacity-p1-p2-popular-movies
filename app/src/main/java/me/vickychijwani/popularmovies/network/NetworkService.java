package me.vickychijwani.popularmovies.network;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.vickychijwani.popularmovies.BuildConfig;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.entity.MovieResults;
import me.vickychijwani.popularmovies.event.DataBusProvider;
import me.vickychijwani.popularmovies.event.events.ApiErrorEvent;
import me.vickychijwani.popularmovies.event.events.CancelAllEvent;
import me.vickychijwani.popularmovies.event.events.LoadMostPopularMoviesEvent;
import me.vickychijwani.popularmovies.event.events.MostPopularMoviesLoadedEvent;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class NetworkService {

    private static final String TAG = "NetworkService";
    public static final String BASE_URL = "http://api.themoviedb.org/3/";

    private MovieDBApiService mApiService;
    private String mApiKey;
    private List<Call> mPendingCalls = new ArrayList<>();

    public NetworkService() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        mApiService = retrofit.create(MovieDBApiService.class);
        mApiKey = BuildConfig.THE_MOVIE_DB_API_KEY;
    }

    public void start() {
        getDataBus().register(this);
    }

    public void stop() {
        getDataBus().unregister(this);
    }

    @Subscribe
    public void onLoadMostPopularMoviesEvent(final LoadMostPopularMoviesEvent event) {
        enqueue(mApiService.fetchMostPopularMovies(mApiKey), new ApiCallback<MovieResults>() {
            @Override
            public void onApiResponse(Response<MovieResults> response, Retrofit retrofit) {
                if (response.body() != null) {
                    List<Movie> movies = response.body().getResults();
                    getDataBus().post(new MostPopularMoviesLoadedEvent(movies));
                } else if (response.errorBody() != null) {
                    try { Log.e(TAG, response.errorBody().string()); } catch (IOException ignored) {}
                } else {
                    Log.e(TAG, "response.body() and response.errorBody() are both null!");
                }
            }

            @Override
            public void onApiFailure(Throwable throwable) {
                getDataBus().post(new ApiErrorEvent(event, throwable.getMessage()));
            }
        });
    }

    @Subscribe
    public void onCancelAllEvent(CancelAllEvent event) {
        for (Call call : mPendingCalls) {
            call.cancel();
        }
    }

    private <T> void enqueue(Call<T> call, ApiCallback<T> apiCallback) {
        mPendingCalls.add(call);
        apiCallback.setCall(call);
        call.enqueue(apiCallback);
    }

    private abstract class ApiCallback<T> implements Callback<T> {
        private Call<T> mCall;

        public void setCall(Call<T> call) {
            mCall = call;
        }

        @Override
        public void onResponse(Response<T> response, Retrofit retrofit) {
            if (mCall != null) {
                mPendingCalls.remove(mCall);
            }
            onApiResponse(response, retrofit);
        }

        @Override
        public void onFailure(Throwable throwable) {
            if (mCall != null) {
                mPendingCalls.remove(mCall);
            }
            Log.e(TAG, "API error: " + throwable.getMessage());
            Log.e(TAG, Log.getStackTraceString(throwable));
            onApiFailure(throwable);
        }

        public abstract void onApiResponse(Response<T> response, Retrofit retrofit);
        public abstract void onApiFailure(Throwable throwable);
    }

    private Bus getDataBus() {
        return DataBusProvider.getBus();
    }

}
