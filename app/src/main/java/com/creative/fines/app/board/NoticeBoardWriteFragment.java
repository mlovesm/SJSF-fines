package com.creative.fines.app.board;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.creative.fines.app.R;
import com.creative.fines.app.bean.Datas;
import com.creative.fines.app.bean.RetrofitService;
import com.creative.fines.app.fragment.ActivityResultEvent;
import com.creative.fines.app.fragment.BusProvider;
import com.creative.fines.app.menu.MainFragment;
import com.creative.fines.app.util.FilePath;
import com.creative.fines.app.util.KeyValueArrayAdapter;
import com.creative.fines.app.util.UtilClass;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.squareup.otto.Subscribe;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class NoticeBoardWriteFragment extends Fragment {
    private static final String TAG = "NoticeBoardWriteFragment";
    private static final String INSERT_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Board/noticeBoardWrite";
    private static String MODIFY_VIEW_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Board/noticeBoardDetail";
    private static String MODIFY_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Board/noticeBoardModify";
    private static String DELETE_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Board/noticeBoardDelete";
    private static String PUSH_DATA_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Board/sendPushData";

    private String mode="";
    private String idx="";
    private String dataSabun;

    @Bind(R.id.top_title) TextView textTitle;
    @Bind(R.id.editText1) EditText et_title;
    @Bind(R.id.editText2) EditText et_memo;
    @Bind(R.id.spinner1) Spinner spn;
    @Bind(R.id.radioGroup) RadioGroup radioGroup;
    @Bind(R.id.textView1) TextView tv_fileName;
    @Bind(R.id.textView2) TextView tv_writerName;

    //파일,앨범,촬영 선택 업로드 관련
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int CROP_FROM_IMAGE = 3;
    private static final int FROM_FILE = 4;
    private boolean fileDown= false;

    final int RESULT_OK=-1;
    private String filePath;
    private String fileName;
    private String fileOrgName;
    private String modifyFileName;
    private String modifyFileOrgName;

    private ProgressDialog dialog;

    private String mCurrentPhotoPath;
    private Uri photoURI, albumURI, fileURI = null;
    private Boolean album =false;

    private String selectedPostionKey;  //스피너 선택된 키값
    private int selectedPostion=0;    //스피너 선택된 Row 값
    private String selectedRadio="A";    //라디오 선택된 라디오 값
    private String push_target;

    private String url = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Board/noticeBoardCodeList/push_target/";
    private String selectGubunKey="";
    private String[] gubunKeyList;
    private String[] gubunValueList;

    private PermissionListener permissionlistener;
    private AQuery aq = new AQuery( getActivity() );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.basic_write, container, false);
        ButterKnife.bind(this, view);

        mode= getArguments().getString("mode");
        view.findViewById(R.id.top_save).setVisibility(View.VISIBLE);

        if(mode.equals("insert")){
            dataSabun= MainFragment.loginSabun;
            view.findViewById(R.id.linear3).setVisibility(View.GONE);
            textTitle.setText("공지사항 작성");
            tv_writerName.setText(MainFragment.loginName);
            radioGroup.check(R.id.radio1);
            getNoticeBoardCodeData("A");
        }else{
            textTitle.setText("공지사항 수정");
            idx= getArguments().getString("push_key");
            push_target= getArguments().getString("push_target");
            fileDown= true;
            if(push_target.equals("A")) {
                radioGroup.check(R.id.radio1);
            }else if(push_target.equals("F")) {
                selectedRadio="F";
                radioGroup.check(R.id.radio2);
            }else{
                selectedRadio="J";
                radioGroup.check(R.id.radio3);
            }
            async_progress_dialog("getBoardDetailInfo");
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radio1:
                        selectGubunKey="";
                        selectedPostion=0;
                        selectedRadio="A";
                        getNoticeBoardCodeData(selectedRadio);
                        break;
                    case R.id.radio2:
                        selectedPostion=0;
                        selectedRadio="F";
                        getNoticeBoardCodeData(selectedRadio);
                        break;
                    case R.id.radio3:
                        selectedPostion=0;
                        selectedRadio="J";
                        getNoticeBoardCodeData(selectedRadio);
                        break;
                    default:
                        break;
                }
            }
        });

        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectGubunKey= adapter.getEntryValue(position);

                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }//onCreateView

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroyView() {
        BusProvider.getInstance().unregister(this);
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UtilClass.logD(TAG, "unregister Called.");
//
//        getActivity().unregisterReceiver(receiverNotificationClicked);
//        receiverNotificationClicked = null;
//
//        getActivity().unregisterReceiver(receiverDownloadComplete);
//        receiverDownloadComplete = null;
    }

    @OnClick(R.id.imageButton1)
    public void imageUpload(){
        permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
//                Toast.makeText(getApplicationContext(), "권한 허가", Toast.LENGTH_SHORT).show();
                if(MainFragment.loginSabun.equals(dataSabun)){
                    imagesChoice();
                }else{
                    Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(getActivity(), "권한 거부 목록\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();

            }
        };
        new TedPermission(getActivity())
                .setPermissionListener(permissionlistener)
                .setRationaleMessage("파일/촬영 업로드를 위해선 권한이 필요합니다.")
                .setDeniedMessage("권한을 확인하세요.\n\n [설정] > [애플리케이션] [해당앱] > [권한]")
                .setGotoSettingButtonText("권한확인")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();
    }

    @OnClick(R.id.textView1)
    public void fileDown(){
        if(fileDown){
            final AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
            alertDlg.setTitle("알림");

            // '예' 버튼이 클릭되면
            alertDlg.setPositiveButton("파일받기", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    new FileDownTask().execute();
                    downManager();
                }
            });
            // '아니오' 버튼이 클릭되면
            alertDlg.setNeutralButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDlg.show();

        }else{

        }
    }

    private void imagesChoice(){
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
        alertDlg.setTitle("선택하세요.")
                .setCancelable(true);

        alertDlg.setPositiveButton("촬영", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int paramInt) {
                getPhotoFromCamera();
            }
        });
        alertDlg.setNegativeButton("파일", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int paramInt) {
                getFileList();
            }
        });

