package com.kaicomsol.kpos.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.activity.LoginActivity;
import com.kaicomsol.kpos.callbacks.CustomerInfoView;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.models.CustomerAccountInfo;
import com.kaicomsol.kpos.models.CustomerInfo;
import com.kaicomsol.kpos.presenters.CustomerInfoPresenter;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerFragment extends Fragment implements CustomerInfoView {

    private Activity activity = null;
    private CustomerInfoPresenter mPresenter;
    //UI View Bind
    @BindView(R.id.layout_main) LinearLayout layout_main;
    @BindView(R.id.animation_view) LottieAnimationView animationView;
    @BindView(R.id.layout_customer) ScrollView layout_customer;
    @BindView(R.id.card_content) CardView card_content;
    //UI Data View Bind
    @BindView(R.id.txt_customer_code) TextView txt_customer_code;
    @BindView(R.id.txt_account_no) TextView txt_account_no;
    @BindView(R.id.txt_address)TextView txt_address;
    @BindView(R.id.txt_apartment) TextView txt_apartment;
    @BindView(R.id.txt_house) TextView txt_house;
    @BindView(R.id.txt_rood) TextView txt_road;
    @BindView(R.id.txt_block) TextView txt_block;
    @BindView(R.id.txt_sector) TextView txt_sector;
    @BindView(R.id.txt_section) TextView txt_section;
    @BindView(R.id.txt_zip_code) TextView txt_zip_code;
    @BindView(R.id.txt_metro_id) TextView txt_metro_id;
    @BindView(R.id.txt_metro) TextView txt_metro;
    @BindView(R.id.txt_zone_id) TextView txt_zone_id;
    @BindView(R.id.txt_zone) TextView txt_zone;
    @BindView(R.id.txt_area_id) TextView txt_area_id;
    @BindView(R.id.txt_area) TextView txt_area;
    @BindView(R.id.txt_sub_area) TextView txt_sub_area;
    @BindView(R.id.txt_title) TextView txt_title;
    @BindView(R.id.txt_postal_code) TextView txt_postal_code;
    @BindView(R.id.txt_first_name) TextView txt_first_name;
    @BindView(R.id.txt_last_name) TextView txt_last_name;
    @BindView(R.id.txt_full_name) TextView txt_full_name;
    @BindView(R.id.txt_email) TextView txt_email;
    @BindView(R.id.txt_number) TextView txt_number;
    @BindView(R.id.txt_identity_no) TextView txt_identityNo;
    @BindView(R.id.txt_balance) TextView txt_blance;
    @BindView(R.id.txt_status_id) TextView txt_statusID;
    @BindView(R.id.txt_status) TextView txt_status;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    public CustomerFragment() {


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer, container, false);
        ButterKnife.bind(this,view);
        mPresenter = new CustomerInfoPresenter(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (activity != null) getCustomerInfo();

    }

    private void getCustomerInfo(){

        String token = SharedDataSaveLoad.load(activity,getString(R.string.preference_access_token));
        String deviceId = SharedDataSaveLoad.load(activity,getString(R.string.preference_device_id));
        String accountNo = SharedDataSaveLoad.load(activity,getString(R.string.preference_account_no));

        if (checkConnection()) {
            showAnimation();
            mPresenter.getCustomerInfo(token,deviceId,accountNo);
        }else CustomAlertDialog.showError(activity,getString(R.string.no_internet_connection));
    }

    @Override
    public void onSuccess(CustomerInfo customerInfo) {
         hideAnimation();
         if (customerInfo != null){
             dataCustomer(customerInfo.getCustomerAccountInfo());
         }else showEmptyAnimation();
    }

    @Override
    public void onError(String error) {
        DebugLog.e("ERROR CUSTOMER");
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

    private void dataCustomer(CustomerAccountInfo customerInfo){

        if(customerInfo == null) return;
        txt_customer_code.setText(customerInfo.getCustomerCode() != null ? customerInfo.getErpCode() : "N/A");
        txt_account_no.setText(customerInfo.getAccountNo() != null ? customerInfo.getAccountNo() : "N/A");
        txt_address.setText(customerInfo.getAddress() != null ? customerInfo.getAddress() : "N/A");
        txt_apartment.setText(customerInfo.getApartment() != null ? customerInfo.getApartment() : "N/A");
        txt_house.setText(customerInfo.getHouse() != null ? customerInfo.getHouse() : "N/A");
        txt_road.setText(customerInfo.getRoad() != null ? customerInfo.getRoad() : "N/A");
        txt_block.setText(customerInfo.getBlock() != null ? customerInfo.getBlock() : "N/A");
        txt_sector.setText(customerInfo.getSector() != null ? customerInfo.getSector() : "N/A");
        txt_section.setText(customerInfo.getSection() != null ? customerInfo.getSection() : "N/A");
        txt_zip_code.setText(customerInfo.getZipCode() != null ? ""+ customerInfo.getZipCode() : "N/A");
        txt_metro_id.setText(customerInfo.getMetroId() != null ? ""+ customerInfo.getMetroId() : "N/A");
        txt_metro.setText(customerInfo.getMetro() != null ? customerInfo.getMetro() : "N/A");
        txt_zone_id.setText(customerInfo.getZoneId() != null ? "" +customerInfo.getZoneId() : "N/A");
        txt_zone.setText(customerInfo.getZone() != null ? customerInfo.getZone() : "N/A");
        txt_area_id.setText(customerInfo.getAreaId() != null ? ""+ customerInfo.getAreaId() : "N/A");
        txt_area.setText(customerInfo.getArea() != null ? customerInfo.getArea() : "N/A");
        txt_sub_area.setText(customerInfo.getSubArea() != null ? customerInfo.getSubArea() : "N/A");
        txt_title.setText(customerInfo.getTitle() != null ?  customerInfo.getTitle() : "N/A");
        txt_postal_code.setText(customerInfo.getPostalCode() != null ? ""+ customerInfo.getPostalCode() : "N/A");
        txt_first_name.setText(customerInfo.getFirstName() != null ?  customerInfo.getFirstName() : "N/A");
        txt_last_name.setText(customerInfo.getLastName() != null ?  customerInfo.getLastName() : "N/A");
        txt_full_name.setText(customerInfo.getFullName() != null ?  customerInfo.getFullName() : "N/A");
        txt_email.setText(customerInfo.getEmail() != null ?  customerInfo.getEmail() : "N/A");
        txt_number.setText(customerInfo.getMobileNumber() != null ?  customerInfo.getMobileNumber() : "N/A");
        txt_identityNo.setText(customerInfo.getIdentityNo() != null ? ""+ customerInfo.getIdentityNo() : "N/A");
        txt_blance.setText(customerInfo.getBalance() != null ? customerInfo.getBalance()+" TK" : "N/A");
        txt_statusID.setText(customerInfo.getStatusId() != null ? ""+ customerInfo.getStatusId() : "N/A");
        txt_status.setText(customerInfo.getStatus() != null ?  customerInfo.getStatus() : "N/A");



    }

    private boolean checkConnection() {

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void showAnimation() {
        layout_customer.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("animation_loading.json");
        animationView.playAnimation();
        animationView.loop(true);
    }

    public void showEmptyAnimation() {
        layout_customer.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("empty_box.json");
        animationView.playAnimation();
        animationView.loop(false);
    }

    public void hideAnimation() {
        layout_customer.setVisibility(View.VISIBLE);
        if (animationView.isAnimating()) animationView.cancelAnimation();
        animationView.setVisibility(View.GONE);
    }
}
