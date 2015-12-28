package me.vickychijwani.popularmovies.ui;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.vickychijwani.popularmovies.R;
import me.vickychijwani.popularmovies.entity.Review;
import me.vickychijwani.popularmovies.util.AppUtil;

public class ReviewFragment extends BaseFragment {

    @Bind(R.id.toolbar)             Toolbar mToolbar;
    @Bind(R.id.review_content)      TextView mReviewContent;

    private Review mReview;

    public ReviewFragment() {}

    public static ReviewFragment newInstance(Review review, int primaryColor, int primaryDarkColor,
                                             int titleTextColor) {
        ReviewFragment fragment = new ReviewFragment();
        Bundle args = new Bundle();
        args.putParcelable(BundleKeys.REVIEW, Review.toParcelable(review));
        args.putInt(BundleKeys.COLOR_PRIMARY, primaryColor);
        args.putInt(BundleKeys.COLOR_PRIMARY_DARK, primaryDarkColor);
        args.putInt(BundleKeys.COLOR_TEXT_TITLE, titleTextColor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReview = Review.fromParcelable(getArguments().getParcelable(BundleKeys.REVIEW));
        if (mReview == null) {
            throw new IllegalStateException("No review given!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);
        ButterKnife.bind(this, view);

        Bundle args = getArguments();
        @ColorInt int primaryColor = args.getInt(BundleKeys.COLOR_PRIMARY, -1);
        @ColorInt int primaryDarkColor = args.getInt(BundleKeys.COLOR_PRIMARY_DARK, -1);
        @ColorInt int titleTextColor = args.getInt(BundleKeys.COLOR_TEXT_TITLE, -1);

        String title = "Review by " + mReview.getAuthor();
        AppUtil.setupToolbar(getActivity(), mToolbar, AppUtil.ToolbarNavIcon.UP, title);

        if (primaryColor != -1 && primaryDarkColor != -1 && titleTextColor != -1) {
            AppUtil.setColorTheme(getActivity(), mToolbar, primaryColor, primaryDarkColor,
                    titleTextColor, false);
        }

        mReviewContent.setText(mReview.getContent());
        return view;
    }

}
