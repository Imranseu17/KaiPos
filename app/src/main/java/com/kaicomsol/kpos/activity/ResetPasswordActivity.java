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
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.callbacks.ResetPasswordCallbacks;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ResetPasswordActivity extends AppCompatActivity implements ResetPasswordCallbacks {

    @BindView(R.id.old_password)
    TextInputEditText oldPasswordText;

    @BindView(R.id.new_password)
    TextInputEditText newPasswordText;

    @BindView(R.id.txt_version)
    TextView txt_version;

    @BindView(R.id.btn_resetPassword)
    Button resetPassword;

    @BindView(R.id.input_layout_oldPassword)
    TextInputLayout oldPasswordLayout;

    @BindView(R.id.input_layout_newPassword)
    TextInputLayout newPasswordLayout;


    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.main_layout)
    LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password");

        ButterKnife.bind(this);

        txt_version.setText("Version : "+getVersion());

        resetPassword.setOnClickListener(new View.OnClickListener() {
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void submitLogin() {



        if (!validatePassword()) {
            return;
        }



        hideKeyboard(this);
        getresetPassword();

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

    private boolean validatePassword() {
        String oldPassword = oldPasswordText.getText().toString().trim();
        String newPassword = newPasswordText.getText().toString().trim();
        if (oldPassword.isEmpty() && newPassword.isEmpty()) {
            oldPasswordLayout.setError(getString(R.string.err_msg_password));
            newPasswordLayout.setError(getString(R.string.err_msg_password));
            requestFocus(oldPasswordText);
            requestFocus(newPasswordText);
            return false;
        } else if (oldPassword.length() < 6 && newPassword.length()<6) {
            oldPasswordLayout.setError(getString(R.string.err_msg_password_length));
            newPasswordLayout.setError(getString(R.string.err_msg_password_length));
            requestFocus(oldPasswordText);
            requestFocus(newPasswordText);
            return false;
        } else {
            oldPasswordLayout.setErrorEnabled(false);
            newPasswordLayout.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
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

    @Override
    public void onSuccess(boolean success) {

    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void onLogout(int code) {

    }

    private void getresetPassword() {
        if (checkConnection()) {
            showAnimation();

            SharedDataSaveLoad.save(this,getString(R.string.preference_meter_serial), deviceId);
            String email = edt_email.getText().toString().trim();
            String password = edt_password.getText().toString().trim();
            mPresenter.attemptLogin(deviceId,email,password);

        } else CustomAlertDialog.showError(this,getString(R.string.no_internet_connection));
    }

    public void showAnimation() {
        mainLayout.setVisibility(View.GONE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation("animation_loading.json");
        animationView.playAnimation();
        animationView.loop(true);
    }

    private boolean checkConnection() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}
