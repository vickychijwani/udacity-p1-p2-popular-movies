package me.vickychijwani.popularmovies.network;

import me.vickychijwani.popularmovies.entity.MovieResults;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface MovieDBApiService {

    @GET("discover/movie?sort_by=popularity.desc")
    Call<MovieResults> fetchMostPopularMovies(@Query("api_key") String apiKey);

    @GET("discover/movie?sort_by=vote_average.desc")
    Call<MovieResults> fetchHighestRatedMovies(@Query("api_key") String apiKey);

}
