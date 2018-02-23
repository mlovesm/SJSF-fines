package com.creative.fines.app.menu;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.fines.app.R;
import com.creative.fines.app.fragment.FragMenuActivity;
import com.creative.fines.app.gear.GearPopupActivity;
import com.creative.fines.app.retrofit.RetrofitService;
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
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends Fragment {

    //    adb shell dumpsys activity activities | findstr "Run"
    private static final String TAG = "MainFragment";
    private RetrofitService service;
//    public static String ipAddress= "http://119.202.60.104:8585";
    public static String ipAddress= "http://192.168.0.22:9191";
    public static String contextPath= "/sjsf_pines";

    public static String UPLOAD_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Common/fileUpload";
    public static String DOWNLOAD_URL = MainFragment.ipAddress+"/uploadFile/";

    private String fileDir= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Download" + File.separator;
    private String fileNm;

    //FCM 관련
    private String INSERT_URL= MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Board/fcmTokenData";
    private String token=null;
    private String phone_num=null;
    public static boolean onAppCheck= false;
    public static String pendingPath= "";
    public static String pendingPathKey= "";

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
        service= RetrofitService.rest_api.create(RetrofitService.class);
        textTitle.setText("");
        view.findViewById(R.id.top_home).setVisibility(View.GONE);

        pref = new SettingPreference("loginData",getActivity());

        loginSabun = pref.getValue("sabun_no","").trim();
        loginName = pref.getValue("user_nm","").trim();
        latestAppVer = pref.getValue("LATEST_APP_VER","");

        String currentAppVer= getAppVersion(getActivity());
        UtilClass.logD(TAG, "currentAppVer="+currentAppVer+", latestAppVer="+latestAppVer);
        if(!currentAppVer.equals(latestAppVer)){
            //최신버전이 아닐때
            fileNm= "sjsf_fines_"+latestAppVer+"-debug.apk";
            alertDialog();
        }

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

    public void alertDialog(){
        final android.app.AlertDialog.Builder alertDlg = new android.app.AlertDialog.Builder(getActivity());
        alertDlg.setTitle("알림");
        alertDlg.setMessage("현재 앱의 버전보다 높은 최신 버전이 있습니다.");

        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("지금 설치", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                installAPK();
            }
        });
        // '아니오' 버튼이 클릭되면
        alertDlg.setNegativeButton("다음에 설치", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDlg.show();
    }

    //파일 다운로드
    public void downloadFile(String fileUrl) {
        final ProgressDialog pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<ResponseBody> call = service.downloadFile(fileUrl);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    boolean writtenToDisk = UtilClass.writeResponseBodyToDisk(response.body(), fileDir, fileNm);
                    UtilClass.logD(TAG, "file download was a success? " + writtenToDisk);

                    if(writtenToDisk){
                        installAPK();
                    }else{
                        Toast.makeText(getActivity(), "다운로드 실패", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    UtilClass.logD(TAG, "response isFailed="+response);
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure downloadFile",Toast.LENGTH_LONG).show();
            }
        });

    }

    public void installAPK() {
        UtilClass.logD("InstallApk", "Start");
        File apkFile = new File(fileDir + fileNm);
        if(apkFile.exists()) {
            try {
                Intent webLinkIntent = new Intent(Intent.ACTION_VIEW);
                Uri uri = null;

                // So you have to use Provider
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", apkFile);

                    // Add in case of if We get Uri from fileProvider.
                    webLinkIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    webLinkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }else{
                    uri = Uri.fromFile(apkFile);
                }

                webLinkIntent.setDataAndType(uri, "application/vnd.android.package-archive");

                startActivity(webLinkIntent);
            } catch (ActivityNotFoundException ex){
                ex.printStackTrace();
                Toast.makeText(getActivity(), "설치에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getActivity(), "최신버전 파일을 다운로드 합니다.", Toast.LENGTH_SHORT).show();
            try {
                downloadFile("http://w-cms.co.kr:9090/app/apkDown.do?appGubun="+fileNm);
            }catch (Exception e){

            }
        }

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

    @OnClick(R.id.imageView8)
    public void getMenu8() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://gcop.posco.co.kr/"));
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
