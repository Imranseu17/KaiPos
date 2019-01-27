package com.kaicomsol.kpos.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kaicomsol.kpos.activity.RechargeActivity;
import com.kaicomsol.kpos.golobal.GlobalBus;
import com.kaicomsol.kpos.utils.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        String status = NetworkUtil.getConnectivityStatusString(context);
        GlobalBus.getBus().post(status);

    }
}