//        alertDlg.setMessage("?");
        alertDlg.show();
    }

    private void getFileList() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        getActivity().startActivityForResult(Intent.createChooser(intent, "Select file to upload "), FROM_FILE);
    }

    private void getPhotoFromCamera() { // 카메라 촬영 후 이미지 가져오기
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(getActivity() != null) {
            File photoFile = createImageFile();      //사진 찍은 후 임시파일 저장

            if(photoFile != null){
                photoURI = Uri.fromFile(photoFile); //임시 파일의 위치,경로 가져옴
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI); //임시 파일 위치에 저장
                getActivity().startActivityForResult(intent, PICK_FROM_CAMERA);
            }
        }
    }

    private void getPhotoFromGallery() { // 갤러리에서 이미지 가져오기
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        getActivity().startActivityForResult(Intent.createChooser(intent, "Select file to upload "), PICK_FROM_ALBUM);
    }

    private File createImageFile() {
        String imageFileName = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File storageDir = new File(Environment.getExternalStorageDirectory()+"/File/", imageFileName);
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
            UtilClass.logD(TAG,"photoURI="+photoURI);
        }else{
            intent.putExtra("output",albumURI);
            UtilClass.logD(TAG,"albumURI="+albumURI);
        }
        getActivity().startActivityForResult(intent, CROP_FROM_IMAGE);
    }

    /**
     * Fragment에서 startactivityForresult실행시 fragment에 들어오지 않는 문제
     *
     * @param activityResultEvent
     */
    @Subscribe
    public void onActivityResultEvent(ActivityResultEvent activityResultEvent){
        UtilClass.logD(TAG, "activeResultEvent="+activityResultEvent);
        onActivityResult(activityResultEvent.getRequestCode(), activityResultEvent.getResultCode(), activityResultEvent.getData());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode){
            case FROM_FILE:{
                fileURI = data.getData();
                filePath= FilePath.getPath(getActivity(), fileURI);
                UtilClass.logD(TAG, "filePath? "+filePath);

                int lastIndexOf = filePath.lastIndexOf("/");
                fileName= filePath.substring(lastIndexOf+1, filePath.length());

                new ImageUploadTask().execute();
//                new NetworkCall().execute("로그인");

                break;
            }

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

                // 임시 파일 삭제
//                File f = new File(filePath);
//                if(f.exists()) {
//                    f.delete();
//                }

                UtilClass.logD(TAG,"filePath="+filePath);
                int lastIndexOf = filePath.lastIndexOf("/");
                fileName= filePath.substring(lastIndexOf+1, filePath.length());

                getActivity().sendBroadcast(intent);
                new ImageUploadTask().execute();

                break;
            }
        }
    }

    private class FileUplaodTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            RetrofitService retrofitService = RetrofitService.retrofit.create(RetrofitService.class);
            try {
                File file = new File(filePath);
                RequestBody reqFile = RequestBody.create(MediaType.parse("*/*"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData(fileName, file.getName(), reqFile);

                final Call<Datas> call = retrofitService.fileUpload(body);

                UtilClass.logD(TAG, "body==="+call.execute().body());
//                return call.execute().body().toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getActivity(),"result======"+result,Toast.LENGTH_SHORT).show();

        }
    }

    class ImageUploadTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(getActivity(), "Uploading", "Please wait...", true);

        }

        @Override
        protected String doInBackground(Void... unsued) {
            try {
                MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//                builder.addTextBody("STRING_KEY", "STRING_VALUE", ContentType.create("Multipart/related", "UTF-8"));
                builder.addPart("fileData", new FileBody(new File(filePath)));

                //전송
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(MainFragment.UPLOAD_URL);
                httpPost.setHeader("Accept-Charset","UTF-8");
                httpPost.setHeader("ENCTYPE","multipart/form-data");

                httpPost.setEntity(builder.build());
                HttpResponse response =httpClient.execute(httpPost);

                String result = "";
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
            } catch (Exception e) {
                if (dialog.isShowing())
                    dialog.dismiss();
                UtilClass.logD(TAG, "업로드 에러");
                Log.e(e.getClass().getName(), e.getMessage(), e);
                return null;
            }

        }

        @Override
        protected void onProgressUpdate(Void... unsued) {

        }

        @Override
        protected void onPostExecute(String sResponse) {
            try {
                if (dialog.isShowing()) dialog.dismiss();
                if(sResponse!=null){
                    JSONObject jso = new JSONObject(sResponse);
                    UtilClass.logD(TAG, "sResponse="+jso);

                    if(jso.get("status").toString().equals("success")){
                        Toast.makeText(getActivity(), "업로드 완료", Toast.LENGTH_SHORT).show();
                        fileDown= true;
                        fileName= jso.get("file_nm").toString();
                        fileOrgName= jso.get("file_org_nm").toString();
                        tv_fileName.setText(fileOrgName);
                    }else{
                        Toast.makeText(getActivity(), "업로드에 실패 하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (Exception e) {
                Toast.makeText(getActivity(), "업로드에 실패 하였습니다.", Toast.LENGTH_LONG).show();
                Log.e(e.getClass().getName(), e.getMessage(), e);
            }
        }
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

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "Loading...", true, true);
        dialog.setInverseBackgroundForced(false);

        String url = null;
        if(callback.equals("getBoardDetailInfo")){
            url= MODIFY_VIEW_URL+"/"+idx;
            aq.ajax( url, null, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status ) {
                    if( object != null) {
                        try {
                            dataSabun= object.getJSONArray("datas").getJSONObject(0).get("input_id").toString();
                            if(MainFragment.loginSabun.equals(dataSabun)){
                            }else{
                                et_title.setFocusableInTouchMode(false);
                                et_memo.setFocusableInTouchMode(false);

                                getActivity().findViewById(R.id.linear1).setVisibility(View.GONE);
                                getActivity().findViewById(R.id.linear2).setVisibility(View.GONE);
                                getActivity().findViewById(R.id.linear3).setVisibility(View.GONE);
                            }
                            selectedPostionKey = object.getJSONArray("datas").getJSONObject(0).get("detail_target").toString();
                            et_title.setText(object.getJSONArray("datas").getJSONObject(0).get("push_title").toString());
                            et_memo.setText(object.getJSONArray("datas").getJSONObject(0).get("push_text").toString());
                            tv_writerName.setText(object.getJSONArray("datas").getJSONObject(0).get("input_nm").toString());

                            String file_org_nm= object.getJSONArray("datas").getJSONObject(0).get("file_org_nm").toString();
                            if(!file_org_nm.equals("null")){
                                tv_fileName.setText(file_org_nm);
                                tv_fileName.setTextColor(Color.rgb(48,48,208));
                                fileOrgName= file_org_nm;
                                modifyFileName= object.getJSONArray("datas").getJSONObject(0).get("file_nm").toString();
                                fileName= modifyFileName;
                            }else{
                                fileDown= false;
                            }

                        } catch ( Exception e ) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "에러코드 Notice 2", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        UtilClass.logD(TAG,"getBoardDetailInfo Data is Null");
                        Toast.makeText(getActivity(),"데이터 로드 실패",Toast.LENGTH_SHORT).show();
                    }
                }
            } );
        }else{

        }
        aq.progress(dialog).ajax(null, JSONObject.class, this, callback);

    }

    //글 수정 데이터
    public void getBoardDetailInfo(String url, JSONObject object, AjaxStatus status) throws JSONException {
        getNoticeBoardCodeData(push_target);
    }

    public void getNoticeBoardCodeData(String push_target) {
        aq.ajax( url+push_target, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status ) {
                if( object != null) {
                    try {
                        gubunKeyList= new String[object.getJSONArray("datas").length()];
                        gubunValueList= new String[object.getJSONArray("datas").length()];
                        for(int i=0; i<object.getJSONArray("datas").length();i++){
                            gubunKeyList[i]= object.getJSONArray("datas").getJSONObject(i).get("code").toString();
                            if(gubunKeyList[i].equals(selectedPostionKey))  selectedPostion= i;
                            gubunValueList[i]= object.getJSONArray("datas").getJSONObject(i).get("name").toString();
                        }
                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntryValues(gubunKeyList);
                        adapter.setEntries(gubunValueList);

                        spn.setPrompt("구분");
                        spn.setAdapter(adapter);
                        spn.setSelection(selectedPostion);
                    } catch ( Exception e ) {

                    }
                }else{
                    UtilClass.logD(TAG,"getNoticeBoardCodeData Data is Null");
                    Toast.makeText(getActivity(),"데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        } );

    }

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

    @OnClick({R.id.textButton1})
    public void alertDialogPush(){
        if(MainFragment.loginSabun.equals(dataSabun)){
            alertDialog("P");
        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.textButton2, R.id.top_save})
    public void alertDialogSave(){
        if(MainFragment.loginSabun.equals(dataSabun)){
            alertDialog("S");
        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.textButton3})
    public void alertDialogDelete(){
        if(MainFragment.loginSabun.equals(dataSabun)){
            alertDialog("D");
        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void alertDialog(final String gubun){
        final AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
        alertDlg.setTitle("알림");
        if(gubun.equals("S")){
            alertDlg.setMessage("작성하시겠습니까?");
        }else if(gubun.equals("D")){
            alertDlg.setMessage("삭제하시겠습니까?");
        }else{
            alertDlg.setMessage("전송하시겠습니까?");
        }
        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(gubun.equals("S")){
                    postData();
                }else if(gubun.equals("D")){
                    deleteData();
                }else{
                    onPushData();
                }
            }
        });
        // '아니오' 버튼이 클릭되면
        alertDlg.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();  // AlertDialog를 닫는다.
            }
        });
        alertDlg.show();
    }

    //삭제
    public void deleteData() {
        WebServiceTask wst = new WebServiceTask(WebServiceTask.DELETE_TASK, getActivity(), "Loading...");
        wst.addNameValuePair("idx",idx);

        wst.execute(new String[] { DELETE_URL+"/"+idx });
    }

    //작성,수정
    public void postData() {
        String push_title = et_title.getText().toString();
        String push_text = et_memo.getText().toString();

        if (et_title.equals("") || et_title.length()==0) {
            Toast.makeText(getActivity(), "빈칸을 채워주세요.",Toast.LENGTH_LONG).show();
            return;
        }

        WebServiceTask wst=null;
        if(mode.equals("insert")){
            wst = new WebServiceTask(WebServiceTask.POST_TASK, getActivity(), "Loading...");
        }else{
            wst = new WebServiceTask(WebServiceTask.PUT_TASK, getActivity(), "Loading...");
        }

        wst.addNameValuePair("writer_sabun", MainFragment.loginSabun);
        wst.addNameValuePair("writer_name", MainFragment.loginName);
        wst.addNameValuePair("push_title",push_title);
        wst.addNameValuePair("push_text",push_text);
        wst.addNameValuePair("push_target",selectedRadio);
        wst.addNameValuePair("detail_target",selectGubunKey);

        if(tv_fileName.length()>1){
            if(!mode.equals("insert")){
                UtilClass.logD(TAG, "fileName="+fileName);
                UtilClass.logD(TAG, "modifyFileName="+modifyFileName);

                if(fileName.equals(modifyFileName)){    //파일 수정
                    wst.addNameValuePair("fileModify","false");
                }else{
                    wst.addNameValuePair("fileModify","true");
                    wst.addNameValuePair("modifyFileName", modifyFileName);
                }

            }
            wst.addNameValuePair("file_nm",fileName);
            wst.addNameValuePair("file_org_nm",fileOrgName);
            wst.addNameValuePair("fileYN","true");
        }else{
            wst.addNameValuePair("fileYN","false");
        }

        // the passed String is the URL we will POST to
        if(mode.equals("insert")){
            wst.execute(new String[] { INSERT_URL });
        }else{
            wst.execute(new String[] { MODIFY_URL+"/"+idx });
        }

    }

    //푸시 전송
    public void onPushData() {
        String push_title = et_title.getText().toString();
        String push_text = et_memo.getText().toString();

        if (et_title.equals("") || et_title.length()==0) {
            Toast.makeText(getActivity(), "빈칸을 채워주세요.",Toast.LENGTH_LONG).show();
            return;
        }

        WebServiceTask wst = new WebServiceTask(WebServiceTask.POST_TASK, getActivity(), "Loading...");

        wst.addNameValuePair("writer_sabun", MainFragment.loginSabun);
        wst.addNameValuePair("writer_name", MainFragment.loginName);
        wst.addNameValuePair("push_target",selectedRadio);
        wst.addNameValuePair("detail_target",selectGubunKey);
        wst.addNameValuePair("title", push_title);
        wst.addNameValuePair("message", push_text);

        wst.execute(new String[] { PUSH_DATA_URL });

    }

    //작성 완료
    public void handleResponse(String response) {
        UtilClass.logD(TAG,"response="+response);

        try {
            JSONObject jso = new JSONObject(response);
            String status= jso.get("status").toString();

            if(status.equals("successOnPush")){
                String pushSend= jso.get("pushSend").toString();

                if(pushSend.equals("success")){
                    Toast.makeText(getActivity(), "푸시데이터가 전송 되었습니다.",Toast.LENGTH_LONG).show();
                }else if(pushSend.equals("empty")){
                    Toast.makeText(getActivity(), "푸시데이터를 받는 사용자가 없습니다.",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getActivity(), "푸시 전송이 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
            }

            if(status.equals("success")){
                getActivity().onBackPressed();
            }else if(status.equals("successOnPush")){

            }else{
                Toast.makeText(getActivity(), "작업에 실패하였습니다.",Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            Toast.makeText(getActivity(), "handleResponse Notice",Toast.LENGTH_LONG).show();
        }

    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private class WebServiceTask extends AsyncTask<String, Integer, String> {

        public static final int POST_TASK = 1;
        public static final int GET_TASK = 2;
        public static final int PUT_TASK = 3;
        public static final int DELETE_TASK = 4;

        private static final String TAG = "WebServiceTask";

        // connection timeout, in milliseconds (waiting to connect)
        private static final int CONN_TIMEOUT = 3000;

        // socket timeout, in milliseconds (waiting for data)
        private static final int SOCKET_TIMEOUT = 5000;

        private int taskType = GET_TASK;
        private Context mContext = null;
        private String processMessage = "Processing...";

        private ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        private JSONObject jsonParams = new JSONObject();

        private ProgressDialog pDlg = null;

        public WebServiceTask(int taskType, Context mContext, String processMessage) {

            this.taskType = taskType;
            this.mContext = mContext;
            this.processMessage = processMessage;
        }

        public void addNameValuePair(String name, String value) {
            params.add(new BasicNameValuePair(name, value));
        }
        public void addJsonValuePair(String name, String value) {
            try {
                jsonParams.put(name, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
            hideKeyboard();
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

        @NotThreadSafe
        class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
            public static final String METHOD_NAME = "DELETE";

            public String getMethod() {
                return METHOD_NAME;
            }
            public HttpDeleteWithBody(final String uri) {
                super();
                setURI(URI.create(uri));
            }
            public HttpDeleteWithBody(final URI uri) {
                super();
                setURI(uri);
            }
            public HttpDeleteWithBody() {
                super();
            }
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
                    case GET_TASK:
                        HttpGet httpget = new HttpGet(url);
                        response= httpclient.execute(httpget);
                        break;
                    case PUT_TASK:
                        HttpPut httpput = new HttpPut(url);
                        // Add parameters
                        httpput.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
                        response= httpclient.execute(httpput);
                        break;
                    case DELETE_TASK:
                        HttpDeleteWithBody httpdel = new HttpDeleteWithBody(url);
                        // Add parameters
                        httpdel.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
                        response= httpclient.execute(httpdel);
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

    }//WebServiceTask

        /*
    *   파일 다운로드 매니저
    * */

    private long mDownloadReference;
    private DownloadManager mDownloadManager;
    private BroadcastReceiver receiverDownloadComplete;    //다운로드 완료 체크
    private BroadcastReceiver receiverNotificationClicked;    //다운로드 시작 체크


    public void downManager(){
        if(mDownloadManager == null) {
            mDownloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        }

        try {
            Uri uri = Uri.parse(MainFragment.ipAddress+"/uploadFile/"+fileName);        //data는 파일을 떨궈 주는 uri
            UtilClass.logD(TAG, "uri="+uri);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("파일다운로드");    //다운로드 완료시 noti에 제목

            request.setVisibleInDownloadsUi(true);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

            //모바일 네트워크와 와이파이일때 가능하도록
            request.setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            //다운로드 완료시 noti에 보여주는것
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS , fileOrgName);
            //다운로드 경로, 파일명을 적어준다
            mDownloadReference = mDownloadManager.enqueue(request);

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity(),"파일 다운로드 실패", Toast.LENGTH_SHORT).show();

        }
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED);

        receiverNotificationClicked = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String extraId = DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS;
                long[] references = intent.getLongArrayExtra(extraId);
                for (long reference : references) {

                }
            }
        };

        getActivity().registerReceiver(receiverNotificationClicked, filter);


        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        receiverDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                if(mDownloadReference == reference){

                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(reference);
                    Cursor cursor = mDownloadManager.query(query);

                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);

                    int status = cursor.getInt(columnIndex);
                    int reason = cursor.getInt(columnReason);

                    cursor.close();

                    switch (status){

                        case DownloadManager.STATUS_SUCCESSFUL :

                            Toast.makeText(getActivity(), "다운로드 완료.", Toast.LENGTH_SHORT).show();
                            break;

                        case DownloadManager.STATUS_PAUSED :

                            Toast.makeText(getActivity(), "다운로드 중지 : " + reason, Toast.LENGTH_SHORT).show();
                            break;

                        case DownloadManager.STATUS_FAILED :

                            Toast.makeText(getActivity(), "다운로드 취소 : " + reason, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        };

        getActivity().registerReceiver(receiverDownloadComplete, intentFilter);
    }


}
