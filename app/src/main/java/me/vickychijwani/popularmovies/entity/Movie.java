package me.vickychijwani.popularmovies.entity;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelPropertyConverter;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
@Parcel(value = Parcel.Serialization.BEAN, analyze = { Movie.class })
public class Movie extends RealmObject {

    @PrimaryKey
    private int id;
    private String title;
    private String posterPath;
    private String backdropPath;

    @SerializedName("overview")
    private String synopsis;

    @SerializedName("vote_average")
    private float rating;

    private Date releaseDate;

    // relationships
    private RealmList<Review> reviews = new RealmList<>();
    private RealmList<Video> videos = new RealmList<>();

    // transient fields are not serialized / deserialized by Gson
    private transient boolean isFavorite = false;



    public Movie() {}

    public Movie(@NonNull Movie other) {
        this.id = other.getId();
        this.title = other.getTitle();
        this.posterPath = other.getPosterPath();
        this.backdropPath = other.getBackdropPath();
        this.synopsis = other.getSynopsis();
        this.rating = other.getRating();
        this.releaseDate = other.getReleaseDate();
        this.isFavorite = other.isFavorite();
        this.reviews = new RealmList<>();
        if (other.reviews != null) {
            for (Review review : other.reviews) {
                this.reviews.add(new Review(review));
            }
        }
        this.videos = new RealmList<>();
        if (other.videos != null) {
            for (Video video : other.videos) {
                this.videos.add(new Video(video));
            }
        }
    }

    public static List<Video> getTrailers(Movie movie) {
        List<Video> trailers = new ArrayList<>();
        for (Video video : movie.getVideos()) {
            if (Video.TYPE_TRAILER.equals(video.getType())) {
                trailers.add(video);
            }
        }
        return trailers;
    }

    public static Parcelable toParcelable(Movie movie) {
        return Parcels.wrap(Movie.class, movie);
    }

    public static ArrayList<Parcelable> toParcelable(List<Movie> movies) {
        ArrayList<Parcelable> parcelables = new ArrayList<>(movies.size());
        for (Movie movie : movies) {
            parcelables.add(Parcels.wrap(Movie.class, movie));
        }
        return parcelables;
    }

    public static Movie fromParcelable(Parcelable parcelable) {
        return Parcels.unwrap(parcelable);
    }

    public static List<Movie> fromParcelable(List<Parcelable> parcelables) {
        List<Movie> movies = new ArrayList<>(parcelables.size());
        for (Parcelable parcelable : parcelables) {
            movies.add(Parcels.<Movie>unwrap(parcelable));
        }
        return movies;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public RealmList<Review> getReviews() {
        return reviews;
    }

    @ParcelPropertyConverter(Review.RealmListParcelConverter.class)
    public void setReviews(RealmList<Review> reviews) {
        this.reviews = reviews;
    }

    public RealmList<Video> getVideos() {
        return videos;
    }

    @ParcelPropertyConverter(Video.RealmListParcelConverter.class)
    public void setVideos(RealmList<Video> videos) {
        this.videos = videos;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

}
