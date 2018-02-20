package com.creative.fines.app.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.fines.app.R;
import com.creative.fines.app.retrofit.Datas;
import com.creative.fines.app.retrofit.RetrofitService;
import com.creative.fines.app.util.SettingPreference;
import com.creative.fines.app.util.UtilClass;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PeoplesCardDialogActivity extends Activity {
    private static final String TAG = "PeoplesCardDialogActivity";
    private SettingPreference pref = new SettingPreference("loginData",this);
    private RetrofitService service;

    @Bind(R.id.textView1) TextView tv_data1;
    @Bind(R.id.textView2) TextView tv_data2;
    @Bind(R.id.textView3) TextView tv_data3;
    @Bind(R.id.textView4) TextView tv_data4;
    @Bind(R.id.editText1) EditText et_data1;
    @Bind(R.id.editText2) EditText et_data2;
    @Bind(R.id.editText3) EditText et_data3;
    @Bind(R.id.editText4) EditText et_data4;

    private String sabunNo;
    private String userPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.poeple_card);
        ButterKnife.bind(this);
        service= RetrofitService.rest_api.create(RetrofitService.class);

        try {
            sabunNo= getIntent().getStringExtra("sabun_no");
            userPwd= pref.getValue("user_pw", "");

            getPersonnelSabunData();

        }catch (Exception e){
            e.printStackTrace();
        }

    }//onCreate

    public void getPersonnelSabunData(){
        final ProgressDialog dialog = new ProgressDialog(PeoplesCardDialogActivity.this);
        UtilClass.showProcessingDialog(dialog);

        Call<Datas> call = service.listData("Personnel","personnelSabunData", sabunNo);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        if(status.equals("success")){
                            if(response.body().getCount()>0){
                                tv_data1.setText(response.body().getList().get(0).get("sabun_no"));
                                tv_data2.setText(response.body().getList().get(0).get("user_nm"));
                                tv_data3.setText(response.body().getList().get(0).get("dept_nm1")+response.body().getList().get(0).get("dept_nm2"));
                                tv_data4.setText(response.body().getList().get(0).get("j_pos"));
                                et_data1.setText(response.body().getList().get(0).get("user_cell"));
                                et_data2.setText(response.body().getList().get(0).get("user_email"));
                                et_data3.setText(response.body().getList().get(0).get("user_addr"));
                                et_data4.setText(userPwd);
                            }else{
                                tv_data1.setText("");
                                tv_data2.setText("");
                                tv_data3.setText("");
                                tv_data4.setText("");
                                et_data1.setText("");
                                et_data2.setText("");
                                et_data3.setText("");
                                et_data4.setText("");
                            }

                        }

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "에러코드 Personnel 1", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(dialog!=null) dialog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(dialog!=null) dialog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getApplicationContext(), "onFailure Personnel 1",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.textButton1)
    public void alertDialogSave(){
        if(MainFragment.loginSabun.equals(sabunNo)){
            alertDialog("S");
        }else{
            Toast.makeText(getApplicationContext(),"본인정보만 수정 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void alertDialog(final String gubun){
        final AlertDialog.Builder alertDlg = new AlertDialog.Builder(PeoplesCardDialogActivity.this);
        alertDlg.setTitle("알림");
        if(gubun.equals("S")){
            alertDlg.setMessage("수정하시겠습니까?");
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
                }else{

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

    //작성,수정
    public void postData() {
        String et_cell = et_data1.getText().toString();
        String et_email = et_data2.getText().toString();
        String et_addr = et_data3.getText().toString();
        String et_pwd = et_data4.getText().toString();

        if (tv_data1.getText().equals("") || tv_data1.length()==0) {
            Toast.makeText(getApplicationContext(), "인사정보가 없습니다.",Toast.LENGTH_LONG).show();
            return;
        }

        if (et_cell.equals("") || et_cell.length()==0) {
            Toast.makeText(getApplicationContext(), "빈칸을 채워주세요.",Toast.LENGTH_LONG).show();
            return;
        }
        if (et_pwd.equals("") || et_pwd.length()<4) {
            Toast.makeText(getApplicationContext(), "비밀번호를 4자리 이상 입력해주세요.",Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> map = new HashMap();
        map.put("sabunNo", sabunNo);
        map.put("user_cell",et_cell.trim());
        map.put("user_email",et_email.trim());
        map.put("user_addr",et_addr);
        map.put("user_pwd",et_pwd);

        final ProgressDialog pDlalog = new ProgressDialog(PeoplesCardDialogActivity.this);
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.updateData("Personnel","personnelSabunDataModify", map);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    try {
                        handleResponse(response);
                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "에러코드 Personnel 2", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getApplicationContext(), "onFailure Personnel 2",Toast.LENGTH_SHORT).show();
            }
        });

    }

    //작성 완료
    public void handleResponse(Response<Datas> response) {
        try {
            String status= response.body().getStatus();
            if(status.equals("success")){
                Toast.makeText(getApplicationContext(), "수정 하였습니다.", Toast.LENGTH_SHORT).show();
                pref.put("user_pw", et_data4.getText().toString());
                finish();
            }else{
                Toast.makeText(getApplicationContext(), "수정에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "작업에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    @OnClick(R.id.textButton2)
    public void doClose() {
        finish();
    }


}
