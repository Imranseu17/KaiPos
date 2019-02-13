package com.kaicomsol.kpos.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.callbacks.ChangePassView;
import com.kaicomsol.kpos.dialogs.CustomAlertDialog;
import com.kaicomsol.kpos.presenters.ChangePassPresenter;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChangePassActivity extends AppCompatActivity implements ChangePassView {



    @BindView(R.id.old_password)
    TextInputEditText oldPasswordText;
    @BindView(R.id.new_password)
    TextInputEditText newPasswordText;
    @BindView(R.id.txt_version)
    TextView txt_version;
    @BindView(R.id.btn_resetPassword)
    ImageView resetPassword;
    @BindView(R.id.input_layout_oldPassword)
    TextInputLayout oldPasswordLayout;
    @BindView(R.id.input_layout_newPassword)
    TextInputLayout newPasswordLayout;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.main_layout)
    LinearLayout mainLayout;
    @BindView(R.id.input_layout_confirmPassword)
    TextInputLayout  layout_confirmPassword;
    @BindView(R.id.confirm_password)
    TextInputEditText confirm_password;

    ChangePassPresenter changePassPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Change password");

        changePassPresenter = new ChangePassPresenter(this);

        ButterKnife.bind(this);

        txt_version.setText("Version : "+getVersion());

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReset();
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

    private void submitReset() {

        if (!oldValidatePassword()) {
            return;
        }

        if(!newValidatePassword()){
            return;
        }

        if(!confirmValidatePassword())
            return;

        if(!matchValidatePassword())
            return;

        if(!matchRegularExpressionPassword())
            return;


        hideKeyboard(this);
        getResetPassword();

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

    private boolean oldValidatePassword() {
        String oldPassword = oldPasswordText.getText().toString().trim();
       if (oldPassword.isEmpty()) {
            oldPasswordLayout.setError(getString(R.string.err_msg_password));
            requestFocus(oldPasswordText);
            return false;
        } else if (oldPassword.length()<6) {
            oldPasswordLayout.setError(getString(R.string.err_msg_password_length));
            requestFocus(oldPasswordText);
            return false;
        } else {
            oldPasswordLayout.setErrorEnabled(false);

        }

        return true;
    }

    private boolean newValidatePassword() {

        String newPassword = newPasswordText.getText().toString().trim();
        if (newPassword.isEmpty()) {
            newPasswordLayout.setError(getString(R.string.err_msg_password));
            requestFocus(newPasswordText);
            return false;
        } else if (newPassword.length() < 6 ) {
            newPasswordLayout.setError(getString(R.string.err_msg_password_length));
            requestFocus(newPasswordText);
            return false;
        } else {
            newPasswordLayout.setErrorEnabled(false);
        }

        return true;
    }

    private boolean matchValidatePassword() {

        String newPassword = newPasswordText.getText().toString().trim();
        String confirmPassword = confirm_password.getText().toString().trim();
        if (!newPassword.equals(confirmPassword)) {
           CustomAlertDialog.showError(this,"Password is Not Matched" +
                   "\n"+"with Confirm Password Field");
            return false;
        } else {
            newPasswordLayout.setErrorEnabled(false);
            layout_confirmPassword.setErrorEnabled(false);
        }

        return true;
    }


    private boolean matchRegularExpressionPassword() {

        String pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
        String newPassword = newPasswordText.getText().toString().trim();

        if (!Pattern.matches(pattern,newPassword)) {
            CustomAlertDialog.showError(this,"Minimum eight characters"+"\n" +
                    " at least one letter" + "\n"+ "one number and one special character");
            return false;
        } else {
            newPasswordLayout.setErrorEnabled(false);

        }

        return true;
    }



    private boolean confirmValidatePassword() {

        String confirmPasswordText  = confirm_password.getText().toString().trim();
        if (confirmPasswordText.isEmpty()) {
            layout_confirmPassword.setError(getString(R.string.err_msg_password));
            requestFocus(confirm_password);
            return false;
        } else if (confirmPasswordText.length() < 6 ) {
           layout_confirmPassword.setError(getString(R.string.err_msg_password_length));
            requestFocus(confirm_password);
            return false;
        } else {
            layout_confirmPassword.setErrorEnabled(false);
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
    public void onSuccess(String success) {
        hideAnimation();
        CustomAlertDialog.showSuccess(this, success);
        oldPasswordText.setText("");
        newPasswordText.setText("");
        confirm_password.setText("");
    }

    @Override
    public void onError(String error) {
        hideAnimation();
        CustomAlertDialog.showError(this, error + "");
    }

    @Override
    public void onLogout(int code) {
        SharedDataSaveLoad.remove(this, getString(R.string.preference_access_token));
        SharedDataSaveLoad.remove(this, getString(R.string.preference_is_service_check));
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        this.finish();
    }

    private void getResetPassword() {
        if (checkConnection()) {
            showAnimation();
            String token = SharedDataSaveLoad.load(this, getString(R.string.preference_access_token));
            String userID = SharedDataSaveLoad.load(this,getString(R.string.preference_user_id));
            String oldPassword = oldPasswordText.getText().toString().trim();
            String newPassword = newPasswordText.getText().toString().trim();
            changePassPresenter.resetPassword(token,oldPassword,newPassword,userID);

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
    public void hideAnimation() {
        if (animationView.isAnimating()) animationView.cancelAnimation();
        mainLayout.setVisibility(View.VISIBLE);
        animationView.setVisibility(View.GONE);
    }

}
