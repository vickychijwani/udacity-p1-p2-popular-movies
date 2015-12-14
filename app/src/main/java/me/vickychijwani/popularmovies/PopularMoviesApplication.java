package me.vickychijwani.popularmovies;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import me.vickychijwani.popularmovies.event.DataBusProvider;
import me.vickychijwani.popularmovies.model.Model;

public class PopularMoviesApplication extends Application {

    private static final int DB_SCHEMA_VERSION = 1;
    private static final String TAG = "Application";

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private Model mModel;

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the database
        RealmConfiguration.Builder realmConfigurationBuilder = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(DB_SCHEMA_VERSION);
        if (BuildConfig.DEBUG) {
            realmConfigurationBuilder.deleteRealmIfMigrationNeeded();
        }
        RealmConfiguration realmConfiguration = realmConfigurationBuilder.build();
        Realm.setDefaultConfiguration(realmConfiguration);

        // setup the Model so it can listen for and handle requests for data
        // hold a reference to it, to save it from GC
        mModel = new Model();

        DataBusProvider.getBus().register(this);
        enableStrictMode();
    }

    /**
     * Used to enable {@link android.os.StrictMode} during development
     */
    public static void enableStrictMode() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        // thread violations
        final StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder();
        threadPolicyBuilder.detectAll();
        threadPolicyBuilder.penaltyLog();
        threadPolicyBuilder.penaltyDialog();
        StrictMode.setThreadPolicy(threadPolicyBuilder.build());

        // activity leaks, unclosed resources, etc
        final StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder();
        vmPolicyBuilder.detectAll();
        vmPolicyBuilder.penaltyLog();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            vmPolicyBuilder.detectLeakedRegistrationObjects();
        }
        StrictMode.setVmPolicy(vmPolicyBuilder.build());
    }

}
