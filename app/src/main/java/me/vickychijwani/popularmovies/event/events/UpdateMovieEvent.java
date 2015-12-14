package me.vickychijwani.popularmovies.event.events;

import me.vickychijwani.popularmovies.entity.Movie;

public class UpdateMovieEvent {

    public final Movie movie;

    public UpdateMovieEvent(Movie movie) {
        this.movie = movie;
    }

}
