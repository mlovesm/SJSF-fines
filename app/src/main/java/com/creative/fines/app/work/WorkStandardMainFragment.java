package com.creative.fines.app.work;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.creative.fines.app.R;
import com.creative.fines.app.adaptor.BoardAdapter;
import com.creative.fines.app.fragment.WebFragment;
import com.creative.fines.app.menu.MainFragment;
import com.creative.fines.app.util.UtilClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WorkStandardMainFragment extends Fragment {
    private static final String TAG = "WorkStandardMainFragment";
    private String url;
    private String manual_kind;

    private ArrayList<HashMap<String,Object>> boardArray;
    private BoardAdapter mAdapter;
    @Bind(R.id.listView1) ListView listView;
    @Bind(R.id.top_title) TextView textTitle;

    @Bind(R.id.search_top) LinearLayout layout;
    @Bind(R.id.search_spi) Spinner search_spi;
    @Bind(R.id.et_search) EditText et_search;
    String search_column;	//검색 컬럼

    private AQuery aq = new AQuery(getActivity());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_basic_list, container, false);
        ButterKnife.bind(this, view);

        view.findViewById(R.id.top_search).setVisibility(View.VISIBLE);
        layout.setVisibility(View.GONE);

        textTitle.setText(getArguments().getString("title"));
        manual_kind = getArguments().getString("code");

        async_progress_dialog("getBoardInfo");

        listView.setOnItemClickListener(new ListViewItemClickListener());
        // Spinner 생성
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.manual_list, android.R.layout.simple_spinner_dropdown_item);
//		search_spi.setPrompt("구분을 선택하세요.");
        search_spi.setAdapter(adapter);

        search_spi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				et_search.setText("position : " + position + parent.getItemAtPosition(position));
                et_search.setText("");
                et_search.setEnabled(true);
                if(position==0){
                    search_column="file_nm";
                }else if(position==1){
                    search_column="title";
                }else{

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }//onCreateView

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "Loading...", true, false);
        dialog.setInverseBackgroundForced(false);

        if(search_column!=null){
            url= MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Safe/workStandardList/"+manual_kind+"/search="+search_column+"/keyword="+et_search.getText();
        }else{
            url= MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Safe/workStandardList/"+manual_kind;
        }

        aq.progress(dialog).ajax(url, JSONObject.class, this, callback);
    }

    public void getBoardInfo(String url, JSONObject object, AjaxStatus status) throws JSONException {
        boardArray = new ArrayList<>();
        if(!object.get("count").equals(0)) {
            try {
                boardArray.clear();
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("idx",object.getJSONArray("datas").getJSONObject(i).get("manual_key").toString());
                    hashMap.put("file_nm",object.getJSONArray("datas").getJSONObject(i).get("file_nm").toString());
                    hashMap.put("data1",object.getJSONArray("datas").getJSONObject(i).get("manual_title").toString());
                    hashMap.put("data2",object.getJSONArray("datas").getJSONObject(i).get("manual_time").toString());
                    hashMap.put("data3",object.getJSONArray("datas").getJSONObject(i).get("file_nm").toString());
                    hashMap.put("data4","");
                    boardArray.add(hashMap);
                }
            } catch ( Exception e ) {
                Toast.makeText(getActivity(), "에러코드 Work 1", Toast.LENGTH_SHORT).show();
            }
        }else{
            Log.d(TAG,"Data is Null");
            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
        mAdapter = new BoardAdapter(getActivity(), boardArray, "WorkStan");
        listView.setAdapter(mAdapter);
    }

    @OnClick(R.id.top_search)
    public void getSearch() {
        if(layout.getVisibility()==View.GONE){
            layout.setVisibility(View.VISIBLE);
            layout.setFocusable(true);
        }else{
            layout.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

    //해당 검색값 데이터 조회
    @OnClick(R.id.button1)
    public void onSearchColumn() {
        //검색하면 키보드 내리기
        InputMethodManager imm= (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);

        if(et_search.getText().toString().length()==0){
            Toast.makeText(getActivity(), "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
        }else{
            async_progress_dialog("getBoardInfo");
        }
    }

    //ListView의 item을 클릭했을 때.
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Fragment frag;
            Bundle bundle = new Bundle();

            FragmentManager fm = getFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WebFragment());
            bundle.putString("title","작업기준상세");
            String file_nm= boardArray.get(position).get("file_nm").toString();
            bundle.putString("url", "http://docs.google.com/gview?embedded=true&url="+MainFragment.ipAddress+"/pdffile/"+file_nm);

            frag.setArguments(bundle);
            fragmentTransaction.addToBackStack("작업기준상세");
            fragmentTransaction.commit();
        }
    }

}
