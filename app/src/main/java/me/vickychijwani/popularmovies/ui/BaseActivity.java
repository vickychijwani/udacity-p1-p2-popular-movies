package me.vickychijwani.popularmovies.ui;

import android.support.v7.app.AppCompatActivity;

import com.squareup.otto.Bus;

import me.vickychijwani.popularmovies.event.DataBusProvider;

public abstract class BaseActivity extends AppCompatActivity {

    protected Bus getDataBus() {
        return DataBusProvider.getBus();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDataBus().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getDataBus().unregister(this);
    }

}
