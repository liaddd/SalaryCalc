package com.liad.salarycalc.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.liad.salarycalc.Constants;

public class SharedPreferencesManager {

    private static SharedPreferencesManager instance;
    private SharedPreferences pref;

    private SharedPreferencesManager(Context context) {
        pref = context.getSharedPreferences(Constants.MY_SHARED_PREF , Context.MODE_PRIVATE);
    }

    public static SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context);
        }
        return instance;
    }

    public SharedPreferences getPref() {
        return pref;
    }

    public SharedPreferences.Editor getEditor() {
        return pref.edit();
    }

    public void putBoolean(String key, boolean value){
        getEditor().putBoolean(key , value);
    }

}
