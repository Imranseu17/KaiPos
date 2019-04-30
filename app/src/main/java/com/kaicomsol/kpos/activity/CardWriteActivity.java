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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.adapter.PrintAdapter;
import com.kaicomsol.kpos.callbacks.CloseClickListener;
import com.kaicomsol.kpos.callbacks.StateView;
import com.kaicomsol.kpos.dbhelper.Transaction;
import com.kaicomsol.kpos.dbhelper.TransactionViewModel;
import com.kaicomsol.kpos.dialogs.CancelCardDialog;
import com.kaicomsol.kpos.dialogs.ChooseAlertDialog;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.dialogs.PromptDialog;
import com.kaicomsol.kpos.dialogs.RechargeCardDialog;
import com.kaicomsol.kpos.golobal.Constants;
import com.kaicomsol.kpos.models.AccessFalica;
import com.kaicomsol.kpos.models.Item;
import com.kaicomsol.kpos.models.Receipt;
import com.kaicomsol.kpos.presenters.StatePresenter;
import com.kaicomsol.kpos.printer.BluetoothPrinter;
import com.kaicomsol.kpos.utils.PrinterCommands;
import com.kaicomsol.kpos.utils.RechargeStatus;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;
import com.kaicomsol.kpos.utils.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.kaicomsol.kpos.golobal.Constants.CONNECTIVITY_ACTION;

public class CardWriteActivity extends AppCompatActivity implements StateView, CloseClickListener {

