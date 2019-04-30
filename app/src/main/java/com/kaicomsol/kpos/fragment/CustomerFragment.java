package com.kaicomsol.kpos.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.activity.LoginActivity;
import com.kaicomsol.kpos.adapter.CardAdapter;
import com.kaicomsol.kpos.adapter.CustomerPropertiesAdapter;
import com.kaicomsol.kpos.adapter.TransactionAdapter;
import com.kaicomsol.kpos.callbacks.CustomerInfoView;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.models.Card;
import com.kaicomsol.kpos.models.CustomerAccountInfo;
import com.kaicomsol.kpos.models.CustomerInfo;
import com.kaicomsol.kpos.models.CustomerProperties;
import com.kaicomsol.kpos.presenters.CustomerInfoPresenter;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.PaginationScrollListener;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerFragment extends Fragment implements CustomerInfoView {

    private CustomerPropertiesAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private Activity activity = null;
    private CustomerInfoPresenter mPresenter;

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

        List<CustomerProperties> customerProperties = new ArrayList<>();

        customerProperties.add(new CustomerProperties(getString(R.string.customer_code), customerInfo.getCustomerCode()));
        customerProperties.add(new CustomerProperties(getString(R.string.accountNo), customerInfo.getAccountNo()));
        customerProperties.add(new CustomerProperties(getString(R.string.address_no), customerInfo.getAddress()));
        customerProperties.add(new CustomerProperties(getString(R.string.apartment_no),customerInfo.getApartment()));
        customerProperties.add(new CustomerProperties(getString(R.string.house_no), customerInfo.getHouse()));
        customerProperties.add(new CustomerProperties(getString(R.string.road_no), customerInfo.getRoad()));
        customerProperties.add(new CustomerProperties(getString(R.string.block_no), customerInfo.getBlock()));
        customerProperties.add(new CustomerProperties(getString(R.string.sector_no), customerInfo.getSector()));
        customerProperties.add(new CustomerProperties(getString(R.string.section_no), customerInfo.getSection()));
        customerProperties.add(new CustomerProperties(getString(R.string.zipCode_no), customerInfo.getZipCode()));
        customerProperties.add(new CustomerProperties(getString(R.string.metroID_no), ""+ customerInfo.getMetroId()));
        customerProperties.add(new CustomerProperties(getString(R.string.metro_no), customerInfo.getMetro()));
        customerProperties.add(new CustomerProperties(getString(R.string.zoneID_no), ""+customerInfo.getZoneId()));
        customerProperties.add(new CustomerProperties(getString(R.string.zone_no), customerInfo.getZone()));
        customerProperties.add(new CustomerProperties(getString(R.string.areaID_no),  ""+customerInfo.getAreaId()));
        customerProperties.add(new CustomerProperties(getString(R.string.area_no), customerInfo.getArea()));
        customerProperties.add(new CustomerProperties(getString(R.string.subArea_no), customerInfo.getSubArea()));
        customerProperties.add(new CustomerProperties(getString(R.string.title_no), customerInfo.getTitle()));
        customerProperties.add(new CustomerProperties(getString(R.string.postal_code), customerInfo.getPostalCode()));
        customerProperties.add(new CustomerProperties(getString(R.string.first_name), customerInfo.getFirstName()));
        customerProperties.add(new CustomerProperties(getString(R.string.last_name), customerInfo.getLastName()));
        customerProperties.add(new CustomerProperties(getString(R.string.full_name), customerInfo.getFullName()));
        customerProperties.add(new CustomerProperties(getString(R.string.email_no), customerInfo.getEmail()));
        customerProperties.add(new CustomerProperties(getString(R.string.mobile_no), customerInfo.getMobileNumber()));
        customerProperties.add(new CustomerProperties(getString(R.string.identity_no), customerInfo.getIdentityNo()));
        customerProperties.add(new CustomerProperties(getString(R.string.balance_no), ""+customerInfo.getBalance()));
        customerProperties.add(new CustomerProperties(getString(R.string.statusID_no), ""+customerInfo.getStatusId()));
        customerProperties.add(new CustomerProperties(getString(R.string.status_no), customerInfo.getStatus()));

        mAdapter = new CustomerPropertiesAdapter(getContext(), customerProperties);
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(activity);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext()));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);



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
