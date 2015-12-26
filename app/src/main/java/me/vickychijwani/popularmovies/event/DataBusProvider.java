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

        public DataBus() {
            super();
            log(Log.INFO, "INIT", "");
        }

        @Override
        public void register(Object object) {
            super.register(object);
            log(Log.INFO, "REG", object.getClass().getSimpleName());
        }

        @Override
        public void unregister(Object object) {
            log(Log.INFO, "UNREG", object.getClass().getSimpleName());
            super.unregister(object);
        }

        @Override
        public void post(Object event) {
            if (event instanceof DeadEvent) {
                String deadEventName = ((DeadEvent) event).event.getClass().getSimpleName();
                log(Log.WARN, "DEAD", "Dead event posted: " + deadEventName);
            } else {
                log(Log.DEBUG, "POST", event.toString());
            }
            super.post(event);
        }

        private void log(int priority, String type, String message) {
            if (BuildConfig.DEBUG || priority >= Log.WARN) {
                String logMsg = String.format("[%1$5s] %2$s on %3$s", type, message, toString());
                Log.println(priority, TAG, logMsg);
            }
        }
    }

}
