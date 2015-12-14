package me.vickychijwani.popularmovies.model;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.util.AppUtil;

class Database {

    private static final String TAG = "Database";

    public void loadFavoriteMovies(final ReadCallback<List<Movie>> callback) {
        final RealmResults<Movie> results = getRealm().where(Movie.class)
                .equalTo("isFavorite", true)
                .findAllAsync();
        results.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                results.removeChangeListener(this);
                List<Movie> favorites = new ArrayList<>(results.size());
                for (Movie movie : results) {
                    favorites.add(movie);
                }
                callback.done(favorites);
            }
        });
    }

    public void loadMovie(int id, ReadCallback<Movie> callback) {
        loadById(new StringOrInt(id), callback, Movie.class);
    }

    @UiThread
    public <T extends RealmObject> void createOrUpdateEntity(@NonNull final T object,
                                                             WriteCallback callback) {
        // TODO add error handling
        Realm realm = getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override @WorkerThread
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(object);
            }
        }, callback);
    }

    @UiThread
    public <T extends RealmObject> void createOrUpdateEntity(@NonNull final Iterable<T> objects,
                                                             WriteCallback callback) {
        Realm realm = getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override @WorkerThread
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(objects);
            }
        }, callback);
    }

    private <T extends RealmObject> void loadById(StringOrInt idStringOrInt,
                                                  final ReadCallback<T> callback,
                                                  final Class<T> clazz) {
        RealmQuery<T> query = getRealm().where(clazz);
        if (idStringOrInt.hasString()) {
            query.equalTo("id", idStringOrInt.string);
        } else {
            query.equalTo("id", idStringOrInt.integer);
        }
        final RealmResults<T> result = query.findAllAsync();
        result.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                result.removeChangeListener(this);
                if (! result.isEmpty()) {
                    callback.done(AppUtil.copy(result.first(), clazz));
                } else {
                    callback.failed();
                }
            }
        });
    }

    private Realm getRealm() {
        return Realm.getDefaultInstance();
    }

    public static abstract class ReadCallback<T> {
        public abstract void done(T result);    // query result found
        public void failed() {}                 // query result not found
    }

    public static abstract class WriteCallback extends Realm.Transaction.Callback {
        @UiThread
        public void done() {}

        @UiThread
        public void failed(Exception e) {}

        @Override @UiThread
        public void onSuccess() {
            done();
        }

        @Override
        public void onError(Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
            failed(e);
        }
    }

    private static class StringOrInt {
        public final String string;
        public final Integer integer;

        public StringOrInt(String string) {
            this.string = string;
            this.integer = null;
        }

        public StringOrInt(int integer) {
            this.string = null;
            this.integer = integer;
        }

        public boolean hasString() {
            return this.string != null;
        }

        public boolean hasInt() {
            return this.integer != null;
        }
    }

}
