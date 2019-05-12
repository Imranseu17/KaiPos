package com.kaicomsol.kpos.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.adapter.PrintAdapter;
import com.kaicomsol.kpos.callbacks.AuthorizeView;
import com.kaicomsol.kpos.callbacks.CloseClickListener;
import com.kaicomsol.kpos.dbhelper.Transaction;
import com.kaicomsol.kpos.dbhelper.TransactionViewModel;
import com.kaicomsol.kpos.dialogs.CardCheckDialog;
import com.kaicomsol.kpos.dialogs.ChooseAlertDialog;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.dialogs.PromptDialog;
import com.kaicomsol.kpos.dialogs.RechargeCardDialog;
import com.kaicomsol.kpos.golobal.Constants;
import com.kaicomsol.kpos.models.AccessFalica;
import com.kaicomsol.kpos.models.Item;
import com.kaicomsol.kpos.models.Payment;
import com.kaicomsol.kpos.models.Receipt;
import com.kaicomsol.kpos.presenters.AuthorizePresenter;
import com.kaicomsol.kpos.printer.BluetoothPrinter;
import com.kaicomsol.kpos.utils.CardCheck;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.PrinterCommands;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.kaicomsol.kpos.golobal.Constants.CONNECTIVITY_ACTION;

public class AuthorizeActivity extends AppCompatActivity implements AuthorizeView, CloseClickListener {

    private TransactionViewModel mTransactionViewModel;
    private Transaction mTransaction;
    private FirebaseDatabase mDatabase;
    private static final int REQUEST_ENABLE_BT = 0;
    private RechargeCardDialog mRechargeCardDialog = null;
    private DecimalFormat decimalFormat;
    private AuthorizePresenter mPresenter;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private AccessFalica mAccessFalica = new AccessFalica();
    private String cardIdm;
    private Tag tag;
    private static OutputStream outputStream;
    private boolean isPrint = false;
    private IntentFilter intentFilter;
    private BluetoothSocket mBluetoothSocket = null;
    private ProgressDialog mProgressDialog;
    private CardCheckDialog mCardCheckDialog = null;

    private String cardId;
    private String historyNo;
    private String credit;
    private String status;

