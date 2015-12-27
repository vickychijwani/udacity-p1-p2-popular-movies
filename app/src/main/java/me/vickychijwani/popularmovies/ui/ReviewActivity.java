package me.vickychijwani.popularmovies.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import me.vickychijwani.popularmovies.R;
import me.vickychijwani.popularmovies.entity.Review;
import me.vickychijwani.popularmovies.util.AppUtil;

public class ReviewActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @BindDrawable(R.drawable.arrow_left)
    Drawable mUpArrow;

    @SuppressWarnings("FieldCanBeLocal")
    private Review mReview;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        mReview = Review.fromParcelable(extras.getParcelable(BundleKeys.REVIEW));
        @ColorInt int primaryColor = extras.getInt(BundleKeys.COLOR_PRIMARY, -1);
        @ColorInt int primaryDarkColor = extras.getInt(BundleKeys.COLOR_PRIMARY_DARK, -1);
        @ColorInt int titleTextColor = extras.getInt(BundleKeys.COLOR_TEXT_TITLE, -1);

        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AppUtil.tintDrawable(mUpArrow, ContextCompat.getColor(this, android.R.color.white));
        getSupportActionBar().setHomeAsUpIndicator(mUpArrow);
        getSupportActionBar().setTitle("Review by " + mReview.getAuthor());

        if (primaryColor != -1 && primaryDarkColor != -1 && titleTextColor != -1) {
            AppUtil.setColorTheme(this, mToolbar, primaryColor, primaryDarkColor, titleTextColor,
                    false);
        }

        if (savedInstanceState == null) {
            Fragment reviewFragment = ReviewFragment.newInstance(mReview);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container, reviewFragment,
                            ReviewFragment.class.getSimpleName())
                    .commit();
        }
    }

}
