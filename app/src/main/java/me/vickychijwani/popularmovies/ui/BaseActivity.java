package me.vickychijwani.popularmovies.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.squareup.otto.Bus;

import io.realm.Realm;
import me.vickychijwani.popularmovies.BuildConfig;
import me.vickychijwani.popularmovies.event.DataBusProvider;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String LIFECYCLE = "Lifecycle";

    private Realm mRealmReference = null;

    protected Bus getDataBus() {
        return DataBusProvider.getBus();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logLifecycleMethod();
        // hold reference to the Realm to increase reference count to 1
        mRealmReference = Realm.getDefaultInstance();
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
        // Let go of Realm reference. If some other Activity has run, that will
        // hold it. If no Activities are running, then decrementing ref count to
        // 0 is a great idea in order to close the Realm correctly.
        mRealmReference.close();
        mRealmReference = null;
    }

    private void logLifecycleMethod() {
        if (BuildConfig.DEBUG) {
            Log.d(LIFECYCLE, getMethodName(this));
        }
    }

    private static String getMethodName(BaseActivity instance) {
        return instance.getClass().getSimpleName() + "#" +
                Thread.currentThread().getStackTrace()[4].getMethodName();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // back and up are different actions, but by default let's have up == back
                // in a simple application like ours, there really is no difference between the two
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
