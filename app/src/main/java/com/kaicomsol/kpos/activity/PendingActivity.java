package com.kaicomsol.kpos.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.adapter.PendingAdapter;
import com.kaicomsol.kpos.callbacks.CaptureView;
import com.kaicomsol.kpos.callbacks.PendingListener;
import com.kaicomsol.kpos.dbhelper.Transaction;
import com.kaicomsol.kpos.dbhelper.TransactionViewModel;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.presenters.CapturePresenter;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PendingActivity extends AppCompatActivity implements CaptureView, PendingListener {

    private CapturePresenter mPresenter;
    private ProgressDialog mProgressDialog;
    private TransactionViewModel mTransactionViewModel;
    private PendingAdapter mAdapter;
    private int paymentId = 0;
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
        setContentView(R.layout.activity_pending);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Pending");

        ButterKnife.bind(this);

        viewConfig();



    }

    private void viewConfig() {

        mProgressDialog = new ProgressDialog(PendingActivity.this);
        // Get a new or existing ViewModel from the ViewModelProvider.
        mTransactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        mTransactionViewModel.getAllTransaction().observe(this, new Observer<List<Transaction>>() {
            @Override
            public void onChanged(@Nullable List<Transaction> transactionList) {
                mAdapter = new PendingAdapter(transactionList, PendingActivity.this);
                mRecyclerView.setLayoutManager(new LinearLayoutManager(PendingActivity.this));
                mRecyclerView.setAdapter(mAdapter);

            }
        });
        mPresenter = new CapturePresenter(this);


    }

    private void capturePayment(String paymentId) {
        if (checkConnection()) {
            showLoading("Capturing...");
            String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
            mPresenter.capturePayment(token, paymentId);
        } else {
            SharedDataSaveLoad.save(this, getString(R.string.preference_capture_failed), true);
        }

    }

    private void showLoading(String msg) {
        mProgressDialog.setTitle(msg);
        mProgressDialog.setCancelable(false);
        if (!((Activity) this).isFinishing()) {
            mProgressDialog.show();
        }
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


    @Override
    public void onCaptureSuccess(int paymentId) {
        if (mProgressDialog != null)  mProgressDialog.dismiss();
        mTransactionViewModel.deleteByPaymentId(paymentId);
        mAdapter.notifyDataSetChanged();
        CustomAlertDialog.showSuccess(this, "Captured successfully!");
    }

    @Override
    public void onError(String error, int code) {
       if (mProgressDialog != null)  mProgressDialog.dismiss();
        if (error != null) CustomAlertDialog.showError(this, error);
        if(code == 455 && paymentId > 0 ){
            mTransactionViewModel.deleteByPaymentId(paymentId);
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onLogout(int code) {
        SharedDataSaveLoad.remove(PendingActivity.this, getString(R.string.preference_access_token));
        SharedDataSaveLoad.remove(PendingActivity.this, getString(R.string.preference_is_service_check));
        Intent intent = new Intent(PendingActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean checkConnection() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onClick(int paymentId) {
           this.paymentId = paymentId;
           capturePayment(paymentId+"");
    }
}
