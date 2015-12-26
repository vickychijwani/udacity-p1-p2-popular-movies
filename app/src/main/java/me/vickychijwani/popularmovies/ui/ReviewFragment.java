package me.vickychijwani.popularmovies.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.vickychijwani.popularmovies.R;
import me.vickychijwani.popularmovies.entity.Review;

public class ReviewFragment extends BaseFragment {

    @Bind(R.id.review_content)      TextView mReviewContent;

    private Review mReview;

    public ReviewFragment() {}

    public static ReviewFragment newInstance(Review review) {
        ReviewFragment fragment = new ReviewFragment();
        Bundle args = new Bundle();
        args.putParcelable(BundleKeys.REVIEW, Review.toParcelable(review));
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
        mReviewContent.setText(mReview.getContent());
        return view;
    }

}
