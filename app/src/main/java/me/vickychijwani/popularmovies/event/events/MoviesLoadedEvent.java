package me.vickychijwani.popularmovies.event.events;

import java.util.List;

import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.entity.MovieResults;

public final class MoviesLoadedEvent {

    public final List<Movie> movies;
    public final MovieResults.SortCriteria sortCriteria;

    public MoviesLoadedEvent(List<Movie> movies, MovieResults.SortCriteria sortCriteria) {
        this.movies = movies;
        this.sortCriteria = sortCriteria;
    }

}