    //print component bind
    @BindView(R.id.layout_print)
    RelativeLayout layout_print;
    @BindView(R.id.recycler_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.txt_date_time)
    TextView txt_date_time;
    @BindView(R.id.txt_transaction_no)
    TextView txt_transaction_no;
    @BindView(R.id.txt_customer_code)
    TextView txt_customer_code;
    @BindView(R.id.txt_prepaid_no)
    TextView txt_prepaid_no;
    @BindView(R.id.txt_meter_no)
    TextView txt_meter_no;
    @BindView(R.id.txt_card_no)
    TextView txt_card_no;
    @BindView(R.id.txt_pos_id)
    TextView txt_pos_id;
    @BindView(R.id.txt_operator_name)
    TextView txt_operator_name;
    @BindView(R.id.txt_deposit_amount)
    TextView txt_deposit_amount;
    @BindView(R.id.txt_total)
    TextView txt_total;
    @BindView(R.id.layout_try_print)
    LinearLayout layout_try_print;
    @BindView(R.id.btn_print)
    Button btn_print;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorize);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Support");
        ButterKnife.bind(this);

        viewConfig();

        cardConfig();
    }

    private void viewConfig() {

        // Write a message to the database
        mDatabase = FirebaseDatabase.getInstance();

        mProgressDialog = new ProgressDialog(AuthorizeActivity.this);

        // Get a new or existing ViewModel from the ViewModelProvider.
        mTransactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);

        mCardCheckDialog = CardCheckDialog.newInstance(this, "User");
        mCardCheckDialog.setCancelable(false);
        customerCardDialog();

        //internet connectivity receiver
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        mRechargeCardDialog = new RechargeCardDialog();
        Bundle args = new Bundle();
        args.putString("msg", "Authorize loading...");
        mRechargeCardDialog.setArguments(args);

        decimalFormat = new DecimalFormat(".##");
        mPresenter = new AuthorizePresenter(this);

        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmPrintAlert();
            }
        });

    }

    private void cardConfig() {


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

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(AuthorizeActivity.this, pendingIntent,
                intentFiltersArray, techListsArray);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {

            mAccessFalica.ReadTag(tag);
            String group = mAccessFalica.getCardGroup(tag);
            if (group.equalsIgnoreCase("77")) {
                mAccessFalica.ReadTag(tag);
                cardId = mAccessFalica.GetCardIdm(tag.getId());
                status = mAccessFalica.getCardStatus(tag);
                credit = mAccessFalica.getCardCredit(tag);
                historyNo = mAccessFalica.getHistoryNo(tag);
                customerCardDismiss();
                authorization(cardId);
            } else {
                CustomAlertDialog.showError(this, getString(R.string.err_card_not_valid));
            }

        } else CustomAlertDialog.showWarning(this, getString(R.string.err_card_read_failed));

    }

    public void showConfirmPrintAlert() {
        ChooseAlertDialog alertDialog = new ChooseAlertDialog(this)
                .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                .setAnimationEnable(true)
                .setTitleText(getString(R.string.confirmation))
                .setContentText(getString(R.string.confirmation_another_print))
                .setNegativeListener(getString(R.string.yes), new ChooseAlertDialog.OnNegativeListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();
                        receiptPayment(mTransaction.getPaymentId() + "");
                    }
                })
                .setPositiveListener(getString(R.string.no), new ChooseAlertDialog.OnPositiveListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();

                    }
                });
        if (!((Activity) this).isFinishing()) {
            alertDialog.show();
        }
    }

    private void receiptPayment(String paymentId) {

        String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
        if (checkConnection()) {
            showLoading("Loading print receipt ...");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            mPresenter.receiptPayment(token, paymentId);
        } else {
            showReceiptAgain();
            showErrorDialog(getString(R.string.no_internet_connection));
        }
    }

    private boolean checkConnection() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void showLoading(String msg) {
        mProgressDialog.setTitle(msg);
        mProgressDialog.setCancelable(false);
        if (!((Activity) this).isFinishing()) {
            mProgressDialog.show();
        }

    }

    private void showReceiptAgain() {
        layout_print.setVisibility(View.GONE);
        layout_try_print.setVisibility(View.VISIBLE);

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

    private void print(Receipt receipt) {
        showPrintLayout(receipt);
        bluetoothPrint(receipt);
        isPrint = true;
        getSupportActionBar().setTitle("Print receipts");

    }

    private void showPrintLayout(Receipt receipt) {

        layout_print.setVisibility(View.VISIBLE);
        layout_try_print.setVisibility(View.GONE);

        Date date = new Date(receipt.getPaymentDate());
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT + " " + Constants.TIME_FORMAT);

        txt_date_time.setText(dateFormat.format(date));
        txt_transaction_no.setText(String.valueOf(receipt.getPaymentId()));
        txt_customer_code.setText(receipt.getCustomerCode());
        txt_prepaid_no.setText(receipt.getPrePaidCode());
        txt_meter_no.setText(receipt.getMeterSerialNo());
        txt_card_no.setText(receipt.getCardNo());
        txt_pos_id.setText(String.valueOf(receipt.getPosId()));
        txt_operator_name.setText(receipt.getOperatorName());
        txt_deposit_amount.setText(String.valueOf(decimalFormat.format(receipt.getAmountPaid())));
        txt_total.setText(String.valueOf(decimalFormat.format(receipt.getItems().getTotal())));

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        PrintAdapter mAdapter = new PrintAdapter(receipt.getItems().getItems());
        mRecyclerView.setAdapter(mAdapter);
    }

    private void bluetoothPrint(final Receipt receipt) {


        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            CustomAlertDialog.showError(this, getString(R.string.bluetooth_printer_not_support));
        } else {
            boolean enable = mBluetoothAdapter.isEnabled();
            if (enable) {
                if (mBluetoothAdapter.getBondedDevices().iterator().hasNext()) {
                    final BluetoothDevice mBtDevice = mBluetoothAdapter.getBondedDevices().iterator().next();
                    final BluetoothPrinter mPrinter = new BluetoothPrinter(mBtDevice);

                    if (mBluetoothSocket == null) {
                        mPrinter.connectPrinter(new BluetoothPrinter.PrinterConnectListener() {

                            @Override
                            public void onConnected() {

                                mBluetoothSocket = mPrinter.getSocket();
                                new PrintAsyncTask(receipt).execute();

                            }

                            @Override
                            public void onFailed() {
                                CustomAlertDialog.showError(AuthorizeActivity.this, "Printer Connection Failed ! Please try again");
                            }
                        });
                    } else {
                        new PrintAsyncTask(receipt).execute();
                    }
                } else {
                    CustomAlertDialog.showError(AuthorizeActivity.this, "Printer Not Found ! Please try again");
                }


            } else {
                showEnableBluetoothDialog();
            }

        }

    }


    public void showEnableBluetoothDialog() {
        ChooseAlertDialog alertDialog = new ChooseAlertDialog(this)
                .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                .setAnimationEnable(true)
                .setTitleText(getString(R.string.bluetooth_enable))
                .setContentText(getString(R.string.info_enable_bluetooth))
                .setNegativeListener(getString(R.string.enable), new ChooseAlertDialog.OnNegativeListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();
                        bluetoothEnabled();
                        Toast.makeText(AuthorizeActivity.this, getString(R.string.bluetooth_enabling), Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveListener(getString(R.string.no), new ChooseAlertDialog.OnPositiveListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();

                    }
                });

        if (!((Activity) this).isFinishing()) {
            alertDialog.show();
        }
    }

    private void bluetoothEnabled() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onSuccess(Payment payment) {

        mProgressDialog.dismiss();

        if (Integer.parseInt(historyNo) < payment.getNewHistoryNo() || !status.equalsIgnoreCase("15")) {
            Transaction transaction = new Transaction(cardId, payment.getPaymentId(), payment.getReceipt().getGasUnit(), payment.getUnitPrice(),
                    payment.getBaseFee(), payment.getEmergencyValue(), payment.getReceipt().getMeterSerialNo(), historyNo, payment.getNewHistoryNo(),
                    "authorize");
            mTransactionViewModel.insert(transaction);
            mTransactionViewModel.getTransactionByCardIdm(cardId).observe(this, new Observer<Transaction>() {
                @Override
                public void onChanged(@Nullable Transaction transaction) {
                    if (transaction != null) {
                        if (CardWriteActivity.cardActivity != null) CardWriteActivity.cardActivity.finish();
                        Intent intent = new Intent(AuthorizeActivity.this, CardWriteActivity.class);
                        intent.putExtra("cardIdm", cardId);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        } else {
            capturePayment(payment.getPaymentId() + "");
            receiptPayment(payment.getPaymentId() + "");
        }


    }

    @Override
    public void onSuccess(Receipt receipt) {
        mProgressDialog.dismiss();
        print(receipt);
    }

    @Override
    public void onError(String error, int code) {
        mProgressDialog.dismiss();
        CustomAlertDialog.showError(this, error);

    }

    @Override
    public void onLogout(int code) {
        SharedDataSaveLoad.remove(AuthorizeActivity.this, getString(R.string.preference_access_token));
        SharedDataSaveLoad.remove(AuthorizeActivity.this, getString(R.string.preference_is_service_check));
        Intent intent = new Intent(AuthorizeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCaptureSuccess(int paymentID) {
        mTransactionViewModel.deleteByPaymentId(paymentID);
    }

    @Override
    public void onCloseClick(int id) {
           finish();
    }

    @Override
    public void onCloseClick(double amount) {

    }

    class PrintAsyncTask extends AsyncTask<Receipt, Void, Void> {

        private Receipt receipt;

        public PrintAsyncTask(Receipt receipt) {
            this.receipt = receipt;
        }

        @Override
        protected Void doInBackground(Receipt... receipts) {
            createReceipt(receipt);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public void createReceipt(Receipt receipt) {

        OutputStream opstream = null;
        try {
            if (mBluetoothSocket != null) opstream = mBluetoothSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        outputStream = opstream;
        //print command
        try {
            byte[] printformat = new byte[]{0x1B, 0x21, 0x01};
            outputStream.write(printformat);
            printCustom("Money Receipt", 2, 1);

            Date date = new Date(receipt.getPaymentDate());
            SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT + " " + Constants.TIME_FORMAT);

            printCustom(new String(new char[42]).replace("\0", "-"), 0, 1);
            printCustom(getFormatStringByLength("Date and Time.", dateFormat.format(date)), 0, 1);
            printCustom(getFormatStringByLength("TransactionModel No.", String.valueOf(receipt.getPaymentId())), 0, 1);
            printCustom(getFormatStringByLength("Customer Code", receipt.getCustomerCode()), 0, 1);
            printCustom(getFormatStringByLength("Prepaid No", receipt.getPrePaidCode()), 0, 1);
            printCustom(getFormatStringByLength("Meter No.", receipt.getMeterSerialNo()), 0, 1);
            printCustom(getFormatStringByLength("Card No.", receipt.getCardNo()), 0, 1);
            printCustom(getFormatStringByLength("POS ID", String.valueOf(receipt.getPosId())), 0, 1);
            printCustom(getFormatStringByLength("Operator Name", receipt.getOperatorName()), 0, 1);
            printCustom(new String(new char[42]).replace("\0", "-"), 0, 1);
            printCustom(getFormatStringByLength("Deposit Amount(TK)", String.valueOf(decimalFormat.format(receipt.getAmountPaid()))), 0, 1);
            printCustom(new String(new char[42]).replace("\0", "-"), 0, 1);
            printCustom(getFormatStringByItem("Item", "Price", "Qty", "Amount"), 0, 1);
            printCustom(new String(new char[42]).replace("\0", "-"), 0, 1);

            for (int i = 0; i < receipt.getItems().getItems().size(); i++) {
                Item item = receipt.getItems().getItems().get(i);
                if (item.getName().length() > 18) {
                    printCustom(getFormatStringByItem(item.getName().substring(0, 18), String.valueOf(decimalFormat.format(item.getPrice())), String.valueOf(decimalFormat.format(item.getQuantity())), String.valueOf(decimalFormat.format(item.getTotal()))), 0, 1);
                } else {
                    printCustom(getFormatStringByItem(item.getName(), String.valueOf(decimalFormat.format(item.getPrice())), String.valueOf(decimalFormat.format(item.getQuantity())), String.valueOf(decimalFormat.format(item.getTotal()))), 0, 1);
                }
            }

            printCustom(new String(new char[42]).replace("\0", "-"), 0, 1);
            printCustom(getFormatStringByTotal("Total:", String.valueOf(decimalFormat.format(receipt.getItems().getTotal()))), 0, 1);
            printCustom(new String(new char[42]).replace("\0", "."), 0, 1);
            printCustom("Customer Support <01707074462>", 0, 1);
            printCustom("Karnaphuli Gas Distribution Company Ltd.", 0, 1);
            printNewLine();
            printNewLine();
            printNewLine();

            outputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //print custom
    private void printCustom(String msg, int size, int align) {

        //Print config "mode"
        byte[] cc = new byte[]{0x1B, 0x21, 0x01};  // 0- normal size text
        byte[] bb = new byte[]{0x1B, 0x21, 0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B, 0x21, 0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B, 0x21, 0x10}; // 3- bold with large text
        try {
            switch (size) {
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

            switch (align) {
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


    //print new line
    private void printNewLine() {
        try {
            outputStream.write(PrinterCommands.FEED_LINE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private String getFormatStringByLength(String title, String value) {
        String concatenation = title + value;
        int count = concatenation.length();
        StringBuilder builder = new StringBuilder();
        String space = new String(new char[42 - count]).replace("\0", " ");
        builder.append(title);
        builder.append(space);
        builder.append(value);
        return builder.toString();
    }


    private String getFormatStringByItem(String item, String price, String qty, String amount) {
        StringBuilder builder = new StringBuilder();
        int count = (qty + amount).length();
        builder.append(item);
        builder.append(getSpace(19 - item.length()));
        builder.append(price);
        builder.append(getSpace(9 - price.length()));
        builder.append(qty);
        builder.append(getSpace(14 - count));
        builder.append(amount);
        return builder.toString();
    }

    private String getFormatStringByTotal(String total, String amount) {
        StringBuilder builder = new StringBuilder();
        int count = (total + amount).length();
        builder.append(getSpace(23));
        builder.append(total);
        builder.append(getSpace(19 - count));
        builder.append(amount);
        return builder.toString();
    }


    private String getSpace(int count) {
        String space = new String(new char[count]).replace("\0", " ");
        return space;
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

        }
        return super.onOptionsItemSelected(item);
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

    private void authorization(String cardIdm) {
        if (checkConnection()) {
            showLoading("Authorize...");
            String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
            mPresenter.getAuthorization(token, cardIdm);
        } else {
            SharedDataSaveLoad.save(this, getString(R.string.preference_capture_failed), true);
        }

    }

    private void customerCardDialog() {
        if (mCardCheckDialog != null) {
            if (!mCardCheckDialog.isAdded()) {
                //show card dialog
                mCardCheckDialog.show(getSupportFragmentManager(), mCardCheckDialog.getTag());
                try {
                    mAdapter.enableForegroundDispatch(AuthorizeActivity.this, pendingIntent, intentFiltersArray, techListsArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }


    }

    private void customerCardDismiss() {
        if (mCardCheckDialog != null) {
            mCardCheckDialog.dismiss();
        }


    }
}
