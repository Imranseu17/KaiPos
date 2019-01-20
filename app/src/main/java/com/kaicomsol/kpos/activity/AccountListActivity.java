package com.kaicomsol.kpos.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.adapter.CustomerAdapter;
import com.kaicomsol.kpos.callbacks.CustomerClickListener;
import com.kaicomsol.kpos.callbacks.CustomerView;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.models.Customer;
import com.kaicomsol.kpos.models.CustomerData;
import com.kaicomsol.kpos.models.Like;
import com.kaicomsol.kpos.presenters.CustomerPresenter;
import com.kaicomsol.kpos.utils.PaginationScrollListener;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AccountListActivity extends AppCompatActivity implements CustomerView, CustomerClickListener {


    private CustomerPresenter mPresenter;
    private CustomerAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private Like like = null;

    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
    private int TOTAL_PAGES = 5;
    private int currentPage = PAGE_START;

    //component bind
    @BindView(R.id.main_view)
    RelativeLayout main_view;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.recycler_list)
    RecyclerView mRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.account_list);

        ButterKnife.bind(this);
        mPresenter = new CustomerPresenter(this);

        viewConfig();

        findLikeSearch(currentPage);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void viewConfig(){
        Intent intent = getIntent();
        like = intent.getParcelableExtra("like");
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter =  new CustomerAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new PaginationScrollListener(mLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;
                findLikeSearchNext(currentPage);
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

    private void findLikeSearch(int currentPage){
        if (checkConnection()){
            showAnimation();
            String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
            mPresenter.findCustomerByProperty(token, like,currentPage);
        }else CustomAlertDialog.showError(this,getString(R.string.no_internet_connection));
    }

    private void findLikeSearchNext(int currentPage){
        if (checkConnection()){
            mAdapter.addLoadingFooter();
            String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
            mPresenter.findCustomerByProperty(token, like,currentPage);
        }else CustomAlertDialog.showError(this,getString(R.string.no_internet_connection));
    }


    @Override
    public void onSuccess(CustomerData customerData, int currentPage) {
        hideAnimation();
        if (currentPage > 1){
            isLoading = false;
            mAdapter.removeLoadingFooter();
        }
        if (customerData.getCustomerList() != null) {
            if (customerData.getCustomerList().size() > 0) {

                mAdapter.setCustomers(customerData.getCustomerList(), currentPage);
            } else showEmptyAnimation();
        }
    }

    @Override
    public void onError(String error) {
        hideAnimation();
        showEmptyAnimation();

    }

    @Override
    public void onLogout(int code) {
        SharedDataSaveLoad.remove(AccountListActivity.this, getString(R.string.preference_access_token));
        SharedDataSaveLoad.remove(AccountListActivity.this, getString(R.string.preference_is_service_check));
        Intent intent = new Intent(AccountListActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    private boolean checkConnection() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
    public void onCustomerClick(Customer customer) {

        SharedDataSaveLoad.save(this,getString(R.string.preference_account_no), customer.getAccountNo());
        SharedDataSaveLoad.save(this,getString(R.string.preference_customer_code), customer.getCustomerCode());
        SharedDataSaveLoad.save(this,getString(R.string.preference_meter_serial), customer.getMeterSerial());
        SharedDataSaveLoad.save(this,getString(R.string.preference_card_idm), customer.getCardNo());
        SharedDataSaveLoad.save(this,getString(R.string.preference_card_status), customer.getStatus());

        Intent intent = new Intent(AccountListActivity.this, CustomerDetailsActivity.class);
        startActivity(intent);
    }

    @Override
    public void retryPageLoad() {


    }
}

