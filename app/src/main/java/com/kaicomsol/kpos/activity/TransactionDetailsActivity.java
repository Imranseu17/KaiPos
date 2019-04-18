package com.kaicomsol.kpos.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.widget.TextView;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.golobal.Constants;
import com.kaicomsol.kpos.models.TransactionModel;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TransactionDetailsActivity extends AppCompatActivity {

    @BindView(R.id.card_content)
    CardView card_content;
    @BindView(R.id.txt_id)
    TextView txt_id;
    @BindView(R.id.txt_invoice_type)
    TextView txt_invoice_type;
    @BindView(R.id.txt_quantity)
    TextView txt_quantity;
    @BindView(R.id.txt_percentage)
    TextView txt_percentage;
    @BindView(R.id.txt_description)
    TextView txt_description;
    @BindView(R.id.txt_price)
    TextView txt_price;
    @BindView(R.id.txt_amount)
    TextView txt_amount;
    @BindView(R.id.txt_payment_date)
    TextView txt_payment_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detais);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Details");
        ButterKnife.bind(this);

        setData();
    }


    private void setData() {



        TransactionModel transaction = getIntent().getParcelableExtra("transactionModel");

        Date date = new Date(transaction.getPaymentDate());
        SimpleDateFormat targetFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
        String formatDate = targetFormat.format(date);

        DecimalFormat decimalFormat = new DecimalFormat(".##");
        double quentity =  Double.parseDouble(decimalFormat.format(transaction.getQuantity()));
        double price =  Double.parseDouble(decimalFormat.format(transaction.getPrice()));


        txt_id.setText(""+transaction.getId());
        txt_invoice_type.setText(transaction.getInvoiceType());
        txt_quantity.setText(quentity+" TK");
        txt_amount.setText(transaction.getAmount()+" TK");
        txt_price.setText(price+" TK");
        txt_description.setText(transaction.getDescription());
        txt_percentage.setText(""+transaction.isPercentage());
        txt_payment_date.setText(formatDate);


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
