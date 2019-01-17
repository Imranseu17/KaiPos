package com.kaicomsol.kpos.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Login {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("errors")
    @Expose
    private Object errors;
    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("posErrMsg")
    @Expose
    private String posErrMsg;
    @SerializedName("posCode")
    @Expose
    private Integer posCode;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getErrors() {
        return errors;
    }

    public void setErrors(Object errors) {
        this.errors = errors;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPosErrMsg() {
        return posErrMsg;
    }

    public void setPosErrMsg(String posErrMsg) {
        this.posErrMsg = posErrMsg;
    }

    public Integer getPosCode() {
        return posCode;
    }

    public void setPosCode(Integer posCode) {
        this.posCode = posCode;
    }

}
