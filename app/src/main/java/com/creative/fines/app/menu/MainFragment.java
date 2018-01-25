package com.creative.fines.app.menu;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.fines.app.R;
import com.creative.fines.app.fragment.FragMenuActivity;
import com.creative.fines.app.fragment.WebFragment;
import com.creative.fines.app.gear.GearPopupActivity;
import com.creative.fines.app.safe.PenaltyWriteFragment;
import com.creative.fines.app.safe.SafePopupActivity;
import com.creative.fines.app.util.BackPressCloseSystem;
import com.creative.fines.app.util.SettingPreference;
import com.creative.fines.app.util.UtilClass;
import com.google.firebase.iid.FirebaseInstanceId;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainFragment extends Fragment {

    //    adb shell dumpsys activity activities | findstr "Run"
    private static final String TAG = "MainFragment";
//    public static String ipAddress= "http://119.202.60.104:8585";
    public static String ipAddress= "http://192.168.0.22:9191";
    public static String contextPath= "/sjsf_pines";

    public static String UPLOAD_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Common/fileUpload";
    public static String DOWNLOAD_URL = MainFragment.ipAddress+"/uploadFile/";

    //FCM 관련
    private String INSERT_URL= MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Board/fcmTokenData";
    private String token=null;
    private String phone_num=null;
    public static boolean onAppCheck= false;
    public static String pendingPath= "";
    public static String pendingPathKey= "";

    private BackPressCloseSystem backPressCloseSystem;
    private PermissionListener permissionlistener;

    private SettingPreference pref;
    public static String loginSabun;
    public static String loginName;
    public static String latestAppVer;

    @Bind(R.id.top_title) TextView textTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main, container, false);
        ButterKnife.bind(this, view);
//        backPressCloseSystem = new BackPressCloseSystem(getActivity());
        textTitle.setText("");
        view.findViewById(R.id.top_home).setVisibility(View.GONE);

        pref = new SettingPreference("loginData",getActivity());

        loginSabun = pref.getValue("sabun_no","").trim();
        loginName = pref.getValue("user_nm","").trim();
        latestAppVer = pref.getValue("LATEST_APP_VER","");

