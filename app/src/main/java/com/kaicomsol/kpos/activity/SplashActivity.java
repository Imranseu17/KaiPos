package com.kaicomsol.kpos.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashActivity extends AppCompatActivity {


    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3000;

    //Bind component
    @BindView(R.id.img_logo)
    ImageView imageLogo;
    @BindView(R.id.txt_welcome)
    TextView textWelcome;
    @BindView(R.id.txt_copyright)
    TextView txtCopyright;
    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        ButterKnife.bind(this);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.transition);
        token = SharedDataSaveLoad.load(SplashActivity.this, getString(R.string.preference_access_token));

        //start animation
        imageLogo.startAnimation(animation);
        textWelcome.startAnimation(animation);
        txtCopyright.startAnimation(animation);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                  checkLogin();
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void checkLogin(){
        if (TextUtils.isEmpty(token)){
            goLogin();
        }else if (!TextUtils.isEmpty(token)){
            goHome();
        }
    }

    private void goHome(){
        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void goLogin(){
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
