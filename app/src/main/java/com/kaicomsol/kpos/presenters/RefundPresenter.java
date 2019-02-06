package com.kaicomsol.kpos.presenters;

import com.google.gson.JsonObject;
import com.kaicomsol.kpos.callbacks.RefundView;
import com.kaicomsol.kpos.models.APIErrors;
import com.kaicomsol.kpos.models.Refund;
import com.kaicomsol.kpos.models.Success;
import com.kaicomsol.kpos.models.UpdateResponse;
import com.kaicomsol.kpos.services.APIClient;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.WorkStatus;

import org.json.JSONArray;
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

public class RefundPresenter {

    private RefundView mViewInterface;
    private APIClient mApiClient;

    public RefundPresenter(RefundView view) {
        this.mViewInterface = view;

        if (this.mApiClient == null) {
            this.mApiClient = new APIClient();
        }
    }

    public void getIssueRefund(String token, String cardNo, String credit, final String refund) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("cardNumber", cardNo);
        jsonObject.addProperty("creditGasUnit", credit);
        jsonObject.addProperty("refundGasUnit", refund);


        mApiClient.getAPI()
                .getIssueRefund(map, jsonObject)
                .enqueue(new Callback<Refund>() {
                    @Override
                    public void onResponse(Call<Refund> call, Response<Refund> response) {

                        if (response.code() == 401) {
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()){
                            Refund refund = response.body();
                            if (refund != null) {
                                mViewInterface.onSuccess(refund);
                            } else {
                                mViewInterface.onError("Error fetching data");
                            }
                        }else errorHandle(response.code(), response.errorBody());
                    }

                    @Override
                    public void onFailure(Call<Refund> call, Throwable e) {
                        DebugLog.e(call.request().toString());
                        if (e instanceof HttpException) {
                            int code = ((HttpException) e).response().code();
                            if (code == 401) {
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

                            mViewInterface.onError(null);
                        } else if (e instanceof IOException) {
                            mViewInterface.onError(null);
                        } else {
                            mViewInterface.onError(null);
                        }
                    }
                });
    }

    public void updateRefund(String token, String id) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("status", WorkStatus.Complete.getIntValue());

        mApiClient.getAPI()
                .updateRefund(map, jsonObject)
                .enqueue(new Callback<UpdateResponse>() {
                    @Override
                    public void onResponse(Call<UpdateResponse> call, Response<UpdateResponse> response) {
                        if (response.code() == 401) {
                            mViewInterface.onLogout(response.code());
                            return;
                        }
                        if (response.isSuccessful()){
                            UpdateResponse updateResponse = response.body();
                            mViewInterface.onSuccess(updateResponse);
                        }else errorHandle(response.code(), response.errorBody());

                    }

                    @Override
                    public void onFailure(Call<UpdateResponse> call, Throwable e) {
                        DebugLog.e(call.request().toString());
                        if (e instanceof HttpException) {

                            int code = ((HttpException) e).response().code();
                            if (code == 401) {
                                mViewInterface.onLogout(code);
                                return;
                            }
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
        else if(code == 406){
            mViewInterface.onError(APIErrors.get406ErrorMessage(responseBody));
        }else mViewInterface.onError(APIErrors.getErrorMessage(responseBody));
    }
}
