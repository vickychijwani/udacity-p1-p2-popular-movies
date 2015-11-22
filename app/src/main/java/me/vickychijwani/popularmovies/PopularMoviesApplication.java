package me.vickychijwani.popularmovies;

import android.app.Application;

import me.vickychijwani.popularmovies.network.NetworkService;

public class PopularMoviesApplication extends Application {

    @SuppressWarnings("FieldCanBeLocal")
    private NetworkService mNetworkService;

    @Override
    public void onCreate() {
        super.onCreate();
        mNetworkService = new NetworkService();
        // start the NetworkService so it can listen for and handle data request events
        mNetworkService.start();
    }

}
