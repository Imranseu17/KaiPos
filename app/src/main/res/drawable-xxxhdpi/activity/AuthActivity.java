package com.kaicomsol.tpos.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaicomsol.tpos.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AuthActivity extends AppCompatActivity implements View.OnClickListener {

    private StringBuilder builder;
    int current = 0;

    @BindView(R.id.img_close)
    ImageView imgClose;
    @BindView(R.id.edt_pin)
    EditText edtPin;
    @BindView(R.id.one)
    TextView one;
    @BindView(R.id.two)
    TextView two;
    @BindView(R.id.three)
    TextView three;
    @BindView(R.id.four)
    TextView four;
    @BindView(R.id.five)
    TextView five;
    @BindView(R.id.six)
    TextView six;
    @BindView(R.id.seven)
    TextView seven;
    @BindView(R.id.eight)
    TextView eight;
    @BindView(R.id.nine)
    TextView nine;
    @BindView(R.id.zero)
    TextView zero;
    @BindView(R.id.delete)
    ImageView delete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_auth);

        ButterKnife.bind(this);

        //Button click listener
        imgClose.setOnClickListener(this);
        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        four.setOnClickListener(this);
        five.setOnClickListener(this);
        six.setOnClickListener(this);
        seven.setOnClickListener(this);
        eight.setOnClickListener(this);
        nine.setOnClickListener(this);
        zero.setOnClickListener(this);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String txt = edtPin.getText().toString();
                if (txt != null && txt.length() > 0 ){
                    txt = txt.substring(0, txt.length() - 1);
                }
                edtPin.setText(txt);
                edtPin.setSelection(edtPin.getText().length());
            }
        });


        builder = new StringBuilder();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_close:
                finish();
                break;
                
            case R.id.one:
                builder.append("1");
                edtPin.append("1");
                break;

            case R.id.two:
                builder.append("2");
                edtPin.append("2");
                break;

            case R.id.three:
                builder.append("3");
                edtPin.append("3");
                break;

            case R.id.four:
                builder.append("4");
                edtPin.append("4");
                break;

            case R.id.five:
                builder.append("5");
                edtPin.append("5");
                break;

            case R.id.six:
                builder.append("6");
                edtPin.append("6");
                break;

            case R.id.seven:
                builder.append("7");
                edtPin.append("7");
                break;


            case R.id.eight:
                builder.append("8");
                edtPin.append("8");
                break;

            case R.id.nine:
                builder.append("9");
                edtPin.append("9");
                break;

            case R.id.zero:
                builder.append("0");
                edtPin.append("0");
                break;

        }
    }
}
