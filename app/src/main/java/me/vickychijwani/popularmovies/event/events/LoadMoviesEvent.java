package me.vickychijwani.popularmovies.event.events;

import me.vickychijwani.popularmovies.entity.MovieResults;

public final class LoadMoviesEvent implements ApiEvent {

    public final MovieResults.SortCriteria sortCriteria;

    public LoadMoviesEvent(MovieResults.SortCriteria sortCriteria) {
        this.sortCriteria = sortCriteria;
    }

}
