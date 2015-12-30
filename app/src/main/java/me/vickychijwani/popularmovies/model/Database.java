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
import me.vickychijwani.popularmovies.BuildConfig;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.util.AppUtil;

class Database {

    private static final String TAG = Database.class.getSimpleName();

    public void loadFavoriteMovies(final ReadCallback<List<Movie>> callback) {
        readAllAsync(new ReadAction<Movie>() {
            @NonNull
            @Override
            public RealmQuery<Movie> getQuery(@NonNull Realm realm) {
                return realm.where(Movie.class).equalTo("isFavorite", true);
            }

            @Override
            public void onResults(RealmResults<Movie> results) {
                List<Movie> favorites = new ArrayList<>(results.size());
                for (Movie movie : results) {
                    favorites.add(movie);
                }
                callback.done(favorites);
            }
        });
    }

    public void loadMovie(final int id, final ReadCallback<Movie> callback) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "[READ ] Load Movie with id = " + id);
        }
        readAllAsync(new ReadAction<Movie>() {
            @NonNull
            @Override
            public RealmQuery<Movie> getQuery(@NonNull Realm realm) {
                return realm.where(Movie.class).equalTo("id", id);
            }

            @Override
            public void onResults(RealmResults<Movie> results) {
                if (!results.isEmpty()) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "[READ ] Success: Movie with id = " + id);
                    }
                    callback.done(AppUtil.copy(results.first(), Movie.class));
                } else {
                    callback.failed(new RuntimeException("No Movie found with id = " + id));
                }
            }
        });
    }

    @UiThread
    public <T extends RealmObject> void createOrUpdateEntity(@NonNull final T object,
                                                             WriteCallback callback) {
        // TODO add error handling
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Creating/updating " + object.getClass().getSimpleName());
        }
        write(new Realm.Transaction() {
            @Override
            @WorkerThread
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(object);
            }
        }, callback);
    }

    @UiThread
    public <T extends RealmObject> void createOrUpdateEntity(@NonNull final Iterable<T> objects,
                                                             WriteCallback callback) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Creating/updating " + objects.getClass().getSimpleName());
        }
        write(new Realm.Transaction() {
            @Override
            @WorkerThread
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(objects);
            }
        }, callback);
    }

    private void write(@NonNull final Realm.Transaction transaction,
                       @NonNull final WriteCallback writeCallback) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            realm.executeTransaction(transaction, writeCallback);
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private <T extends RealmObject> void readAllAsync(@NonNull final ReadAction<T> readAction) {
        Realm realm = null;
        try {
            realm = Realm.getDefaultInstance();
            RealmQuery<T> query = readAction.getQuery(realm);
            final RealmResults<T> results = query.findAllAsync();
            results.addChangeListener(new RealmChangeListener() {
                @Override
                public void onChange() {
                    results.removeChangeListener(this);
                    readAction.onResults(results);
                }
            });
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    private interface ReadAction<T extends RealmObject> {
        @NonNull RealmQuery<T> getQuery(@NonNull Realm realm);
        void onResults(RealmResults<T> results);
    }

    public static abstract class ReadCallback<T> {
        // query result found
        public abstract void done(T result);

        // query result not found
        public void failed(RuntimeException e) {
            Log.e(TAG, "[READ ] Error:\n" + Log.getStackTraceString(e));
        }
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

}
