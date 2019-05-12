package com.kaicomsol.kpos.activity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.callbacks.PosDeviceView;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.dialogs.PromptDialog;
import com.kaicomsol.kpos.presenters.PosDevicePresenter;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DeviceRegisterActivity extends AppCompatActivity implements PosDeviceView {

    private PosDevicePresenter mPresenter;
    //UI View Bind
    @BindView(R.id.main_wrapper)
    RelativeLayout mainWrapper;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.main_layout)
    LinearLayout mainLayout;
    @BindView(R.id.input_layout_token)
    TextInputLayout input_layout_token;
    @BindView(R.id.edt_token)
    TextInputEditText edt_token;
    @BindView(R.id.btn_submit)
    ImageView btn_submit;
    @BindView(R.id.txt_version)
    TextView txt_version;

    private String token = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_register);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Register POS");

        ButterKnife.bind(this);

        token = getIntent().getStringExtra("token");
        //init presenter
        txt_version.setText("Version : "+getVersion());
        mPresenter = new PosDevicePresenter(this);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitLogin();
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
                finish();
                break;
        }
        return true;
    }

    private void getDeviceRegister() {
        if (checkConnection()) {
            showAnimation();
            final String deviceId = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
            String pos_token = edt_token.getText().toString().trim();
            mPresenter.activatePosDevice(token, pos_token, deviceId);

        } else CustomAlertDialog.showError(this,getString(R.string.no_internet_connection));
    }

    /**
     * Validating form
     */
    private void submitLogin() {

        if (!validateToken()) {
            return;
        }

        hideKeyboard(this);
        getDeviceRegister();

    }

    @Override
    public void onSuccess(String success) {
        hideAnimation();
        showSuccessDialog(success);

    }

    @Override
    public void onError(String error) {
        hideErrorAnimation();
        CustomAlertDialog.showError(this,error);

    }

    private boolean validateToken() {
        String pos_token = edt_token.getText().toString().trim();
        if (TextUtils.isEmpty(pos_token)) {
            input_layout_token.setError("Enter pos device register token");
            requestFocus(edt_token);
            return false;
        } else {
            input_layout_token.setErrorEnabled(false);
        }

        return true;
    }


    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    public void showAnimation() {
        mainLayout.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("animation_loading.json");
        animationView.playAnimation();
        animationView.loop(true);
    }

    public void hideAnimation() {
        if (animationView.isAnimating()) animationView.cancelAnimation();
        mainLayout.setVisibility(View.VISIBLE);
        animationView.setVisibility(View.GONE);
    }

    public void hideErrorAnimation() {
        mainLayout.setVisibility(View.VISIBLE);
        if (animationView.isAnimating()) animationView.cancelAnimation();
        animationView.setVisibility(View.GONE);
    }

    private boolean checkConnection() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    public void showSuccessDialog(String message) {
        new PromptDialog(this)
                .setDialogType(PromptDialog.DIALOG_TYPE_SUCCESS)
                .setAnimationEnable(true)
                .setTitleText(getString(R.string.success))
                .setContentText(message)
                .setPositiveListener(getString(R.string.ok), new PromptDialog.OnPositiveListener() {
                    @Override
                    public void onClick(PromptDialog dialog) {
                        dialog.dismiss();
                        finish();
                    }
                }).show();
    }
}
