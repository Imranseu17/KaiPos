package com.kaicomsol.kpos.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.widget.TextView;

import com.kaicomsol.kpos.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SalesHistoryDetailsActivity extends AppCompatActivity {

    @BindView(R.id.card_content)
    CardView card_content;
    @BindView(R.id.txt_account_no)
    TextView accountNO;
    @BindView(R.id.txt_meter_serial)
    TextView meterSerial;
    @BindView(R.id.txt_pos_id)
    TextView posID;
    @BindView(R.id.txt_amount)
    TextView amount;
    @BindView(R.id.txt_email)
    TextView email;
    @BindView(R.id.txt_payment_charge)
    TextView paymentCharge;
    @BindView(R.id.txt_total_amount)
    TextView totalAmount;
    @BindView(R.id.txt_payment_method)
    TextView paymentMethod;
    @BindView(R.id.txt_sales_date)
    TextView salesDate;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_history_details);
        ButterKnife.bind(this);
    }
}
