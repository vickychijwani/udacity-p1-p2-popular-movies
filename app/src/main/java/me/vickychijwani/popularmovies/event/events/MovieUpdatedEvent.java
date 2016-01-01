package me.vickychijwani.popularmovies.event.events;

import android.support.annotation.NonNull;

import me.vickychijwani.popularmovies.entity.Movie;

public final class MovieUpdatedEvent {

    public final Movie movie;

    public MovieUpdatedEvent(@NonNull Movie movie) {
        this.movie = movie;
    }

}
