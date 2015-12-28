package me.vickychijwani.popularmovies.ui;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import butterknife.ButterKnife;
import me.vickychijwani.popularmovies.R;
import me.vickychijwani.popularmovies.entity.Review;

public class ReviewActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        Review review = Review.fromParcelable(extras.getParcelable(BundleKeys.REVIEW));
        @ColorInt int primaryColor = extras.getInt(BundleKeys.COLOR_PRIMARY, -1);
        @ColorInt int primaryDarkColor = extras.getInt(BundleKeys.COLOR_PRIMARY_DARK, -1);
        @ColorInt int titleTextColor = extras.getInt(BundleKeys.COLOR_TEXT_TITLE, -1);

        if (savedInstanceState == null) {
            Fragment reviewFragment = ReviewFragment.newInstance(review, primaryColor,
                    primaryDarkColor, titleTextColor);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, reviewFragment,
                            ReviewFragment.class.getSimpleName())
                    .commit();
        }
    }

}
