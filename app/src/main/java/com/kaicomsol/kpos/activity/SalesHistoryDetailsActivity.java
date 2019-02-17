package com.kaicomsol.kpos.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.widget.TextView;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.models.Content;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SalesHistoryDetailsActivity extends AppCompatActivity {

    @BindView(R.id.card_content)
    CardView card_content;
    @BindView(R.id.txt_account_no)
    TextView txt_account_no;
    @BindView(R.id.txt_meter_serial)
    TextView txt_meterSerial;
    @BindView(R.id.txt_pos_id)
    TextView txt_posID;
    @BindView(R.id.txt_amount)
    TextView txt_amount;
    @BindView(R.id.txt_email)
    TextView txt_email;
    @BindView(R.id.txt_payment_charge)
    TextView txt_paymentCharge;
    @BindView(R.id.txt_total_amount)
    TextView txt_totalAmount;
    @BindView(R.id.txt_payment_method)
    TextView txt_paymentMethod;
    @BindView(R.id.txt_sales_date)
    TextView txt_salesDate;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_history_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Details");
        ButterKnife.bind(this);

        setData();

    }

    private void setData() {

        Content content =   getIntent().getParcelableExtra("content");

        Date date = new Date(content.getSaleDateTimeInLong());
        SimpleDateFormat targetFormat = new SimpleDateFormat("MMM-dd-yyyy hh:mm a");
        String formatDate = targetFormat.format(date);

      DecimalFormat  decimalFormat = new DecimalFormat(".##");
       double paymentCharge =  Double.parseDouble(decimalFormat.format(content.getPaymentCharge()));


        txt_account_no.setText(content.getCustomerAccNo());
        txt_meterSerial.setText(content.getMeterSerialNo());
        txt_posID.setText(content.getPosId());
        txt_amount.setText(content.getAmount()+" TK");
        txt_email.setText(content.getPosUser());
        txt_paymentCharge.setText(paymentCharge+" TK");
        txt_totalAmount.setText(content.getTotalAmount()+" TK");
        txt_paymentMethod.setText(content.getPaymentMethod());
        txt_salesDate.setText(formatDate);


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
}
