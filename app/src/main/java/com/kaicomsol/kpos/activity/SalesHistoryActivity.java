package com.kaicomsol.kpos.activity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.adapter.SalesHistoryAdapter;
import com.kaicomsol.kpos.callbacks.HistoryClickListener;
import com.kaicomsol.kpos.callbacks.HistoryView;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.model.Content;
import com.kaicomsol.kpos.model.SalesHistory;
import com.kaicomsol.kpos.presenters.HistoryPresenter;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SalesHistoryActivity extends AppCompatActivity implements HistoryView, HistoryClickListener {


    //date picker dialog
    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;
    private SimpleDateFormat dateFormatter;
    private String startDate = "";
    private String endDate = "";

    //network request
    private HistoryPresenter mPresenter;
    private SalesHistoryAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;

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
    @BindView(R.id.recycler_list)
    RecyclerView mRecyclerView;
    //


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

        getSalesHistory();

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSalesHistory();
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

        String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        String currentDateFormatted = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());

        startDate = currentDate;
        endDate = currentDate;

        edt_start_date.setText(currentDateFormatted);
        edt_end_date.setText(currentDateFormatted);

        dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        setDatePickerField();

        edt_start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromDatePickerDialog.show();
            }
        });
        edt_end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toDatePickerDialog.show();
            }
        });
    }

    private void getSalesHistory(){
        if (checkConnection()){
            showAnimation();
            String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));

            mPresenter.getSalesHistory(token,startDate,endDate);
        }else  CustomAlertDialog.showError(this, getString(R.string.no_internet_connection));
    }


    @Override
    public void onSuccess(SalesHistory salesHistory) {

        hideAnimation();
        if (salesHistory.getContentList() != null) {
            if (salesHistory.getContentList().size() > 0) {
                mAdapter =  new SalesHistoryAdapter(this,salesHistory.getContentList(), this);
                mRecyclerView.setAdapter(mAdapter);
            }else showEmptyAnimation();
        }

    }

    @Override
    public void onError(String error) {
        hideAnimation();
        showEmptyAnimation();

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
    public void onHistoryClick(Content content) {

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
