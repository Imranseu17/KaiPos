package com.kaicomsol.kpos.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.models.Like;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AccountSearchActivity extends AppCompatActivity {

    //input layout bind
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.layout_main)
    LinearLayout layout_main;
    @BindView(R.id.layout_content)
    LinearLayout layout_content;
    @BindView(R.id.input_layout_account)
    TextInputLayout input_layout_account;
    @BindView(R.id.input_layout_code)
    TextInputLayout input_layout_code;
    @BindView(R.id.input_layout_zone)
    TextInputLayout input_layout_zone;
    @BindView(R.id.input_layout_address)
    TextInputLayout input_layout_address;
    @BindView(R.id.input_layout_cardno)
    TextInputLayout input_layout_cardno;
    @BindView(R.id.input_layout_phone)
    TextInputLayout input_layout_phone;
    @BindView(R.id.input_layout_apartment)
    TextInputLayout input_layout_apartment;
    @BindView(R.id.input_layout_area)
    TextInputLayout input_layout_area;
    @BindView(R.id.input_layout_meter)
    TextInputLayout input_layout_meter;

    //edit text bind
    @BindView(R.id.edt_account)
    TextInputEditText edt_account;
    @BindView(R.id.edt_code)
    TextInputEditText edt_code;
    @BindView(R.id.edt_zone)
    TextInputEditText edt_zone;
    @BindView(R.id.edt_address)
    TextInputEditText edt_address;
    @BindView(R.id.edt_apartment)
    TextInputEditText edt_apartment;
    @BindView(R.id.edt_area)
    TextInputEditText edt_area;
    @BindView(R.id.edt_meter)
    TextInputEditText edt_meter;

    @BindView(R.id.edt_cardno)
    TextInputEditText edt_cardno;
    @BindView(R.id.edt_phone)
    TextInputEditText edt_phone;

    //button component bind
    @BindView(R.id.btn_search)
    Button btn_search;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_search);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.search);

        ButterKnife.bind(this);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              likeSearch();

            }
        });


    }

    private void likeSearch(){
        String account = edt_account.getText().toString().trim();
        String customerCode = edt_code.getText().toString().trim();
        String meterSerial = edt_meter.getText().toString().trim();
        String cardNo = edt_cardno.getText().toString().trim();
        String phone = edt_phone.getText().toString().trim();
        String zone = edt_zone.getText().toString().trim();
        String address = edt_address.getText().toString().trim();
        String apartment = edt_apartment.getText().toString().trim();
        String area = edt_area.getText().toString().trim();

        Like like = new Like();
        like.account = !TextUtils.isEmpty(account) ? "like:"+account : "";
        like.customerCode = !TextUtils.isEmpty(customerCode) ? "like:"+customerCode : "";
        like.meterSerial = !TextUtils.isEmpty(meterSerial) ? "like:"+meterSerial : "";
        like.cardNo = !TextUtils.isEmpty(cardNo) ? "like:"+cardNo : "";
        like.phone = !TextUtils.isEmpty(phone) ? "like:"+phone : "";
        like.zone = !TextUtils.isEmpty(zone) ? "like:"+zone : "";
        like.address = !TextUtils.isEmpty(address) ? "like:"+address : "";
        like.apartment = !TextUtils.isEmpty(apartment) ? "like:"+apartment : "";
        like.area = !TextUtils.isEmpty(area) ? "like:"+area : "";

        Intent intent = new Intent(AccountSearchActivity.this, AccountListActivity.class);
        intent.putExtra("like", like);
        startActivity(intent);
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_search:
                likeSearch();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean validateCode() {
        if (edt_code.getText().toString().trim().isEmpty()) {
            input_layout_code.setError(getString(R.string.err_msg_required));
            requestFocus(edt_code);
            return false;
        } else {
            input_layout_code.setErrorEnabled(false);
        }

        return true;
    }


    private boolean validateZone() {
        if (edt_zone.getText().toString().trim().isEmpty()) {
            input_layout_zone.setError(getString(R.string.err_msg_required));
            requestFocus(edt_zone);
            return false;
        } else {
            input_layout_zone.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateAddress() {
        if (edt_address.getText().toString().trim().isEmpty()) {
            input_layout_address.setError(getString(R.string.err_msg_required));
            requestFocus(edt_address);
            return false;
        } else {
            input_layout_address.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateApartment() {
        if (edt_apartment.getText().toString().trim().isEmpty()) {
            input_layout_apartment.setError(getString(R.string.err_msg_required));
            requestFocus(edt_apartment);
            return false;
        } else {
            input_layout_apartment.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateArea() {
        if (edt_area.getText().toString().trim().isEmpty()) {
            input_layout_area.setError(getString(R.string.err_msg_required));
            requestFocus(edt_area);
            return false;
        } else {
            input_layout_area.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateMeter() {
        if (edt_meter.getText().toString().trim().isEmpty()) {
            input_layout_meter.setError(getString(R.string.err_msg_required));
            requestFocus(edt_meter);
            return false;
        } else {
            input_layout_meter.setErrorEnabled(false);
        }

        return true;
    }


    private static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && Patterns.PHONE.matcher(phone).matches();
    }


    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


}

