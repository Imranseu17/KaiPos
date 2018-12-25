package com.kaicomsol.kpos.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import com.kaicomsol.kpos.adapter.TransactionAdapter;
import com.kaicomsol.kpos.callbacks.TransactionView;
import com.kaicomsol.kpos.model.Transaction;
import com.kaicomsol.kpos.presenters.TransactionPresenter;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionFragment extends Fragment implements TransactionView {


    private Activity activity = null;
    private TransactionPresenter mPresenter;
    private TransactionAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    //component bind
    @BindView(R.id.main_view)
    RelativeLayout main_view;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.recycler_list)
    RecyclerView mRecyclerView;

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
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (activity != null) getTransitionInfo();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && activity != null) {
            getTransitionInfo();
        }
    }


    private void getTransitionInfo(){

        String token = SharedDataSaveLoad.load(activity,getString(R.string.preference_access_token));
        String deviceId = SharedDataSaveLoad.load(activity,getString(R.string.preference_device_id));
        String accountNo = SharedDataSaveLoad.load(activity,getString(R.string.preference_account_no));
        String customerCode = SharedDataSaveLoad.load(activity,getString(R.string.preference_customer_code));

        if (checkConnection()) {
            showAnimation();
            mPresenter.getTransactionInfo(token,deviceId,accountNo,customerCode);
        }else showSnackbar(main_view, getString(R.string.no_internet_connection));
    }

    @Override
    public void onSuccess(List<Transaction> transactionList) {

        hideAnimation();
        if (transactionList != null) {
            if (transactionList.size() > 0) {
                mAdapter =  new TransactionAdapter(activity, transactionList);
                mRecyclerView.setAdapter(mAdapter);
            }else showEmptyAnimation();
        }else showEmptyAnimation();

    }

    @Override
    public void onError(String error) {
        DebugLog.e("TRANSACTION ERROR");
        hideAnimation();
        showEmptyAnimation();
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
        mRecyclerView.setVisibility(View.VISIBLE);
        if (animationView.isAnimating()) animationView.cancelAnimation();
        animationView.setVisibility(View.GONE);
    }

}
