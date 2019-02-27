package com.kaicomsol.kpos.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.callbacks.CloseClickListener;
import com.kaicomsol.kpos.callbacks.RefundView;
import com.kaicomsol.kpos.dialogs.CardCheckDialog;
import com.kaicomsol.kpos.dialogs.ChooseAlertDialog;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.dialogs.PromptDialog;
import com.kaicomsol.kpos.dialogs.RechargeCardDialog;
import com.kaicomsol.kpos.models.ReadCard;
import com.kaicomsol.kpos.models.Refund;
import com.kaicomsol.kpos.models.UpdateResponse;
import com.kaicomsol.kpos.presenters.RefundPresenter;
import com.kaicomsol.kpos.utils.CardPropertise;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RefundActivity extends AppCompatActivity implements RefundView, CloseClickListener {

    private CardCheckDialog mCardCheckDialog = null;
    private RechargeCardDialog mRechargeCardDialog = null;

    private RefundPresenter mPresenter;
    private DecimalFormat decimalFormat;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private ReadCard readCard;
    private Tag tag;
    private int id = 0;
    private boolean isCardRefund = false;
    private Vibrator vibrator;

    //Bind component
    @BindView(R.id.layout_main)
    LinearLayout layout_main;
    @BindView(R.id.layout_refund)
    LinearLayout layout_refund;
    @BindView(R.id.txt_account_no)
    TextView txt_account_no;
    @BindView(R.id.txt_credit)
    TextView txt_credit;
    @BindView(R.id.txt_refund1)
    TextView txt_refund1;
    @BindView(R.id.txt_refund2)
    TextView txt_refund2;
    @BindView(R.id.btn_submit)
    ImageView btn_submit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refund);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.refund);
        //view config
        viewConfig();
        //card configuration
        cardConfig();


    }

    @Override
    protected void onResume() {
        super.onResume();

        mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
    }

    private void viewConfig() {

        readCard = new ReadCard();
        mCardCheckDialog = CardCheckDialog.newInstance(this, "User");
        mRechargeCardDialog = new RechargeCardDialog();
        Bundle args = new Bundle();
        args.putString("msg", "Until refund success");
        mRechargeCardDialog.setArguments(args);

        mCardCheckDialog.setCancelable(false);
        decimalFormat = new DecimalFormat(".##");
        mPresenter = new RefundPresenter(this);

        customerCardDialog();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        btn_submit.setEnabled(true);
        btn_submit.setBackground(ContextCompat.getDrawable(this,R.drawable.circle_bg_green));

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id > 0) showConfirmDialog();
                else CustomAlertDialog.showError(RefundActivity.this, "Issue refund failed Please try again!");
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

    // manages key presses not handled in other Views from this Activity
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }

        return true;

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        new ReadAsyncTask(tag).execute();


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

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
    }


    @Override
    public void onSuccess(Refund refund) {
        if (mAdapter != null) mAdapter.disableForegroundDispatch(this);
        id = refund.getId();
    }

    @Override
    public void onSuccess(UpdateResponse updateResponse) {

        double refund1 = Double.parseDouble(readCard.readCardArgument.Refund1);
        double refund2 = Double.parseDouble(readCard.readCardArgument.Refund2);
        double credit =Double.parseDouble(readCard.readCardArgument.Credit);

        double value = refund1+refund2+credit;
        boolean response = readCard.GasChargeRefundCard(tag, Double.parseDouble(decimalFormat.format(value)),
                updateResponse.getUnitPrice(),updateResponse.getBaseFee(),updateResponse.getEmergencyValue());

        if (credit == 0.0){
            String historyNo = readCard.readCardArgument.CardHistoryNo;
           readCard.WriteStatus(tag, Integer.parseInt(historyNo)+1);
        }
        if (response){
            rechargeCardDismiss();
            updatedData();
            Toast.makeText(this, "Refund update successfully!", Toast.LENGTH_LONG).show();
        }else if (value > 999.00){
            CustomAlertDialog.showError(this, "Gas volume limit exceed!");
        }else CustomAlertDialog.showError(this, "Update failed please try again");

        //finish();
    }

    @Override
    public void onError(String error) {
        mAdapter.disableForegroundDispatch(this);
        CustomAlertDialog.showError(this, error);
    }

    @Override
    public void onLogout(int code) {
        SharedDataSaveLoad.remove(RefundActivity.this, getString(R.string.preference_access_token));
        SharedDataSaveLoad.remove(RefundActivity.this, getString(R.string.preference_is_service_check));
        Intent intent = new Intent(RefundActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateRefund() {

        if (checkConnection()) {
            String token = SharedDataSaveLoad.load(RefundActivity.this, getString(R.string.preference_access_token));
            mPresenter.updateRefund(token, String.valueOf(id));
        } else CustomAlertDialog.showError(this, getString(R.string.no_internet_connection));

    }

    private boolean checkConnection() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void customerCardDialog() {
        if (mCardCheckDialog != null) {
            if (!mCardCheckDialog.isAdded()) {
                mCardCheckDialog.show(getSupportFragmentManager(), mCardCheckDialog.getTag());
            }
        }
    }

    private void customerCardDismiss() {
        if (mCardCheckDialog != null) {
            mCardCheckDialog.dismiss();
        }
    }

    private void rechargeCardDialog() {
        if (mRechargeCardDialog != null) {
            if (!mRechargeCardDialog.isAdded()) {
                isCardRefund = true;
                mRechargeCardDialog.show(getSupportFragmentManager(), mRechargeCardDialog.getTag());
            }
        }
    }

    private void rechargeCardDismiss() {
        if (mRechargeCardDialog != null) {
            mRechargeCardDialog.dismiss();
        }
    }

    public void updatedData() {
        layout_refund.setVisibility(View.VISIBLE);
        txt_account_no.setText(readCard.readCardArgument.CustomerId);
        txt_credit.setText(addCredit(readCard.readCardArgument.Credit, readCard.readCardArgument.Refund1));
        txt_refund1.setText("0.0");
        txt_refund2.setText("0.0");
        btn_submit.setEnabled(false);
        btn_submit.setBackground(ContextCompat.getDrawable(this,R.drawable.circle_bg_gray));

    }

    private String addCredit(String credit, String refund1) {
        double cr = Double.parseDouble(credit);
        double ref1 = Double.parseDouble(refund1);

        double creditUpdate = cr + ref1;

        return String.valueOf(Double.parseDouble(decimalFormat.format(creditUpdate)));
    }

    public void showConfirmDialog() {
        new ChooseAlertDialog(this)
                .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                .setAnimationEnable(true)
                .setTitleText(getString(R.string.confirmation))
                .setContentText(getString(R.string.confirm_refund_gas))
                .setNegativeListener(getString(R.string.yes), new ChooseAlertDialog.OnNegativeListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();
                        mAdapter.enableForegroundDispatch(RefundActivity.this, pendingIntent, intentFiltersArray, techListsArray);
                        rechargeCardDialog();

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
    public void onCloseClick(int id) {
        finish();
    }

    @Override
    public void onCloseClick(double amount) {

    }

    class ReadAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private Tag tag;
        public ReadAsyncTask(Tag tag) {
            this.tag = tag;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            readCard.ReadTag(tag);
            boolean response = readCard.SetReadCardData(tag, readCard.webAPI, readCard.readCardArgument);
            return response;
        }

        @Override
        protected void onPostExecute(Boolean response) {
            vibrator.vibrate(1000);
            if (response) {
                if (readCard.readCardArgument.CardGroup.equals(CardPropertise.CUSTOMER_CARD.getCode())
                        && readCard.readCardArgument.CardStatus.equals(CardPropertise.CARD_REFUNDED.getCode())) {
                    if (!isCardRefund) {
                        customerCardDismiss();
                        layout_refund.setVisibility(View.VISIBLE);
                        txt_account_no.setText(readCard.readCardArgument.CustomerId);
                        txt_credit.setText(readCard.readCardArgument.Credit);
                        txt_refund1.setText(readCard.readCardArgument.Refund1);
                        txt_refund2.setText(readCard.readCardArgument.Refund2);

                        if (checkConnection()) {
                            String token = SharedDataSaveLoad.load(RefundActivity.this, getString(R.string.preference_access_token));
                            mPresenter.getIssueRefund(token, readCard.readCardArgument.CardIdm, readCard.readCardArgument.Credit, readCard.readCardArgument.Refund1);
                        } else
                            CustomAlertDialog.showError(RefundActivity.this, getString(R.string.no_internet_connection));
                    } else {
                        rechargeCardDismiss();
                        updateRefund();

                    }

                } else CustomAlertDialog.showError(RefundActivity.this, getString(R.string.not_available_refund));
            } else CustomAlertDialog.showWarning(RefundActivity.this, getString(R.string.err_card_read_failed));

            vibrator.cancel();
        }
    }

}
