package com.kaicomsol.kpos.fragment;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.Gson;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.adapter.InvoiceAdapter;
import com.kaicomsol.kpos.adapter.MeterAdapter;
import com.kaicomsol.kpos.callbacks.CloseClickListener;
import com.kaicomsol.kpos.callbacks.InvoiceView;
import com.kaicomsol.kpos.dialogs.CardCheckDialog;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.model.Invoice;
import com.kaicomsol.kpos.model.Invoices;
import com.kaicomsol.kpos.model.Meter;
import com.kaicomsol.kpos.presenters.InvoicePresenter;
import com.kaicomsol.kpos.presenters.MeterPresenter;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class InvoiceFragment extends DialogFragment implements InvoiceView {

    private Activity activity = null;
    private InvoicePresenter mPresenter;
    private InvoiceAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private double amount = 0.0;

    //component bind
    @BindView(R.id.main_view)
    RelativeLayout main_view;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.recycler_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.txt_total_amount)
    TextView txt_total_amount;
    @BindView(R.id.btn_add)
    Button btn_add;
    private String cardNo = "";
    private Invoices invoices = null;

    private static CloseClickListener mCloseClickListener = null;
    public static InvoiceFragment newInstance(CloseClickListener listener){
        InvoiceFragment dialogFragment = new InvoiceFragment();
        mCloseClickListener = listener;

        return dialogFragment;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    public InvoiceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        View view = inflater.inflate(R.layout.fragment_invoice, container, false);
        ButterKnife.bind(this, view);
        mPresenter = new InvoicePresenter(this);
        viewConfig();

        return view;
    }

    private void viewConfig(){
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.setLayoutManager(mLayoutManager);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCloseClickListener != null){
                    Dialog dialog = getDialog();
                    if (dialog != null) dialog.dismiss();
                    mCloseClickListener.onCloseClick(amount);
                }
            }
        });
        String invoice = getArguments().getString("invoice");
        invoices = new Gson().fromJson(invoice, Invoices.class);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //if (activity != null) getInvoices();
        if (invoices != null){
            mAdapter =  new InvoiceAdapter(activity, invoices.getInvoices());
            totalAmount(invoices.getInvoices());
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser && activity != null) {
//            getInvoices();
//        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }


    private void getInvoices(){

        String token = SharedDataSaveLoad.load(activity, getString(R.string.preference_access_token));

        if (checkConnection()) {
            showAnimation();
            mPresenter.getInvoices(token,cardNo);
        }else CustomAlertDialog.showError(activity, getString(R.string.no_internet_connection));
    }

    @Override
    public void onSuccess(Invoices invoices) {

        hideAnimation();
        if (invoices.getInvoices() != null) {
            if (invoices.getInvoices().size() > 0) {
                mAdapter =  new InvoiceAdapter(activity, invoices.getInvoices());
                totalAmount(invoices.getInvoices());
                mRecyclerView.setAdapter(mAdapter);
            }else showEmptyAnimation();
        }else showEmptyAnimation();

    }

    @Override
    public void onError(String error) {
        DebugLog.e(error+"ERRROR");
        hideAnimation();
        showEmptyAnimation();
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
        txt_total_amount.setVisibility(View.GONE);
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

    private void totalAmount(List<Invoice> invoices){
        for (Invoice invoice: invoices){
            amount+= invoice.getAmount();
        }
        txt_total_amount.setText("Total Amount : "+amount+" TK");
    }

}
