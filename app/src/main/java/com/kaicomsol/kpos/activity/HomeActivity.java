package com.kaicomsol.kpos.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;


import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.adapter.PendingAdapter;
import com.kaicomsol.kpos.dialogs.ChooseAlertDialog;
import com.kaicomsol.kpos.dialogs.PromptDialog;
import com.kaicomsol.kpos.utils.CountDrawable;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;
import com.kaicomsol.kpos.utils.WaterMarkBg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends AppCompatActivity {

    private TransactionViewModel mTransactionViewModel;
    public static HomeActivity home;
    private Menu defaultMenu;

    @BindView(R.id.layout_main)
    LinearLayout layout_main;
    @BindView(R.id.card_gas)
    CardView cardGas;
    @BindView(R.id.line_gas)
    View line_gas;
    @BindView(R.id.card_account)
    CardView cardAccount;
    @BindView(R.id.line_account)
    View line_account;
    @BindView(R.id.card_inspect)
    CardView cardInspect;
    @BindView(R.id.line_inspect)
    View line_inspect;
    @BindView(R.id.card_refund)
    CardView cardRefund;
    @BindView(R.id.line_refund)
    View line_refund;
    @BindView(R.id.card_history)
    CardView cardHistory;
    @BindView(R.id.line_history)
    View line_history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.titlle_dashboard);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);

        ButterKnife.bind(this);
        home = this;
        //configView();


        cardGas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                line_gas.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.green));
                line_account.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_inspect.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_refund.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_history.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));

                Intent intent = new Intent(HomeActivity.this, RechargeActivity.class);
                startActivity(intent);
            }
        });

        cardAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                line_gas.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_account.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.green));
                line_inspect.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_refund.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_history.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));

                Intent intent = new Intent(HomeActivity.this, AccountSearchActivity.class);
                intent.putExtra("path", "account");
                startActivity(intent);
            }
        });

        cardInspect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                line_gas.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_account.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_inspect.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.green));
                line_refund.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_history.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));

                Intent intent = new Intent(HomeActivity.this, InspectActivity.class);
                startActivity(intent);
            }
        });

        cardRefund.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                line_gas.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_account.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_inspect.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_refund.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.green));
                line_history.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));

                Intent intent = new Intent(HomeActivity.this, RefundActivity.class);
                startActivity(intent);
            }
        });

        cardHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                line_gas.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_account.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_inspect.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_refund.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.black_overlay));
                line_history.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.green));


                Intent intent = new Intent(HomeActivity.this, SalesHistoryActivity.class);
                intent.putExtra("path", "history");
                startActivity(intent);
            }
        });

    }

    private void configView() {
        SimpleDateFormat createTimeSdf1 = new SimpleDateFormat("yyyy-MM-dd");

        List<String> labels = new ArrayList<>();
        labels.add("KGDCL");
        labels.add("Date：" + createTimeSdf1.format(new Date()));
        labels.add("Copyright by KGDCL");
        layout_main.setBackground(new WaterMarkBg(HomeActivity.this, labels, -15, 13));
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        defaultMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_setting:
                startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_pending:
                startActivity(new Intent(HomeActivity.this, PendingActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Get a new or existing ViewModel from the ViewModelProvider.
        mTransactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        mTransactionViewModel.getAllTransaction().observe(this, new Observer<List<Transaction>>() {
            @Override
            public void onChanged(@Nullable List<Transaction> transactionList) {
                //if (transactionList != null) setCount(HomeActivity.this, transactionList.size()+"");
                setCount(HomeActivity.this, "3");
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    public void setCount(Context context, String count) {
        MenuItem menuItem = defaultMenu.findItem(R.id.action_pending);
        LayerDrawable icon = (LayerDrawable) menuItem.getIcon();

        CountDrawable badge;

        // Reuse drawable if possible
        Drawable reuse = icon.findDrawableByLayerId(R.id.ic_pending_count);
        if (reuse != null && reuse instanceof CountDrawable) {
            badge = (CountDrawable) reuse;
        } else {
            badge = new CountDrawable(context);
        }

        badge.setCount(count);
        icon.mutate();
        icon.setDrawableByLayerId(R.id.ic_pending_count, badge);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        showExitDialog();
    }

    public void showExitDialog() {
        new ChooseAlertDialog(this)
                .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                .setAnimationEnable(true)
                .setTitleText(R.string.warning)
                .setContentText(R.string.warning_exit_app)
                .setNegativeListener(getString(R.string.no), new ChooseAlertDialog.OnNegativeListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .setPositiveListener(getString(R.string.yes), new ChooseAlertDialog.OnPositiveListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        SharedDataSaveLoad.remove(HomeActivity.this, getString(R.string.preference_is_service_check));
                        SharedDataSaveLoad.remove(HomeActivity.this, getString(R.string.preference_access_token));
                        dialog.dismiss();
                        finish();

                    }
                }).show();
    }


}
