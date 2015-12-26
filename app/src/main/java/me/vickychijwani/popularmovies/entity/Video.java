package me.vickychijwani.popularmovies.entity;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

@RealmClass
@Parcel(value = Parcel.Serialization.BEAN, analyze = { Video.class })
public class Video extends RealmObject {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({TYPE_TRAILER})
    public @interface Type {}

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({SITE_YOUTUBE})
    public @interface Site {}

    public static final String TYPE_TRAILER = "Trailer";
    public static final String SITE_YOUTUBE = "YouTube";

    @PrimaryKey
    private String id;
    private String name;
    @Site private String site;
    @SerializedName("key") private String videoId;
    private int size;
    @Type private String type;



    public Video() {}

    public Video(@NonNull Video other) {
        this.setId(other.getId());
        this.setName(other.getName());
        this.setSite(other.getSite());
        this.setVideoId(other.getVideoId());
        this.setSize(other.getSize());
        this.setType(other.getType());
    }

    public static String getUrl(@NonNull Video video) {
        if (SITE_YOUTUBE.equals(video.getSite())) {
            return String.format("http://www.youtube.com/watch?v=%1$s", video.getVideoId());
        } else {
            throw new UnsupportedOperationException("Only YouTube is supported!");
        }
    }

    public static String getThumbnailUrl(@NonNull Video video) {
        if (SITE_YOUTUBE.equals(video.getSite())) {
            return String.format("http://img.youtube.com/vi/%1$s/0.jpg", video.getVideoId());
        } else {
            throw new UnsupportedOperationException("Only YouTube is supported!");
        }
    }




    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public @Site String getSite() {
        return site;
    }

    public void setSite(@Site String site) {
        this.site = site;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public @Type String getType() {
        return type;
    }

    public void setType(@Type String type) {
        this.type = type;
    }

    public static class RealmListParcelConverter extends
            me.vickychijwani.popularmovies.entity.RealmListParcelConverter<Video> {
        @Override
        public void itemToParcel(Video input, android.os.Parcel parcel) {
            parcel.writeParcelable(Parcels.wrap(Video.class, input), 0);
        }

        @Override
        public Video itemFromParcel(android.os.Parcel parcel) {
            return Parcels.unwrap(parcel.readParcelable(Video.class.getClassLoader()));
        }
    }

}
