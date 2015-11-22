package me.vickychijwani.popularmovies.event;

import com.squareup.otto.Bus;

public class DataBusProvider {

    private static final Bus mBus = new Bus();

    private DataBusProvider() {}

    public static Bus getBus() { return mBus; }

}
