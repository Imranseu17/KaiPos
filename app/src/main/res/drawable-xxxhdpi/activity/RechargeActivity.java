package com.kaicomsol.tpos.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kaicomsol.tpos.R;
import com.kaicomsol.tpos.callbacks.CloseClickListener;
import com.kaicomsol.tpos.callbacks.PaymentView;
import com.kaicomsol.tpos.dbhelper.Transaction;
import com.kaicomsol.tpos.dbhelper.TransactionViewModel;
import com.kaicomsol.tpos.dialogs.CardCheckDialog;
import com.kaicomsol.tpos.dialogs.ChooseAlertDialog;
import com.kaicomsol.tpos.dialogs.CustomAlertDialog;
import com.kaicomsol.tpos.dialogs.PromptDialog;
import com.kaicomsol.tpos.fragment.InvoiceFragment;
import com.kaicomsol.tpos.models.AccessFalica;
import com.kaicomsol.tpos.models.Invoices;
import com.kaicomsol.tpos.models.Payment;
import com.kaicomsol.tpos.presenters.PaymentPresenter;
import com.kaicomsol.tpos.utils.CardCheck;
import com.kaicomsol.tpos.utils.RechargeStatus;
import com.kaicomsol.tpos.utils.SharedDataSaveLoad;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.kaicomsol.tpos.golobal.Constants.CONNECTIVITY_ACTION;

public class RechargeActivity extends AppCompatActivity implements PaymentView, CloseClickListener {

    private static final String phoneregex = "(^([+]{1}[8]{2}|0088)?(01){1}[3-9]{1}\\d{8})$";
    private TransactionViewModel mTransactionViewModel;
    private static final int REQUEST_ENABLE_BT = 0;
    private CardCheckDialog mCardCheckDialog = null;
    private boolean isRecharge = false;
    private boolean isProvider = false;
    private DecimalFormat decimalFormat;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private AccessFalica mAccessFalica = new AccessFalica();
    private String cardIdm;
    private String cardHistoryNo;
    private Tag tag;
    private PaymentPresenter mPresenter;
    private Vibrator vibrator;
    private IntentFilter intentFilter;
    private ProgressDialog mProgressDialog;

    //Bind component
    @BindView(R.id.layout_recharge)
    LinearLayout layout_recharge;
    @BindView(R.id.txt_account_no)
    TextView txt_account_no;
    @BindView(R.id.layout_price)
    LinearLayout layoutPrice;
    @BindView(R.id.txt_price)
    TextView txt_price;
    @BindView(R.id.txt_taka)
    TextView txt_taka;
    @BindView(R.id.txt_gas)
    TextView txt_gas;
    @BindView(R.id.input_layout_amount)
    TextInputLayout inputLayoutAmount;
    @BindView(R.id.edt_amount)
    TextInputEditText edtAmount;
    @BindView(R.id.txt_total_amount)
    TextView txt_total_amount;
    @BindView(R.id.btn_submit)
    Button btn_submit;
    @BindView(R.id.txt_tax)
    TextView txt_tax;

