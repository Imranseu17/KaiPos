package com.kaicomsol.kpos.presenters;

import com.kaicomsol.kpos.callbacks.CaptureView;
import com.kaicomsol.kpos.callbacks.StateView;
import com.kaicomsol.kpos.models.APIErrors;
import com.kaicomsol.kpos.models.PaymentID;
import com.kaicomsol.kpos.services.APIClient;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.RechargeStatus;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class CapturePresenter {

    private CaptureView mViewInterface;
    private APIClient mApiClient;

    public CapturePresenter(CaptureView view) {
        this.mViewInterface = view;

        if (this.mApiClient == null) {
            this.mApiClient = new APIClient();
        }
    }

    public void capturePayment(String token, final String paymentId) {
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
                            PaymentID payment = response.body();
                            if (payment != null) {
                                mViewInterface.onCaptureSuccess(payment.getPaymentId());
                            } else {
                                mViewInterface.onError("Error fetching data", RechargeStatus.CAPTURE_ERROR.getCode());
                            }
                        } else getErrorMessage(response.code(), response.errorBody());


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
                                    mViewInterface.onError(APIErrors.get500ErrorMessage(responseBody), RechargeStatus.CAPTURE_ERROR.getCode());
                                else
                                    mViewInterface.onError(APIErrors.getErrorMessage(responseBody), RechargeStatus.CAPTURE_ERROR.getCode());
                            }
                        } else if (e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error", RechargeStatus.CAPTURE_ERROR.getCode());
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IOException", RechargeStatus.CAPTURE_ERROR.getCode());
                        } else {
                            mViewInterface.onError("Unknown exception", RechargeStatus.CAPTURE_ERROR.getCode());
                        }
                    }
                });
    }

    private void getErrorMessage(int code, ResponseBody responseBody) {
        switch (code) {
            case 500:
                mViewInterface.onError(APIErrors.get500ErrorMessage(responseBody), RechargeStatus.ERROR_CODE_100.getCode());
                break;
            case 400:
                mViewInterface.onError(APIErrors.get500ErrorMessage(responseBody), RechargeStatus.ERROR_CODE_100.getCode());
                break;
            case 401:
                mViewInterface.onLogout(code);
                break;
            case 406:
                mViewInterface.onError(APIErrors.get406ErrorMessage(responseBody),RechargeStatus.ERROR_CODE_406.getCode());
                break;
            default:
                mViewInterface.onError(APIErrors.getErrorMessage(responseBody), RechargeStatus.ERROR_CODE_100.getCode());
        }
    }
}
