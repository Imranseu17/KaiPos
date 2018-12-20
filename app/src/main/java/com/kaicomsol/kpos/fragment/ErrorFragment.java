package com.kaicomsol.kpos.fragment;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.kaicomsol.kpos.model.Error;

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.adapter.ErrorAdapter;
import com.kaicomsol.kpos.model.NFCData;
import com.kaicomsol.kpos.nfcfelica.HttpResponsAsync;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class ErrorFragment extends Fragment {

    private ErrorAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private Activity activity = null;

    //component bind
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.recycler_list)
    RecyclerView mRecyclerView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_error, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewConfig();
    }

    private void viewConfig(){

        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
    }

    @Override
    public void onResume() {
        super.onResume();
        HttpResponsAsync.ReadCardArgument argument =  NFCData.getInstance().getArgument();
        adapterData(argument);

    }

    @Override
    public void onPause() {
        super.onPause();

    }
    private void adapterData(HttpResponsAsync.ReadCardArgument argument) {

        List<Error> errorList = new ArrayList<>();

        for(int i = 0; i < argument.ErrorHistory.size(); i++){
            HttpResponsAsync.ReadCardArgumentErrorHistory cardArgument = argument.ErrorHistory.get(i);
            errorList.add(new Error(cardArgument.ErrorGroup, cardArgument.ErrorType, cardArgument.ErrorTime));
        }

        mAdapter = new ErrorAdapter(getActivity(), errorList);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void showAnimation() {
        mRecyclerView.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("animation_loading.json");
        animationView.playAnimation();
        animationView.loop(true);
    }

    public void hideAnimation() {
        mRecyclerView.setVisibility(View.VISIBLE);
        if (animationView.isAnimating()) animationView.cancelAnimation();
        animationView.setVisibility(View.GONE);
    }


}
