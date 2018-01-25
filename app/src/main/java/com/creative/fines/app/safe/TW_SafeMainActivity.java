package com.creative.fines.app.safe;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.fines.app.R;
import com.creative.fines.app.menu.MainFragment;
import com.creative.fines.app.util.SettingPreference;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TW_SafeMainActivity extends AppCompatActivity {
    private static final String TAG = "TW_SafeMainActivity";
    private String url= MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Safe/safe_lCategoryList";
//    private String url="http://mlovesm.cafe24.com:8080/RestAPI/safe/safe_write.do?";
    private String daeClass_code;
    private String jungClass_code;
    private String user_code;

    private SettingPreference pref = new SettingPreference("loginData",this);
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int CROP_FROM_IMAGE = 3;
    private String filePath;
    private String file_seq;
    private String fileName;
    private ProgressDialog dialog;
    private String mCurrentPhotoPath;
    private Uri photoURI, albumURI = null;
    private Boolean album =false;

    @Bind(R.id.webView1) WebView webView;
    @Bind(R.id.top_title) TextView textTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_view);
        ButterKnife.bind(this);

        textTitle.setText(getIntent().getStringExtra("title"));
        findViewById(R.id.top_save).setVisibility(View.VISIBLE);

        daeClass_code = getIntent().getStringExtra("daeClass_code");
        jungClass_code = getIntent().getStringExtra("jungClass_code");
        loadLoginData();
        final Context myApp = this;

        //자바스크립트 Alert,confirm 사용
        webView.setWebChromeClient(new WebChromeClient() {
            ProgressBar pb = (ProgressBar)TW_SafeMainActivity.this.findViewById(R.id.progressBar1);

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new android.app.AlertDialog.Builder(myApp)
                        .setTitle("경고")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                // TODO Auto-generated method stub
                //return super.onJsConfirm(view, url, message, result);
                new android.app.AlertDialog.Builder(view.getContext())
                        .setTitle("알림")
                        .setMessage(message)
                        .setPositiveButton("네",
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("아니오",
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.cancel();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }

            public void onProgressChanged(WebView webView, int paramInt) {
                this.pb.setProgress(paramInt);
                if (paramInt == 100)
                {
                    this.pb.setVisibility(View.GONE);
                    return;
                }
                this.pb.setVisibility(View.VISIBLE);
            }
        });//setWebChromeClient 재정의

        WebSettings wSetting = webView.getSettings();
        webView.setWebViewClient(new WebViewClient()); // 이걸 안해주면 새창이 뜸
        webView.setWebViewClient(new MyWebViewClient());
        wSetting.setJavaScriptEnabled(true);      // 웹뷰에서 자바 스크립트 사용
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

//        webView.loadUrl(url+"large_cd="+daeClass_code+"&mid_cd="+jungClass_code+"&user_code="+user_code);
        webView.loadUrl(url+"cate1_code="+daeClass_code+"&cate2_code="+jungClass_code+"&user_code="+user_code);
        webView.addJavascriptInterface(new AndroidBridge(), "android");

    }//onCreate

    private class AndroidBridge {
        @JavascriptInterface
        public void imgChoice(final String paramString){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    file_seq = paramString;
                    imagesChoice();
                }
            });
        }

    }

    private void loadLoginData() {
        user_code = pref.getValue("sabun_no","");
        String user_nm= pref.getValue("user_nm","");
        String user_sosok= pref.getValue("user_sosok","");
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
//            Log.d("shouldOveride","웹뷰클릭 됨="+url);
            view.loadUrl(url);

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            dialog = new ProgressDialog(TW_SafeMainActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Loading...");
            dialog.setProgress(0);
            dialog.setMax(100);
            dialog.setCancelable(false);
            dialog.show();
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            try {
                if (dialog.isShowing()) {
                    dialog.cancel();
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        }

        @Override
        public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.d("onReceivedError", "errorCode=" + errorCode);
            switch(errorCode) {
                case ERROR_AUTHENTICATION:              // 서버에서 사용자 인증 실패
                case ERROR_BAD_URL:                     // 잘못된 URL
                case ERROR_CONNECT:                     // 서버로 연결 실패
                case ERROR_FAILED_SSL_HANDSHAKE:     	// SSL handshake 수행 실패
                case ERROR_FILE:                        // 일반 파일 오류
                case ERROR_FILE_NOT_FOUND:              // 파일을 찾을 수 없습니다
                case ERROR_HOST_LOOKUP:            		// 서버 또는 프록시 호스트 이름 조회 실패
                case ERROR_IO:                          // 서버에서 읽거나 서버로 쓰기 실패
                case ERROR_PROXY_AUTHENTICATION:    	// 프록시에서 사용자 인증 실패
                case ERROR_REDIRECT_LOOP:               // 너무 많은 리디렉션
                case ERROR_TIMEOUT:                     // 연결 시간 초과
                case ERROR_TOO_MANY_REQUESTS:           // 페이지 로드중 너무 많은 요청 발생
                case ERROR_UNKNOWN:                     // 일반 오류
                case ERROR_UNSUPPORTED_AUTH_SCHEME:  	// 지원되지 않는 인증 체계
                case ERROR_UNSUPPORTED_SCHEME:			// URI가 지원되지 않는 방식

                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(view.getContext());
                    builder.setTitle("Error");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.setMessage("네트워크 상태가 원활하지 않습니다. 잠시 후 다시 시도해 주세요.");
                    builder.show();

                    break;
            }
        }
    }//MyWebViewClient

    private void imagesChoice(){
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(TW_SafeMainActivity.this);
        alertDlg.setTitle("선택하세요.")
        .setCancelable(false);

        alertDlg.setPositiveButton("앨범", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int paramInt) {
                getPhotoFromGallery();
            }
        });
        alertDlg.setNegativeButton("촬영", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int paramInt) {
                getPhotoFromCamera();
            }
        });

