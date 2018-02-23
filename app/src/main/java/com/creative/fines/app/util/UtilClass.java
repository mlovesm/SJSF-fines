package com.creative.fines.app.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import com.creative.fines.app.BuildConfig;
import com.creative.fines.app.fragment.FragMenuActivity;
import com.creative.fines.app.menu.MainFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.ResponseBody;
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
    public static String getCurrentDate(int gubun, String type) {
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
            returnData= year+type+_month+type+_day;
        }else if(gubun==2){
            String _month= UtilClass.addZero(month+1);
            returnData= year+type+_month+type+"01";
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

    //밀리언타입 Date 변환
    public static String MillToDate(long mills) {
        String pattern = "yyyy-MM-dd HH:mm";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        String date = (String) formatter.format(new Timestamp(mills));

        return date;
    }

    //날짜 시간 현재 시간에서 선택
    public static ArrayList<Integer> dateAndTimeChoiceList(TextView view, String gubun) {
        ArrayList<Integer> list = new ArrayList();
        if(view.length()>0){
            String date= view.getText().toString();
            if(gubun.equals("D")){
                int firstPoint= date.indexOf(".");
                int lastPoint= date.lastIndexOf(".");
                int year= Integer.parseInt(date.substring(0,firstPoint));
                int month= Integer.parseInt(date.substring(firstPoint+1, lastPoint));
                int day= Integer.parseInt(date.substring(lastPoint+1));
                list.add(year);
                list.add(month);
                list.add(day);
            }else{
                int point= date.indexOf(":");
                int hour= Integer.parseInt(date.substring(0,point));
                int minute= Integer.parseInt(date.substring(point+1));
                list.add(hour);
                list.add(minute);
            }
        }else{

        }
        return list;
    }

    public static void showProcessingDialog(ProgressDialog dialog) {
        dialog.setMessage("Processing...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();
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

    public static void dataNullCheckZero(HashMap<String, String> hashMap) {
        for (Iterator iter = hashMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String)entry.getKey();

            if(entry.getValue()==null){
                entry.setValue("");
            }
        }
    }

    public static boolean writeResponseBodyToDisk(ResponseBody body, String fileDir, String fileNm) {
        try {
            // todo change the file location/name according to your needs
            String externalState = Environment.getExternalStorageState();
//            UtilClass.logD(TAG, "경로1="+Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Download" + File.separator  + fileNm);
//            UtilClass.logD(TAG, "경로2="+getActivity().getExternalFilesDir(null).getAbsolutePath()+ File.separator + fileNm);
            File createFile = new File(fileDir  + fileNm);

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(createFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;
//                    UtilClass.logD(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }
                outputStream.flush();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
