package com.creative.fines.app.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by GS on 2016-02-14.
 */
public class AsyncTaskClass extends AsyncTask<String, Integer, String> {

    public static final int POST_TASK = 1;
    public static final int GET_TASK = 2;
    public static final int PUT_TASK = 3;

    private static final String TAG = "AsyncTaskClass";

    // connection timeout, in milliseconds (waiting to connect)
    private static final int CONN_TIMEOUT = 3000;

    // socket timeout, in milliseconds (waiting for data)
    private static final int SOCKET_TIMEOUT = 5000;

    private int taskType = GET_TASK;
    private Context mContext = null;
    private String processMessage = "Processing...";

    private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

    private ProgressDialog pDlg = null;

    public AsyncTaskClass(String processMessage) {
        this.processMessage = processMessage;
    }

    public AsyncTaskClass(Context mContext, String processMessage) {
        this.mContext = mContext;
        this.processMessage = processMessage;
    }

    public void addNameValuePair(String name, String value) {
        params.add(new BasicNameValuePair(name, value));
    }

    private void showProgressDialog() {
        pDlg = new ProgressDialog(mContext);
        pDlg.setMessage(processMessage);
        pDlg.setProgressDrawable(mContext.getWallpaper());
        pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDlg.setCancelable(false);
        pDlg.show();
    }

    @Override
    protected void onPreExecute() {
//        hideKeyboard();
        showProgressDialog();
    }

    protected String doInBackground(String... urls) {

        String url = urls[0];
        String result = "";
        Log.d(TAG+" 첫번째 인자=",url);

        return result;
    }

    @Override
    protected void onPostExecute(String response) {

//        handleResponse(response);
        pDlg.dismiss();
    }

    // Establish connection and socket (data retrieval) timeouts
    private HttpParams getHttpParams() {

        HttpParams htpp = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(htpp, CONN_TIMEOUT);
        HttpConnectionParams.setSoTimeout(htpp, SOCKET_TIMEOUT);

        return htpp;
    }



    private String inputStreamToString(InputStream is) {

        String line = "";
        StringBuilder total = new StringBuilder();

        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        try {
            // Read response until the end
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

        // Return full string
        return total.toString();
    }

    private void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void handleResponse(String response) {
        try {

            Log.d(TAG,"response="+response);
//            JSONObject jso = new JSONObject(response);

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

    }

}

