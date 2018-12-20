package com.kaicomsol.kpos.presenters;

import com.google.gson.JsonObject;
import com.kaicomsol.kpos.callbacks.RefundView;
import com.kaicomsol.kpos.model.APIErrors;
import com.kaicomsol.kpos.model.Refund;
import com.kaicomsol.kpos.model.Success;
import com.kaicomsol.kpos.services.APIClient;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.WorkStatus;

import org.json.JSONArray;
import org.json.JSONException;
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

    public void getIssueRefund(String token,String cardNo, String credit, String refund) {
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
                        DebugLog.i(call.request()+" || "+response.code());

                        if (response.isSuccessful()){
                            Refund refund = response.body();
                            if (refund != null) {
                                mViewInterface.onSuccess(refund);
                            } else {
                                mViewInterface.onError("Error fetching data");
                            }
                        }else {
                            try {
                                JSONObject jsonObject = new JSONObject(response.errorBody().string());
                                JSONArray jsonArray = jsonObject.getJSONArray("fieldErrors");
                                JSONObject jsonError = jsonArray.getJSONObject(0);
                                mViewInterface.onError(jsonError.getString("message"));
                            } catch (Exception e) {
                                mViewInterface.onError("Error occurred Please try again");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Refund> call, Throwable e) {
                        DebugLog.e(call.request().toString());
                        if (e instanceof HttpException) {
                            int code = ((HttpException) e).response().code();
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

    public void updateRefund(String token,String id) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        jsonObject.addProperty("status", WorkStatus.Complete.getIntValue());

        mApiClient.getAPI()
                .updateRefund(map, jsonObject)
                .enqueue(new Callback<Success>() {
                    @Override
                    public void onResponse(Call<Success> call, Response<Success> response) {
                        if (response.isSuccessful()){
                            Success success = response.body();
                            mViewInterface.onSuccess(success);
                        }else errorHandle(response.code(), response.errorBody());

                    }

                    @Override
                    public void onFailure(Call<Success> call, Throwable e) {
                        DebugLog.e(call.request().toString());
                        mViewInterface.onError("ERROR");
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
