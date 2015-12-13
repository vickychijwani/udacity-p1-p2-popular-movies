package me.vickychijwani.popularmovies.entity;

import org.parceler.converter.CollectionParcelConverter;

import io.realm.RealmList;
import io.realm.RealmObject;

abstract class RealmListParcelConverter<T extends RealmObject>
        extends CollectionParcelConverter<T, RealmList<T>> {

    @Override
    public RealmList<T> createCollection() {
        return new RealmList<>();
    }

}
