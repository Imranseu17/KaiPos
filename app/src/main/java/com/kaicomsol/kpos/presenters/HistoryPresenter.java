package com.kaicomsol.kpos.presenters;

import com.google.gson.JsonObject;
import com.kaicomsol.kpos.callbacks.HistoryView;
import com.kaicomsol.kpos.models.APIErrors;
import com.kaicomsol.kpos.models.SalesHistory;
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

public class HistoryPresenter {

    private HistoryView mViewInterface;
    private APIClient mApiClient;

    public HistoryPresenter(HistoryView view) {
        this.mViewInterface = view;

        if (this.mApiClient == null) {
            this.mApiClient = new APIClient();
        }
    }

    public void getSalesHistory(String token, final int currentPage, String start, String end) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("start", start);
        jsonObject.addProperty("end", end);
        jsonObject.addProperty("pageSize", "10");
        jsonObject.addProperty("pageNumber", currentPage);

        mApiClient.getAPI()
                .getSalesHistory(map, jsonObject)
                .enqueue(new Callback<SalesHistory>() {
                    @Override
                    public void onResponse(Call<SalesHistory> call, Response<SalesHistory> response) {
                        if (response.code() == 401) {
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()) {
                            SalesHistory salesHistory = response.body();
                            if (salesHistory != null) {
                                mViewInterface.onSuccess(salesHistory, currentPage);
                            } else {
                                mViewInterface.onError("Error fetching data");
                            }
                        } else errorHandle(response.code(), response.errorBody());
                    }

                    @Override
                    public void onFailure(Call<SalesHistory> call, Throwable e) {
                        e.printStackTrace();
                        mViewInterface.onError("ERROR");
                        if (e instanceof HttpException) {

                            int code = ((HttpException) e).response().code();
                            if (code == 401) {
                                mViewInterface.onLogout(code);
                                return;
                            }
                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
                            errorHandle(code,responseBody);

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
