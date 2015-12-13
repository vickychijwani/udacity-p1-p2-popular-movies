package me.vickychijwani.popularmovies.entity;

import java.util.List;

// wrapper class needed by Retrofit to parse API responses from the Movie DB API
// response is of the form: { "page": 1, "results": [...] }
class Results<T> {

    private int page;
    private List<T> results;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }


}
