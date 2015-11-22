package me.vickychijwani.popularmovies.event.events;

// represents a network / API related error
public final class ApiErrorEvent implements ApiEvent {

    public final ApiEvent sourceEvent;
    public final String message;

    public ApiErrorEvent(ApiEvent sourceEvent, String message) {
        this.sourceEvent = sourceEvent;
        this.message = message;
    }

}
