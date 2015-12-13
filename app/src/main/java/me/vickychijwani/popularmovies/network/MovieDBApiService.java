package me.vickychijwani.popularmovies.network;

import me.vickychijwani.popularmovies.entity.MovieResults;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

interface MovieDBApiService {

    @GET("discover/movie?vote_count.gte=250")
    Call<MovieResults> fetchMovies(@Query("api_key") String apiKey, @Query("sort_by") String sortBy);

}
