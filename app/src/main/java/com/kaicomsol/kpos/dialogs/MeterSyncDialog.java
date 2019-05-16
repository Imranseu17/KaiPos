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

public class MeterSyncDialog extends DialogFragment {

    private static CloseClickListener mCloseClickListener = null;
    public static MeterSyncDialog newInstance(CloseClickListener listener, String service){
        MeterSyncDialog dialogFragment = new MeterSyncDialog();
        mCloseClickListener = listener;
        Bundle bundle = new Bundle();
        bundle.putString("msg",service);
        dialogFragment.setArguments(bundle);

        return dialogFragment;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.meter_sync_dialog, container, false);
        ImageView btn_close = (ImageView) view.findViewById(R.id.btn_close);
        TextView txt_msg = (TextView) view.findViewById(R.id.txt_msg);
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
        String service = getArguments().getString("msg");
        txt_msg.setText(service);

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
