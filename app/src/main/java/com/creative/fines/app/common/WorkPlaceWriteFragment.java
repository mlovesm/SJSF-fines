package com.creative.fines.app.common;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.creative.fines.app.R;
import com.creative.fines.app.adaptor.BaseAdapter;
import com.creative.fines.app.menu.MainFragment;
import com.creative.fines.app.util.UtilClass;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WorkPlaceWriteFragment extends Fragment {
    private static final String TAG = "WorkPlaceWriteFragment";
    private static final String INSERT_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Common/workPlaceWrite";
    private static String MODIFY_VIEW_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Common/workPlaceDetail";
    private static String MODIFY_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Common/workPlaceModify";
    private static String DELETE_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Common/workPlaceDelete";

    private String mode="";
    private String idx="";
    private String dataSabun;

    @Bind(R.id.top_title) TextView textTitle;
    @Bind(R.id.textView1) TextView tv_date;
    @Bind(R.id.textView2) TextView tv_userName;
    @Bind(R.id.textView3) TextView tv_userSosok;
    @Bind(R.id.textView4) TextView tv_writerName;
    @Bind(R.id.editText1) EditText et_memo1;
    @Bind(R.id.editText2) EditText et_memo2;

    //검색 다이얼로그
    private Dialog mDialog = null;
    private Spinner search_spi;
    private String search_gubun;	//검색 구분
    private EditText et_search;
    private ListView listView;
    private ArrayList<HashMap<String,Object>> arrayList;
    private BaseAdapter mAdapter;
    private Button btn_search;
    private TextView btn_cancel;
    private String selectSabunKey="";

    private AQuery aq = new AQuery( getActivity() );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.work_place_write, container, false);
        ButterKnife.bind(this, view);

        mode= getArguments().getString("mode");
        if(mode==null) mode="";

        if(mode.equals("insert")){
            dataSabun= MainFragment.loginSabun;
            view.findViewById(R.id.linear2).setVisibility(View.GONE);
            textTitle.setText("작업개소현황 작성");
            tv_date.setText(UtilClass.getCurrentDate(1));
            tv_writerName.setText(MainFragment.loginName);
        }else{
            textTitle.setText("작업개소현황 수정");
            idx= getArguments().getString("work_key");
            async_progress_dialog("getBoardDetailInfo");
        }
        view.findViewById(R.id.top_save).setVisibility(View.VISIBLE);

        return view;
    }//onCreateView

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "Loading...", true, true);
        dialog.setInverseBackgroundForced(false);

        String url = null;
        if(callback.equals("searchUserData")){
            url= MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Login/searchUserData/gubun="+search_gubun+"/param="+et_search.getText();
        }else if(callback.equals("getBoardDetailInfo")){
            url= MODIFY_VIEW_URL+"/"+idx;
            aq.ajax( url, null, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status ) {
                    if( object != null) {
                        try {
                            dataSabun= object.getJSONArray("datas").getJSONObject(0).get("input_id").toString();
                            if(MainFragment.loginSabun.equals(dataSabun)){
                            }else{
                                et_memo1.setFocusableInTouchMode(false);
                                et_memo2.setFocusableInTouchMode(false);
                                getActivity().findViewById(R.id.linear1).setVisibility(View.GONE);
                                getActivity().findViewById(R.id.linear2).setVisibility(View.GONE);
                            }
                            selectSabunKey= object.getJSONArray("datas").getJSONObject(0).get("worker_id").toString();
                            tv_userName.setText(object.getJSONArray("datas").getJSONObject(0).get("worker_nm").toString().trim());
                            tv_userSosok.setText(object.getJSONArray("datas").getJSONObject(0).get("worker_sosok").toString().trim());
                            tv_date.setText(object.getJSONArray("datas").getJSONObject(0).get("work_date").toString());
                            et_memo1.setText(object.getJSONArray("datas").getJSONObject(0).get("work_loc").toString());
                            et_memo2.setText(object.getJSONArray("datas").getJSONObject(0).get("work_order").toString());
                            tv_writerName.setText(object.getJSONArray("datas").getJSONObject(0).get("input_nm").toString());

                        } catch ( Exception e ) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "에러코드 Work 2", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        UtilClass.logD(TAG,"Data is Null");
                        Toast.makeText(getActivity(),"데이터 로드 실패",Toast.LENGTH_SHORT).show();
                    }
                }
            } );
        }else{

        }
        aq.progress(dialog).ajax(url, JSONObject.class, this, callback);

    }

    //유저 조회 데이터
    public void searchUserData(String url, JSONObject object, AjaxStatus status) {
//        UtilClass.logD(TAG, "object= "+object);

        if( object != null) {
            try {
                arrayList = new ArrayList<>();
                arrayList.clear();
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("data1",object.getJSONArray("datas").getJSONObject(i).get("user_no").toString());
                    hashMap.put("data2",object.getJSONArray("datas").getJSONObject(i).get("user_nm").toString().trim());
                    hashMap.put("user_sosok",object.getJSONArray("datas").getJSONObject(i).get("user_sosok").toString().trim());
                    arrayList.add(hashMap);
                }
                mAdapter = new BaseAdapter(getActivity(), arrayList);
                listView.setAdapter(mAdapter);
            } catch ( Exception e ) {
                Toast.makeText(getActivity(), "에러코드 Peer 1", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }else{
            UtilClass.logD(TAG,"Data is Null");
            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //다이얼로그
    private void userSearchDialog() {
        final View linear = View.inflate(getActivity(), R.layout.search_dialog, null);
        mDialog = new Dialog(getActivity());
        mDialog.setTitle("직원 검색");
        search_spi= (Spinner) linear.findViewById(R.id.search_spi);
        et_search= (EditText) linear.findViewById(R.id.et_search);
        listView= (ListView) linear.findViewById(R.id.listView1);

        // Spinner 생성
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.user_list, android.R.layout.simple_spinner_dropdown_item);
//		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        search_spi.setPrompt("선택하세요.");
        search_spi.setAdapter(adapter);
        search_spi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				et_search.setText("position : " + position + parent.getItemAtPosition(position));
//				search_spi.getSelectedItem().toString();
                if(position==0){
                    search_gubun="user_nm";
                }else if(position==1){
                    search_gubun="sabun_no";
                }else{
                    search_gubun="";
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mDialog.setContentView(linear);

        // Back키 눌렀을 경우 Dialog Cancle 여부 설정
        mDialog.setCancelable(true);

        // Dialog 생성시 배경화면 어둡게 하지 않기
//		mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Dialog 밖을 터치 했을 경우 Dialog 사라지게 하기
        mDialog.setCanceledOnTouchOutside(true);

        btn_search = (Button) linear.findViewById(R.id.button1);
        btn_cancel = (TextView) linear.findViewById(R.id.textButton1);

        btn_search.setOnClickListener(button_click_listener);
        btn_cancel.setOnClickListener(button_click_listener);
        listView.setOnItemClickListener(new ListViewItemClickListener());

        // Dialog Cancle시 Event 받기
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dismissDialog();
            }
        });

        // Dialog Show시 Event 받기
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

            }
        });

        // Dialog Dismiss시 Event 받기
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        });

        mDialog.show();
    }

    private void dismissDialog() {
        if(mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private View.OnClickListener button_click_listener = new View.OnClickListener() {

        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button1:
                    InputMethodManager imm= (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);

                    async_progress_dialog("searchUserData");
                    break;

                case R.id.textButton1:
                    dismissDialog();
                    break;
            }
        }
    };

    //검색창 ListView의 item을 클릭했을 때.
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap = arrayList.get(position);
            ArrayList<String> arr = new ArrayList<>();
            for (Iterator iter = hashMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                arr.add((String) entry.getValue());
            }
