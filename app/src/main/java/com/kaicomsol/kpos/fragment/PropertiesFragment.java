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

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.adapter.CardAdapter;
import com.kaicomsol.kpos.models.Card;
import com.kaicomsol.kpos.models.NFCData;
import com.kaicomsol.kpos.nfcfelica.HttpResponsAsync;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class PropertiesFragment extends Fragment {
    
    private CardAdapter mAdapter;
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

        View view = inflater.inflate(R.layout.fragment_properties, container, false);
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
        if (argument != null) adapterData(argument);

    }

    @Override
    public void onPause() {
        super.onPause();

    }
    private void adapterData(HttpResponsAsync.ReadCardArgument argument) {
        
        List<Card> cardList = new ArrayList<>();
//        long dateTime = Long.parseLong(argument.LidTime);
//        Date date = new Date(dateTime);
//        SimpleDateFormat targetFormat = new SimpleDateFormat("MM-dd-yyyy");
//        String formatDate = targetFormat.format(date);

        cardList.add(new Card(getString(R.string.version_No), argument.VersionNo));
        cardList.add(new Card(getString(R.string.card_status), getStatus(argument.CardStatus)));
        cardList.add(new Card(getString(R.string.card_Id), argument.CardIdm));
        cardList.add(new Card(getString(R.string.customer_Id),argument.CustomerId));
        cardList.add(new Card(getString(R.string.card_Group), getGroup(argument.CardGroup)));
        cardList.add(new Card(getString(R.string.credit), argument.Credit));
        cardList.add(new Card(getString(R.string.unit), argument.Unit));
        cardList.add(new Card(getString(R.string.basic_fee), argument.BasicFee));
        cardList.add(new Card(getString(R.string.refund1), argument.Refund1));
        cardList.add(new Card(getString(R.string.refund2), argument.Refund2));
        cardList.add(new Card(getString(R.string.untreated_fee), argument.UntreatedFee));
        cardList.add(new Card(getString(R.string.open_count), argument.OpenCount));
        cardList.add(new Card(getString(R.string.emergency_balence), argument.ConfigData.EmergencyValue));
        cardList.add(new Card(getString(R.string.card_history_no), argument.CardHistoryNo));
        cardList.add(new Card(getString(R.string.card_error_no), argument.ErrorNo));
        cardList.add(new Card(getString(R.string.lid_time), argument.LidTime));
        
        mAdapter = new CardAdapter(getContext(), cardList);
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

    public void showEmptyAnimation() {
        mRecyclerView.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("empty_box.json");
        animationView.playAnimation();
        animationView.loop(false);
    }

    private String getStatus(String status){

        if (status == null) return "N/A";

        switch (status){
            case "15":
                return getString(R.string.card_recharged);
            case "30":
                return getString(R.string.card_initialized);
            case "06":
                return getString(R.string.meter_initialized);
            case "05":
                return getString(R.string.card_refund);
            default:
                return  "N/A";
        }

    }

    private String getGroup(String group){

        if (group == null) return "N/A";

        switch (group){

            case "77":
                return getString(R.string.customer_Card);
            case "88":
                return getString(R.string.service_Card);
            default:
                return  "N/A";
        }

    }



}
