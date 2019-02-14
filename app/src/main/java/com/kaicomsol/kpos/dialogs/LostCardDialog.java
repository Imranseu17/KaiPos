package com.kaicomsol.kpos.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.callbacks.CloseClickListener;
import com.kaicomsol.kpos.callbacks.LostCardListener;

import java.util.ArrayList;

public class LostCardDialog extends DialogFragment {

    private static LostCardListener mLostCardListener = null;
    public static LostCardDialog newInstance(LostCardListener listener){
        LostCardDialog dialogFragment = new LostCardDialog();
        mLostCardListener = listener;

        return dialogFragment;



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.lost_card_dialog, container, false);
        ImageView btn_close = (ImageView) view.findViewById(R.id.btn_close);
        ImageView btn_submit = (ImageView) view.findViewById(R.id.btn_submit);
        final TextInputEditText edtGDNo = (TextInputEditText) view.findViewById(R.id.edt_gdno);
        final TextInputEditText edtCardNo = (TextInputEditText) view.findViewById(R.id.edt_cardno);
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
                if (mLostCardListener != null){
                    Dialog dialog = getDialog();
                    if (dialog != null) dialog.dismiss();
                    String gdno = edtGDNo.getText().toString();
                    String cardNo = edtCardNo.getText().toString();
                    mLostCardListener.onLostInfo(gdno,cardNo);
                }
            }
        });

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
}
