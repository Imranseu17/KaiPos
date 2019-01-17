package com.kaicomsol.kpos.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.activity.LoginActivity;
import com.kaicomsol.kpos.activity.MeterDetailsActivity;
import com.kaicomsol.kpos.adapter.MeterAdapter;
import com.kaicomsol.kpos.callbacks.MeterClickListener;
import com.kaicomsol.kpos.callbacks.MeterView;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.models.Meter;
import com.kaicomsol.kpos.models.MeterList;
import com.kaicomsol.kpos.presenters.MeterPresenter;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeterFragment extends Fragment implements MeterView, MeterClickListener {


    private Activity activity = null;
    private MeterPresenter mPresenter;
    private MeterAdapter mAdapter;
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

    public MeterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meter, container, false);
        ButterKnife.bind(this, view);
        mPresenter = new MeterPresenter(this);
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

        if (activity != null) getMeterInfo();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && activity != null) {
            getMeterInfo();
        }
    }


    private void getMeterInfo(){

        String token = SharedDataSaveLoad.load(activity, getString(R.string.preference_access_token));
        String deviceId = SharedDataSaveLoad.load(activity,getString(R.string.preference_device_id));
        String accountNo = SharedDataSaveLoad.load(activity,getString(R.string.preference_account_no));

        if (checkConnection()) {
            showAnimation();
            mPresenter.getMeterInfo(token,deviceId,accountNo);
        }else CustomAlertDialog.showError(activity, getString(R.string.no_internet_connection));
    }

    @Override
    public void onSuccess(Meter meter) {

        hideAnimation();
        if (meter.getMeterList() != null) {
            if (meter.getMeterList().size() > 0) {
                mAdapter =  new MeterAdapter(activity, this);
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.setMeterList(meter.getMeterList());
            }else showEmptyAnimation();
        }else showEmptyAnimation();

    }

    @Override
    public void onError(String error) {
        DebugLog.e("METER ERROR");
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

    @Override
    public void onMeterClick(MeterList meter) {

            SharedDataSaveLoad.saveInt(activity,getString(R.string.preference_meter_type_id), meter.getMeterTypeId());

            Intent intent = new Intent(activity, MeterDetailsActivity.class);
            startActivity(intent);
    }
}
