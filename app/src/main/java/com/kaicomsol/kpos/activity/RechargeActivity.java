package com.kaicomsol.kpos.activity;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.provider.Settings;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.Gson;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.adapter.PrintAdapter;
import com.kaicomsol.kpos.callbacks.CloseClickListener;
import com.kaicomsol.kpos.callbacks.PaymentView;
import com.kaicomsol.kpos.dialogs.CardCheckDialog;
import com.kaicomsol.kpos.dialogs.ChooseAlertDialog;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.dialogs.PromptDialog;
import com.kaicomsol.kpos.dialogs.RechargeCardDialog;
import com.kaicomsol.kpos.fragment.InvoiceFragment;
import com.kaicomsol.kpos.models.Invoices;
import com.kaicomsol.kpos.models.Item;
import com.kaicomsol.kpos.models.Items;
import com.kaicomsol.kpos.models.Payment;
import com.kaicomsol.kpos.models.ReadCard;
import com.kaicomsol.kpos.nfcfelica.HttpResponsAsync;
import com.kaicomsol.kpos.presenters.PaymentPresenter;
import com.kaicomsol.kpos.printer.BluetoothPrinter;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.PrinterCommands;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;
import com.kaicomsol.kpos.utils.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class RechargeActivity extends AppCompatActivity implements PaymentView,CloseClickListener {

    private static final int REQUEST_ENABLE_BT = 0;
    private CardCheckDialog mCardCheckDialog = null;
    private RechargeCardDialog mRechargeCardDialog = null;
    private boolean isRecharge = false;
    private DecimalFormat decimalFormat;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private ReadCard readCard;
    private Tag tag;
    private PaymentPresenter mPresenter;
    private double totalAmount = 0.0;
    private static OutputStream outputStream;

    //Bind component
    @BindView(R.id.layout_main) LinearLayout layout_main;
    @BindView(R.id.layout_loading) LinearLayout layout_loading;
    @BindView(R.id.animation_view) LottieAnimationView animationView;
    @BindView(R.id.gas_content) ScrollView gas_content;
    @BindView(R.id.txt_account_no) TextView txt_account_no;
    @BindView(R.id.layout_price) LinearLayout layoutPrice;
    @BindView(R.id.txt_price) TextView txt_price;
    @BindView(R.id.txt_taka) TextView txt_taka;
    @BindView(R.id.txt_gas) TextView txt_gas;
    @BindView(R.id.input_layout_amount) TextInputLayout inputLayoutAmount;
    @BindView(R.id.edt_amount) TextInputEditText edtAmount;
    @BindView(R.id.txt_total_amount) TextView txt_total_amount;
    @BindView(R.id.btn_submit) Button btn_submit;


    //print component bind
    @BindView(R.id.layout_print) RelativeLayout layout_print;
    @BindView(R.id.recycler_list) RecyclerView mRecyclerView;
    @BindView(R.id.txt_date_time) TextView txt_date_time;
    @BindView(R.id.txt_transaction_no) TextView txt_transaction_no;
    @BindView(R.id.txt_customer_code) TextView txt_customer_code;
    @BindView(R.id.txt_prepaid_no) TextView txt_prepaid_no;
    @BindView(R.id.txt_meter_no) TextView txt_meter_no;
    @BindView(R.id.txt_card_no) TextView txt_card_no;
    @BindView(R.id.txt_pos_id) TextView txt_pos_id;
    @BindView(R.id.txt_operator_name) TextView txt_operator_name;
    @BindView(R.id.txt_deposit_amount) TextView txt_deposit_amount;
    @BindView(R.id.btn_print) Button btn_print;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.add_gas);

        ButterKnife.bind(this);
        //view init
        viewConfig();
        //card configuration
        cardConfig();


    }

    @Override
    protected void onResume() {
        super.onResume();
        //show card dialog
        mAdapter.enableForegroundDispatch(RechargeActivity.this,pendingIntent, intentFiltersArray, techListsArray);
    }

    private void getInvoices(String cardNo){

        String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
        if (checkConnection()) {
            showAnimation();
            mPresenter.getInvoices(token,cardNo);
            readCard(token, readCard.readCardArgument);
        }
    }

    private void readCard(String token, HttpResponsAsync.ReadCardArgument argument){
        if (readCard.readCardArgument != null) mPresenter.readCard(token, argument);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        readCard.ReadTag(tag);
        final boolean response = readCard.SetReadCardData(tag, readCard.webAPI, readCard.readCardArgument);
        if (response){
            if(readCard.readCardArgument.CardGroup.equals("77")
                    && (readCard.readCardArgument.CardStatus.equals("06")
                    || readCard.readCardArgument.CardStatus.equals("30"))){

                txt_account_no.setText(readCard.readCardArgument.CustomerId);
                DebugLog.e(readCard.readCardArgument.CustomerId);
                customerCardDismiss();
                if (isRecharge) gasRecharge();
                else getInvoices(readCard.readCardArgument.CardIdm);

            }else CustomAlertDialog.showError(this, getString(R.string.err_card_not_valid));
        }else CustomAlertDialog.showWarning(this, getString(R.string.err_card_read_failed));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    private void viewConfig() {

        //card check dialog
        mCardCheckDialog = CardCheckDialog.newInstance(this,"User");
        mCardCheckDialog.setCancelable(false);
        mRechargeCardDialog = new RechargeCardDialog();
        Bundle args = new Bundle();
        args.putString("msg", "Payment successfully");
        mRechargeCardDialog.setArguments(args);
        //decimalFormat = new DecimalFormat(".##");
        decimalFormat = new DecimalFormat("#,##0.00");
        mPresenter = new PaymentPresenter(this);
        layoutPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAmount();
            }
        });
        edtAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                takaToGas(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

                // TODO Auto-generated method stub
            }
        });

        customerCardDialog();

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = txt_total_amount.getText().toString().trim();
                if (totalAmount < Double.parseDouble(amount)) showConfirmDialog();
                else showErrorDialog("Amount is less then unpaid amount plus payment charge");
            }
        });
        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmPrintAlert();
            }
        });
    }

    private void gasRecharge(){
        if (checkConnection()) {
            addPayment();
        }else CustomAlertDialog.showError(this, getString(R.string.no_internet_connection));
    }

    private void cardConfig() {

        readCard = new ReadCard();
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndef.addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        intentFiltersArray = new IntentFilter[]{ndef};


        techListsArray = new String[][]{
                new String[]{NfcF.class.getName()}
        };
        mAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

    }


    private void customerCardDialog(){
        if (mCardCheckDialog != null){
            if(!mCardCheckDialog.isAdded()) {
                mCardCheckDialog.show(getSupportFragmentManager(), mCardCheckDialog.getTag());
            }
        }
    }

    private void rechargeCardDialog(){
        if (mRechargeCardDialog != null){
            if(!mRechargeCardDialog.isAdded()) {
                mRechargeCardDialog.show(getSupportFragmentManager(), mRechargeCardDialog.getTag());
            }
        }
    }


    private void customerCardDismiss(){
        if (mCardCheckDialog != null){
            mCardCheckDialog.dismiss();
        }
    }

    private void rechargeCardDismiss(){
        if (mRechargeCardDialog != null){
            mRechargeCardDialog.dismiss();
            isRecharge = false;
        }
    }

    private void showAmount(){
        final CharSequence[] items = {"100 TK","200 TK","300 TK","400 TK","500 TK","Manual"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_select_amount);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String amount = items[item].toString();
                if (amount.equals("Manual")) {
                    expand(inputLayoutAmount);
                } else {
                    String value = amount.replace(" TK", "");
                    txt_price.setText(value);
                    takaToGas(value);
                    collapse(inputLayoutAmount);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals("Manual")) {
            expand(inputLayoutAmount);
        } else {
            String amount = item.getTitle().toString().replace(" TK", "");
            txt_price.setText(amount);
            takaToGas(amount);
            collapse(inputLayoutAmount);
        }
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void takaToGas(String amount) {
        if (!TextUtils.isEmpty(amount)){
            double value = Double.parseDouble(amount) / 9.1;
            txt_taka.setText(amount);
            txt_price.setText(amount);
            txt_total_amount.setText(amount);
            txt_gas.setText(String.valueOf(decimalFormat.format(value)));
        }
    }

    private void addPayment(){
        String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
        String amount = txt_total_amount.getText().toString().trim();
        String cardIdm = readCard.readCardArgument.CardIdm;
        String cardHistoryNo = readCard.readCardArgument.CardHistoryNo;
        final String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID);
        double value = Double.parseDouble(amount) / 9.1;

        DebugLog.i(decimalFormat.format(value));


        mPresenter.addPayment(token,amount,cardIdm,cardHistoryNo,"1");

    }

    private void capturePayment(String paymentId){
        String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
        mPresenter.capturePayment(token, paymentId);

    }

    @Override
    public void onSuccess(Payment payment) {

            capturePayment(String.valueOf(payment.getPaymentId()));
            rechargeCardDismiss();
            String amount = txt_total_amount.getText().toString().trim();
            double value = payment.getReceipt().getGasUnit();
            readCard.GasChargeCard(tag, value,
                0, 0, payment.getEmergencyValue(), payment.getReceipt().getMeterSerialNo());
            //print(payment.getNewHistoryNo(), amount, value);
            print(payment);

    }

    @Override
    public void onSuccess(Invoices invoices) {
        mAdapter.disableForegroundDispatch(this);
        hideAnimation();
        if (invoices != null){
            if (invoices.getInvoices() != null && invoices.getInvoices().size() > 0){
               String invoiceList = new Gson().toJson(invoices);
                InvoiceFragment invoiceFragment = InvoiceFragment.newInstance(this);
                invoiceFragment.setCancelable(false);
                Bundle bundle = new Bundle();
                bundle.putString("invoice",invoiceList);
                invoiceFragment.setArguments(bundle);
                invoiceFragment.show(getSupportFragmentManager(), invoiceFragment.getTag());
            }
        }

    }

    @Override
    public void onSuccess(int paymentId) {


    }

    @Override
    public void onSuccess(String readCard) {
        mAdapter.disableForegroundDispatch(this);

    }


    @Override
    public void onError(String error, int code) {
        switch (code){
            case 100:
                if (error != null) CustomAlertDialog.showError(this, error);
                break;
            case 200:
                hideAnimation();
                break;
            case 201:
               // hideAnimation();
                break;
            case 300:
                break;
            case 406:
                if (error != null) CustomAlertDialog.showError(this, error);
                break;
            default:
                if (error != null) CustomAlertDialog.showError(this, error);
                break;
        }

    }

    @Override
    public void onLogout(int code) {
        SharedDataSaveLoad.remove(RechargeActivity.this, getString(R.string.preference_access_token));
        SharedDataSaveLoad.remove(RechargeActivity.this, getString(R.string.preference_is_service_check));
        Intent intent = new Intent(RechargeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean checkConnection() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public static void expand(final View v) {
        v.measure(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayoutCompat.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    private void showPrintLayout(Payment payment){
        layout_loading.setVisibility(View.GONE);
        gas_content.setVisibility(View.GONE);
        layout_print.setVisibility(View.VISIBLE);

        Date date = new Date(payment.getReceipt().getPaymentDate());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        txt_date_time.setText(dateFormat.format(date));
        txt_transaction_no.setText(String.valueOf(payment.getPaymentId()));
        txt_customer_code.setText(readCard.readCardArgument.CustomerId);
        txt_meter_no.setText(payment.getReceipt().getMeterSerialNo());
        txt_card_no.setText(payment.getReceipt().getCardNo());
        txt_pos_id.setText(String.valueOf(payment.getReceipt().getPosId()));
        txt_operator_name.setText(String.valueOf(payment.getReceipt().getOperatorName()));
        txt_deposit_amount.setText(String.valueOf(payment.getReceipt().getAmountPaid()));

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        PrintAdapter mAdapter = new PrintAdapter(payment.getReceipt().getItems().getItems());
        mRecyclerView.setAdapter(mAdapter);
    }


    private void print(Payment payment) {


        readCard.WriteStatus(tag, payment.getNewHistoryNo());
        bluetoothPrint(payment);

        getSupportActionBar().setTitle("Print receipts");
        showPrintLayout(payment);
    }

    private void bluetoothPrint(final Payment payment){


        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            CustomAlertDialog.showError(this,getString(R.string.bluetooth_printer_not_support));
        } else {
            if (mBluetoothAdapter.isEnabled()) {

                final BluetoothDevice mBtDevice = mBluetoothAdapter.getBondedDevices().iterator().next();
                final BluetoothPrinter mPrinter = new BluetoothPrinter(mBtDevice);


                mPrinter.connectPrinter(new BluetoothPrinter.PrinterConnectListener() {

                    @Override
                    public void onConnected() {

                        try {
                            if (mPrinter.isConnected()) {
                                outputStream = mPrinter.getSocket().getOutputStream();
                                byte[] printformat = new byte[]{0x1B,0x21,0x03};
                                outputStream.write(printformat);
                                printCustom("Money Receipt",3,1);

                                Date date = new Date(payment.getReceipt().getPaymentDate());
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                                printCustom(new String(new char[42]).replace("\0", "-"),0,1);
                                printCustom(getFormatStringByLength("Date and Time.", dateFormat.format(date)),0,0);
                                printCustom(getFormatStringByLength("Transaction No.",String.valueOf(payment.getPaymentId())),0,1);
                                printCustom(getFormatStringByLength("Customer Code",readCard.readCardArgument.CustomerId),0,0);
                                printCustom(getFormatStringByLength("Meter No.",payment.getReceipt().getMeterSerialNo()),0,1);
                                printCustom(getFormatStringByLength("Card No.",payment.getReceipt().getCardNo()),0,0);
                                printCustom(getFormatStringByLength("POS ID",String.valueOf(payment.getReceipt().getPosId())),0,0);
                                printCustom(getFormatStringByLength("Operator Name",payment.getReceipt().getOperatorName()),0,0);
                                printCustom(new String(new char[42]).replace("\0", "-"),0,0);
                                printCustom(getFormatStringByLength("Deposit Amount(TK)",String.valueOf(payment.getReceipt().getAmountPaid())),0,0);
                                printCustom(new String(new char[42]).replace("\0", "-"),0,0);
                                printCustom(getFormatStringByItem("Item","Price","Qty","Amount"),0,0);
                                printCustom(new String(new char[42]).replace("\0", "-"),0,1);
                                for (Item item : payment.getReceipt().getItems().getItems()){
                                    printCustom(getFormatStringByItem(item.getName(),String.valueOf(item.getPrice()),String.valueOf(item.getQuantity()),String.valueOf(decimalFormat.format(item.getTotal()))),0,0);
                                }
                                printCustom(new String(new char[42]).replace("\0", "-"),0,0);
                                printCustom(getFormatStringByTotal("Total:",String.valueOf(decimalFormat.format(payment.getReceipt().getItems().getTotal()))),0,0);
                                printCustom(new String(new char[42]).replace("\0", "."),0,1);
                                printCustom("Customer Support ("+readCard.readCardArgument.CustomerId+")",0,1);
                                printCustom("Karnaphuli Gas Distribution Company Ltd.",0,1);
                                printNewLine();
                                printNewLine();
                                printNewLine();


                            }else DebugLog.e("NOT CONNECTED");
                           try {
                                Thread.sleep(1000);
                                mPrinter.finish();
                                outputStream.flush();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }


                        } catch (Exception e) {

                            e.printStackTrace();

                        }


                    }

                    @Override
                    public void onFailed() {
                        //this callback may be running thread so
                        DebugLog.e("Print Error!");
                    }

                });

            }else showEnableBluetoothDialog();
        }

    }



    public void showConfirmDialog() {
        new ChooseAlertDialog(this)
                .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                .setAnimationEnable(true)
                .setTitleText(getString(R.string.confirmation))
                .setContentText(getString(R.string.confirm_recharge_gas))
                .setNegativeListener(getString(R.string.yes), new ChooseAlertDialog.OnNegativeListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();
                        mAdapter.enableForegroundDispatch(RechargeActivity.this, pendingIntent, intentFiltersArray, techListsArray);
                        rechargeCardDialog();
                        isRecharge = true;
                    }
                })
                .setPositiveListener(getString(R.string.no), new ChooseAlertDialog.OnPositiveListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();

                    }
                }).show();
    }

    public void showConfirmPrintAlert() {
        new ChooseAlertDialog(this)
                .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                .setAnimationEnable(true)
                .setTitleText(getString(R.string.confirmation))
                .setContentText(getString(R.string.confirmation_another_print))
                .setNegativeListener(getString(R.string.yes), new ChooseAlertDialog.OnNegativeListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();
                        //bluetoothPrint();
                    }
                })
                .setPositiveListener(getString(R.string.no), new ChooseAlertDialog.OnPositiveListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();

                    }
                }).show();
    }

    public void showEnableBluetoothDialog() {
        new ChooseAlertDialog(this)
                .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                .setAnimationEnable(true)
                .setTitleText(getString(R.string.bluetooth_enable))
                .setContentText(getString(R.string.info_enable_bluetooth))
                .setNegativeListener(getString(R.string.enable), new ChooseAlertDialog.OnNegativeListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();
                        bluetoothEnabled();
                        Toast.makeText(RechargeActivity.this,getString(R.string.bluetooth_enabling),Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveListener(getString(R.string.no), new ChooseAlertDialog.OnPositiveListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();

                    }
                }).show();
    }

    private void bluetoothEnabled(){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BT);
    }


    @Override
    public void onCloseClick(int id) {

        if (id == 1) finish();
    }

    @Override
    public void onCloseClick(double amount) {
        this.totalAmount = amount;
    }

    //bluetoothPrint();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK){
                    //bluetooth is on
                    //bluetoothPrint();
                }else Toast.makeText(this,"Could't on bluetooth",Toast.LENGTH_SHORT).show();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showErrorDialog(String message) {
        new PromptDialog(this)
                .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                .setAnimationEnable(true)
                .setTitleText(getString(R.string.error))
                .setContentText(message)
                .setPositiveListener(getString(R.string.ok), new PromptDialog.OnPositiveListener() {
                    @Override
                    public void onClick(PromptDialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public void showAnimation() {
        gas_content.setVisibility(View.GONE);
        layout_loading.setVisibility(View.VISIBLE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("animation_loading.json");
        animationView.playAnimation();
        animationView.loop(true);
    }

    public void hideAnimation() {
        gas_content.setVisibility(View.VISIBLE);
        if (animationView.isAnimating()) animationView.cancelAnimation();
        layout_loading.setVisibility(View.GONE);
        animationView.setVisibility(View.GONE);
    }

    //print custom
    private void printCustom(String msg, int size, int align) {

        //Print config "mode"
        byte[] cc = new byte[]{0x1B,0x21,0x03};  // 0- normal size text
        byte[] bb = new byte[]{0x1B,0x21,0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B,0x21,0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
        try {
            switch (size){
                case 0:
                    outputStream.write(cc);
                    break;
                case 1:
                    outputStream.write(bb);
                    break;
                case 2:
                    outputStream.write(bb2);
                    break;
                case 3:
                    outputStream.write(bb3);
                    break;
            }

            switch (align){
                case 0:
                    //left align
                    outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                    break;
                case 1:
                    //center align
                    outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                    break;
                case 2:
                    //right align
                    outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                    break;
            }
            outputStream.write(msg.getBytes());
            outputStream.write(PrinterCommands.LF);

            //outputStream.write(cc);
            //printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //print photo
    public void printPhoto(int img) {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                    img);
            if(bmp!=null){
                byte[] command = Utils.decodeBitmap(bmp);
                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                printText(command);
            }else{
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    //print unicode
    public void printUnicode(){
        try {
            outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
            printText(Utils.UNICODE_TEXT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //print new line
    private void printNewLine() {
        try {
            outputStream.write(PrinterCommands.FEED_LINE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void resetPrint() {
        try{
            outputStream.write(PrinterCommands.ESC_FONT_COLOR_DEFAULT);
            outputStream.write(PrinterCommands.FS_FONT_ALIGN);
            outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
            outputStream.write(PrinterCommands.ESC_CANCEL_BOLD);
            outputStream.write(PrinterCommands.LF);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //print text
    private void printText(String msg) {
        try {
            // Print normal text
            outputStream.write(msg.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //print byte[]
    private void printText(byte[] msg) {
        try {
            // Print normal text
            outputStream.write(msg);
            printNewLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String leftRightAlign(String str1, String str2) {
        String ans = str1 +str2;
        if(ans.length() <31){
            int n = (31 - str1.length() + str2.length());
            ans = str1 + new String(new char[n]).replace("\0", " ") + str2;
        }
        return ans;
    }

    private String getFormatStringByLength(String title, String value){
        String concatenation = title+value;
        int count = concatenation.length();
        StringBuilder builder = new StringBuilder();
        String space = new String(new char[42-count]).replace("\0", " ");
        builder.append(title);
        builder.append(space);
        builder.append(value);
        return builder.toString();
    }

    private String getFormatStringByItem(String item, String price, String qty, String amount){
        StringBuilder builder = new StringBuilder();
        int count = (qty+amount).length();
        builder.append(item);
        builder.append(getSpace(17-item.length()));
        builder.append(price);
        builder.append(getSpace(10-price.length()));
        builder.append(qty);
        builder.append(getSpace(14-count));
        builder.append(amount);
        return builder.toString();
    }

    private String getFormatStringByTotal(String total, String amount){
        StringBuilder builder = new StringBuilder();
        int count = (total+amount).length();
        builder.append(getSpace(23));
        builder.append(total);
        builder.append(getSpace(19-count));
        builder.append(amount);
        return builder.toString();
    }

    private String getSpace(int count){
        String space = new String(new char[count]).replace("\0", " ");
        return space;
    }


    private String[] getDateTime() {
        final Calendar c = Calendar.getInstance();
        String dateTime [] = new String[2];
        dateTime[0] = c.get(Calendar.DAY_OF_MONTH) +"/"+ c.get(Calendar.MONTH) +"/"+ c.get(Calendar.YEAR);
        dateTime[1] = c.get(Calendar.HOUR_OF_DAY) +":"+ c.get(Calendar.MINUTE);
        return dateTime;
    }
}
