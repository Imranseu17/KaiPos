package com.kaicomsol.kpos.presenters;


import com.kaicomsol.kpos.callbacks.TransactionView;
import com.kaicomsol.kpos.model.APIErrors;
import com.kaicomsol.kpos.model.Transaction;
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

public class TransactionPresenter {

    private TransactionView mViewInterface;
    private APIClient mApiClient;

    public TransactionPresenter(TransactionView view) {
        this.mViewInterface = view;

        if (this.mApiClient == null) {
            this.mApiClient = new APIClient();
        }
    }

    public void getTransactionInfo(String token, String deviceId, String accountNo, String customerCode) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Device-Id", deviceId);
        map.put("Content-Type", "application/json");

        mApiClient.getAPI()
                .getTransitionInfo(map, accountNo, customerCode)
                .enqueue(new Callback<List<Transaction>>() {
                    @Override
                    public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {

                        if (response.code() == 401){
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()){
                            List<Transaction> transactionList = response.body();
                            if (transactionList != null) {
                                mViewInterface.onSuccess(transactionList);
                            } else {
                                mViewInterface.onError("Error fetching data");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Transaction>> call, Throwable e) {
                        e.printStackTrace();
                        if (e instanceof HttpException) {

                            int code = ((HttpException) e).response().code();
                            if (code == 401){
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
        else mViewInterface.onError(APIErrors.getErrorMessage(responseBody));
    }
}
