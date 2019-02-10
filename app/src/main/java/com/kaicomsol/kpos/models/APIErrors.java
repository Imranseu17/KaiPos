package com.kaicomsol.kpos.models;

import com.kaicomsol.kpos.utils.DebugLog;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.ResponseBody;

public class APIErrors {

    public static String getErrorMessage(ResponseBody response) {
        try {
            String jsonStr = response.string();
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray jsonArray = jsonObject.getJSONArray("fieldErrors");
            JSONObject jsonError = jsonArray.getJSONObject(0);
            return jsonError.getString("message");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred Please try again";
        }
    }

    public static String get406ErrorMessage(ResponseBody response) {
        try {
            String jsonStr = response.string();
            JSONObject jObjError = new JSONObject(jsonStr);
            String message = jObjError.getString("message");
            return message;
        } catch (Exception e) {
            return "Error occurred Please try again";
        }
    }

    public static String get500ErrorMessage(ResponseBody response) {
        try {
            String jsonStr = response.string();
            JSONObject jsonObject = new JSONObject(jsonStr);
            return jsonObject.getString("message");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred Please try again";
        }
    }
}
