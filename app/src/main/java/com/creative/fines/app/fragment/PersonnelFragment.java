package com.creative.fines.app.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
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
import com.creative.fines.app.adaptor.PersonnelAdapter;
import com.creative.fines.app.menu.MainFragment;
import com.creative.fines.app.util.UtilClass;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PersonnelFragment extends Fragment {
    private static final String TAG = "PersonnelFragment";
    private String url;

    private ArrayList<HashMap<String, Object>> peopleListArray;
    private PersonnelAdapter mAdapter;
    @Bind(R.id.listView1) ListView listView;
    @Bind(R.id.top_title) TextView textTitle;

    @Bind(R.id.search_top) LinearLayout layout;
    @Bind(R.id.search_spi) Spinner search_spi;
    @Bind(R.id.et_search) EditText et_search;
    String search_column;	//검색 컬럼

    private boolean lastItemVisibleFlag = false;        //화면에 리스트의 마지막 아이템이 보여지는지 체크
    private int startRow=1;

    private PermissionListener permissionlistener;
    private AQuery aq = new AQuery(getActivity());

    @Override
    public void onStart() {
        super.onStart();
    }//onStart

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_basic_list, container, false);
        ButterKnife.bind(this, view);

        textTitle.setText(getArguments().getString("title"));
        view.findViewById(R.id.top_search).setVisibility(View.VISIBLE);

//        async_progress_dialog("getPersonnelList");

        listView.setOnItemClickListener(new ListViewItemClickListener());
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //현재 화면에 보이는 첫번째 리스트 아이템의 번호(firstVisibleItem)
                //+ 현재 화면에 보이는 리스트 아이템의 갯수(visibleItemCount)가 리스트 전체의 갯수(totalItemCount) -1 보다 크거나 같을때
                lastItemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
            }
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //OnScrollListener.SCROLL_STATE_IDLE은 스크롤이 이동하다가 멈추었을때 발생되는 스크롤 상태입니다.
                //즉 스크롤이 바닦에 닿아 멈춘 상태에 처리를 하겠다는 뜻
                if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag) {
                    //TODO 화면이 바닦에 닿을때 처리
                    startRow++;
                    UtilClass.logD(TAG,"바닥임, startRow="+startRow);
                    async_progress_dialog("addPersonnelList");
                }else{

                }
            }

        });

        // Spinner 생성
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.search_list, android.R.layout.simple_spinner_dropdown_item);
//		search_spi.setPrompt("구분을 선택하세요.");
        search_spi.setAdapter(adapter);

        search_spi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				et_search.setText("position : " + position + parent.getItemAtPosition(position));
                et_search.setText("");
                et_search.setEnabled(true);
                startRow=1;
                if(position==0){
                    search_column="user_nm";
                }else if(position==1){
                    search_column="user_no";
                }else if(position==2){
                    search_column="user_cell";
                }else{
                    search_column=null;
                    et_search.setEnabled(false);
                    async_progress_dialog("getPersonnelList");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "Loading...", true, false);
        dialog.setInverseBackgroundForced(false);

        if(search_column!=null){
            url= MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Personnel/PersonnelList/startRow="+startRow+"/search="+search_column+"/keyword="+et_search.getText();
        }else{
            url= MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Personnel/PersonnelList/startRow="+startRow;
        }
        aq.progress(dialog).ajax(url, JSONObject.class, this, callback);
    }

    public void getPersonnelList(String url, JSONObject object, AjaxStatus status) throws JSONException {
        peopleListArray = new ArrayList<>();
        if(!object.get("count").equals(0)) {
            try {
                peopleListArray.clear();
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("user_no",object.getJSONArray("datas").getJSONObject(i).get("user_no").toString().trim());
                    hashMap.put("user_nm",object.getJSONArray("datas").getJSONObject(i).get("user_nm").toString().trim());
                    hashMap.put("user_cell",object.getJSONArray("datas").getJSONObject(i).get("user_cell").toString());
                    hashMap.put("user_pic",object.getJSONArray("datas").getJSONObject(i).get("user_pic").toString());
                    hashMap.put("user_email",object.getJSONArray("datas").getJSONObject(i).get("user_email").toString());
                    peopleListArray.add(hashMap);
                }
            } catch ( Exception e ) {
                Toast.makeText(getActivity(), "에러코드 Personnel 1", Toast.LENGTH_SHORT).show();
            }
        }else{
            Log.d(TAG,"Data is Null");
            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
        mAdapter = new PersonnelAdapter(getActivity() , peopleListArray);
        listView.setAdapter(mAdapter);
    }

    public void addPersonnelList(String url, JSONObject object, AjaxStatus status) throws JSONException {
//        Log.d(TAG,url+",콜백 상태");

        if(!object.get("count").equals(0)) {
            try {
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("user_no",object.getJSONArray("datas").getJSONObject(i).get("user_no").toString().trim());
                    hashMap.put("user_nm",object.getJSONArray("datas").getJSONObject(i).get("user_nm").toString().trim());
                    hashMap.put("user_cell",object.getJSONArray("datas").getJSONObject(i).get("user_cell").toString());
                    hashMap.put("user_pic",object.getJSONArray("datas").getJSONObject(i).get("user_pic").toString());
                    hashMap.put("user_email",object.getJSONArray("datas").getJSONObject(i).get("user_email").toString());
                    peopleListArray.add(hashMap);
                }
                mAdapter.setArrayList(peopleListArray);
                mAdapter.notifyDataSetChanged();
            } catch ( Exception e ) {
                Toast.makeText(getActivity(), "에러코드 Personnel 2", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getActivity(), "마지막 데이터 입니다.", Toast.LENGTH_SHORT).show();
            startRow--;
        }
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
        startRow=1;

        if(et_search.getText().toString().length()==0){
            Toast.makeText(getActivity(), "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
        }else{
            async_progress_dialog("getPersonnelList");
        }

    }

    //ListView의 item을 클릭했을 때.
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            alertDialog(peopleListArray.get(position).get("user_cell").toString());
        }
    }

    public void alertDialog(final String phone_num){
        final AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
        alertDlg.setTitle("알림")
                .setCancelable(true);

        alertDlg.setPositiveButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int paramInt) {
                dialog.dismiss();
            }
        });
        alertDlg.setNegativeButton("통화", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int paramInt) {
                permissionlistener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        Toast.makeText(getActivity(), "권한 허가", Toast.LENGTH_SHORT).show();
                        if(phone_num!=null||phone_num!=""){
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone_num));
                            startActivity(intent);
                        }else{
                            Toast.makeText(getActivity(), "잘못된 전화번호입니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                        Toast.makeText(getActivity(), "권한 거부 목록\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();

                    }
                };
                new TedPermission(getActivity())
                        .setPermissionListener(permissionlistener)
                        //                .setRationaleMessage("전화번호 정보를 가져오기 위해선 권한이 필요합니다.")
                        .setDeniedMessage("권한을 확인하세요.\n\n [설정] > [애플리케이션] [해당앱] > [권한]")
                        .setGotoSettingButtonText("권한확인")
                        .setPermissions(Manifest.permission.CALL_PHONE)
                        .check();
            }
        });

//        alertDlg.setMessage("?");
        alertDlg.show();
    }

}
