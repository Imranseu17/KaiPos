package com.kaicomsol.kpos.presenters;

import com.kaicomsol.kpos.callbacks.PosDeviceView;
import com.kaicomsol.kpos.models.APIErrors;
import com.kaicomsol.kpos.services.APIClient;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.ErrorCode;

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

public class PosDevicePresenter {

    private PosDeviceView mViewInterface;
    private APIClient mApiClient;

    public PosDevicePresenter(PosDeviceView view) {
        this.mViewInterface = view;

        if (this.mApiClient == null) {
            this.mApiClient = new APIClient();
        }
    }

    public void activatePosDevice(String token, String pos_token, String device_id) {
        Map<String, String> map = new HashMap<>();
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        mApiClient.getAPI()
                .activatePosDevice(map, pos_token, device_id)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()){
                            mViewInterface.onSuccess("Your POS token successfully saved!");
                        }else errorHandle(response.code(),response.errorBody());
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable e) {
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
                            mViewInterface.onError("Server connection error!");
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IO Exception");
                        } else {
                            mViewInterface.onError("Unknown error");
                        }
                    }
                });
    }

    private void errorHandle(int code, ResponseBody responseBody){
        if (code == ErrorCode.ERRORCODE500.getCode()) mViewInterface.onError(APIErrors.get500ErrorMessage(responseBody));
        else if(code == ErrorCode.ERRORCODE406.getCode()){
            mViewInterface.onError(APIErrors.get406ErrorMessage(responseBody));
        }else mViewInterface.onError(APIErrors.getErrorMessage(responseBody));
    }
}
