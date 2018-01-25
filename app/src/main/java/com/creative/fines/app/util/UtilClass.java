package com.creative.fines.app.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.creative.fines.app.BuildConfig;
import com.creative.fines.app.fragment.FragMenuActivity;
import com.creative.fines.app.menu.MainFragment;

import java.util.Calendar;
import java.util.GregorianCalendar;

import retrofit2.Call;

/**
 * Created by GS on 2016-12-09.
 */
public class UtilClass {

    public static void goHome(Activity activity) {
        Intent intent = new Intent(activity, FragMenuActivity.class);
        intent.putExtra("title", "메인");
        intent.putExtra("mode", "home");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
    }

    //현재날짜,시간
    public static String getCurrentDate(int gubun) {
        int year, month, day, hour, minute;

        GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day= calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        String returnData;
        if(gubun==1){
            String _month= UtilClass.addZero(month+1);
            String _day= UtilClass.addZero(day);
            returnData= year+"-"+_month+"-"+_day;
        }else{
            String _hour= UtilClass.addZero(hour);
            String _minute= UtilClass.addZero(minute);
            returnData= _hour+":"+_minute;
        }

        return returnData;
    }

    //날짜 한자리 0추가
    public static String addZero(int arg) {
        String val = String.valueOf(arg);
        if (arg < 10)
            val = "0" + val;

        return val;
    }

    public static void showProgressDialog(ProgressDialog dialog){
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Loading...");
        dialog.setProgress(0);
        dialog.setMax(100);
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void closeProgressDialog(ProgressDialog dialog){
        if (dialog.isShowing()) {
            dialog.cancel();
        }
    }

    public static final void logD (String TAG, String message) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message);
        }
    }
}