    private TransactionViewModel mTransactionViewModel;
    private Transaction mTransaction;
    private boolean isCancel = false;
    private FirebaseDatabase mDatabase;
    private static final int REQUEST_ENABLE_BT = 0;
    private RechargeCardDialog mRechargeCardDialog = null;
    private CancelCardDialog mCancelCardDialog = null;
    private DecimalFormat decimalFormat;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private AccessFalica mAccessFalica = new AccessFalica();
    private String cardIdm;
    private Tag tag;
    private StatePresenter mPresenter;
    private static OutputStream outputStream;
    private Vibrator vibrator;
    private boolean isPrint = false;
    private IntentFilter intentFilter;
    private BluetoothSocket mBluetoothSocket = null;
    private ProgressDialog mProgressDialog;

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
    @BindView(R.id.layout_card_again)
    LinearLayout layout_card_again;
    @BindView(R.id.layout_try_print)
    LinearLayout layout_try_print;
    @BindView(R.id.btn_try_again)
    Button tryAgain;
    @BindView(R.id.btn_print)
    Button btn_print;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_write);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.add_gas);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
        ButterKnife.bind(this);

        viewConfig();

        cardConfig();


    }



    private void viewConfig() {

        // Write a message to the database
        mDatabase = FirebaseDatabase.getInstance();

        mProgressDialog = new ProgressDialog(CardWriteActivity.this);

        //internet connectivity receiver
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        //Vibrator init
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //Recharge card dialog init
        mRechargeCardDialog = new RechargeCardDialog();
        Bundle args = new Bundle();
        args.putString("msg", "Until payment success");
        mRechargeCardDialog.setArguments(args);

        //Cancel card dialog init
        mCancelCardDialog = new CancelCardDialog();
        Bundle arg = new Bundle();
        arg.putString("msg", "Until payment cancel");
        mCancelCardDialog.setArguments(arg);
        mCancelCardDialog.setCancelable(false);

        Intent intent = getIntent();
        cardIdm = intent.getStringExtra("cardIdm");
        // Get a new or existing ViewModel from the ViewModelProvider.
        mTransactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        mTransactionViewModel.getTransactionByCardIdm(cardIdm).observeForever(new Observer<Transaction>() {
            @Override
            public void onChanged(@Nullable Transaction transaction) {
                if (transaction != null) {
                    mTransaction = transaction;
                    rechargeCardDialog();
                }
            }
        });


        decimalFormat = new DecimalFormat(".##");
        mPresenter = new StatePresenter(this);

        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmPrintAlert();
            }
        });
        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rechargeCardDialog();
            }
        });
    }

    private void cardConfig() {

        mAccessFalica = new AccessFalica();
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
        mAdapter.enableForegroundDispatch(CardWriteActivity.this, pendingIntent, intentFiltersArray, techListsArray);

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
            if (!isCancel){
                cardWriteForFirstAttempt(tag);
            }else {
                cardCheckForCancelAttempt(tag);
            }

        } else CustomAlertDialog.showWarning(this, getString(R.string.err_card_read_failed));

    }

    private void cardWriteForFirstAttempt(final Tag tag){
        mAccessFalica.ReadTag(tag);
        String checkStatus = mAccessFalica.getCardStatus(tag);
        if (checkStatus != null && checkStatus.equals("15")) {
            rechargeCardDismiss();
            receiptPayment(mTransaction.getPaymentId() + "");
            capturePayment(mTransaction.getPaymentId() + "");
        } else {
            new WriteAsyncTask(mTransaction).execute();
        }
    }

    private void cardCheckForCancelAttempt(final Tag tag){
        mAccessFalica.ReadTag(tag);
        String checkStatus = mAccessFalica.getCardStatus(tag);
        if (checkStatus != null && checkStatus.equals("15")) {
            rechargeCardDismiss();
            Toast.makeText(CardWriteActivity.this,"TransactionModel already successful", Toast.LENGTH_SHORT).show();
            receiptPayment(mTransaction.getPaymentId() + "");
            capturePayment(mTransaction.getPaymentId() + "");
        } else {
            mAccessFalica.ReadTag(tag);
            final String historyNo = mAccessFalica.getHistoryNo(tag);
            mTransactionViewModel.getTransactionByCardIdm(cardIdm).observeForever(new Observer<Transaction>() {
                @Override
                public void onChanged(@Nullable Transaction transaction) {
                    if (transaction != null) {
                        if (historyNo.equalsIgnoreCase(transaction.getHistoryNo())){
                            cancelPayment(mTransaction.getPaymentId() + "");
                        }else {
                            mAccessFalica.ReadTag(tag);
                            boolean response = mAccessFalica.writeHistory(tag, Integer.parseInt(transaction.getHistoryNo()), mDatabase);
                            if (response) cancelPayment(mTransaction.getPaymentId() + "");
                            else showTryAgain();
                        }
                    }
                    cancelCardDismiss();
                }
            });
        }
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
        if(!((Activity) this).isFinishing())
        {
            alertDialog.show();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (isPrint) {
                    finish();
                    return true;
                } else {
                    mTransactionViewModel.getTransactionByCardIdm(cardIdm).observeForever(new Observer<Transaction>() {
                        @Override
                        public void onChanged(@Nullable Transaction transaction) {
                            if (transaction != null) {
                                showCancelDialog();
                                return;
                            }
                        }
                    });
                }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isPrint) {
            super.onBackPressed();
        } else {
            mTransactionViewModel.getTransactionByCardIdm(cardIdm).observeForever(new Observer<Transaction>() {
                @Override
                public void onChanged(@Nullable Transaction transaction) {
                    if (transaction != null) {
                        showCancelDialog();
                    }
                }
            });
        }


    }

    public void showCancelDialog() {
        ChooseAlertDialog alertDialog= new ChooseAlertDialog(this)
                .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                .setAnimationEnable(true)
                .setTitleText(R.string.warning)
                .setContentText("Are you sure you want to cancel recharge")
                .setNegativeListener(getString(R.string.no), new ChooseAlertDialog.OnNegativeListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .setPositiveListener(getString(R.string.yes), new ChooseAlertDialog.OnPositiveListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        isCancel = true;
                        hideAllView();
                        cancelCardDialog();
                        dialog.dismiss();

                    }
                });
        if(!((Activity) this).isFinishing())
        {
            alertDialog.show();
        }
    }

    private void receiptPayment(String paymentId) {

        String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
        if (checkConnection()) {
            showLoading("Loading print receipt");
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            layout_card_again.setVisibility(View.GONE);
            mPresenter.receiptPayment(token, paymentId);
        } else {
            showReceiptAgain();
            showErrorDialog(getString(R.string.no_internet_connection));
        }
    }

    private void rechargeCardDialog() {
        if (mRechargeCardDialog != null) {
            if (!mRechargeCardDialog.isAdded()) {
                try {
                    mAdapter.enableForegroundDispatch(CardWriteActivity.this, pendingIntent, intentFiltersArray, techListsArray);
                    mRechargeCardDialog.show(getSupportFragmentManager(), mRechargeCardDialog.getTag());
                }catch (Exception e){
                      e.printStackTrace();
                }

            }
        }
    }


    private void rechargeCardDismiss() {
        if (mRechargeCardDialog != null) {
            mRechargeCardDialog.dismiss();
            try {
                mAdapter.disableForegroundDispatch(CardWriteActivity.this);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void cancelCardDialog() {
        if (mCancelCardDialog != null) {
            if (!mCancelCardDialog.isAdded()) {
                try {
                    mAdapter.enableForegroundDispatch(CardWriteActivity.this, pendingIntent, intentFiltersArray, techListsArray);
                    mCancelCardDialog.show(getSupportFragmentManager(), mCancelCardDialog.getTag());
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }


    private void cancelCardDismiss() {
        if (mCancelCardDialog != null) {
            mCancelCardDialog.dismiss();
            try {
                mAdapter.disableForegroundDispatch(CardWriteActivity.this);
            }catch (Exception e){
                e.printStackTrace();
            }
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

    //print photo
    public void printPhoto(int img) {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                    img);
            if (bmp != null) {
                byte[] command = Utils.decodeBitmap(bmp);
                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                printText(command);
            } else {
                Log.e("Print Photo error", "the file isn't exists");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PrintTools", "the file isn't exists");
        }
    }

    //print unicode
    public void printUnicode() {
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
        try {
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
        String ans = str1 + str2;
        if (ans.length() < 31) {
            int n = (31 - str1.length() + str2.length());
            ans = str1 + new String(new char[n]).replace("\0", " ") + str2;
        }
        return ans;
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


    private String[] getDateTime() {
        final Calendar c = Calendar.getInstance();
        String dateTime[] = new String[2];
        dateTime[0] = c.get(Calendar.DAY_OF_MONTH) + "/" + c.get(Calendar.MONTH) + "/" + c.get(Calendar.YEAR);
        dateTime[1] = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE);
        return dateTime;
    }

    @Override
    public void onCloseClick(int id) {

    }

    @Override
    public void onCloseClick(double amount) {

    }

    @Override
    public void onSuccess(Receipt receipt) {
        mProgressDialog.dismiss();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        print(receipt);
    }

    @Override
    public void onCaptureSuccess(int paymentId) {
        mTransactionViewModel.deleteByPaymentId(paymentId);
    }

    @Override
    public void onCancelSuccess(int paymentId) {
        mProgressDialog.dismiss();
        Toast.makeText(CardWriteActivity.this, "Recharge canceled!", Toast.LENGTH_SHORT).show();
        mTransactionViewModel.deleteByPaymentId(paymentId);
        finish();
    }

    @Override
    public void onError(String error, int code) {
        RechargeStatus rechargeStatus = RechargeStatus.getByCode(code);
        switch (rechargeStatus) {
            case RECEIPT_ERROR:
                showReceiptAgain();
                break;
            case CANCEL_ERROR:
                showTryAgain();
                break;
            default:
                showTryAgain();
                break;
        }
        mProgressDialog.dismiss();
        Toast.makeText(CardWriteActivity.this, error, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onLogout(int code) {
        SharedDataSaveLoad.remove(CardWriteActivity.this, getString(R.string.preference_access_token));
        SharedDataSaveLoad.remove(CardWriteActivity.this, getString(R.string.preference_is_service_check));
        Intent intent = new Intent(CardWriteActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

    class WriteAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private Transaction mTransaction;

        public WriteAsyncTask(Transaction transaction) {
            this.mTransaction = transaction;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            mAccessFalica.ReadTag(tag);
            boolean response = mAccessFalica.creditChargeCard(tag, mTransaction.getGasUnit(), mTransaction.getUnitPrice(), mTransaction.getBaseFee(), mTransaction.getEmergencyValue(), mTransaction.getMeterSerialNo());
            return response;
        }

        @Override
        protected void onPostExecute(Boolean response) {

            //If card volume and config data write successfully
            if (response) {
                mAccessFalica.ReadTag(tag);
                boolean writeStatus = mAccessFalica.writeStatus(tag, Integer.parseInt(mTransaction.getHistoryNo()), mTransaction.getNewHistoryNo(), mDatabase);
                if (writeStatus) {
                    isPrint = true;
                    //Finally success
                    rechargeCardDismiss();
                    receiptPayment(mTransaction.getPaymentId() + "");
                    capturePayment(mTransaction.getPaymentId() + "");

                } else {
                    rechargeCardDismiss();
                    showTryAgain();
                }

            }
        }
    }

    private void showTryAgain() {
        layout_print.setVisibility(View.GONE);
        layout_try_print.setVisibility(View.GONE);
        layout_card_again.setVisibility(View.VISIBLE);
    }

    private void showReceiptAgain() {
        layout_print.setVisibility(View.GONE);
        layout_try_print.setVisibility(View.VISIBLE);
        layout_card_again.setVisibility(View.GONE);
    }

    private void hideAllView(){
        layout_print.setVisibility(View.GONE);
        layout_try_print.setVisibility(View.GONE);
        layout_card_again.setVisibility(View.GONE);
    }

    private void print(Receipt receipt) {
        showPrintLayout(receipt);
        bluetoothPrint(receipt);
        isPrint = true;
        getSupportActionBar().setTitle("Print receipts");

    }

    private void capturePayment(String paymentId) {
        if (checkConnection()) {
            String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
            mPresenter.capturePayment(token, paymentId);
        } else {
            SharedDataSaveLoad.save(this, getString(R.string.preference_capture_failed), true);
        }

    }

    private void cancelPayment(String paymentId) {
        if (checkConnection()) {
            showLoading("Loading recharge cancel");
            String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
            mPresenter.cancelPayment(token, paymentId);
        } else SharedDataSaveLoad.save(this, getString(R.string.preference_cancel_failed), true);
    }

    private void showLoading(String msg){
        mProgressDialog.setTitle(msg);
        mProgressDialog.setCancelable(false);
        if(!((Activity) this).isFinishing())
        {
            mProgressDialog.show();
        }

    }

    private void showPrintLayout(Receipt receipt) {

        layout_print.setVisibility(View.VISIBLE);
        layout_card_again.setVisibility(View.GONE);
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
                                CustomAlertDialog.showError(CardWriteActivity.this, "Printer Connection Failed ! Please try again");
                            }
                        });
                    } else {
                        new PrintAsyncTask(receipt).execute();
                    }
                } else {
                    CustomAlertDialog.showError(CardWriteActivity.this, "Printer Not Found ! Please try again");
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
                        Toast.makeText(CardWriteActivity.this, getString(R.string.bluetooth_enabling), Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveListener(getString(R.string.no), new ChooseAlertDialog.OnPositiveListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();

                    }
                });

        if(!((Activity) this).isFinishing())
        {
            alertDialog.show();
        }
    }

    private void bluetoothEnabled() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BT);
    }

    private boolean checkConnection() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
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

}
