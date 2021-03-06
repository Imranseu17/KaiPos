package com.kaicomsol.kpos.presenters;

import com.google.gson.JsonObject;
import com.kaicomsol.kpos.callbacks.CardInfoView;
import com.kaicomsol.kpos.models.APIErrors;
import com.kaicomsol.kpos.models.CardData;
import com.kaicomsol.kpos.models.Emergency;
import com.kaicomsol.kpos.services.APIClient;
import com.kaicomsol.kpos.utils.CardEnum;
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

public class CardPresenter {

    private CardInfoView mViewInterface;
    private APIClient mApiClient;


    public CardPresenter(CardInfoView view) {
        this.mViewInterface = view;

        if (this.mApiClient == null) {
            this.mApiClient = new APIClient();
        }
    }

    public void getEmergencyValue(String token, String meterSerial) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        mApiClient.getAPI()
                .getEmergencyValue(map, meterSerial)
                .enqueue(new Callback<Emergency>() {
                    @Override
                    public void onResponse(Call<Emergency> call, Response<Emergency> response) {

                        if (response.code() == ErrorCode.LOGOUTERROR.getCode()){
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()) {
                            Emergency emergency = response.body();
                            if (emergency != null) {
                                mViewInterface.onEmergencyValue(emergency.getEmergencyValue());
                            } else {
                                mViewInterface.onError("Error fetching data", CardEnum.EMERGENCY_VALUE_FAILED.getCode());
                            }
                        } else errorHandle(response.code(), CardEnum.EMERGENCY_VALUE_FAILED.getCode(), response.errorBody());
                    }

                    @Override
                    public void onFailure(Call<Emergency> call, Throwable e) {
                        DebugLog.e(call.request().toString());
                        if (e instanceof HttpException) {

                            int code = ((HttpException) e).response().code();
                            if (code == ErrorCode.LOGOUTERROR.getCode()){
                                mViewInterface.onLogout(code);
                            }
                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
                            errorHandle(code, CardEnum.EMERGENCY_VALUE_FAILED.getCode(), responseBody);

                        } else if (e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error",CardEnum.EMERGENCY_VALUE_FAILED.getCode());
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IOException",CardEnum.EMERGENCY_VALUE_FAILED.getCode());
                        } else {
                            mViewInterface.onError("Unknown exception",CardEnum.EMERGENCY_VALUE_FAILED.getCode());
                        }
                    }
                });
    }

    public void getMeterInfo(String token, String meterSerial) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        mApiClient.getAPI()
                .getMeter(map, meterSerial)
                .enqueue(new Callback<CardData>() {
                    @Override
                    public void onResponse(Call<CardData> call, Response<CardData> response) {

                        if (response.code() == ErrorCode.LOGOUTERROR.getCode()){
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()) {
                            CardData cardData = response.body();
                            if (cardData != null) {
                                mViewInterface.onCard(cardData);
                            } else {
                                mViewInterface.onError("Error fetching data",CardEnum.GET_METER_INFO_FAILED.getCode());
                            }
                        } else errorHandle(response.code(), CardEnum.GET_METER_INFO_FAILED.getCode(), response.errorBody());
                    }

                    @Override
                    public void onFailure(Call<CardData> call, Throwable e) {
                        DebugLog.e(call.request().toString());
                        if (e instanceof HttpException) {

                            int code = ((HttpException) e).response().code();
                            if (code == ErrorCode.LOGOUTERROR.getCode()){
                                mViewInterface.onLogout(code);
                            }
                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
                            errorHandle(code, CardEnum.GET_METER_INFO_FAILED.getCode(), responseBody);

                        } else if (e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error",CardEnum.GET_METER_INFO_FAILED.getCode());
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IOException",CardEnum.GET_METER_INFO_FAILED.getCode());
                        } else {
                            mViewInterface.onError("Unknown exception",CardEnum.GET_METER_INFO_FAILED.getCode());
                        }
                    }
                });
    }

    public void addCard(String token, String cardNo, String meterSerialNo, String status) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("cardNo", cardNo);
        jsonObject.addProperty("meterSerialNo", meterSerialNo);
        jsonObject.addProperty("status", "A");

        mApiClient.getAPI()
                .addCard(map, jsonObject)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (response.code() == ErrorCode.LOGOUTERROR.getCode()){
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()) {
                            mViewInterface.onAddCard(true);
                        }else {
                            errorHandle(response.code(), CardEnum.ADD_CARD_FAILED.getCode(), response.errorBody());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable e) {
                        DebugLog.e(call.request().toString());
                        if (e instanceof HttpException) {

                            int code = ((HttpException) e).response().code();
                            if (code == ErrorCode.LOGOUTERROR.getCode()){
                                mViewInterface.onLogout(code);
                            }else {

                                ResponseBody responseBody = ((HttpException) e).response().errorBody();
                                errorHandle(code, CardEnum.ADD_CARD_FAILED.getCode(), responseBody);
                            }

                        } else if (e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error",CardEnum.ADD_CARD_FAILED.getCode());
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IOException",CardEnum.ADD_CARD_FAILED.getCode());
                        } else {
                            mViewInterface.onError("Unknown exception",CardEnum.ADD_CARD_FAILED.getCode());
                        }
                    }
                });
    }

    public void activeCard(String token, String cardIdm) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("cardIdm", cardIdm);

        mApiClient.getAPI()
                .activeCard(map, jsonObject)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (response.code() == ErrorCode.LOGOUTERROR.getCode()){
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()) {
                            //String active = response.body();
                            mViewInterface.onActiveCard("");
                        } else errorHandle(response.code(), CardEnum.ACTIVE_CARD_FAILED.getCode(), response.errorBody());
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable e) {
                        DebugLog.e(call.request().toString());
                        if (e instanceof HttpException) {

                            int code = ((HttpException) e).response().code();
                            if (code == ErrorCode.LOGOUTERROR.getCode()){
                                mViewInterface.onLogout(code);
                            }
                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
                            errorHandle(code, CardEnum.ACTIVE_CARD_FAILED.getCode(), responseBody);

                        } else if (e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error",CardEnum.ACTIVE_CARD_FAILED.getCode());
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IOException",CardEnum.ACTIVE_CARD_FAILED.getCode());
                        } else {
                            mViewInterface.onError("Unknown exception",CardEnum.ACTIVE_CARD_FAILED.getCode());
                        }
                    }
                });
    }

    public void deleteCard(String token, String cardIdm) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");


        mApiClient.getAPI()
                .deleteCard(map, cardIdm)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (response.code() == ErrorCode.LOGOUTERROR.getCode()){
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()) {
                            mViewInterface.onDeleteCard(true);
                        } else errorHandle(response.code(), CardEnum.DELETE_CARD_FAILED.getCode(), response.errorBody());
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable e) {
                        DebugLog.e(call.request().toString());
                        if (e instanceof HttpException) {

                            int code = ((HttpException) e).response().code();
                            if (code == ErrorCode.LOGOUTERROR.getCode()){
                                mViewInterface.onLogout(code);
                            }
                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
                            errorHandle(code, CardEnum.DELETE_CARD_FAILED.getCode(), responseBody);

                        } else if (e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error",CardEnum.DELETE_CARD_FAILED.getCode());
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IOException",CardEnum.DELETE_CARD_FAILED.getCode());
                        } else {
                            mViewInterface.onError("Unknown exception",CardEnum.DELETE_CARD_FAILED.getCode());
                        }
                    }
                });
    }

    public void lostCard(String token,String cardNo,String userId,
                         String description, String remarks) {

        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("cardNo", cardNo);
        jsonObject.addProperty("cardStatus", "L");
        jsonObject.addProperty("createdBy", userId);
        jsonObject.addProperty("description", description);
        jsonObject.addProperty("remarks", remarks);
        jsonObject.addProperty("status", "1");
        jsonObject.addProperty("workOrderOriginCode", "pos_device");
        jsonObject.addProperty("workOrderTypeCode", "card_lost");

        mApiClient.getAPI()
                .lostCard(map, jsonObject)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        DebugLog.e(response.message());
                        DebugLog.e(response.toString());

                        if (response.code() == ErrorCode.LOGOUTERROR.getCode()){
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()) {
                            mViewInterface.onLostCard("");
                        } else errorHandle(response.code(), CardEnum.LOST_CARD_FAILED.getCode(), response.errorBody());

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable e) {

                        DebugLog.e(e.getStackTrace().toString());

                        if (e instanceof HttpException) {


                            int code = ((HttpException) e).response().code();
                            if (code == ErrorCode.LOGOUTERROR.getCode()){
                                mViewInterface.onLogout(code);
                            }

                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
                            errorHandle(code, CardEnum.LOST_CARD_FAILED.getCode(), responseBody);

                        } else if (e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error",CardEnum.LOST_CARD_FAILED.getCode());
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IOException",CardEnum.LOST_CARD_FAILED.getCode());
                        } else {
                            mViewInterface.onError("Unknown exception",CardEnum.LOST_CARD_FAILED.getCode());
                        }
                    }
                });
    }

    public void damageCard(String token, String cardNo, String meterSerialNo) {
        Map<String, String> map = new HashMap<>();
        DebugLog.e(token);
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("cardNo", cardNo);
        jsonObject.addProperty("meterSerialNo", meterSerialNo);
        jsonObject.addProperty("status", "D");

        mApiClient.getAPI()
                .damageCard(map, jsonObject)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        if (response.code() == ErrorCode.LOGOUTERROR.getCode()){
                            mViewInterface.onLogout(response.code());
                            return;
                        }

                        if (response.isSuccessful()) {
                            mViewInterface.onDamageCard("");
                        } else errorHandle(response.code(), CardEnum.DAMAGE_CARD_FAILED.getCode(), response.errorBody());

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable e) {

                        DebugLog.e(e.getStackTrace().toString());

                        if (e instanceof HttpException) {


                            int code = ((HttpException) e).response().code();
                            if (code == ErrorCode.LOGOUTERROR.getCode()){
                                mViewInterface.onLogout(code);
                            }

                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
                            errorHandle(code, CardEnum.DAMAGE_CARD_FAILED.getCode(), responseBody);

                        } else if (e instanceof SocketTimeoutException) {

                            mViewInterface.onError("Server connection error",CardEnum.DAMAGE_CARD_FAILED.getCode());
                        } else if (e instanceof IOException) {
                            mViewInterface.onError("IOException",CardEnum.DAMAGE_CARD_FAILED.getCode());
                        } else {
                            mViewInterface.onError("Unknown exception",CardEnum.DAMAGE_CARD_FAILED.getCode());
                        }
                    }
                });
    }

    private void errorHandle(int code,int errorType, ResponseBody responseBody){

        ErrorCode errorCode = ErrorCode.getByCode(code);

        if(errorCode != null){
            switch (errorCode){
                case ERRORCODE500:
                    mViewInterface.onError(APIErrors.get500ErrorMessage(responseBody),errorType);
                    break;
                case ERRORCODE406:
                    mViewInterface.onError(APIErrors.get406ErrorMessage(responseBody),errorType);
                    break;
                case ERRORCODE400:
                    mViewInterface.onError(APIErrors.get500ErrorMessage(responseBody),errorType);
                    break;
                default:
                    mViewInterface.onError(APIErrors.getErrorMessage(responseBody),errorType);
                    break;
            }
        }else{

            mViewInterface.onError("Error occurred Please try again",code);

        }

    }
}
