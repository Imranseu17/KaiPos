package com.kaicomsol.kpos.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.activity.LoginActivity;
import com.kaicomsol.kpos.activity.TransactionDetailsActivity;
import com.kaicomsol.kpos.adapter.TransactionAdapter;
import com.kaicomsol.kpos.callbacks.TransactionHistoryClickListener;
import com.kaicomsol.kpos.callbacks.TransactionView;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.models.TransactionModel;
import com.kaicomsol.kpos.presenters.TransactionPresenter;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.PaginationScrollListener;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionFragment extends Fragment implements TransactionView, TransactionHistoryClickListener {


    private Activity activity = null;
    private TransactionPresenter mPresenter;
    private TransactionAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
    private int TOTAL_PAGES = 5;
    private int MY_TOTAL_PAGE = 0;
    private int currentPage = PAGE_START;

    //component bind
    @BindView(R.id.main_view)
    RelativeLayout main_view;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.line)
    View line;
    @BindView(R.id.recycler_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.amount_layout)
    RelativeLayout amount_layout;
    @BindView(R.id.total)
    TextView total;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    public TransactionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        ButterKnife.bind(this, view);
        mPresenter = new TransactionPresenter(this);
        viewConfig();

        return view;
    }

    private void viewConfig(){
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter =  new TransactionAdapter(getContext(), this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new PaginationScrollListener(mLayoutManager) {
            @Override
            protected void loadMoreItems() {
                if (MY_TOTAL_PAGE > currentPage){
                    isLoading = true;
                    currentPage += 1;
                    getTransactionHistoryNext(currentPage);
                }
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;

            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }

        });
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (activity != null) getTransitionInfo(currentPage);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && activity != null) {
            getTransitionInfo(currentPage);
        }
    }


    private void getTransitionInfo(int currentPage){

        String token = SharedDataSaveLoad.load(activity,getString(R.string.preference_access_token));
        String deviceId = SharedDataSaveLoad.load(activity,getString(R.string.preference_device_id));
        String accountNo = SharedDataSaveLoad.load(activity,getString(R.string.preference_account_no));
        String customerCode = SharedDataSaveLoad.load(activity,getString(R.string.preference_customer_code));

        if (checkConnection()) {
            showAnimation();
            mPresenter.getTransactionInfo(token,currentPage, deviceId,accountNo,customerCode);
        }else showSnackbar(main_view, getString(R.string.no_internet_connection));
    }


    private void getTransactionHistoryNext(int currentPage){

        String token = SharedDataSaveLoad.load(activity,getString(R.string.preference_access_token));
        String deviceId = SharedDataSaveLoad.load(activity,getString(R.string.preference_device_id));
        String accountNo = SharedDataSaveLoad.load(activity,getString(R.string.preference_account_no));
        String customerCode = SharedDataSaveLoad.load(activity,getString(R.string.preference_customer_code));
        if (checkConnection()){
            mAdapter.addLoadingFooter();
            mPresenter.getTransactionInfo(token, currentPage,deviceId,accountNo,customerCode);
        }else  CustomAlertDialog.showError(getContext(),getString(R.string.no_internet_connection));
    }

    @Override
    public void onSuccess(List<TransactionModel> transactionModelList, int currentPage) {
        double totalAmount = 0.0;
        List<TransactionModel> transactionModels = transactionModelList;
        for (TransactionModel transactionModel : transactionModels){
            totalAmount += transactionModel.getAmount();
        }
        if (totalAmount > 0.0) amount_layout.setVisibility(View.VISIBLE);
        else amount_layout.setVisibility(View.GONE);
        DecimalFormat decimalFormat = new DecimalFormat(".##");
        total.setText("Total Amount: " +decimalFormat.format(totalAmount)+ " TK");

        hideAnimation();
        if (transactionModelList != null) MY_TOTAL_PAGE = currentPage;
        if (currentPage > 1){
            isLoading = false;
            mAdapter.removeLoadingFooter();
        }
        if (transactionModelList != null) {
            if (transactionModelList.size() > 0) {
                line.setVisibility(View.VISIBLE);
                mAdapter.setTransaction(transactionModelList, currentPage);
            }else {
                if (mAdapter != null ) mAdapter.clear();
                CustomAlertDialog.showError(getContext(),"TransactionModel not found");
            }

            //showEmptyAnimation();
        }

    }

    @Override
    public void onError(String error) {
        hideAnimation();
        if (currentPage == 1){
            CustomAlertDialog.showError(getContext(),"TransactionModel not found");
        }else {
            isLoading = false;
            mAdapter.removeLoadingFooter();
        }
    }

    @Override
    public void onLogout(int code) {
        SharedDataSaveLoad.remove(activity, getString(R.string.preference_access_token));
        SharedDataSaveLoad.remove(activity, getString(R.string.preference_is_service_check));
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        activity.finish();
    }

    private boolean checkConnection() {

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void showSnackbar(View view, String msg) {
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_LONG);
        TextView tv = (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        tv.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Nexa-Light.otf"));
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        snackbar.show();
    }

    public void showAnimation() {
        line.setVisibility(View.GONE);
        amount_layout.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("animation_loading.json");
        animationView.playAnimation();
        animationView.loop(true);
    }

    public void showEmptyAnimation() {
        mRecyclerView.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("empty_box.json");
        animationView.playAnimation();
        animationView.loop(false);
    }

    public void hideAnimation() {
        line.setVisibility(View.GONE);
        amount_layout.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        if (animationView.isAnimating()) animationView.cancelAnimation();
        animationView.setVisibility(View.GONE);
    }

    @Override
    public void onHistoryClick(TransactionModel transactionModel) {

        Intent intent = new Intent(getContext(), TransactionDetailsActivity.class) ;
        intent.putExtra("transactionModel", transactionModel);
        startActivity(intent);

    }

    @Override
    public void retryPageLoad() {
        DebugLog.e("retryPageLoad()");
    }

    public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;

        public SimpleDividerItemDecoration(Context context) {
            mDivider = context.getResources().getDrawable(R.drawable.line_divider);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

}
