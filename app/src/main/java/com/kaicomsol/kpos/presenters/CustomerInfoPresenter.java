package com.kaicomsol.kpos.presenters;


import com.kaicomsol.kpos.callbacks.CustomerInfoView;
import com.kaicomsol.kpos.model.APIErrors;
import com.kaicomsol.kpos.model.CustomerInfo;
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

public class CustomerInfoPresenter {

    private CustomerInfoView mViewInterface;
    private APIClient mApiClient;

    public CustomerInfoPresenter(CustomerInfoView view) {
        this.mViewInterface = view;

        if (this.mApiClient == null) {
            this.mApiClient = new APIClient();
        }
    }

    public void getCustomerInfo(String token, String deviceId, String accountNo) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Device-Id", deviceId);
        map.put("Content-Type", "application/json");

        mApiClient.getAPI()
                .getCustomerInfo(map, accountNo)
                .enqueue(new Callback<CustomerInfo>() {
                    @Override
                    public void onResponse(Call<CustomerInfo> call, Response<CustomerInfo> response) {

                        if (response.code() == 401){
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()) {
                            CustomerInfo customerInfo = response.body();
                            if (customerInfo != null) {
                                mViewInterface.onSuccess(customerInfo);
                            } else {
                                mViewInterface.onError("Error");
                            }
                        } else {
                            try {
                                JSONObject jObjError = new JSONObject(response.errorBody().string());
                                mViewInterface.onError(jObjError.getString("message"));
                            } catch (Exception e) {
                                mViewInterface.onError("Error occurred! Please try again");
                            }
                        }

                    }

                    @Override
                    public void onFailure(Call<CustomerInfo> call, Throwable e) {

                        if (e instanceof HttpException) {

                            int code = ((HttpException) e).response().code();
                            if (code ==401){
                                mViewInterface.onLogout(code);
                                return;
                            }
                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
                            errorHandle(code, responseBody);

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

    private void errorHandle(int code, ResponseBody responseBody){
        if (code == 500) mViewInterface.onError(APIErrors.get500ErrorMessage(responseBody));
        else if(code == 406){
            try {
                JSONObject jObjError = new JSONObject(responseBody.string());
                mViewInterface.onError(jObjError.getString("message"));
            } catch (Exception e) {
                mViewInterface.onError(e.getMessage());
            }
        }
        else mViewInterface.onError(APIErrors.getErrorMessage(responseBody));
    }

}
