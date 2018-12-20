package com.kaicomsol.kpos.presenters;


import com.kaicomsol.kpos.callbacks.SubscriptionView;
import com.kaicomsol.kpos.model.APIErrors;
import com.kaicomsol.kpos.model.SubData;
import com.kaicomsol.kpos.services.APIClient;
import com.kaicomsol.kpos.utils.DebugLog;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class SubscriptionPresenter {

    private SubscriptionView mViewInterface;
    private APIClient mApiClient;

    public SubscriptionPresenter(SubscriptionView view) {
        this.mViewInterface = view;

        if (this.mApiClient == null) {
            this.mApiClient = new APIClient();
        }
    }

    public void getSubscriptionInfo(String token, String deviceId, String meterSerial) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Device-Id", deviceId);
        map.put("Content-Type", "application/json");

        mApiClient.getAPI()
                .getSubscriptionInfo(map, meterSerial)
                .enqueue(new Callback<SubData>() {
                    @Override
                    public void onResponse(Call<SubData> call, Response<SubData> response) {
                        if (response.isSuccessful()){
                            SubData subData = response.body();
                            if (subData != null) {
                                mViewInterface.onSuccess(subData);
                            } else {
                                mViewInterface.onError("Error fetching data");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SubData> call, Throwable e) {
                        DebugLog.e(call.request().toString());
                        if (e instanceof HttpException) {

                            int code = ((HttpException) e).response().code();
                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
                            errorHandle(code, responseBody);

                        }else if(e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error");
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IOException");
                        } else {
                            mViewInterface.onError("Unknown exception");
                        }
                    }
                });
    }


    private void errorHandle(int code, ResponseBody responseBody){
        if (code == 500) mViewInterface.onError(APIErrors.get500ErrorMessage(responseBody));
        else mViewInterface.onError(APIErrors.getErrorMessage(responseBody));
    }
}
