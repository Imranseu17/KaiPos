package com.kaicomsol.kpos.presenters;

import com.kaicomsol.kpos.callbacks.InvoiceView;
import com.kaicomsol.kpos.callbacks.MeterView;
import com.kaicomsol.kpos.model.APIErrors;
import com.kaicomsol.kpos.model.Invoices;
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

public class InvoicePresenter {

    private InvoiceView mViewInterface;
    private APIClient mApiClient;

    public InvoicePresenter(InvoiceView view) {
        this.mViewInterface = view;

        if (this.mApiClient == null) {
            this.mApiClient = new APIClient();
        }
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
                        if (response.isSuccessful()){
                            Invoices invoices = response.body();
                            if (invoices != null) {
                                mViewInterface.onSuccess(invoices);
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
                    public void onFailure(Call<Invoices> call, Throwable e) {
                        e.printStackTrace();
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
