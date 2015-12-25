package me.vickychijwani.popularmovies.event;

import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.DeadEvent;

import me.vickychijwani.popularmovies.BuildConfig;

public class DataBusProvider {

    private static final Bus mBus = new DataBus();

    private DataBusProvider() {}

    public static Bus getBus() { return mBus; }

    private static final class DataBus extends Bus {
        private static final String TAG = "DataBus";

        @Override
        public void register(Object object) {
            super.register(object);
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "[REG  ] " + object.getClass().getSimpleName());
            }
        }

        @Override
        public void unregister(Object object) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "[UNREG] " + object.getClass().getSimpleName());
            }
            super.unregister(object);
        }

        @Override
        public void post(Object event) {
            if (event instanceof DeadEvent) {
                Log.w(TAG, "[DEAD ] Dead event posted: " +
                        ((DeadEvent) event).event.getClass().getSimpleName());
            } else if (BuildConfig.DEBUG) {
                Log.d(TAG, "[POST ] " + event.toString());
            }
            super.post(event);
        }
    }

}
