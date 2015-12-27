package me.vickychijwani.popularmovies.ui;

import android.os.Bundle;

import me.vickychijwani.popularmovies.R;

public class MoviesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        getSupportFragmentManager().findFragmentById(R.id.movies_fragment).setHasOptionsMenu(true);
    }

}
