package me.vickychijwani.popularmovies.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.squareup.otto.Bus;

import me.vickychijwani.popularmovies.BuildConfig;
import me.vickychijwani.popularmovies.event.DataBusProvider;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String LIFECYCLE = "Lifecycle";

    protected Bus getDataBus() {
        return DataBusProvider.getBus();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logLifecycleMethod();
    }

    @Override
    public void onStart() {
        super.onStart();
        logLifecycleMethod();
        getDataBus().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        logLifecycleMethod();
    }

    @Override
    protected void onPause() {
        super.onPause();
        logLifecycleMethod();
    }

    @Override
    public void onStop() {
        super.onStop();
        logLifecycleMethod();
        getDataBus().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logLifecycleMethod();
    }

    public void logLifecycleMethod() {
        if (BuildConfig.DEBUG) {
            Log.i(LIFECYCLE, getMethodName(this));
        }
    }

    public static String getMethodName(BaseActivity instance) {
        return instance.getClass().getSimpleName() + "#" +
                Thread.currentThread().getStackTrace()[4].getMethodName();
    }

}
