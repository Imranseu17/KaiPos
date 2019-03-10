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
import com.kaicomsol.kpos.models.AccessFalica;
import com.kaicomsol.kpos.models.Card;
import com.kaicomsol.kpos.models.NFCData;
import com.kaicomsol.kpos.models.ReadCard;
import com.kaicomsol.kpos.nfcfelica.HttpResponsAsync;
import com.kaicomsol.kpos.utils.CardPropertise;
import com.kaicomsol.kpos.utils.RechargeStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        AccessFalica accessFalica = NFCData.getInstance().getAccessFalica();
        if (accessFalica != null) adapterData(accessFalica);

    }

    @Override
    public void onPause() {
        super.onPause();

    }
    private void adapterData(AccessFalica accessFalica) {
        
        List<Card> cardList = new ArrayList<>();
//        long dateTime = Long.parseLong(argument.LidTime);
//        Date date = new Date(dateTime);
//        SimpleDateFormat targetFormat = new SimpleDateFormat("MM-dd-yyyy");
//        String formatDate = targetFormat.format(date);

        cardList.add(new Card(getString(R.string.version_No), accessFalica.versionNO));
        cardList.add(new Card(getString(R.string.card_status), getStatus(accessFalica.cardStatus)));
        cardList.add(new Card(getString(R.string.card_Id), accessFalica.cardIDm));
        cardList.add(new Card(getString(R.string.customer_Id),accessFalica.strCustomerId));
        cardList.add(new Card(getString(R.string.card_Group), getGroup(accessFalica.cardGroup)));
        cardList.add(new Card(getString(R.string.credit), accessFalica.credit));
        cardList.add(new Card(getString(R.string.unit), accessFalica.unit));
        cardList.add(new Card(getString(R.string.basic_fee), accessFalica.basicFee));
        cardList.add(new Card(getString(R.string.refund1), accessFalica.refund1));
        cardList.add(new Card(getString(R.string.refund2), accessFalica.refund2));
        cardList.add(new Card(getString(R.string.untreated_fee), accessFalica.untreatedFee));
        cardList.add(new Card(getString(R.string.open_count), accessFalica.openCount));
        cardList.add(new Card(getString(R.string.emergency_balence), accessFalica.readCardArgument.ConfigData.EmergencyValue));
        cardList.add(new Card(getString(R.string.card_history_no), String.valueOf(accessFalica.historyNO)));
        cardList.add(new Card(getString(R.string.card_error_no), String.valueOf(accessFalica.errorNO)));
        cardList.add(new Card(getString(R.string.lid_time), accessFalica.lidTime));
        
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



        CardPropertise cardPropertise = CardPropertise.getByCode(status);

        if (status == null) return "N/A";

        switch (cardPropertise){
            case CARD_RECHARGED:
                return getString(R.string.card_recharged);
            case CARD_INITIAL:
                return getString(R.string.card_initialized);
            case CARD_CHARGED_METER:
                return getString(R.string.meter_initialized);
            case CARD_REFUNDED:
                return getString(R.string.card_refund);
            default:
                return  "N/A";
        }

    }

    private String getGroup(String group){

        CardPropertise cardPropertise = CardPropertise.getByCode(group);

        if (group == null) return "N/A";

        switch (cardPropertise){

            case CUSTOMER_CARD:
                return getString(R.string.customer_Card);
            case SERVICE_CARD:
                return getString(R.string.service_Card);
            default:
                return  "N/A";
        }

    }



}
