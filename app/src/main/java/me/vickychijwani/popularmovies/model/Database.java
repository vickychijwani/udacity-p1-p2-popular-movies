package me.vickychijwani.popularmovies.model;

import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import me.vickychijwani.popularmovies.entity.Movie;
import me.vickychijwani.popularmovies.util.AppUtil;

class Database {

    private static final String TAG = "Database";

    public void loadMovie(int id, ReadCallback<Movie> callback) {
        loadById(new StringOrInt(id), callback, Movie.class);
    }

    @UiThread
    public <T extends RealmObject> void createOrUpdateEntity(final T object,
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
    public <T extends RealmObject> void createOrUpdateEntity(final Iterable<T> objects,
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
                    callback.done(AppUtil.makeCopyByParcelling(result.first(), clazz));
                } else {
                    callback.failed();
                }
            }
        });
    }

    private Realm getRealm() {
        return Realm.getDefaultInstance();
    }

    public interface ReadCallback<T> {
        void done(T result);    // query result found
        void failed();          // query result not found
    }

    public static abstract class WriteCallback extends Realm.Transaction.Callback {
        @UiThread
        public abstract void done();

        @Override @UiThread
        public void onSuccess() {
            done();
        }

        @Override
        public void onError(Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
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
