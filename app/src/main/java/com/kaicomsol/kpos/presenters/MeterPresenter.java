package com.kaicomsol.kpos.presenters;


import com.kaicomsol.kpos.callbacks.MeterView;
import com.kaicomsol.kpos.model.Meter;
import com.kaicomsol.kpos.services.APIClient;
import com.kaicomsol.kpos.utils.DebugLog;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class MeterPresenter {

    private MeterView mViewInterface;
    private APIClient mApiClient;

    public MeterPresenter(MeterView view) {
        this.mViewInterface = view;

        if (this.mApiClient == null) {
            this.mApiClient = new APIClient();
        }
    }

    public void getMeterInfo(String token, String deviceId, String accountNo) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Device-Id", deviceId);
        map.put("Content-Type", "application/json");

        mApiClient.getAPI()
                .getMeterInfo(map, accountNo)
                .enqueue(new Callback<Meter>() {
                    @Override
                    public void onResponse(Call<Meter> call, Response<Meter> response) {

                        if (response.code() == 401){
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()){
                            Meter meter = response.body();
                            if (meter != null) {
                                mViewInterface.onSuccess(meter);
                            } else {
                                mViewInterface.onError("Error fetching data");
                            }
                        }else {
                            try {
                                JSONObject jObjError = new JSONObject(response.errorBody().string());
                                mViewInterface.onError(jObjError.getString("message"));
                            } catch (Exception e) {
                                mViewInterface.onError("Error occurred! Please try again");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Meter> call, Throwable e) {
                        e.printStackTrace();
                        if (e instanceof HttpException) {
                            int code = ((HttpException) e).response().code();
                            if (code == 401){
                                mViewInterface.onLogout(code);
                                return;
                            }
                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
                            try {
                                JSONObject jObjError = new JSONObject(responseBody.string());
                                mViewInterface.onError(jObjError.getString("message"));
                            } catch (Exception e2) {
                                mViewInterface.onError("Error occurred! Please try again");
                            }
                        } else if (e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error");
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IOException");
                        } else {
                            mViewInterface.onError("Unknown exception");
                        }
                    }
                });
    }

    private String getErrorMessage(ResponseBody responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody.string());
            return jsonObject.getString("message");
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
