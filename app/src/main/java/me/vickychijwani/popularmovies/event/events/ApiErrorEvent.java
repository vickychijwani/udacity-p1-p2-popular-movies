package me.vickychijwani.popularmovies.event.events;

// represents a network / API related error
public final class ApiErrorEvent implements ApiEvent {

    public final ApiEvent sourceEvent;
    public final Throwable throwable;

    public ApiErrorEvent(ApiEvent sourceEvent, Throwable throwable) {
        this.sourceEvent = sourceEvent;
        this.throwable = throwable;
    }

}
