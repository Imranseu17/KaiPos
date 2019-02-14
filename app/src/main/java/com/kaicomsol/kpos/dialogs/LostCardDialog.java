package com.kaicomsol.kpos.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.callbacks.CloseClickListener;
import com.kaicomsol.kpos.callbacks.LostCardListener;
import com.kaicomsol.kpos.utils.SharedDataSaveLoad;


import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LostCardDialog extends DialogFragment {

    //date picker dialog
    private DatePickerDialog mDatePickerDialog;
    private SimpleDateFormat dateFormatter;
    private String lostDate = "";
    private String select_thana = "";
    private static LostCardListener mLostCardListener = null;

    //Date picker dialog
    @BindView(R.id.btn_close)
    ImageView btn_close;
    @BindView(R.id.btn_submit)
    ImageView btn_submit;
    @BindView(R.id.edt_date)
    TextInputEditText edtDate;
    @BindView(R.id.edt_thana)
    TextInputEditText edtThana;
    @BindView(R.id.edt_gdno)
    TextInputEditText edtGDNo;
    @BindView(R.id.edt_remarks)
    TextInputEditText edtRemarks;
    @BindView(R.id.input_layout_date)
    TextInputLayout input_layout_date;
    @BindView(R.id.input_layout_thana)
    TextInputLayout thana_layout;
    @BindView(R.id.input_layout_gdno)
    TextInputLayout layout_gdNo;


    public static LostCardDialog newInstance(LostCardListener listener){
        LostCardDialog dialogFragment = new LostCardDialog();
        mLostCardListener = listener;

        return dialogFragment;



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lost_card_dialog, container, false);

        ButterKnife.bind(this, view);
        viewConfig();
        setDatePickerField();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setLayout(width, height);
        }
    }


    private void viewConfig(){
        dateFormatter = new SimpleDateFormat("MM-dd-yyyy");

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLostCardListener != null){
                    Dialog dialog = getDialog();
                    if (dialog != null) dialog.dismiss();
                    mLostCardListener.onDialogClose();
                }
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  submitData();
            }
        });

        edtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                mDatePickerDialog.show();
            }
        });

        edtThana.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu dropDownMenu = new PopupMenu(getActivity(), edtThana);
                dropDownMenu.getMenuInflater().inflate(R.menu.thana_drop_down_menu, dropDownMenu.getMenu());
                dropDownMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        edtThana.setText(menuItem.getTitle());
                        return true;
                    }
                });
                dropDownMenu.show();
            }
        });
    }

    private void setDatePickerField() {

        Calendar newCalendar = Calendar.getInstance();
        mDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);

                String monthString = String.valueOf(monthOfYear+1);
                if (monthString.length() == 1) {
                    monthString = "0" + monthString;
                }
                String dayString = String.valueOf(dayOfMonth);
                if (dayString.length() == 1) {
                    dayString = "0" + dayString;
                }
                lostDate = year+monthString+dayString;
                edtDate.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

    }

    private boolean validatelostDate() {

        if (TextUtils.isEmpty(edtDate.getText().toString().trim())) {
            input_layout_date.setError(getString(R.string.lostDate));
            requestFocus(edtDate);
            return false;
        } else {
            input_layout_date.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateThana() {

        if (TextUtils.isEmpty(edtThana.getText().toString().trim())) {
            thana_layout.setError(getString(R.string.select_thana));
            requestFocus(edtThana);
            return false;
        } else {
            thana_layout.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateGDNO() {

        if (TextUtils.isEmpty(edtGDNo.getText().toString().trim())) {
            layout_gdNo.setError(getString(R.string.gd_No));
            requestFocus(edtGDNo);
            return false;
        } else {
            layout_gdNo.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity(). getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void submitData() {


        if (!validatelostDate()) {
            return;
        }

        if (!validateThana()) {
            return;
        }

        if (!validateGDNO()) {
            return;
        }

       getData();

    }

    private void getData() {

        if (mLostCardListener != null){
            Dialog dialog = getDialog();
            if (dialog != null) dialog.dismiss();
            String date = edtDate.getText().toString();
            String thana = edtThana.getText().toString();
            String gdno = edtGDNo.getText().toString();
            String remarks = edtRemarks.getText().toString();
            mLostCardListener.onLostInfo(date,thana,gdno,remarks);
        }
    }
}
