package com.kaicomsol.kpos.activity;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.text.Editable;
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
import com.kaicomsol.kpos.callbacks.CloseClickListener;
import com.kaicomsol.kpos.callbacks.PaymentView;
import com.kaicomsol.kpos.dialogs.CardCheckDialog;
import com.kaicomsol.kpos.dialogs.ChooseAlertDialog;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.dialogs.PromptDialog;
import com.kaicomsol.kpos.dialogs.RechargeCardDialog;
import com.kaicomsol.kpos.fragment.InvoiceFragment;
import com.kaicomsol.kpos.model.Invoices;
import com.kaicomsol.kpos.model.Payment;
import com.kaicomsol.kpos.model.ReadCard;
import com.kaicomsol.kpos.nfcfelica.HttpResponsAsync;
import com.kaicomsol.kpos.presenters.PaymentPresenter;
import com.kaicomsol.kpos.printer.BluetoothPrinter;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;

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
    @BindView(R.id.txt_print) TextView txt_print;
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
        decimalFormat = new DecimalFormat(".##");
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
        readCard.GasChargeCard(tag, Double.parseDouble(decimalFormat.format(value)),
                0, 0, 9, "10003419");

    }

    private void capturePayment(String paymentId){
        String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
        mPresenter.capturePayment(token, paymentId);

    }

    @Override
    public void onSuccess(Payment payment) {

            capturePayment(String.valueOf(payment.getPaymentId()));
            DebugLog.e(String.valueOf(payment.getPaymentId())+" Payment ID ");
            rechargeCardDismiss();
            String amount = txt_total_amount.getText().toString().trim();
            double value = Double.parseDouble(amount) / 9.1;
            print(payment.getNewHistoryNo(), amount, value);

    }

    @Override
    public void onSuccess(Invoices invoices) {

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
         DebugLog.i("ADD GAS capturePayment SUCCESS");

    }

    @Override
    public void onSuccess(String readCard) {

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

    private void showPrintLayout(){
        layout_loading.setVisibility(View.GONE);
        gas_content.setVisibility(View.GONE);
        layout_print.setVisibility(View.VISIBLE);
    }


    private void print(int historyNo, String amount, double value) {


        readCard.WriteStatus(tag, historyNo);
        Calendar calendar = Calendar.getInstance();
        final String currentDate = DateFormat.getDateTimeInstance().format(calendar.getTime());

        final StringBuilder textData = new StringBuilder();


        textData.append("            Money Receipt       ");
        textData.append("\n");
        textData.append("----------------------------------------\n");


        textData.append(getResources().getString(R.string.receipt_print_date_time) + "  " + currentDate);
        textData.append("\n");
        textData.append("Transaction No.                122097651");
        textData.append("\n");
        textData.append(getResources().getString(R.string.receipt_print_prepaid_no) + "                " + readCard.readCardArgument.CustomerId);
        textData.append("\n");
        textData.append(getResources().getString(R.string.receipt_print_card) + "                   " + readCard.readCardArgument.CardIdm);
        textData.append("\n");


        textData.append("----------------------------------------\n");


        textData.append(getResources().getString(R.string.receipt_print_deposit_ammount) + "         " + amount);
        textData.append("\n");
        textData.append(getResources().getString(R.string.receipt_print_previous_balance) + "       0.00");
        textData.append("\n");
        textData.append(getResources().getString(R.string.receipt_print_current_balance) + "        0.00");
        textData.append("\n");
        textData.append("----------------------------------------\n");


        textData.append(getResources().getString(R.string.receipt_print_meter_rent) + "             0.00");
        textData.append("\n");
        textData.append(getResources().getString(R.string.receipt_print_other_charge) + "           0.00");
        textData.append("\n");
        textData.append(getResources().getString(R.string.receipt_print_gas_charge) + "             0.00");
        textData.append("\n");
        textData.append(getResources().getString(R.string.receipt_print_gas_volume) + "             "+ Double.parseDouble(decimalFormat.format(value)));
        textData.append("\n");
        textData.append("----------------------------------------");


        textData.append("\n");
        textData.append("  Customer Support <0124587632>");
        textData.append("\n");
        textData.append("  Karnaphuli Gas Co Ltd.");


        getSupportActionBar().setTitle("Print receipts");
        showPrintLayout();
        txt_print.setText(textData.toString());

        bluetoothPrint();

    }

    private void bluetoothPrint(){


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

                            mPrinter.printText(txt_print.getText().toString());
                            mPrinter.addNewLine();
                            try {
                                Thread.sleep(5000);
                                mPrinter.finish();
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
                        bluetoothPrint();
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
                    bluetoothPrint();
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
}
