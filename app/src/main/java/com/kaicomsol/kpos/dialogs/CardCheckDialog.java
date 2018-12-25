package com.kaicomsol.kpos.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kaicomsol.kpos.R;
import com.kaicomsol.kpos.callbacks.CloseClickListener;

public class CardCheckDialog extends DialogFragment {

    private static CloseClickListener mCloseClickListener = null;
    public static CardCheckDialog newInstance(CloseClickListener listener, String service){
        CardCheckDialog dialogFragment = new CardCheckDialog();
        mCloseClickListener = listener;
        Bundle bundle = new Bundle();
        bundle.putString("service",service);
        dialogFragment.setArguments(bundle);

        return dialogFragment;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.card_check_dialog, container, false);
        ImageView btn_close = (ImageView) view.findViewById(R.id.btn_close);
        TextView txt_service = (TextView) view.findViewById(R.id.txt_service);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCloseClickListener != null){
                    Dialog dialog = getDialog();
                    if (dialog != null) dialog.dismiss();
                    mCloseClickListener.onCloseClick(1);
                }
            }
        });
        String service = getArguments().getString("service");
        txt_service.setText(service);

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