//            UtilClass.logD(TAG, "?="+arr);
             tv_userSosok.setText(arrayList.get(position).get("user_sosok").toString().trim());
            tv_userName.setText(arrayList.get(position).get("data2").toString().trim());
            selectSabunKey= arrayList.get(position).get("data1").toString();
            dismissDialog();
        }
    }

    @OnClick({R.id.textButton1, R.id.top_save})
    public void alertDialogSave(){
        if(MainFragment.loginSabun.equals(dataSabun)){
            alertDialog("S");
        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.textButton2})
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
        }else{
            alertDlg.setMessage("삭제하시겠습니까?");
        }
        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(gubun.equals("S")){
                    postData();
                }else{
                    deleteData();
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
        String user_name = tv_userName.getText().toString();
        String work_date = tv_date.getText().toString();
        String work_loc = et_memo1.getText().toString();
        String work_order = et_memo2.getText().toString();

        if (user_name.equals("") || user_name.length()==0) {
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
        wst.addNameValuePair("user_name",user_name);
        wst.addNameValuePair("work_date",work_date);
        wst.addNameValuePair("work_loc",work_loc);
        wst.addNameValuePair("work_order",work_order);
        wst.addNameValuePair("work_per",selectSabunKey);

        // the passed String is the URL we will POST to
        if(mode.equals("insert")){
            wst.execute(new String[] { INSERT_URL });
        }else{
            wst.execute(new String[] { MODIFY_URL+"/"+idx });
        }

    }

    //작성 완료
    public void handleResponse(String response) {
        UtilClass.logD(TAG,"response="+response);

        try {
            JSONObject jso = new JSONObject(response);
            String status= jso.get("status").toString();

            if(status.equals("success")){
                getActivity().onBackPressed();
            }else{
                Toast.makeText(getActivity(), "작업에 실패하였습니다.",Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            Toast.makeText(getActivity(), "handleResponse Work",Toast.LENGTH_LONG).show();
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

    }

    @OnClick(R.id.button1)
    public void getUserSearch() {
        userSearchDialog();
    }

    //날짜설정
    @OnClick(R.id.date_button)
    public void getDateDialog() {
        getDialog("D");
    }


    public void getDialog(String gubun) {
        int year, month, day, hour, minute;

        GregorianCalendar calendar = new GregorianCalendar();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day= calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), date_listener, year, month, day);
        dialog.show();
    }

    private DatePickerDialog.OnDateSetListener date_listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Toast.makeText(getActivity(), year + "년" + (monthOfYear+1) + "월" + dayOfMonth +"일", Toast.LENGTH_SHORT).show();
            String month= UtilClass.addZero(monthOfYear+1);
            String day= UtilClass.addZero(dayOfMonth);

            tv_date.setText(year+"-"+month+"-"+day);
        }
    };

}
