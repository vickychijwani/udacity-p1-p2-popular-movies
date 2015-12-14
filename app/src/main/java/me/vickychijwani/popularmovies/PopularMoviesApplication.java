package me.vickychijwani.popularmovies;

import android.app.Application;
import android.util.Log;

import com.squareup.otto.Subscribe;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import me.vickychijwani.popularmovies.event.DataBusProvider;
import me.vickychijwani.popularmovies.event.events.ApiErrorEvent;
import me.vickychijwani.popularmovies.model.Database;
import me.vickychijwani.popularmovies.network.NetworkService;

public class PopularMoviesApplication extends Application {

    private static final int DB_SCHEMA_VERSION = 1;
    private static final String TAG = "Application";

    @SuppressWarnings("FieldCanBeLocal")
    private NetworkService mNetworkService;

    @SuppressWarnings("FieldCanBeLocal")
    private Database mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the database
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(DB_SCHEMA_VERSION)
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
        mDatabase = new Database();

        // start the NetworkService so it can listen for and handle data request events
        mNetworkService = new NetworkService(mDatabase);
        mNetworkService.start();

        DataBusProvider.getBus().register(this);
    }

}