//        FirebaseMessaging.getInstance().subscribeToTopic("all");
//        FirebaseMessaging.getInstance().unsubscribeFromTopic("all");
        token = FirebaseInstanceId.getInstance().getToken();
        UtilClass.logD(TAG, "Refreshed token: " + token);

        String mode= getArguments().getString("mode");
        if(mode.equals("first")){
            permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
//                Toast.makeText(getApplicationContext(), "권한 허가", Toast.LENGTH_SHORT).show();
                    phone_num= getPhoneNumber();
                    postData();
                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    Toast.makeText(getActivity(), "권한 거부 목록\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                    phone_num="";
                    postData();
                }
            };
            new TedPermission(getActivity())
                    .setPermissionListener(permissionlistener)
                    .setRationaleMessage("전화번호 정보를 가져오기 위해선 권한이 필요합니다.")
                    .setDeniedMessage("권한을 확인하세요.\n\n [설정] > [애플리케이션] [해당앱] > [권한]")
                    .setGotoSettingButtonText("권한확인")
                    .setPermissions(Manifest.permission.CALL_PHONE)
                    .check();
        }

        onAppCheck= true;

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String currentAppVer= getAppVersion(getActivity());
        UtilClass.logD(TAG, "currentAppVer="+currentAppVer+", latestAppVer="+latestAppVer);

        if(!currentAppVer.equals(latestAppVer)){
            //최신버전이 아닐때
//            Snackbar.make(getView(), "현재 앱의 버전보다 높은 최신 버전이 있습니다.", Snackbar.LENGTH_INDEFINITE).setAction("다운받기", new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    new Thread() {
//                        public void run() {
//                            download_WebLink();
//                        }
//                    }.start();
//                }
//            }).show();

        }else{

        }

    }

    public static String getAppVersion(Context context) {
        // application version
        String versionName = "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            UtilClass.logD(TAG, "getAppVersion Error");
        }

        return versionName;
    }

    public void download_WebLink() {
        try {
            Log.e("DOWNLOAD", "start");
            URL url = new URL("http://59.11.9.94:9090/app/apkDown.do?appGubun=pines");

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            Log.e("DOWNLOAD", "Connect");

            File SDCardRoot = Environment.getExternalStorageDirectory();
            File file = new File(SDCardRoot,"pines_"+latestAppVer+".apk");
            FileOutputStream fileOutput = new FileOutputStream(file);
            Log.e("DOWNLOAD", "fileoutput");

            InputStream inputStream = urlConnection.getInputStream();
            int totalSize = urlConnection.getContentLength();
            int downloadedSize = 0;

            byte[] buffer = new byte[1024];
            int bufferLength = 0;
            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                //mProgressBar.setProgress(downloadedSize);
                Log.e("DOWNLOAD", "saving...");
            }
            fileOutput.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "에러코드 Main MalURL", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "에러코드 Main IO", Toast.LENGTH_SHORT).show();

        }
        Log.e("DOWNLOAD", "end");

        Log.e("DOWNLOAD", "InstallAPK Method Called");
        installAPK();
    }

    public void installAPK()
    {
        Log.e("InstallApk", "Start");
        File apkFile = new File("/sdcard/"+"pines_"+latestAppVer+".apk");

        Uri apkUri = Uri.fromFile(apkFile);

        Intent webLinkIntent = new Intent(Intent.ACTION_VIEW);
        webLinkIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");

        startActivity(webLinkIntent);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @OnClick(R.id.imageView1)
    public void getMenu1() {
        Intent intent = new Intent(getActivity(),FragMenuActivity.class);
        intent.putExtra("title", "공지사항");
        startActivity(intent);
    }

    @OnClick(R.id.imageView2)
    public void getMenu2() {
        Intent intent = new Intent(getActivity(),FragMenuActivity.class);
        intent.putExtra("title", "작업개소현황");
        startActivity(intent);
    }

    @OnClick(R.id.imageView3)
    public void getMenu3() {
        Intent intent = new Intent(getActivity(),FragMenuActivity.class);
        intent.putExtra("title", "패널티카드");
        startActivity(intent);
    }

    @OnClick(R.id.imageView4)
    public void getMenu4() {
        Intent intent = new Intent(getActivity(),FragMenuActivity.class);
        intent.putExtra("title", "동료사랑카드");
        startActivity(intent);
    }

    @OnClick(R.id.imageView5)
    public void getMenu5() {
        Intent intent = new Intent(getActivity(),FragMenuActivity.class);
        intent.putExtra("title", "아이디어낙서방");
        startActivity(intent);
    }

    @OnClick(R.id.imageView6)
    public void getMenu6() {
        Intent intent = new Intent(getActivity(),SafePopupActivity.class);
        intent.putExtra("title", "위험기계사용점검");
        startActivity(intent);
    }

    @OnClick(R.id.imageView7)
    public void getMenu7() {
        Intent intent = new Intent(getActivity(),GearPopupActivity.class);
        intent.putExtra("title", "장비점검");
        startActivity(intent);
    }

    //푸시 데이터 전송
    public void postData() {
        String sabun_no= pref.getValue("sabun_no","");
        String android_id = "" + android.provider.Settings.Secure.getString(getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        WebServiceTask wst= new WebServiceTask(WebServiceTask.POST_TASK, getActivity(), "Loading...");

        wst.addNameValuePair("Token",token);
        wst.addNameValuePair("phone_num",phone_num);
        wst.addNameValuePair("sabun_no",sabun_no);
        wst.addNameValuePair("and_id",android_id);

        // the passed String is the URL we will POST to
        wst.execute(new String[] { INSERT_URL });

    }

    //작성 완료
    public void handleResponse(String response) {
        UtilClass.logD(TAG,"response="+response);

        try {
            JSONObject jso = new JSONObject(response);
            String status= jso.get("status").toString();

            if(status.equals("success")){

            }else{
                Toast.makeText(getActivity(), "토큰 생성에 실패하였습니다.",Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            Toast.makeText(getActivity(), "handleResponse Main",Toast.LENGTH_SHORT).show();
        }

    }

    private class WebServiceTask extends AsyncTask<String, Integer, String> {

        public static final int POST_TASK = 1;

        private static final String TAG = "WebServiceTask";

        // connection timeout, in milliseconds (waiting to connect)
        private static final int CONN_TIMEOUT = 3000;

        // socket timeout, in milliseconds (waiting for data)
        private static final int SOCKET_TIMEOUT = 5000;

        private int taskType = POST_TASK;
        private Context mContext = null;
        private String processMessage = "Processing...";

        private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        private ProgressDialog pDlg = null;
        public WebServiceTask(int taskType, Context mContext, String processMessage) {

            this.taskType = taskType;
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
            showProgressDialog();
        }

        protected String doInBackground(String... urls) {
            String url = urls[0];
            String result = "";
            HttpResponse response = doResponse(url);

            if (response == null) {
                return result;
            } else {
                try {
                    result = inputStreamToString(response.getEntity().getContent());

                } catch (IllegalStateException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                } catch (IOException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String response) {
            handleResponse(response);
            pDlg.dismiss();
        }

        // Establish connection and socket (data retrieval) timeouts
        private HttpParams getHttpParams() {
            HttpParams htpp = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(htpp, CONN_TIMEOUT);
            HttpConnectionParams.setSoTimeout(htpp, SOCKET_TIMEOUT);

            return htpp;
        }


        private HttpResponse doResponse(String url) {
            HttpClient httpclient = new DefaultHttpClient(getHttpParams());
            HttpResponse response = null;

            try {
                switch (taskType) {

                    case POST_TASK:
                        HttpPost httppost = new HttpPost(url);
                        // Add parameters
                        httppost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
                        response= httpclient.execute(httppost);
                        break;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }

            return response;
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

    }

    // 단말기 핸드폰번호 얻어오기
    public String getPhoneNumber() {
        String num = null;
        try {
            TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            num = tm.getLine1Number();
            if(num!=null&&num.startsWith("+82")){
                num = num.replace("+82", "0");
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "오류가 발생 하였습니다!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return num;
    }
}
