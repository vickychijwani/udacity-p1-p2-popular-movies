package me.vickychijwani.popularmovies.event.events;

import java.util.List;

import me.vickychijwani.popularmovies.entity.Movie;

public final class MoviesLoadedEvent {

    public final List<Movie> movies;

    public MoviesLoadedEvent(List<Movie> movies) {
        this.movies = movies;
    }

}
