package com.kaicomsol.kpos.fragment;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.activity.LoginActivity;
import com.kaicomsol.kpos.callbacks.CardInfoView;
import com.kaicomsol.kpos.callbacks.LostCardListener;
import com.kaicomsol.kpos.dialogs.CardCheckDialog;
import com.kaicomsol.kpos.dialogs.ChooseAlertDialog;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.dialogs.LostCardDialog;
import com.kaicomsol.kpos.dialogs.PromptDialog;
import com.kaicomsol.kpos.dialogs.RechargeCardDialog;
import com.kaicomsol.kpos.models.CardData;
import com.kaicomsol.kpos.models.MeterCard;
import com.kaicomsol.kpos.models.ReadCard;
import com.kaicomsol.kpos.presenters.CardPresenter;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddCardInfoFragment extends Fragment implements View.OnClickListener, CardInfoView, LostCardListener {

    private Activity activity = null;
    private RechargeCardDialog mRechargeCardDialog = null;
    private LostCardDialog mLostCardDialog = null;
    private CardPresenter mPresenter;
    private int emergencyValue = 0;
    private double unitPrice = 0.0;
    private int basePrice = 0;
    private String prepaidCode = "";
    private boolean cardDeleteHidden = false;
    //NFC card info initial
    ReadCard readCard = new ReadCard();
    public Tag myTag = null;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    //Bind component
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.card_content)
    CardView card_content;
    @BindView(R.id.txt_card_no)
    TextView txt_card_no;
    @BindView(R.id.txt_meter_serial)
    TextView txt_meter_serial;
    @BindView(R.id.txt_issue_date)
    TextView txt_issue_date;
    @BindView(R.id.txt_status)
    TextView txt_status;
    @BindView(R.id.btn_add)
    TextView btn_add;
    @BindView(R.id.btn_active)
    TextView btn_active;
    @BindView(R.id.btn_delete)
    TextView btn_delete;
    @BindView(R.id.btn_lost)
    TextView btn_lost;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    public AddCardInfoFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_card, container, false);


        ButterKnife.bind(this, view);


        //view config
        viewConfig();
        //card configuration
        cardConfig();


        return view;
    }

    private void cardConfig() {
        pendingIntent = PendingIntent.getActivity(
                activity, 0, new Intent(getActivity(),
                        getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
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

        mAdapter = NfcAdapter.getDefaultAdapter(activity.getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    private void enableNFCReaderMode() {



        Bundle options = new Bundle();
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 1);
        mAdapter.enableReaderMode(getActivity(), new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(Tag tag) {
                myTag = tag;
                String cardIdm = ByteArrayToHexString(tag.getId());
                if (!TextUtils.isEmpty(cardIdm)) addCard(cardIdm);
                else CustomAlertDialog.showWarning(activity, getString(R.string.err_card_read_failed));

                //statusChecked(tag);
            }
        }, Integer.MAX_VALUE, options);
    }


    private String ByteArrayToHexString(byte [] inarray) {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";

        for(j = 0 ; j < inarray.length ; ++j)
        {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }


    private void statusChecked(final Tag tag) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                try {
                    readCard.ReadTag(tag);
                    String cardIdm = readCard.readCardArgument.CardIdm;

                    final boolean response = readCard.SetReadCardData(tag, readCard.webAPI, readCard.readCardArgument);
                    if (response) {

                        addCard(cardIdm);
                    } else CustomAlertDialog.showWarning(activity, getString(R.string.err_card_read_failed));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }


    private void viewConfig() {
        prepaidCode = SharedDataSaveLoad.load(activity, getString(R.string.preference_prepaid_code));
        mPresenter = new CardPresenter(this);
        mRechargeCardDialog = new RechargeCardDialog();
        mLostCardDialog = LostCardDialog.newInstance(this);
        Bundle args = new Bundle();
        args.putString("msg", "Until card added success");
        mRechargeCardDialog.setArguments(args);
        btn_add.setOnClickListener(this);
        btn_active.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_lost.setOnClickListener(this);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (activity != null) getMeterInfo();
        if (activity != null) getEmergencyValue();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && activity != null) getMeterInfo();
    }


    private void getMeterInfo() {

        String token = SharedDataSaveLoad.load(activity, getString(R.string.preference_access_token));
        String meterSerial = SharedDataSaveLoad.load(activity, getString(R.string.preference_meter_serial));

        if (checkConnection()) {
            showAnimation();
            mPresenter.getMeterInfo(token, meterSerial);
        } else CustomAlertDialog.showError(activity, getString(R.string.no_internet_connection));
    }

    private void getEmergencyValue() {

        String token = SharedDataSaveLoad.load(activity, getString(R.string.preference_access_token));
        final int meterTypeId = SharedDataSaveLoad.loadInt(activity,getString(R.string.preference_meter_type_id));

        if (checkConnection()) {
            showAnimation();
            mPresenter.getEmergencyValue(token, String.valueOf(meterTypeId));
        } else CustomAlertDialog.showError(activity, getString(R.string.no_internet_connection));
    }



    private void addCard(String cardIdm) {

        String token = SharedDataSaveLoad.load(activity, getString(R.string.preference_access_token));
        String meterSerial = SharedDataSaveLoad.load(activity, getString(R.string.preference_meter_serial));
        String status = SharedDataSaveLoad.load(activity, getString(R.string.preference_card_status));

        if (checkConnection()) {
            mPresenter.addCard(token, cardIdm, meterSerial, status);
        } else CustomAlertDialog.showError(activity, getString(R.string.no_internet_connection));
    }

    private void activeCard() {

        String token = SharedDataSaveLoad.load(activity, getString(R.string.preference_access_token));
        String cardIdm = txt_card_no.getText().toString().trim();
        DebugLog.i(cardIdm);

        if (checkConnection()) {
            mPresenter.activeCard(token, cardIdm);
        } else CustomAlertDialog.showError(activity, getString(R.string.no_internet_connection));
    }

    private void deleteCard(String cardIdm) {

        String token = SharedDataSaveLoad.load(activity, getString(R.string.preference_access_token));
        DebugLog.e(cardIdm);
        if (checkConnection()) {
            mPresenter.deleteCard(token, cardIdm);
        } else CustomAlertDialog.showError(activity, getString(R.string.no_internet_connection));
    }

    private void lostCard() {



        String token = SharedDataSaveLoad.load(activity, getString(R.string.preference_access_token));
        String cardIdm = txt_card_no.getText().toString().trim();


        if (checkConnection()) {
            mPresenter.lostCard(token, cardIdm);
        } else CustomAlertDialog.showError(activity, getString(R.string.no_internet_connection));
    }

    private boolean checkConnection() {

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onClick(View v) {

        if (v == btn_add) {
            rechargeCardDialog();
        } else if (v == btn_active) {
            activeCard();
        } else if (v == btn_delete) {
            showDeleteDialog();
        } else if (v == btn_lost) {
            showLostCardDialog();
            //lostCard();
        }

    }

    @Override
    public void onEmergencyValue(int emergencyValue) {
        this.emergencyValue = emergencyValue;
    }

    @Override
    public void onCard(CardData cardData) {
        if (cardData != null) {
            if (cardData.getMeterCards() != null && cardData.getMeterCards().size() > 0) {
                hideAnimation();
                unitPrice = cardData.getUnitPrice();
                basePrice = cardData.getBasePrice();
                MeterCard meterCard = cardData.getMeterCards().get(0);
                addValue(meterCard);
            } else {
                showEmptyAnimation();
                activeButton(btn_add);
                disableButton(btn_active);
                disableButton(btn_delete);
                disableButton(btn_lost);
            }
        } else {
            showEmptyAnimation();
            activeButton(btn_add);
            disableButton(btn_active);
            disableButton(btn_delete);
            disableButton(btn_lost);
        }
    }

    @Override
    public void onAddCard(boolean isAdded) {

        boolean response = false;
        try {
            if (myTag != null){
                readCard.ReadTag(myTag);
                response = readCard.GamInitCard(myTag, prepaidCode,(byte) 119, unitPrice, basePrice, emergencyValue);
            }else response = false;

        } catch (Throwable t) {
            response = false;
            t.printStackTrace();
        }
        if (response){
            rechargeCardDismiss();
            hideAnimation();
            CustomAlertDialog.showSuccess(activity, "Card add successfully");
            disableButton(btn_add);
            activeButton(btn_active);
            disableButton(btn_delete);
            disableButton(btn_lost);
            getMeterInfo();
        }else {
            rechargeCardDismiss();
            CustomAlertDialog.showWarning(activity, getString(R.string.err_card_read_failed));
            String cardIdm = ByteArrayToHexString(myTag.getId());
            cardDeleteHidden = true;
            if (!TextUtils.isEmpty(cardIdm)) deleteCard(cardIdm);
            else CustomAlertDialog.showError(activity, "Card id not found!");
        }
    }

    @Override
    public void onActiveCard(String active) {
        hideAnimation();
        CustomAlertDialog.showSuccess(activity, "Card active successfully");
        disableButton(btn_add);
        disableButton(btn_active);
        activeButton(btn_delete);
        activeButton(btn_lost);
        getMeterInfo();
    }

    @Override
    public void onDeleteCard(boolean isDelete) {
        hideAnimation();
        if (!cardDeleteHidden) CustomAlertDialog.showSuccess(activity, "Card delete successfully");
        activeButton(btn_add);
        disableButton(btn_active);
        disableButton(btn_delete);
        disableButton(btn_lost);
        getMeterInfo();
    }

    @Override
    public void onLostCard(String lost) {
        hideAnimation();
        CustomAlertDialog.showSuccess(activity, "Card lost successfully");
        disableButton(btn_add);
        disableButton(btn_active);
        activeButton(btn_delete);
        activeButton(btn_lost);
        getMeterInfo();
    }

    @Override
    public void onError(String error) {
        if (mRechargeCardDialog != null && mRechargeCardDialog.isResumed()){
            rechargeCardDismiss();
        }else {
            showEmptyAnimation();
        }
        if (!TextUtils.isEmpty(error)) CustomAlertDialog.showError(activity, error);
    }

    @Override
    public void onLogout(int code) {
        SharedDataSaveLoad.remove(activity, getString(R.string.preference_access_token));
        SharedDataSaveLoad.remove(activity, getString(R.string.preference_is_service_check));
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        activity.finish();
    }

    @Override
    public void onDialogClose() {
        lostCardDismiss();
    }

    @Override
    public void onLostInfo(String gdNo, String cardNo) {
        DebugLog.e(gdNo+" || "+cardNo);
       lostCardDismiss();
    }

    private void activeButton(TextView textView) {
        textView.setEnabled(true);
        textView.setTextColor(ContextCompat.getColor(activity, R.color.green));
    }

    private void disableButton(TextView textView) {
        textView.setEnabled(false);
        textView.setTextColor(ContextCompat.getColor(activity, R.color.disable));
    }

    private void addValue(MeterCard meterCard) {
        Date date = new Date(meterCard.getIssueDate());
        SimpleDateFormat targetFormat = new SimpleDateFormat("MM-dd-yyyy");
        String formatDate = targetFormat.format(date);
        card_content.setVisibility(View.VISIBLE);
        txt_card_no.setText(meterCard.getCardNumber() != null ? meterCard.getCardNumber() : "N/A");
        txt_meter_serial.setText(meterCard.getMeterSerialNo() != null ? meterCard.getMeterSerialNo() : "N/A");
        txt_issue_date.setText(formatDate);
        if (meterCard.getStatus().equalsIgnoreCase("A")) {
            txt_status.setText("Active");
            activeButton(btn_delete);
            activeButton(btn_lost);
            disableButton(btn_add);
            disableButton(btn_active);
        } else {
            txt_status.setText("Initial");
            disableButton(btn_add);
            activeButton(btn_active);
            disableButton(btn_delete);
            disableButton(btn_lost);
        }

    }

    public void showDeleteDialog() {
        new ChooseAlertDialog(activity)
                .setDialogType(PromptDialog.DIALOG_TYPE_WRONG)
                .setAnimationEnable(true)
                .setTitleText(R.string.delete)
                .setContentText(R.string.warning_delete_card)
                .setNegativeListener(getString(R.string.yes), new ChooseAlertDialog.OnNegativeListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();
                        String cardIdm = txt_card_no.getText().toString().trim();
                        if (!TextUtils.isEmpty(cardIdm)) deleteCard(cardIdm);
                        else CustomAlertDialog.showError(activity, "Card id not found!");
                    }
                })
                .setPositiveListener(getString(R.string.cancel), new ChooseAlertDialog.OnPositiveListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void rechargeCardDialog() {
        if (mRechargeCardDialog != null) {
            if (!mRechargeCardDialog.isAdded()) {
                enableNFCReaderMode();
                mAdapter.enableForegroundDispatch(getActivity(), pendingIntent, intentFiltersArray, techListsArray);
                mRechargeCardDialog.show(getFragmentManager(), mRechargeCardDialog.getTag());
            }
        }
    }

    private void rechargeCardDismiss() {
        if (mRechargeCardDialog != null) {
            mAdapter.disableForegroundDispatch(activity);
            mRechargeCardDialog.dismiss();
        }
    }

    private void showLostCardDialog() {
        if (mLostCardDialog != null) {
            if (!mLostCardDialog.isAdded()) {
                mLostCardDialog.show(getFragmentManager(), mLostCardDialog.getTag());
            }
        }
    }

    private void lostCardDismiss() {
        if (mLostCardDialog != null) {
            mLostCardDialog.dismiss();
        }
    }

    public void showAnimation() {
        card_content.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("animation_loading.json");
        animationView.playAnimation();
        animationView.loop(true);
    }

    public void showEmptyAnimation() {
        card_content.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("empty_box.json");
        animationView.playAnimation();
        animationView.loop(false);
    }

    public void hideAnimation() {
        card_content.setVisibility(View.VISIBLE);
        if (animationView.isAnimating()) animationView.cancelAnimation();
        animationView.setVisibility(View.GONE);
    }

}