    //Bind Operator component
    @BindView(R.id.layout_operator)
    LinearLayout layoutOperator;
    @BindView(R.id.input_layout_phone)
    TextInputLayout inputLayoutPhone;
    @BindView(R.id.input_layout_pin)
    TextInputLayout inputLayoutPin;
    @BindView(R.id.edt_phone)
    TextInputEditText edtPhone;
    @BindView(R.id.edt_pin)
    TextInputEditText edtPin;





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
        mAdapter.enableForegroundDispatch(com.kaicomsol.tpos.activity.RechargeActivity.this, pendingIntent, intentFiltersArray, techListsArray);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }


    private void getInvoices(String cardNo) {

        String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
        if (checkConnection()) {
            isRecharge = true;
            showLoading("Loading invoice...");
            mPresenter.getInvoices(token, cardNo);
            new ReadAsyncTask(tag).execute();
        } else CustomAlertDialog.showError(this, getString(R.string.no_internet_connection));
    }

    private void readCard(String token, AccessFalica accessFalica) {
        if (mAccessFalica.readCardArgument != null) mPresenter.readCard(token, accessFalica);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            if (!isRecharge) {
                mAccessFalica.ReadTag(tag);
                int response = mAccessFalica.checkCardRecharge(tag);
                CardCheck cardCheck = CardCheck.getByCode(response);
                switch (cardCheck) {
                    case VALID_CARD:
                        cardIdm = mAccessFalica.GetCardIdm(tag.getId());
                        cardHistoryNo = mAccessFalica.getHistoryNo(tag);
                        txt_account_no.setText(mAccessFalica.getPrepaidCode(tag));
                        layout_recharge.setVisibility(View.VISIBLE);
                        customerCardDismiss();
                        getInvoices(cardIdm);
                        break;
                    case INVALID_CARD:
                        CustomAlertDialog.showError(com.kaicomsol.tpos.activity.RechargeActivity.this, getString(R.string.err_card_not_valid));
                        break;
                    case EXCEPTION_CARD:
                        CustomAlertDialog.showWarning(this, getString(R.string.err_card_read_failed));
                        break;
                    default:
                        CustomAlertDialog.showWarning(this, getString(R.string.err_card_read_failed));
                }
            }

        } else CustomAlertDialog.showWarning(this, getString(R.string.err_card_read_failed));
    }

    private void viewConfig() {

        //internet connectivity receiver
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        mProgressDialog = new ProgressDialog(com.kaicomsol.tpos.activity.RechargeActivity.this);
        //Vibrator init
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        // Get a new or existing ViewModel from the ViewModelProvider.
        mTransactionViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        mTransactionViewModel.deleteAll();
        //card check dialog
        mCardCheckDialog = CardCheckDialog.newInstance(this, "User");
        mCardCheckDialog.setCancelable(false);
        customerCardDialog();

        //is provider conditional view show/hide
        isProvider = SharedDataSaveLoad.loadBoolean(this, getString(R.string.preference_is_provider));
        if (isProvider) layoutOperator.setVisibility(View.VISIBLE);
        else layoutOperator.setVisibility(View.GONE);

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

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String amount = txt_total_amount.getText().toString().trim();
                double creditAmount = Double.parseDouble(amount);

                if (!validateAmount(creditAmount)) {
                    return;
                }

                if (isProvider) {
                    if (!validatePhone()) {
                        return;
                    }

                    if (!validatePin()) {
                        return;
                    }
                }

                showConfirmDialog();

            }
        });
    }

    private void gasRecharge() {
        if (checkConnection()) {
            addPayment();
        } else CustomAlertDialog.showError(this, getString(R.string.no_internet_connection));
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


    private void customerCardDialog() {
        if (mCardCheckDialog != null) {
            if (!mCardCheckDialog.isAdded()) {
                //show card dialog
                mCardCheckDialog.show(getSupportFragmentManager(), mCardCheckDialog.getTag());
            }
        }
    }

    private void customerCardDismiss() {
        if (mCardCheckDialog != null) {
            mCardCheckDialog.dismiss();
        }
    }

    private void showAmount() {
        final CharSequence[] items = {"500 TK", "1000 TK", "1500 TK", "2000 TK", "2500 TK", "Manual"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_select_amount);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String amount = items[item].toString();
                if (amount.equals("Manual")) {
                    txt_price.setText("Manual");
                    txt_taka.setText("0.0");
                    txt_gas.setText("0.0");
                    txt_tax.setText("0.0");
                    txt_total_amount.setText("0.0");
                    expand(inputLayoutAmount);
                    inputLayoutAmount.requestFocus();

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
        try {

            if (item.getTitle().equals("Manual")) {
                txt_price.setText("Manual");
                txt_taka.setText("0.0");
                txt_gas.setText("0.0");
                txt_tax.setText("0.0");
                txt_total_amount.setText("0.0");
                expand(inputLayoutAmount);
            } else {
                String amount = item.getTitle().toString().replace(" TK", "");
                txt_price.setText(amount);
                takaToGas(amount);
                collapse(inputLayoutAmount);
            }

        } catch (Exception e) {
            e.printStackTrace();
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
        if (!TextUtils.isEmpty(amount)) {
            double value = Double.parseDouble(amount) / 9.1;
            txt_taka.setText(decimalFormat.format(Double.parseDouble(amount)));
            txt_price.setText(decimalFormat.format(Double.parseDouble(amount)));
            txt_total_amount.setText(decimalFormat.format(Double.parseDouble(amount)));
            txt_gas.setText(String.valueOf(decimalFormat.format(value)));
        } else {
            txt_taka.setText("0.0");
            txt_price.setText("0.0");
            txt_total_amount.setText("0.0");
            txt_gas.setText("0.0");
        }
    }

    private void addPayment() {
        String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
        String amount = txt_total_amount.getText().toString().trim();
        if (cardHistoryNo != null) {
            showLoading("Loading authorize...");
            String phone = edtPhone.getText().toString().trim();
            String pin = edtPin.getText().toString().trim();
            mPresenter.addPayment(token, amount, cardIdm, cardHistoryNo, phone, pin,"1");
        }

    }


    @Override
    public void onSuccess(Payment payment) {

        //Add local authorise data
        mProgressDialog.dismiss();

        Transaction transaction = new Transaction(cardIdm, payment.getPaymentId(), payment.getReceipt().getGasUnit(), payment.getUnitPrice(),
                payment.getBaseFee(), payment.getEmergencyValue(), payment.getReceipt().getMeterSerialNo(), cardHistoryNo, payment.getNewHistoryNo(),
                "authorize");
        mTransactionViewModel.insert(transaction);
        mTransactionViewModel.getTransactionByCardIdm(cardIdm).observe(this, new Observer<Transaction>() {
            @Override
            public void onChanged(@Nullable Transaction transaction) {
                if (transaction != null) {

                    Intent intent = new Intent(com.kaicomsol.tpos.activity.RechargeActivity.this, com.kaicomsol.tpos.activity.CardWriteActivity.class);
                    intent.putExtra("cardIdm", cardIdm);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }


    @Override
    public void onSuccess(Invoices invoices) {
        mAdapter.disableForegroundDispatch(this);
        mProgressDialog.dismiss();
        if (invoices != null) {
            if (invoices.getInvoices() != null && invoices.getInvoices().size() > 0) {
                String invoiceList = new Gson().toJson(invoices);
                InvoiceFragment invoiceFragment = InvoiceFragment.newInstance(this);
                invoiceFragment.setCancelable(false);
                Bundle bundle = new Bundle();
                bundle.putString("invoice", invoiceList);
                invoiceFragment.setArguments(bundle);
                invoiceFragment.show(getSupportFragmentManager(), invoiceFragment.getTag());
            }
        }

    }


    @Override
    public void onSuccess(String readCard) {
        try {
            if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onError(String error, int code) {

        RechargeStatus rechargeStatus = RechargeStatus.getByCode(code);
        switch (rechargeStatus) {
            case PAYMENT_ERROR:
                mProgressDialog.dismiss();
                if (error != null) CustomAlertDialog.showError(this, error);
                break;
            case INVOICE_ERROR:
                mProgressDialog.dismiss();
                if (error != null) CustomAlertDialog.showError(this, error);
                break;
            default:
                mProgressDialog.dismiss();
                if (error != null) CustomAlertDialog.showError(this, error);
                break;

        }
    }

    @Override
    public void onLogout(int code) {
        SharedDataSaveLoad.remove(com.kaicomsol.tpos.activity.RechargeActivity.this, getString(R.string.preference_access_token));
        SharedDataSaveLoad.remove(com.kaicomsol.tpos.activity.RechargeActivity.this, getString(R.string.preference_is_service_check));
        Intent intent = new Intent(com.kaicomsol.tpos.activity.RechargeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean validateAmount(double creditAmount) {
        if (creditAmount <= 5000 && creditAmount > 0.0) {
            return true;
        } else if (creditAmount <= 0.0) {
            CustomAlertDialog.showError(com.kaicomsol.tpos.activity.RechargeActivity.this, "Amount Can not be less equal zero");
            return false;
        } else {
            CustomAlertDialog.showError(com.kaicomsol.tpos.activity.RechargeActivity.this, "Maximum payment limit is 5000 BDT");
            return false;
        }
    }

    private boolean validatePhone() {
        String phone = edtPhone.getText().toString().trim();

        if (phone.isEmpty() || !isValidPhone(phone)) {
            inputLayoutPhone.setError(getString(R.string.err_msg_phone));
            requestFocus(edtPhone);
            return false;
        } else {
            inputLayoutPhone.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePin() {
        if (edtPin.getText().toString().trim().isEmpty()) {
            inputLayoutPin.setError(getString(R.string.err_msg_pin));
            requestFocus(edtPin);
            return false;
        } else {
            inputLayoutPin.setErrorEnabled(false);
        }

        return true;
    }

    private static boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && Pattern.matches(phoneregex, phone);
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean checkConnection() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    public void expand(final View v) {
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

        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(
                v.getApplicationWindowToken(),
                InputMethodManager.SHOW_FORCED, 0);

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
                        gasRecharge();
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCloseClick(int id) {

        if (id == 1) finish();
    }

    @Override
    public void onCloseClick(double amount) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Intent connectIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(connectIntent, REQUEST_ENABLE_BT);
                } else {
                    Toast.makeText(this, "Bluetooth Connection Failed Please Try Again", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    class ReadAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private Tag tag;

        public ReadAsyncTask(Tag tag) {
            this.tag = tag;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            mAccessFalica.ReadTag(tag);
            final boolean response = mAccessFalica.getReadCard(tag);
            return response;
        }

        @Override
        protected void onPostExecute(Boolean response) {
            if (response) {
                String token = SharedDataSaveLoad.load(com.kaicomsol.tpos.activity.RechargeActivity.this, getString(R.string.preference_access_token));
                readCard(token, mAccessFalica);
            }


        }
    }

    private void showLoading(String msg) {
        mProgressDialog.setTitle(msg);
        mProgressDialog.setCancelable(false);
        if (!((Activity) this).isFinishing()) {
            mProgressDialog.show();
        }
    }


}
