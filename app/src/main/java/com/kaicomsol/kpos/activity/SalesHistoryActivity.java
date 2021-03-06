package com.kaicomsol.kpos.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Parcelable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.adapter.SalesHistoryAdapter;
import com.kaicomsol.kpos.callbacks.HistoryClickListener;
import com.kaicomsol.kpos.callbacks.HistoryView;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.golobal.Constants;
import com.kaicomsol.kpos.models.Content;
import com.kaicomsol.kpos.models.SalesHistory;
import com.kaicomsol.kpos.presenters.HistoryPresenter;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.PaginationScrollListener;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SalesHistoryActivity extends AppCompatActivity implements HistoryView, HistoryClickListener {


    //date picker dialog
    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;
    private SimpleDateFormat dateFormatter;
    private DecimalFormat decimalFormat;
    private String startDate = "";
    private String endDate = "";

    //network request
    private HistoryPresenter mPresenter;
    private SalesHistoryAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
    private int TOTAL_PAGES = 5;
    private int MY_TOTAL_PAGE = 0;
    private int currentPage = PAGE_START;

    //component bind
    @BindView(R.id.main_view)
    RelativeLayout main_view;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.input_layout_start_date)
    TextInputLayout input_layout_start_date;
    @BindView(R.id.input_layout_end_date)
    TextInputLayout input_layout_end_date;
    @BindView(R.id.edt_start_date)
    TextInputEditText edt_start_date;
    @BindView(R.id.edt_end_date)
    TextInputEditText edt_end_date;
    @BindView(R.id.btn_search)
    ImageView btn_search;
    @BindView(R.id.line)
    View line;
    @BindView(R.id.recycler_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.amount_layout)
    RelativeLayout amount_layout;
    @BindView(R.id.total_amount)
    TextView total_amout;

    double totalAmount = 0.0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_history);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.title_sales_history);

        ButterKnife.bind(this);
        viewConfig();
        mPresenter = new HistoryPresenter(this);

        getSalesHistory(currentPage);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                totalAmount = 0.0;
                getSalesHistory(currentPage);
            }
        });



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

        final String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String currentDateFormatted = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(new Date());

        startDate = currentDate;
        endDate = currentDate;

        edt_start_date.setText(currentDateFormatted);
        edt_end_date.setText(currentDateFormatted);

        decimalFormat = new DecimalFormat(".##");

        dateFormatter = new SimpleDateFormat(Constants.DATE_FORMAT);
        mRecyclerView.setHasFixedSize(false);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter =  new SalesHistoryAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);
        setDatePickerField();

        edt_start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                fromDatePickerDialog.show();
            }
        });
        edt_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                toDatePickerDialog.show();
            }
        });

        mRecyclerView.addOnScrollListener(new PaginationScrollListener(mLayoutManager) {
            @Override
            protected void loadMoreItems() {
                if (MY_TOTAL_PAGE > currentPage){
                    isLoading = true;
                    currentPage += 1;
                    getSalesHistoryNext(currentPage);
                }
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

    private void getSalesHistory(int currentPage){
        if (checkConnection()){
            showAnimation();
            String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
            mPresenter.getSalesHistory(token, currentPage, startDate, endDate);
        }else  CustomAlertDialog.showError(this,getString(R.string.no_internet_connection));
    }

    private void getSalesHistoryNext(int currentPage){
        if (checkConnection()){
            mAdapter.addLoadingFooter();
            String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
            mPresenter.getSalesHistory(token, currentPage, startDate, endDate);
        }else  CustomAlertDialog.showError(this,getString(R.string.no_internet_connection));
    }




    @Override
    public void onSuccess(SalesHistory salesHistory, int currentPage) {

       List<Content> contentList = salesHistory.getContentList();
       for (Content content : contentList){
          totalAmount += content.getAmount();
       }
       if (totalAmount > 0.0) amount_layout.setVisibility(View.VISIBLE);
       else amount_layout.setVisibility(View.GONE);
       total_amout.setText("Total Amount: " +decimalFormat.format(totalAmount)+ " TK");

        hideAnimation();
        if (salesHistory != null) MY_TOTAL_PAGE = salesHistory.getTotalPages();
        if (currentPage > 1){
            isLoading = false;
            mAdapter.removeLoadingFooter();
        }
        if (salesHistory.getContentList() != null) {
            if (salesHistory.getContentList().size() > 0) {
                line.setVisibility(View.VISIBLE);
                mAdapter.setHistory(salesHistory.getContentList(), currentPage);
            }else {
                if (mAdapter != null ) mAdapter.clear();
                CustomAlertDialog.showError(this,"TransactionModel not found");
            }

                //showEmptyAnimation();
        }

    }

    @Override
    public void onError(String error) {

        hideAnimation();
        if (currentPage == 1){
            CustomAlertDialog.showError(this,"TransactionModel not found");
        }else {
            isLoading = false;
            mAdapter.removeLoadingFooter();
        }
    }

    @Override
    public void onLogout(int code) {
        SharedDataSaveLoad.remove(SalesHistoryActivity.this, getString(R.string.preference_access_token));
        SharedDataSaveLoad.remove(SalesHistoryActivity.this, getString(R.string.preference_is_service_check));
        Intent intent = new Intent(SalesHistoryActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean checkConnection() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void showAnimation() {
        line.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("animation_loading.json");
        animationView.playAnimation();
        animationView.loop(true);
    }

    public void showEmptyAnimation() {
        line.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("empty_box.json");
        animationView.playAnimation();
        animationView.loop(false);
    }

    public void hideAnimation() {
        line.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        if (animationView.isAnimating()) animationView.cancelAnimation();
        animationView.setVisibility(View.GONE);
    }


    @Override
    public void onHistoryClick(Content content) {
        Intent intent = new Intent(SalesHistoryActivity.this, SalesHistoryDetailsActivity.class) ;
             intent.putExtra("content",content);
             startActivity(intent);


    }

    @Override
    public void retryPageLoad() {
          DebugLog.e("retryPageLoad()");
    }

    private void setDatePickerField() {

        Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                String monthString = String.valueOf(monthOfYear+1);
                if (monthString.length() == 1) {
                    monthString = "0" + monthString;
                }
                String dayString = String.valueOf(dayOfMonth);
                if (dayString.length() == 1) {
                    dayString = "0" + dayString;
                }
                startDate = year+monthString+dayString;
                edt_start_date.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        toDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                String monthString = String.valueOf(monthOfYear+1);
                if (monthString.length() == 1) {
                    monthString = "0" + monthString;
                }
                String dayString = String.valueOf(dayOfMonth);
                if (dayString.length() == 1) {
                    dayString = "0" + dayString;
                }
                endDate = year+monthString+dayString;
                edt_end_date.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }
}
