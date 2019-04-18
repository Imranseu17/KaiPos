package com.kaicomsol.tpos.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kaicomsol.tpos.R;
import com.kaicomsol.tpos.callbacks.LanguageSelectListener;
import com.kaicomsol.tpos.dialogs.ChooseAlertDialog;
import com.kaicomsol.tpos.dialogs.LanguageCustomDialog;
import com.kaicomsol.tpos.dialogs.PromptDialog;
import com.kaicomsol.tpos.utils.SharedDataSaveLoad;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SettingsActivity extends AppCompatActivity implements LanguageSelectListener {

    private boolean isSettings = false;
    private LanguageCustomDialog mLanguageDialog;
    private BluetoothAdapter mBluetoothAdapter;
    //Bind component
    @BindView(R.id.layout_settings)
    LinearLayout layout_settings;
    @BindView(R.id.layout_device_info)
    LinearLayout layout_device_info;
    @BindView(R.id.layout_device)
    RelativeLayout layout_device;
    @BindView(R.id.layout_reset_password)
    RelativeLayout layout_reset_password;
    @BindView(R.id.layout_language)
    RelativeLayout layout_language;
    @BindView(R.id.layout_signout)
    RelativeLayout layout_signout;

    //Bind Device component
    @BindView(R.id.btn_dc)
    TextView btn_dc;
    @BindView(R.id.btn_dr)
    TextView btn_dr;
    @BindView(R.id.edt_name)
    TextInputEditText edt_name;
    @BindView(R.id.edt_support)
    TextInputEditText edt_support;
    @BindView(R.id.txt_printer_name)
    TextView txt_printer_name;
    @BindView(R.id.txt_printer_address)
    TextView txt_printer_address;
    @BindView(R.id.txt_device_id)
    TextView txt_device_id;
    @BindView(R.id.btn_save)
    ImageView btn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.title_settings);

        ButterKnife.bind(this);
        initConfig();

    }

    private void initConfig(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mLanguageDialog = new LanguageCustomDialog();
        closeOptionsMenu();

        layout_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeviceInfo();
            }
        });

        layout_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLanguageDialog.showDialog(com.kaicomsol.tpos.activity.SettingsActivity.this, com.kaicomsol.tpos.activity.SettingsActivity.this);
            }
        });

        layout_reset_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(com.kaicomsol.tpos.activity.SettingsActivity.this, ChangePassActivity.class));

            }
        });

        layout_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        btn_dc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_dc.setEnabled(false);
                btn_dr.setEnabled(true);
                btn_dc.setTextColor(ContextCompat.getColor(com.kaicomsol.tpos.activity.SettingsActivity.this,R.color.green));
                btn_dr.setTextColor(ContextCompat.getColor(com.kaicomsol.tpos.activity.SettingsActivity.this,R.color.light_gray));

            }
        });
        btn_dr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_dc.setEnabled(true);
                btn_dr.setEnabled(false);
                btn_dr.setTextColor(ContextCompat.getColor(com.kaicomsol.tpos.activity.SettingsActivity.this,R.color.green));
                btn_dc.setTextColor(ContextCompat.getColor(com.kaicomsol.tpos.activity.SettingsActivity.this,R.color.light_gray));
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                infoSave();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backToSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showDeviceInfo(){
        isSettings = true;
        openOptionsMenu();
        layout_settings.setVisibility(View.GONE);
        layout_device_info.setVisibility(View.VISIBLE);
        btn_save.setVisibility(View.VISIBLE);
        printerInfo();
    }
    private void backToSettings(){
        if (isSettings){
            isSettings = false;
            layout_settings.setVisibility(View.VISIBLE);
            btn_save.setVisibility(View.GONE);
            layout_device_info.setVisibility(View.GONE);
        }else {
            finish();
        }
    }

    @Override
    public void onLanguageSelect(String lang) {
        setLanguage(lang);
    }

    private void setLanguage(String lang) {
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(lang.toLowerCase())); // API 17+ only.
        res.updateConfiguration(conf, dm);
        SharedDataSaveLoad.save(this, getString(R.string.preference_language_key), lang);
        restartActivity();
    }

    private void restartActivity() {
        Intent setting = getIntent();
        Intent home = com.kaicomsol.tpos.activity.HomeActivity.home.getIntent();

        com.kaicomsol.tpos.activity.HomeActivity.home.finish();
        com.kaicomsol.tpos.activity.SettingsActivity.this.finish();

        startActivity(home);
        startActivity(setting);
    }

    private void printerInfo(){
        String deviceID = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
        txt_device_id.setText(deviceID);
        if(mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()){
            final BluetoothDevice mBluetoothDevice = mBluetoothAdapter.getBondedDevices().iterator().next();
            String name = mBluetoothDevice.getName();
            String address = mBluetoothDevice.getAddress();
            txt_printer_name.setText(name);
            txt_printer_address.setText(address);

        }else Toast.makeText(this," There no printer connected",Toast.LENGTH_LONG).show();
    }

    private void infoSave(){
        String deviceId = txt_device_id.getText().toString();
        String printerName = txt_printer_name.getText().toString();
        String printerAddress = txt_printer_address.getText().toString();
        String dc_dr = btn_dc.isEnabled() ? "dc" : "dr";
        String operatorName = edt_name.getText().toString();
        String customerSupport = edt_support.getText().toString();

        SharedDataSaveLoad.save(this, getString(R.string.preference_device_id),deviceId);
        SharedDataSaveLoad.save(this, getString(R.string.preference_printer_name),printerName);
        SharedDataSaveLoad.save(this, getString(R.string.preference_printer_address),printerAddress);
        SharedDataSaveLoad.save(this, getString(R.string.preference_dc_dr),dc_dr);
        SharedDataSaveLoad.save(this, getString(R.string.preference_operator_name),operatorName);
        SharedDataSaveLoad.save(this, getString(R.string.preference_customer_support),customerSupport);
    }

    public void showLogoutDialog() {
        new ChooseAlertDialog(com.kaicomsol.tpos.activity.SettingsActivity.this)
                .setDialogType(PromptDialog.DIALOG_TYPE_WARNING)
                .setAnimationEnable(true)
                .setTitleText(getString(R.string.logout))
                .setContentText(getString(R.string.info_want_to_logout))
                .setNegativeListener(getString(R.string.cancel), new ChooseAlertDialog.OnNegativeListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .setPositiveListener(getString(R.string.yes), new ChooseAlertDialog.OnPositiveListener() {
                    @Override
                    public void onClick(ChooseAlertDialog dialog) {
                        dialog.dismiss();
                        logoutActivity();
                    }
                }).show();
    }

    private void logoutActivity() {
        SharedDataSaveLoad.remove(com.kaicomsol.tpos.activity.SettingsActivity.this, getString(R.string.preference_access_token));
        SharedDataSaveLoad.remove(this, getString(R.string.preference_is_service_check));
        Intent intent = new Intent(com.kaicomsol.tpos.activity.SettingsActivity.this, com.kaicomsol.tpos.activity.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getVersion(){
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }


}
