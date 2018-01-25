package com.creative.fines.app.gear;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.creative.fines.app.R;
import com.creative.fines.app.fragment.FragMenuActivity;
import com.creative.fines.app.menu.MainFragment;
import com.creative.fines.app.util.KeyValueArrayAdapter;
import com.creative.fines.app.util.SettingPreference;
import com.creative.fines.app.util.UtilClass;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GearPopupActivity extends Activity {
    private static final String TAG = "GearPopupActivity";
    private String l_url = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Gear/gearGubunList";
    private String m_url = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Gear/gearInfoList/gear_cd=";

    private String[] daeClassKeyList;
    private String[] daeClassValueList;
    private String[] jungClassKeyList;
    private String[] jungClassValueList;
    String selectDaeClassKey="";
    String selectJungClassKey="";

    @Bind(R.id.spinner1) Spinner spn_daeClass;
    @Bind(R.id.spinner2) Spinner spn_jungClass;
    @Bind(R.id.textView1) TextView _user_name;

    private SettingPreference pref = new SettingPreference("loginData",this);
    private AQuery aq = new AQuery( this );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gear_popup);
        ButterKnife.bind(this);
        this.setFinishOnTouchOutside(false);

        getDaeClassData();
        loadLoginData();

        spn_daeClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectDaeClassKey= adapter.getEntryValue(position);
                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));
                async_progress_dialog("getJungClass");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spn_jungClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectJungClassKey= adapter.getEntryValue(position);
                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }//onCreate

    private void loadLoginData() {
        String sabun_no= pref.getValue("sabun_no","");
        String user_nm= pref.getValue("user_nm","");
        String user_sosok= pref.getValue("user_sosok","");
        String user_pw= pref.getValue("user_pw","");

        _user_name.setText(user_nm);

    }

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(GearPopupActivity.this, "", "Loading...", true);

        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setInverseBackgroundForced(false);
        dialog.setCanceledOnTouchOutside(true);

         if(callback.equals("getJungClass")){
            aq.ajax( m_url+selectDaeClassKey, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status ) {
                    if( object != null) {
                        try {
                            jungClassKeyList= new String[object.getJSONArray("datas").length()];
                            jungClassValueList= new String[object.getJSONArray("datas").length()];
                            for(int i=0; i<object.getJSONArray("datas").length();i++){
                                jungClassKeyList[i]= object.getJSONArray("datas").getJSONObject(i).get("gear_cd").toString();
                                jungClassValueList[i]= object.getJSONArray("datas").getJSONObject(i).get("gear_cd").toString()
                                        +object.getJSONArray("datas").getJSONObject(i).get("gear_nm").toString().trim();
                            }

                            KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(GearPopupActivity.this, android.R.layout.simple_spinner_dropdown_item);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            adapter.setEntries(jungClassValueList);
                            adapter.setEntryValues(jungClassKeyList);

                            spn_jungClass.setPrompt("장비번호");
                            spn_jungClass.setAdapter(adapter);
                        } catch ( Exception e ) {
                            Toast.makeText(getApplicationContext(),"에러코드 Gear 2", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        UtilClass.logD(TAG,"Data is Null");
                        Toast.makeText(getApplicationContext(),"데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            } );

        }
        aq.progress(dialog).ajax(m_url, JSONObject.class, this, callback);
    }

    public void getDaeClassData() {
        aq.ajax( l_url, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status ) {
                if( object != null) {
                    try {
                        daeClassKeyList= new String[object.getJSONArray("datas").length()];
                        daeClassValueList= new String[object.getJSONArray("datas").length()];
                        for(int i=0; i<object.getJSONArray("datas").length();i++){
                            daeClassKeyList[i]= object.getJSONArray("datas").getJSONObject(i).get("gear_gcd").toString();
                            daeClassValueList[i]= object.getJSONArray("datas").getJSONObject(i).get("gear_gnm").toString();
                        }

                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(GearPopupActivity.this, android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntries(daeClassValueList);
                        adapter.setEntryValues(daeClassKeyList);

                        spn_daeClass.setPrompt("차종");
                        spn_daeClass.setAdapter(adapter);
                    } catch ( Exception e ) {
                        Toast.makeText(getApplicationContext(),"에러코드 Gear 1", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    UtilClass.logD(TAG,"Data is Null");
                    Toast.makeText(getApplicationContext(),"데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        } );
    }


    @OnClick(R.id.textView3)
    public void closePopup() {
        finish();
    }


    @OnClick(R.id.textView2)   //작성
    public void nextDataInfo() {
        Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("title", "장비점검");
        intent.putExtra("selectGearKey", selectJungClassKey);
        startActivity(intent);
        finish();
    }
}
