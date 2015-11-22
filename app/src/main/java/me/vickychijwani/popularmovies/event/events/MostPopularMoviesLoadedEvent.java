package me.vickychijwani.popularmovies.event.events;

import java.util.List;

import me.vickychijwani.popularmovies.entity.Movie;

public final class MostPopularMoviesLoadedEvent implements ApiEvent {

    public final List<Movie> movies;

    public MostPopularMoviesLoadedEvent(List<Movie> movies) {
        this.movies = movies;
    }

}
