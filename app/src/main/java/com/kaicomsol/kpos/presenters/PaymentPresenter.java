package com.kaicomsol.kpos.presenters;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kaicomsol.kpos.callbacks.PaymentView;
import com.kaicomsol.kpos.models.APIErrors;
import com.kaicomsol.kpos.models.AccessFalica;
import com.kaicomsol.kpos.models.Invoices;
import com.kaicomsol.kpos.models.Payment;
import com.kaicomsol.kpos.models.PaymentID;
import com.kaicomsol.kpos.models.ReadCard;
import com.kaicomsol.kpos.models.Receipt;
import com.kaicomsol.kpos.nfcfelica.HttpResponsAsync;
import com.kaicomsol.kpos.services.APIClient;
import com.kaicomsol.kpos.utils.DebugLog;
import com.kaicomsol.kpos.utils.RechargeStatus;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public void addPayment(String token, String  amount, String cardNo, String historyNo, String paymentMethodId) {
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

                        if (response.code() == 401) {
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()) {
                            Payment payment = response.body();
                            if (payment != null) {
                                mViewInterface.onSuccess(payment);
                            } else {
                                mViewInterface.onError("Error fetching data", RechargeStatus.PAYMENT_ERROR.getCode());
                            }
                        } else getErrorMessage(response.code(), response.errorBody());


                    }

                    @Override
                    public void onFailure(Call<Payment> call, Throwable e) {
                        e.printStackTrace();
                        if (e instanceof HttpException) {
                            int code = ((HttpException) e).response().code();
                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
                            getErrorMessage(code, responseBody);

                        } else if (e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error", RechargeStatus.PAYMENT_ERROR.getCode());
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IOException", RechargeStatus.PAYMENT_ERROR.getCode());
                        } else {
                            mViewInterface.onError("Unknown exception", RechargeStatus.PAYMENT_ERROR.getCode());
                        }
                    }
                });
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
                                mViewInterface.onSuccess(payment.getPaymentId(),RechargeStatus.CAPTURE_SUCCESS.getCode());
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

    public void cancelPayment(String token, final String paymentId) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        mApiClient.getAPI()
                .cancelPayment(map, paymentId)
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
                                mViewInterface.onSuccess(payment.getPaymentId(), RechargeStatus.CANCEL_SUCCESS.getCode());
                            } else {
                                mViewInterface.onError("Error fetching data", RechargeStatus.CANCEL_ERROR.getCode());
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
                                    mViewInterface.onError(APIErrors.get500ErrorMessage(responseBody), RechargeStatus.CANCEL_ERROR.getCode());
                                else
                                    mViewInterface.onError(APIErrors.getErrorMessage(responseBody), RechargeStatus.CANCEL_ERROR.getCode());
                            }
                        } else if (e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error", RechargeStatus.CANCEL_ERROR.getCode());
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IOException", RechargeStatus.CANCEL_ERROR.getCode());
                        } else {
                            mViewInterface.onError("Unknown exception", RechargeStatus.CANCEL_ERROR.getCode());
                        }
                    }
                });
    }

    public void receiptPayment(String token, String paymentId) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        mApiClient.getAPI()
                .receiptPayment(map, paymentId)
                .enqueue(new Callback<Receipt>() {
                    @Override
                    public void onResponse(Call<Receipt> call, Response<Receipt> response) {

                        if (response.code() == 401) {
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()) {
                            Receipt receipt = response.body();
                            if (receipt != null) {
                                DebugLog.e("Code"+response.code());
                                mViewInterface.onSuccess(receipt);
                            } else {
                                DebugLog.e("Code"+response.code());
                                mViewInterface.onError("Error fetching data", RechargeStatus.RECEIPT_ERROR.getCode());
                            }
                        } else getErrorMessage(response.code(), response.errorBody());


                    }

                    @Override
                    public void onFailure(Call<Receipt> call, Throwable e) {
                        DebugLog.e(call.request().toString());
                        if (e instanceof HttpException) {

                            int code = ((HttpException) e).response().code();
                            if (code == 401) {
                                mViewInterface.onLogout(code);
                                return;
                            } else {
                                ResponseBody responseBody = ((HttpException) e).response().errorBody();
                                if (code == 500)
                                    mViewInterface.onError(APIErrors.get500ErrorMessage(responseBody), RechargeStatus.RECEIPT_ERROR.getCode());
                                else
                                    mViewInterface.onError(APIErrors.getErrorMessage(responseBody), RechargeStatus.RECEIPT_ERROR.getCode());
                            }
                        } else if (e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error", RechargeStatus.RECEIPT_ERROR.getCode());
                        } else if (e instanceof IOException) {
                        } else {
                            mViewInterface.onError("Unknown exception", RechargeStatus.RECEIPT_ERROR.getCode());
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
                                mViewInterface.onError(null, RechargeStatus.INVOICE_ERROR.getCode());
                            }
                        } else mViewInterface.onError(APIErrors.get500ErrorMessage(response.errorBody()), RechargeStatus.INVOICE_ERROR.getCode());
                    }

                    @Override
                    public void onFailure(Call<Invoices> call, Throwable e) {
                        e.printStackTrace();
                        if (e instanceof HttpException) {
                            int code = ((HttpException) e).response().code();
                            if (code == 401) {
                                mViewInterface.onLogout(code);
                                return;
                              } else mViewInterface.onError(null, RechargeStatus.INVOICE_ERROR.getCode());

                        }

                    }
                });
    }

    public void readCard(String token, AccessFalica accessFalica) {
        Map<String, String> map = new HashMap<>();
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        JsonObject rootObject = new JsonObject();
        JsonObject cardObj = new JsonObject();
        //get card history
        JsonArray jsonHistory = new JsonArray();
        for(int i = 0; i < accessFalica.readCardArgument.CardHistory.size(); i++){
            HttpResponsAsync.ReadCardArgumentCardHistory cardHistory = accessFalica.readCardArgument.CardHistory.get(i);
            JsonObject objHistory = new JsonObject();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = null;
            try {
                date = sdf.parse(cardHistory.HistoryTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            objHistory.addProperty("HistoryTime",date.getTime());
            objHistory.addProperty("HistoryType",cardHistory.HistoryType);
            jsonHistory.add(objHistory);

        }

        //get error history
        JsonArray arrErrorHistory = new JsonArray();
        for(int i = 0; i < accessFalica.readCardArgument.ErrorHistory.size(); i++){
            HttpResponsAsync.ReadCardArgumentErrorHistory errorHistory = accessFalica.readCardArgument.ErrorHistory.get(i);
            JsonObject objErrHistory = new JsonObject();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = null;
            try {
                date = sdf.parse(errorHistory.ErrorTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            objErrHistory.addProperty("ErrorGroup",errorHistory.ErrorGroup);
            objErrHistory.addProperty("ErrorTime",date.getTime());
            objErrHistory.addProperty("ErrorType",errorHistory.ErrorType);
            arrErrorHistory.add(objErrHistory);

        }

        //get Log Day
        JsonArray arrLogDay = new JsonArray();
        for(int i = 0; i < accessFalica.readCardArgument.LogDay.size(); i++){
            HttpResponsAsync.ReadCardArgumentLogDay logDay = accessFalica.readCardArgument.LogDay.get(i);
            JsonObject objLogDay = new JsonObject();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = null;
            try {
                date = sdf.parse(logDay.GasTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            objLogDay.addProperty("GasTime",date.getTime());
            objLogDay.addProperty("GasValue",logDay.GasValue);
            arrLogDay.add(objLogDay);

        }

        //get Log Hour
        JsonArray arrLogHour = new JsonArray();
        for(int i = 0; i < accessFalica.readCardArgument.LogHour.size(); i++){
            HttpResponsAsync.ReadCardArgumentLogHour logHour = accessFalica.readCardArgument.LogHour.get(i);
            JsonObject objlogHour = new JsonObject();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = null;
            try {
                date = sdf.parse(logHour.GasTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            objlogHour.addProperty("GasTime", date.getTime());
            objlogHour.addProperty("GasValue", logHour.GasValue);
            arrLogHour.add(objlogHour);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(accessFalica.lidTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        cardObj.addProperty("BasicFee", accessFalica.basicFee);
        cardObj.addProperty("CardGroup", accessFalica.cardGroup);
        cardObj.addProperty("CardHistoryNo", accessFalica.historyNO);
        cardObj.addProperty("CardIdm", accessFalica.cardIDm);
        cardObj.addProperty("CardStatus", accessFalica.cardStatus);
        cardObj.addProperty("Credit", accessFalica.credit);
        cardObj.addProperty("CustomerId", accessFalica.strCustomerId);
        cardObj.addProperty("ErrorNo", accessFalica.errorNO);
        cardObj.addProperty("LidTime", date.getTime());
        cardObj.addProperty("OpenCount", accessFalica.openCount);
        cardObj.addProperty("Refund1", accessFalica.refund1);
        cardObj.addProperty("Refund2", accessFalica.refund2);
        cardObj.addProperty("Unit", accessFalica.unit);
        cardObj.addProperty("UntreatedFee", accessFalica.untreatedFee);
        cardObj.addProperty("VersionNo", accessFalica.versionNO);

        cardObj.add("CardHistory", jsonHistory);
        cardObj.add("ErrorHistory", arrErrorHistory);
        cardObj.add("LogDay", arrLogDay);
        cardObj.add("LogHour", arrLogHour);
        //this is final json object
        rootObject.add("cardData", cardObj);

        mApiClient.getAPI()
                .readCard(map, rootObject)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (response.code() == 401) {
                            mViewInterface.onLogout(response.code());
                            return;
                        }
                        if (response.isSuccessful()){
                            mViewInterface.onSuccess("readCard");
                        }else mViewInterface.onError(null, RechargeStatus.READ_CARD_ERROR.getCode());
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable e) {
                        e.printStackTrace();
                        if (e instanceof HttpException) {
                            int code = ((HttpException) e).response().code();
                            if (code == 401) {
                                mViewInterface.onLogout(code);
                                return;
                            } else mViewInterface.onError(null, RechargeStatus.READ_CARD_ERROR.getCode());

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
