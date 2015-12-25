package me.vickychijwani.popularmovies.entity;

import android.support.annotation.NonNull;

import org.parceler.Parcel;
import org.parceler.Parcels;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
@Parcel(value = Parcel.Serialization.BEAN, analyze = { Review.class })
public class Review extends RealmObject {

    @PrimaryKey
    private String id;
    private String author;
    private String content;
    private String url;



    public Review() {}

    public Review(@NonNull Review other) {
        this.id = other.getId();
        this.author = other.getAuthor();
        this.content = other.getContent();
        this.url = other.getUrl();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static class RealmListParcelConverter extends
            me.vickychijwani.popularmovies.entity.RealmListParcelConverter<Review> {
        @Override
        public void itemToParcel(Review input, android.os.Parcel parcel) {
            parcel.writeParcelable(Parcels.wrap(Review.class, input), 0);
        }

        @Override
        public Review itemFromParcel(android.os.Parcel parcel) {
            return Parcels.unwrap(parcel.readParcelable(Review.class.getClassLoader()));
        }
    }

}
