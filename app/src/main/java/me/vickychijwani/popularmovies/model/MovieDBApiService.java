package me.vickychijwani.popularmovies.model;

import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.entity.MovieResults;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

interface MovieDBApiService {

    @GET("discover/movie?vote_count.gte=250")
    Call<MovieResults> fetchMovies(@Query("api_key") String apiKey, @Query("sort_by") String sortBy);

    @GET("movie/{id}?append_to_response=videos,reviews")
    Call<Movie> fetchMovie(@Path("id") int id, @Query("api_key") String apiKey);

}
