package com.kaicomsol.kpos.presenters;


import com.google.gson.JsonObject;
import com.kaicomsol.kpos.callbacks.LoginView;
import com.kaicomsol.kpos.models.APIErrors;
import com.kaicomsol.kpos.models.Login;
import com.kaicomsol.kpos.services.APIClient;

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

public class LoginPresenter {

    private LoginView mViewInterface;
    private APIClient mApiClient;

    public LoginPresenter(LoginView view) {
        this.mViewInterface = view;

        if (this.mApiClient == null) {
            this.mApiClient = new APIClient();
        }
    }

    public void attemptLogin(String device_id, String email, String password) {
        Map<String, String> map = new HashMap<>();
        map.put("Content-Type", "application/json");
        map.put("Device-Type", "android");
        map.put("Device-Id", device_id);

        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("username", email);
        jsonObject.addProperty("password", password);

        mApiClient.getAPI()
                .getLogin(map, jsonObject)
                .enqueue(new Callback<Login>() {
                    @Override
                    public void onResponse(Call<Login> call, Response<Login> response) {

                        if (response.isSuccessful()) {
                            Login login = response.body();
                            if (login != null) {
                                mViewInterface.onSuccess(login);
                            } else {
                                mViewInterface.onError("Error fetching data");
                            }
                        } else mViewInterface.onError(APIErrors.get500ErrorMessage(response.errorBody()));
                    }

                    @Override
                    public void onFailure(Call<Login> call, Throwable e) {
                        if (e instanceof HttpException) {

                            int code = ((HttpException) e).response().code();
                            ResponseBody responseBody = ((HttpException) e).response().errorBody();
                            mViewInterface.onError(APIErrors.get500ErrorMessage(responseBody));

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
