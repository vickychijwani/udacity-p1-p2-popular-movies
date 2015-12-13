package me.vickychijwani.popularmovies.event.events;

import android.support.annotation.NonNull;

import me.vickychijwani.popularmovies.entity.Movie;

public class MovieLoadedEvent {

    public final Movie movie;

    public MovieLoadedEvent(@NonNull Movie movie) {
        this.movie = movie;
    }

}
