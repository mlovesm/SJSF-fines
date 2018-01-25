package com.creative.fines.app.menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.creative.fines.app.R;
import com.creative.fines.app.fragment.FragMenuActivity;
import com.creative.fines.app.util.BackPressCloseSystem;
import com.creative.fines.app.util.SettingPreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private String url = "";
    private boolean valid = true;

    private BackPressCloseSystem backPressCloseSystem;

    @Bind(R.id.editText1) EditText _sabun_no;
    @Bind(R.id.editText2) EditText _user_nm;
    @Bind(R.id.editText3) EditText _user_sosok;
    @Bind(R.id.editText4) EditText _user_pw;
    @Bind(R.id.button1) Button loginButton;

    private SettingPreference pref = new SettingPreference("loginData",this);
    private AQuery aq = new AQuery( this );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        ButterKnife.bind(this);
        backPressCloseSystem = new BackPressCloseSystem(this);
        loadLoginData();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

    }//onCreate

    @Override
    protected void onNewIntent(Intent intent) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void saveLoginData(JSONObject object) {
        String sabun_noStr= _sabun_no.getText().toString();
        String user_nm= _user_nm.getText().toString();
        String user_sosok= _user_sosok.getText().toString();
        String user_pwStr= _user_pw.getText().toString();
        String latest_app_ver="";
        try {
            if(object.get("LATEST_APP_VER")!=null){
                latest_app_ver= object.get("LATEST_APP_VER").toString();
            }
//            latest_app_ver= object.get("result").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pref.put("sabun_no",sabun_noStr);
        pref.put("user_nm",user_nm);
        pref.put("user_sosok",user_sosok);
        pref.put("user_pw",user_pwStr);
        pref.put("LATEST_APP_VER",latest_app_ver);
    }

    private void loadLoginData() {
        String sabun_no= pref.getValue("sabun_no","");
        String user_nm= pref.getValue("user_nm","");
        String user_sosok= pref.getValue("user_sosok","");
        String user_pw= pref.getValue("user_pw","");

        _sabun_no.setText(sabun_no);
        _user_nm.setText(user_nm);
        _user_sosok.setText(user_sosok);
        _user_pw.setText(user_pw);

/*        Toast.makeText(getApplicationContext(), "sabun_no : " + sabun_no+",user_nm : " + user_nm+",user_sosok : "
                + user_sosok+",user_pw : " + user_pw, Toast.LENGTH_LONG).show();*/
    }

    public void async_progress_dialog(String callback){

        ProgressDialog dialog = ProgressDialog.show(LoginActivity.this, "", "Loading...", true, false);
        dialog.setInverseBackgroundForced(false);
//        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                Log.d(TAG, "onDismissed() ");
//                aq.progress(dialog).ajaxCancel();
//            }
//        });

        HashMap<String, Object> map = new HashMap<>();
        if(callback.equals("loginCheck")){  //로그인
            loginButton.setEnabled(false);
            url= MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Login/loginCheckApp/sid="+_sabun_no.getText()+"/password="+_user_pw.getText();

        }else if(callback.equals("getUserInfo")){   //조회
            InputMethodManager imm= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(_sabun_no.getWindowToken(), 0);
            url= MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Login/sid_check_ajax/sid="+_sabun_no.getText();
        }

        aq.progress(dialog).ajax(url, JSONObject.class, this, callback);
    }

    public void loginCheck(String url, JSONObject object, AjaxStatus status) throws JSONException {
        Log.d(TAG,"object="+object);
        if( object != null && object.get("result").equals(1)) {
            try {
                onLoginSuccess();
                saveLoginData(object);

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),"에러코드  Login 1",Toast.LENGTH_SHORT).show();
            }
        }else{
            Log.d(TAG,"Data is Null");
            onLoginFailed2();
        }
    }

    public void getUserInfo(String url, JSONObject object, AjaxStatus status) throws JSONException {
        Log.d(TAG,"object="+object);
        if( object != null && object.get("status").equals(true)) {
            try {
                JSONObject jsonObject;
                jsonObject= (JSONObject) object.get("datas");
                _user_nm.setText(jsonObject.get("user_nm").toString());
                _user_sosok.setText(jsonObject.get("buseo_nm").toString());

            } catch ( Exception e ) {
                Toast.makeText(getApplicationContext(),"에러코드 Login 2",Toast.LENGTH_SHORT).show();
            }
        }else{
            Log.d(TAG,"Data is Null");
            _user_nm.setText("");
            _user_sosok.setText("");
            Toast.makeText(getApplicationContext(),"데이터가 없습니다.",Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.button3)  //사용자 정보 조회
    public void getUserInfo() {
        async_progress_dialog("getUserInfo");
    }

    //스플래시 표시하는 것과 초기화를 동시에 진행시키기 위하여 쓰레드 처리
    private void initialize() {
        InitializationRunnable init = new InitializationRunnable();
        new Thread(init).start();
    }

    //초기화 작업 처리
    class InitializationRunnable implements Runnable {
        public void run() {
            // 여기서부터 초기화 작업 처리

        }
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }
        //로그인 체크
        if(valid) async_progress_dialog("loginCheck");
    }

    @Override
    public void onBackPressed() {
        backPressCloseSystem.onBackPressed();
    }

    public void onLoginSuccess() {
        loginButton.setEnabled(true);

        Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
        intent.putExtra("title", "메인");
        intent.putExtra("mode", "login");
        startActivity(intent);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "접속에 실패 하였습니다.\n비밀번호를 확인해 주세요.", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public void onLoginFailed2() {
        Toast.makeText(getBaseContext(), "접속에 실패 하였습니다.\n서버 정보를 확인해 주세요.", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public boolean validate() {
        valid = true;
        String user_id = _user_nm.getText().toString();
        String password = _user_pw.getText().toString();

        if (user_id.isEmpty()) {
            _user_nm.setError("이름을 입력하세요.");
            valid = false;
        } else {
            _user_nm.setError(null);
        }

        if (password.isEmpty() || password.length() <= 3 || password.length() >= 16) {
            _user_pw.setError("비밀번호를 4자리이상 15자리이하로 입력하세요.");
            valid = false;
        } else {
            _user_pw.setError(null);
        }

        return valid;
    }

}
