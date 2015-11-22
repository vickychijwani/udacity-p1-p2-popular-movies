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

}
