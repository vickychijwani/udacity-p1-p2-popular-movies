package me.vickychijwani.popularmovies.event.events;

public final class LoadMoviesEvent implements ApiEvent {

    public enum SortCriteria {
        POPULARITY("popularity.desc"), RATING("vote_average.desc");
        public final String str;
        SortCriteria(String str) {
            this.str = str;
        }
    }

    public final SortCriteria sortCriteria;

    public LoadMoviesEvent(SortCriteria sortCriteria) {
        this.sortCriteria = sortCriteria;
    }

}
