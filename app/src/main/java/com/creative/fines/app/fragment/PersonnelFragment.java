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

import com.creative.fines.app.R;
import com.creative.fines.app.adaptor.PersonnelAdapter;
import com.creative.fines.app.retrofit.Datas;
import com.creative.fines.app.retrofit.RetrofitService;
import com.creative.fines.app.util.KeyValueArrayAdapter;
import com.creative.fines.app.util.UtilClass;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonnelFragment extends Fragment {
    private static final String TAG = "PersonnelFragment";

    private ArrayList<HashMap<String, String>> peopleListArray;
    private PersonnelAdapter mAdapter;
    @Bind(R.id.listView1) ListView listView;
    @Bind(R.id.top_title) TextView textTitle;

    @Bind(R.id.search_top) LinearLayout layout;
    @Bind(R.id.search_spi) Spinner search_spi;
    @Bind(R.id.spinner2) Spinner spinner2;
    private String[] spn2KeyList;
    private String[] spn2ValueList;
    String selectSpn2Key="";
    @Bind(R.id.et_search) EditText et_search;
    String search_column;	//검색 컬럼

    private boolean lastItemVisibleFlag = false;        //화면에 리스트의 마지막 아이템이 보여지는지 체크
    private int startRow=1;

    private PermissionListener permissionlistener;

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
                    getPersonnelList("addPersonnelList");
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
                    et_search.setVisibility(view.VISIBLE);
                    spinner2.setVisibility(view.GONE);
                }else if(position==1){
                    search_column="dept_cd";
                    et_search.setVisibility(view.GONE);
                    spinner2.setVisibility(view.VISIBLE);
                    getDepartData();
                }else if(position==2){
                    search_column="work";
                    et_search.setVisibility(view.VISIBLE);
                    spinner2.setVisibility(view.GONE);
                    et_search.setEnabled(false);

                    getPersonnelList("getPersonnelList");

                }else{
                    search_column="all";
                    et_search.setVisibility(view.VISIBLE);
                    spinner2.setVisibility(view.GONE);
                    et_search.setEnabled(false);

                    getPersonnelList("getPersonnelList");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectSpn2Key= adapter.getEntryValue(position);
                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));

                getPersonnelList("getPersonnelList");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    public void getPersonnelList(final String gubun){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        final ProgressDialog pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);
        String keyword;
        if(search_column.equals("dept_cd")){
            keyword= selectSpn2Key;
        }else if(search_column.equals("work")){
            keyword= "work";
        }else if(search_column.equals("all")){
            keyword= "all";
        }else{
            keyword= et_search.getText().toString();
        }
        Call<Datas> call = service.listData("Personnel","PersonnelList", "startRow="+startRow, "search="+search_column, "keyword="+keyword);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        if(gubun.equals("getPersonnelList")){
                            if(response.body().getCount()==0){
                                Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                            peopleListArray = new ArrayList<>();
                            peopleListArray.clear();
                            for(int i=0; i<response.body().getList().size();i++){
                                HashMap<String,String> hashMap = new HashMap<>();
                                hashMap.put("user_nm",response.body().getList().get(i).get("user_nm"));
                                hashMap.put("user_pic",response.body().getList().get(i).get("user_pic"));
                                hashMap.put("L1",response.body().getList().get(i).get("L1"));
                                hashMap.put("L2",response.body().getList().get(i).get("L2"));
                                hashMap.put("R1",response.body().getList().get(i).get("R1"));
                                hashMap.put("R2",response.body().getList().get(i).get("R2"));
                                hashMap.put("R3",response.body().getList().get(i).get("R3"));
                                hashMap.put("R4",response.body().getList().get(i).get("R4"));
                                hashMap.put("R5",response.body().getList().get(i).get("R5"));
                                peopleListArray.add(hashMap);
                            }

                            mAdapter = new PersonnelAdapter(getActivity(), peopleListArray);
                            listView.setAdapter(mAdapter);

                        }else{
                            for(int i=0; i<response.body().getList().size();i++){
                                HashMap<String,String> hashMap = new HashMap<>();
                                hashMap.put("user_nm",response.body().getList().get(i).get("user_nm"));
                                hashMap.put("user_pic",response.body().getList().get(i).get("user_pic"));
                                hashMap.put("L1",response.body().getList().get(i).get("L1"));
                                hashMap.put("L2",response.body().getList().get(i).get("L2"));
                                hashMap.put("R1",response.body().getList().get(i).get("R1"));
                                hashMap.put("R2",response.body().getList().get(i).get("R2"));
                                hashMap.put("R3",response.body().getList().get(i).get("R3"));
                                hashMap.put("R4",response.body().getList().get(i).get("R4"));
                                hashMap.put("R5",response.body().getList().get(i).get("R5"));
                                peopleListArray.add(hashMap);
                            }
                            mAdapter.setArrayList(peopleListArray);
                            mAdapter.notifyDataSetChanged();

                            if(response.body().getList().size()==0){
                                Toast.makeText(getActivity(), "마지막 데이터 입니다.", Toast.LENGTH_SHORT).show();
                                startRow--;
                            }
                        }

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 Personnel 1", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure Personnel",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void getDepartData(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        final ProgressDialog pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Login","dept1DataList");
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        if(response.body().getCount()==0){
                            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                        spn2KeyList= new String[response.body().getList().size()];
                        spn2ValueList= new String[response.body().getList().size()];

                        for(int i=0; i<response.body().getList().size();i++){
                            spn2KeyList[i]= response.body().getList().get(i).get("C001");
                            spn2ValueList[i]= response.body().getList().get(i).get("C002");
                        }

                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntries(spn2ValueList);
                        adapter.setEntryValues(spn2KeyList);

                        spinner2.setPrompt("선택");
                        spinner2.setAdapter(adapter);

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 Personnel 2", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure Board",Toast.LENGTH_SHORT).show();
            }
        });

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
            getPersonnelList("getPersonnelList");
        }

    }

    //ListView의 item을 클릭했을 때.
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            alertDialog(peopleListArray.get(position).get("R3").toString());

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
