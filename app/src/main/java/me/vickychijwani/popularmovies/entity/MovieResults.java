package me.vickychijwani.popularmovies.entity;

import java.util.List;

// wrapper class needed by Retrofit to parse API responses from the Movie DB API
// response is of the form: { "page": 1, "results": [...] }
public class MovieResults {

    private int page;
    private List<Movie> results;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<Movie> getResults() {
        return results;
    }

    public void setResults(List<Movie> results) {
        this.results = results;
    }

    public enum SortCriteria {
        POPULARITY("popularity.desc"), RATING("vote_average.desc");
        public final String str;
        SortCriteria(String str) {
            this.str = str;
        }
        public int getId() {
            return this.str.hashCode();
        }
        public String toString() {
            return this.str;
        }
    }

}
