package com.creative.fines.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by GS on 2016-10-12.
 */
public class SettingPreference {
    private String pref_name;
    static Context mContext;

    public SettingPreference(String pref_name,Context c) {
        this.pref_name = pref_name;
        mContext = c;
    }

    public SettingPreference(Context c) {
        mContext = c;
    }

    public void put(String key, String value) {
        SharedPreferences pref = mContext.getSharedPreferences(pref_name, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(key, value);
        editor.commit();
    }

    public void put(String key, boolean value) {
        SharedPreferences pref = mContext.getSharedPreferences(pref_name,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(key, value);
        editor.commit();
    }

    public void put(String key, int value) {
        SharedPreferences pref = mContext.getSharedPreferences(pref_name,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(key, value);
        editor.commit();
    }

    public String getValue(String key, String dftValue) {
        SharedPreferences pref = mContext.getSharedPreferences(pref_name,
                Activity.MODE_PRIVATE);

        try {
            return pref.getString(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }

    }

    public int getValue(String key, int dftValue) {
        SharedPreferences pref = mContext.getSharedPreferences(pref_name,
                Activity.MODE_PRIVATE);

        try {
            return pref.getInt(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }

    }

    public boolean getValue(String key, boolean dftValue) {
        SharedPreferences pref = mContext.getSharedPreferences(pref_name,
                Activity.MODE_PRIVATE);

        try {
            return pref.getBoolean(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }
    }
}
