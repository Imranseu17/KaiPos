package com.kaicomsol.kpos.presenters;


import com.kaicomsol.kpos.callbacks.CustomerView;
import com.kaicomsol.kpos.models.APIErrors;
import com.kaicomsol.kpos.models.CustomerData;
import com.kaicomsol.kpos.models.Like;
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

public class CustomerPresenter {

    private CustomerView mViewInterface;
    private APIClient mApiClient;

    public CustomerPresenter(CustomerView view) {
        this.mViewInterface = view;

        if (this.mApiClient == null) {
            this.mApiClient = new APIClient();
        }
    }

    public void findCustomerByProperty(String token, Like like,final int currentPage) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");


        mApiClient.getAPI()
                .findCustomer(map, like.account, like.customerCode, like.cardNo, like.meterSerial, like.zone, like.area, like.address, like.apartment,currentPage,5)
                .enqueue(new Callback<CustomerData>() {
                    @Override
                    public void onResponse(Call<CustomerData> call, Response<CustomerData> response) {

                        if (response.code() == 401){
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()){
                            CustomerData customerData = response.body();
                            if (customerData != null) {
                                mViewInterface.onSuccess(customerData,currentPage);
                            } else {
                                mViewInterface.onError("Error fetching data");
                            }
                        }else{
                            try {
                                JSONObject jObjError = new JSONObject(response.errorBody().string());
                                mViewInterface.onError(jObjError.getString("message"));
                            } catch (Exception e) {
                                mViewInterface.onError("Error occurred! Please try again");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<CustomerData> call, Throwable e) {
                        DebugLog.e(call.request().toString());
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
