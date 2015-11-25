package me.vickychijwani.popularmovies.network;

import me.vickychijwani.popularmovies.entity.MovieResults;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface MovieDBApiService {

    @GET("discover/movie")
    Call<MovieResults> fetchMovies(@Query("api_key") String apiKey, @Query("sort_by") String sortBy);

}
