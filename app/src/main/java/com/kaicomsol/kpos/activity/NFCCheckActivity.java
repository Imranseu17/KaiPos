package com.kaicomsol.kpos.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.model.NFCData;
import com.kaicomsol.kpos.model.ReadCard;
import com.kaicomsol.kpos.nfcfelica.HttpResponsAsync;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NFCCheckActivity extends AppCompatActivity {


    private ReadCard readCard;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private Vibrator vibrator;
    private Animation mAnimation;
    private String path = "";

    //bind component
    @BindView(R.id.img_close)
    ImageView imgClose;
    @BindView(R.id.img_nfc)
    ImageView imgNfc;
    @BindView(R.id.txt_service)
    TextView txtService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_nfc_check);

        ButterKnife.bind(this);

        viewConfig();


    }

    private void viewConfig() {

        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        if (!TextUtils.isEmpty(path) && path.equals("service")) txtService.setText("Service");
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        readCard = new ReadCard();
        mAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);
        //imgNfc.startAnimation(mAnimation);

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

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        readCard.ReadTag(tag);
        final boolean response = readCard.SetReadCardData(tag, readCard.webAPI, readCard.readCardArgument);
        if (response) {
            vibrator.vibrate(1000);
            if (readCard.readCardArgument.CardGroup.equals("88") && path.equals("service")) {
                SharedDataSaveLoad.save(this, getString(R.string.preference_is_service_check), true);
                activityHome();
            } else if (!readCard.readCardArgument.CardGroup.equals("88") && path.equals("service")) {
                CustomAlertDialog.showError(this, getString(R.string.err_service_card));
            } else {
                goNext(path);
            }
        }else CustomAlertDialog.showWarning(this, getString(R.string.err_card_read_failed));
        vibrator.cancel();

    }


    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    private void goNext(String path) {
        DebugLog.e(path);
        switch (path) {
            case "service":
                activityHome();
                break;
            case "gas":
                activityGas(readCard.readCardArgument.CardGroup);
            case "inspect":
                activityInspect();
                break;
        }
    }

    //authentication for add gas activity
    private void activityHome() {
        startActivity(new Intent(NFCCheckActivity.this, HomeActivity.class));
        finish();
    }

    //authentication for add gas activity
    private void activityGas(String group) {
        startActivity(new Intent(NFCCheckActivity.this, HomeActivity.class));
        finish();
    }

    //authentication for invoice activity
    private void activityInvoice(String group) {
        startActivity(new Intent(NFCCheckActivity.this, HomeActivity.class));
        finish();
    }

    //authentication for refund activity
    private void activityRefund(String group) {
        startActivity(new Intent(NFCCheckActivity.this, HomeActivity.class));
        finish();
    }

    //authentication for inspect activity
    private void activityInspect() {

        HttpResponsAsync.ReadCardArgument argument = readCard.readCardArgument;
        NFCData.getInstance().setArgument(argument);
        Intent intent = new Intent(NFCCheckActivity.this, InspectActivity.class);
        startActivity(intent);
        finish();

    }


}
