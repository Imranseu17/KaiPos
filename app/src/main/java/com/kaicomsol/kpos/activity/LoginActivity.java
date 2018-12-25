package com.kaicomsol.kpos.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.callbacks.LoginView;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.model.Login;
import com.kaicomsol.kpos.presenters.LoginPresenter;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class LoginActivity extends AppCompatActivity implements LoginView {


    private LoginPresenter mPresenter;
    //UI View Bind
    @BindView(R.id.main_wrapper)
    RelativeLayout mainWrapper;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.main_layout)
    LinearLayout mainLayout;
    @BindView(R.id.input_layout_email)
    TextInputLayout input_layout_email;
    @BindView(R.id.edt_email)
    TextInputEditText edt_email;
    @BindView(R.id.input_layout_password)
    TextInputLayout input_layout_password;
    @BindView(R.id.edt_password)
    TextInputEditText edt_password;
    @BindView(R.id.btn_login)
    ImageView btn_login;
    @BindView(R.id.txt_version)
    TextView txt_version;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.login));

        ButterKnife.bind(this);

        //init presenter
        txt_version.setText("Version : "+getVersion());
        mPresenter = new LoginPresenter(this);
        btn_login.setOnClickListener(new View.OnClickListener() {
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

    private void getLogin() {
        if (checkConnection()) {
            showAnimation();
            final String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID);
            DebugLog.i(deviceId);
            SharedDataSaveLoad.save(this,getString(R.string.preference_meter_serial), deviceId);
            String email = edt_email.getText().toString().trim();
            String password = edt_password.getText().toString().trim();
            mPresenter.attemptLogin(deviceId,email,password);

        } else CustomAlertDialog.showError(this,getString(R.string.no_internet_connection));
    }

    /**
     * Validating form
     */
    private void submitLogin() {


        if (!validateEmail()) {
            return;
        }

        if (!validatePassword()) {
            return;
        }

        hideKeyboard(this);
        getLogin();

    }

    @Override
    public void onSuccess(Login login) {

        hideAnimation();
        JWT jwt = new JWT(login.getToken());
        Claim claim = jwt.getClaim("deviceId");
        if (claim.asString() != null) goDashboard(login.getToken());
        else goDeviceRegister(login.getToken());
    }

    @Override
    public void onError(String error) {
        hideErrorAnimation();
        CustomAlertDialog.showError(this,error);

    }

    private void goDashboard(String token){
        SharedDataSaveLoad.save(this, getString(R.string.preference_access_token), "Bearer "+token);
        Intent intent = new Intent(this, NFCCheckActivity.class);
        intent.putExtra("path","service");
        startActivity(intent);
        finish();
    }

    private void goDeviceRegister(String token){
        Intent intent = new Intent(this, DeviceRegisterActivity.class);
        intent.putExtra("token", "Bearer "+token);
        startActivity(intent);
    }

    private boolean validateEmail() {
        String email = edt_email.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            input_layout_email.setError(getString(R.string.err_msg_email));
            requestFocus(edt_email);
            return false;
        } else {
            input_layout_email.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validatePassword() {
        String password = edt_password.getText().toString().trim();
        if (password.isEmpty()) {
            input_layout_password.setError(getString(R.string.err_msg_password));
            requestFocus(edt_password);
            return false;
        } else if (password.length() < 6) {
            input_layout_password.setError(getString(R.string.err_msg_password_length));
            requestFocus(edt_password);
            return false;
        } else {
            input_layout_password.setErrorEnabled(false);
        }

        return true;
    }

    public static boolean isValidEmail(String email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
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
}
