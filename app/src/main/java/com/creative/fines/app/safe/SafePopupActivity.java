package com.creative.fines.app.safe;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.creative.fines.app.R;
import com.creative.fines.app.fragment.FragMenuActivity;
import com.creative.fines.app.menu.MainFragment;
import com.creative.fines.app.util.KeyValueArrayAdapter;
import com.creative.fines.app.util.UtilClass;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SafePopupActivity extends Activity {
    private static final String TAG = "SafePopupActivity";
//    private String url = "http://192.168.0.20:9191/RestAPI/rest/API/sampleList";
    private String l_url = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Safe/safe_lCategoryList";
    private String m_url = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Safe/safe_mCategoryList/large_cd=";

    private String[] daeClassKeyList;
    private String[] daeClassValueList;
    private String[] jungClassKeyList;
    private String[] jungClassValueList;
    String selectDaeClassKey="";
    String selectJungClassKey="";

    @Bind(R.id.spinner1) Spinner spn_daeClass;
    @Bind(R.id.spinner2) Spinner spn_jungClass;

    private AQuery aq = new AQuery( this );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.safe_popup);
        ButterKnife.bind(this);
        this.setFinishOnTouchOutside(false);

        getDaeClassData();

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

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(SafePopupActivity.this, "", "Loading...", true);

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
                                jungClassKeyList[i]= object.getJSONArray("datas").getJSONObject(i).get("mid_cd").toString();
                                jungClassValueList[i]= object.getJSONArray("datas").getJSONObject(i).get("mid_nm").toString();
                            }

                            KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(SafePopupActivity.this, android.R.layout.simple_spinner_dropdown_item);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            adapter.setEntries(jungClassValueList);
                            adapter.setEntryValues(jungClassKeyList);

                            spn_jungClass.setPrompt("중분류");
                            spn_jungClass.setAdapter(adapter);
                        } catch ( Exception e ) {

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
                            daeClassKeyList[i]= object.getJSONArray("datas").getJSONObject(i).get("large_cd").toString();
                            daeClassValueList[i]= object.getJSONArray("datas").getJSONObject(i).get("large_nm").toString();
                        }

                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(SafePopupActivity.this, android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntries(daeClassValueList);
                        adapter.setEntryValues(daeClassKeyList);

                        spn_daeClass.setPrompt("대분류");
                        spn_daeClass.setAdapter(adapter);
                    } catch ( Exception e ) {
                        Toast.makeText(getApplicationContext(),"에러코드 Safe 1", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    UtilClass.logD(TAG,"Data is Null");
                    Toast.makeText(getApplicationContext(),"데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        } );
    }


    @OnClick(R.id.textView1)
    public void closePopup() {
        finish();
    }

    @OnClick(R.id.textView2)   //이력
    public void getHistory() {
        Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("title", "위험기계사용점검이력팝업");
        intent.putExtra("selectDaeClassKey", selectDaeClassKey);
        intent.putExtra("selectJungClassKey", selectJungClassKey);
        startActivity(intent);
    }

    @OnClick(R.id.textView3)   //작성
    public void nextDataInfo() {
        Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("title", "위험기계사용점검작성");
        intent.putExtra("selectDaeClassKey", selectDaeClassKey);
        intent.putExtra("selectJungClassKey", selectJungClassKey);
        startActivity(intent);
        finish();
    }
}
