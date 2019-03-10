package com.kaicomsol.kpos.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.models.AccessFalica;
import com.kaicomsol.kpos.models.ReadCard;
import com.kaicomsol.kpos.utils.CardPropertise;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NFCCheckActivity extends AppCompatActivity {


    private ReadCard readCard;
    private AccessFalica falica;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private Vibrator vibrator;

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

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        readCard = new ReadCard();
        falica = new AccessFalica();

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
        String cardGroup = "";
        if (tag != null) {
            falica.ReadTag(tag);
            cardGroup = falica.getCardGroup(tag);
            if (!TextUtils.isEmpty(cardGroup) && cardGroup.equals(CardPropertise.SERVICE_CARD.getCode())) {
                SharedDataSaveLoad.save(this, getString(R.string.preference_is_service_check), true);
                activityHome();

//                String userId = SharedDataSaveLoad.load(this, getString(R.string.preference_user_id));
//                if (!TextUtils.isEmpty(userId) && userId.equals(readCard.readCardArgument.CustomerId)) {
//                    SharedDataSaveLoad.save(this, getString(R.string.preference_is_service_check), true);
//                    activityHome();
//                } else {
//                    CustomAlertDialog.showError(this, "Service card & user mismatch");
//                }

            }else CustomAlertDialog.showError(this, getString(R.string.err_service_card));

        } else CustomAlertDialog.showWarning(this, getString(R.string.err_card_read_failed));
    }


    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
    }

    //authentication for dashboard activity
    private void activityHome() {
        startActivity(new Intent(NFCCheckActivity.this, HomeActivity.class));
        finish();
    }

}