//        alertDlg.setMessage("?");
        alertDlg.show();
    }

    private void getPhotoFromCamera() { // 카메라 촬영 후 이미지 가져오기
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();      //사진 찍은 후 임시파일 저장

            if(photoFile != null){
                photoURI = Uri.fromFile(photoFile); //임시 파일의 위치,경로 가져옴
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI); //임시 파일 위치에 저장
                startActivityForResult(intent, PICK_FROM_CAMERA);
            }
        }
    }

    private void getPhotoFromGallery() { // 갤러리에서 이미지 가져오기
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select file to upload "), PICK_FROM_ALBUM);
    }

    private File createImageFile() {
        String imageFileName = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File storageDir = new File(Environment.getExternalStorageDirectory()+"/Safe/", imageFileName);
        mCurrentPhotoPath = storageDir.getAbsolutePath();
        return storageDir;
    }

    private void cropImage() {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoURI, "image/*");

//        intent.putExtra("outputX", 200);
//        intent.putExtra("outputY", 200);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);

        if(album == false) {
            intent.putExtra("output",photoURI);
            Log.d(TAG,"photoURI="+photoURI);
        }else{
            intent.putExtra("output",albumURI);
            Log.d(TAG,"albumURI="+albumURI);
        }
        startActivityForResult(intent, CROP_FROM_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode){
            case  PICK_FROM_ALBUM:{
                album = true;
                File albumFile  = createImageFile();
                if(albumFile != null){
                    albumURI = Uri.fromFile(albumFile);
                }
                photoURI = data.getData();  //앨범이미지 경로
            }

            case PICK_FROM_CAMERA:{
                cropImage();

                break;
            }

            case CROP_FROM_IMAGE:{
                Bitmap photo = BitmapFactory.decodeFile(photoURI.getPath());    //크롭된 이미지
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);  //동기화

                if(album == false) {
                    intent.setData(photoURI);
                    filePath= photoURI.getPath();
                }else{
                    album= false;
                    intent.setData(albumURI);
                    filePath= albumURI.getPath();
                }
                Log.d(TAG,"filePath="+filePath);
                int lastIndexOf = filePath.lastIndexOf("/");
                fileName= filePath.substring(lastIndexOf+1, filePath.length());

                this.sendBroadcast(intent);

                dialog = ProgressDialog.show(TW_SafeMainActivity.this, "Uploading", "Please wait...", true);
                new ImageUploadTask().execute();

                break;
            }
        }
    }

    // 파일명 찾기
    private String getName(Uri uri) {
        String[] projection = { MediaStore.Images.ImageColumns.DISPLAY_NAME };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DISPLAY_NAME);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    // 실제 경로
    private String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void storeCropImage(Bitmap bitmap, String filePath){
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Safe";
        File directory_Smart = new File(dirPath);

        if(!directory_Smart.exists())   directory_Smart.mkdir();
        File copyFile = new File(filePath);
        BufferedOutputStream out = null;

        try {
            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile)));
            out.flush();
            out.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    class ImageUploadTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... unsued) {
            try {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//                builder.addTextBody("STRING_KEY", "STRING_VALUE", ContentType.create("Multipart/related", "UTF-8"));
                builder.addPart("fileData", new FileBody(new File(filePath)));

                //전송
                HttpClient httpClient = AndroidHttpClient.newInstance("Android");
//                HttpPost httpPost = new HttpPost("http://192.168.0.20:9191/RestAPI/safeFileUpload.do");
                HttpPost httpPost = new HttpPost("http://mlovesm.cafe24.com:8080/RestAPI/safeFileUpload.do");
                httpPost.setHeader("Accept-Charset","UTF-8");
                httpPost.setHeader("ENCTYPE","multipart/form-data");

                httpPost.setEntity(builder.build());
                httpClient.execute(httpPost);
//                HttpResponse httpResponse = httpClient.execute(httpPost);

//                BufferedReader reader = new BufferedReader(
//                        new InputStreamReader(
//                                httpResponse.getEntity().getContent(), "UTF-8"));
//
//                String sResponse = reader.readLine();
                return filePath;
            } catch (Exception e) {
                if (dialog.isShowing())
                    dialog.dismiss();
//                Toast.makeText(getApplicationContext(), "업로드에 실패 하였습니다.", Toast.LENGTH_LONG).show();
                Log.e(e.getClass().getName(), e.getMessage(), e);
                return null;
            }

            // (null);
        }

        @Override
        protected void onProgressUpdate(Void... unsued) {

        }

        @Override
        protected void onPostExecute(String sResponse) {
            try {
                if (dialog.isShowing()) dialog.dismiss();
                Log.d(TAG, "sResponse="+sResponse);
                if(sResponse!=null){
                    Toast.makeText(getApplicationContext(), "업로드 완료", Toast.LENGTH_SHORT).show();
                    webView.loadUrl("javascript: setUploadedImg('" + file_seq + "','" + fileName + "');");
                }else{
                    Toast.makeText(getApplicationContext(), "업로드에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "업로드에 실패 하였습니다.", Toast.LENGTH_LONG).show();
                Log.e(e.getClass().getName(), e.getMessage(), e);
            }
        }
    }


    @OnClick(R.id.top_home)
    public void goHome() {
        Intent intent = new Intent(getBaseContext(),MainFragment.class);
        startActivity(intent);
    }

    @OnClick(R.id.top_save)
    public void saveData() {
        webView.loadUrl("javascript:formSubmit();");
    }


}
