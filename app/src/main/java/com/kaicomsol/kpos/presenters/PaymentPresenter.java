package com.kaicomsol.kpos.presenters;

import com.google.gson.JsonObject;
import com.kaicomsol.kpos.callbacks.PaymentView;
import com.kaicomsol.kpos.model.APIErrors;
import com.kaicomsol.kpos.model.Invoices;
import com.kaicomsol.kpos.model.Payment;
import com.kaicomsol.kpos.model.PaymentID;
import com.kaicomsol.kpos.services.APIClient;
import com.kaicomsol.kpos.utils.DebugLog;

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

public class PaymentPresenter {

    private PaymentView mViewInterface;
    private APIClient mApiClient;

    public PaymentPresenter(PaymentView view) {
        this.mViewInterface = view;

        if (this.mApiClient == null) {
            this.mApiClient = new APIClient();
        }
    }

    public void addPayment(String token, String amount, String cardNo, String historyNo, String paymentMethodId) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("amount", amount);
        jsonObject.addProperty("cardNo", cardNo);
        jsonObject.addProperty("cardHistoryNo", historyNo);
        jsonObject.addProperty("paymentMethodId", paymentMethodId);

        DebugLog.e(jsonObject.toString());

        mApiClient.getAPI()
                .addPayment(map, jsonObject)
                .enqueue(new Callback<Payment>() {
                    @Override
                    public void onResponse(Call<Payment> call, Response<Payment> response) {

                        if (response.isSuccessful()) {
                            Payment payment = response.body();
                            if (payment != null) {
                                mViewInterface.onSuccess(payment);
                            } else {
                                mViewInterface.onError("Error fetching data", 100);
                            }
                        } else getErrorMessage(response.code(), response.errorBody());


                    }

                    @Override
                    public void onFailure(Call<Payment> call, Throwable e) {
                        DebugLog.e(call.request().toString());
                        if (e instanceof HttpException) {

                            int code = ((HttpException) e).response().code();
                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
                            getErrorMessage(code, responseBody);

                        } else if (e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error", 100);
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IOException", 100);
                        } else {
                            mViewInterface.onError("Unknown exception", 100);
                        }
                    }
                });
    }

    public void capturePayment(String token, String paymentId) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        mApiClient.getAPI()
                .capturePayment(map, paymentId)
                .enqueue(new Callback<PaymentID>() {
                    @Override
                    public void onResponse(Call<PaymentID> call, Response<PaymentID> response) {

                        if (response.code() == 401) {
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()) {
                            mViewInterface.onSuccess(1);
                        } else {
                            if (response.code() == 500)
                                mViewInterface.onError(APIErrors.get500ErrorMessage(response.errorBody()), 300);
                            else
                                mViewInterface.onError(APIErrors.getErrorMessage(response.errorBody()), 300);
                        }


                    }

                    @Override
                    public void onFailure(Call<PaymentID> call, Throwable e) {
                        DebugLog.e(call.request().toString());
                        if (e instanceof HttpException) {

                            int code = ((HttpException) e).response().code();
                            if (code == 401) {
                                mViewInterface.onLogout(code);
                                return;
                            } else {
                                ResponseBody responseBody = ((HttpException) e).response().errorBody();
                                if (code == 500)
                                    mViewInterface.onError(APIErrors.get500ErrorMessage(responseBody), 300);
                                else
                                    mViewInterface.onError(APIErrors.getErrorMessage(responseBody), 300);
                            }
                        } else if (e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error", 300);
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IOException", 300);
                        } else {
                            mViewInterface.onError("Unknown exception", 300);
                        }
                    }
                });
    }

    public void getInvoices(String token, String cardNo) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(cardNo);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        mApiClient.getAPI()
                .getInvoices(map, cardNo)
                .enqueue(new Callback<Invoices>() {
                    @Override
                    public void onResponse(Call<Invoices> call, Response<Invoices> response) {

                        if (response.code() == 401) {
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()) {
                            Invoices invoices = response.body();
                            if (invoices != null) {
                                mViewInterface.onSuccess(invoices);
                            } else {
                                // mViewInterface.onError("Error fetching data");
                                mViewInterface.onError(null, 200);
                            }
                        } else {
                            mViewInterface.onError(null, 200);
//                            try {
//                                JSONObject jObjError = new JSONObject(response.errorBody().string());
//                                mViewInterface.onError(jObjError.getString("message"));
//                            } catch (Exception e) {
//                                mViewInterface.onError("Error occurred! Please try again");
//                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Invoices> call, Throwable e) {
                        e.printStackTrace();
                        int code = ((HttpException) e).response().code();
                        if (code == 401) {
                            mViewInterface.onLogout(code);
                            return;
                        } else mViewInterface.onError(null, 200);
//                        if (e instanceof HttpException) {
//                            int code = ((HttpException) e).response().code();
//                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
//                            try {
//                                JSONObject jObjError = new JSONObject(responseBody.string());
//                                mViewInterface.onError(jObjError.getString("message"));
//                            } catch (Exception e2) {
//                                mViewInterface.onError("Error occurred! Please try again");
//                            }
//                        } else if (e instanceof SocketTimeoutException) {
//
//                            mViewInterface.onError("Server connection error");
//                        } else if (e instanceof IOException) {
//                            mViewInterface.onError("IOException");
//                        } else {
//                            mViewInterface.onError("Unknown exception");
//                        }
                    }
                });
    }

    private void getErrorMessage(int code, ResponseBody responseBody) {
        switch (code) {
            case 500:
                mViewInterface.onError(APIErrors.get500ErrorMessage(responseBody), 100);
                break;
            case 400:
                mViewInterface.onError(APIErrors.get500ErrorMessage(responseBody), 100);
                break;
            case 401:
                mViewInterface.onLogout(code);
                break;
            default:
                mViewInterface.onError(APIErrors.getErrorMessage(responseBody), 100);
        }
    }


}
