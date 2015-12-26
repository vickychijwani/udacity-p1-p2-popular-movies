package me.vickychijwani.popularmovies.ui;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
        setSupportActionBar(mToolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        mReview = Review.fromParcelable(extras.getParcelable(BundleKeys.REVIEW));
        @ColorInt int primaryColor = extras.getInt(BundleKeys.COLOR_PRIMARY, -1);
        @ColorInt int primaryDarkColor = extras.getInt(BundleKeys.COLOR_PRIMARY_DARK, -1);
        @ColorInt int titleTextColor = extras.getInt(BundleKeys.COLOR_TEXT_TITLE, -1);
        boolean validColors = (primaryColor != -1 && primaryDarkColor != -1 && titleTextColor != -1);

        if (validColors) {
            mToolbar.setBackgroundColor(primaryColor);
            mToolbar.setTitleTextColor(titleTextColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(primaryDarkColor);
            }
        }

        // credits for up arrow color tinting: http://stackoverflow.com/a/26837072/504611
        @ColorInt int upArrowColor;
        if (validColors) {
            upArrowColor = titleTextColor;
        } else {
            upArrowColor = getResources().getColor(android.R.color.white);
        }
        AppUtil.tintDrawable(mUpArrow, upArrowColor);
        getSupportActionBar().setHomeAsUpIndicator(mUpArrow);

        getSupportActionBar().setTitle("Review by " + mReview.getAuthor());

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
