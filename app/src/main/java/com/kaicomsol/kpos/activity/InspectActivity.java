package com.kaicomsol.kpos.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;


import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.adapter.ViewPagerAdapter;
import com.kaicomsol.kpos.callbacks.CloseClickListener;
import com.kaicomsol.kpos.dialogs.CardCheckDialog;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.fragment.ErrorFragment;
import com.kaicomsol.kpos.fragment.HistoryFragment;
import com.kaicomsol.kpos.fragment.PropertiesFragment;
import com.kaicomsol.kpos.models.AccessFalica;
import com.kaicomsol.kpos.models.NFCData;
import com.kaicomsol.kpos.models.ReadCard;
import com.kaicomsol.kpos.models.Receipt;
import com.kaicomsol.kpos.nfcfelica.HttpResponsAsync;
import com.kaicomsol.kpos.utils.DebugLog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class InspectActivity extends AppCompatActivity implements CloseClickListener {

    private CardCheckDialog mCardCheckDialog = null;

    //bind component
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    private AccessFalica mAccessFalica = new AccessFalica();
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private NfcAdapter mAdapter;
    private PendingIntent pendingIntent;
    private Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspect);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Inspect");

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.setVisibility(View.GONE);
        tabLayout.setVisibility(View.GONE);

        viewConfig();

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PropertiesFragment(), getString(R.string.properties));
        adapter.addFragment(new HistoryFragment(), getString(R.string.history));
        adapter.addFragment(new ErrorFragment(), getString(R.string.error));
        viewPager.setAdapter(adapter);
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

    private void viewConfig() {
        mCardCheckDialog = CardCheckDialog.newInstance(this, "NFC");
        mCardCheckDialog.setCancelable(false);
        customerCardDialog();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        pendingIntent = PendingIntent.getActivity(
                InspectActivity.this, 0, new Intent(
                        InspectActivity.this, getClass()).
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                0);

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
                mCardCheckDialog.show(getSupportFragmentManager(), mCardCheckDialog.getTag());
            }
        }
    }

    private void customerCardDismiss() {
        if (mCardCheckDialog != null) {
            mCardCheckDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(InspectActivity.this,
                pendingIntent, intentFiltersArray, techListsArray);


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        new ReadAsyncTask(tag).execute();


    }


    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.disableForegroundDispatch(this);
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

            mAccessFalica.ReadTag(tag);
            final boolean response = mAccessFalica.getInspectCard(tag);
            return response;
        }

        @Override
        protected void onPostExecute(Boolean response) {
            vibrator.vibrate(1000);
            if (response) {
                customerCardDismiss();
                viewPager.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);

                NFCData.getInstance().setAccessFalica(mAccessFalica);
            } else {
                CustomAlertDialog.showWarning(InspectActivity.this, getString(R.string.err_card_read_failed));
            }
            vibrator.cancel();
        }
    }

    class ReadTestAsyncTask extends AsyncTask<Void, Void, Boolean> {


        private Tag tag;
        public ReadTestAsyncTask(Tag tag) {
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
            vibrator.vibrate(1000);
            if (response) {
                customerCardDismiss();
                for(int i = 0; i < 20; i++){
                    HttpResponsAsync.ReadCardArgumentLogHour logDay = mAccessFalica.readCardArgument.LogHour.get(i);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = null;
                    try {
                        date = sdf.parse(logDay.GasTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    DebugLog.e("GasTime : "+sdf.format(date));
                    DebugLog.e("GasValue : "+logDay.GasValue);

                }

            } else {
                CustomAlertDialog.showWarning(InspectActivity.this, getString(R.string.err_card_read_failed));
            }
            vibrator.cancel();
        }
    }
}
