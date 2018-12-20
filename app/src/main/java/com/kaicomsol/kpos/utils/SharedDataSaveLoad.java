package com.kaicomsol.kpos.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.kaicomsol.kpos.R;


/**
 * Created by newton-pc on 6/22/2015.
 */
public class SharedDataSaveLoad {

    public static void save(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public static void save(Context context, String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
    public static void save(Context context,String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static String  load(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }
    public static boolean isAvailable(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }
    public static void saveInt(Context context, String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int loadInt(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, 0);
    }
    public static int loadInteger(Context context,String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, 0);
    }

    public static boolean loadBoolean(Context context,String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }


    public static void remove(Context context,String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }


}
