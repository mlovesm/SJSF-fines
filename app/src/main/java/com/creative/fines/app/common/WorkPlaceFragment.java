package com.creative.fines.app.common;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.creative.fines.app.R;
import com.creative.fines.app.adaptor.BoardAdapter;
import com.creative.fines.app.menu.MainFragment;
import com.creative.fines.app.util.UtilClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WorkPlaceFragment extends Fragment {
    private static final String TAG = "WorkPlaceFragment";
    private String url = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Common/workPlaceList";

    private ArrayList<HashMap<String,Object>> arrayList;
    private BoardAdapter mAdapter;
    @Bind(R.id.listView1) ListView listView;
    @Bind(R.id.top_title) TextView textTitle;

    private boolean lastItemVisibleFlag = false;        //화면에 리스트의 마지막 아이템이 보여지는지 체크
    private int startRow=1;

    private AQuery aq = new AQuery(getActivity());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.basic_list, container, false);
        ButterKnife.bind(this, view);

        textTitle.setText(getArguments().getString("title"));
        view.findViewById(R.id.top_write).setVisibility(View.VISIBLE);

        async_progress_dialog("getBoardInfo");

        listView.setOnItemClickListener(new ListViewItemClickListener());
//        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                //현재 화면에 보이는 첫번째 리스트 아이템의 번호(firstVisibleItem)
//                //+ 현재 화면에 보이는 리스트 아이템의 갯수(visibleItemCount)가 리스트 전체의 갯수(totalItemCount) -1 보다 크거나 같을때
//                lastItemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
//            }
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                //OnScrollListener.SCROLL_STATE_IDLE은 스크롤이 이동하다가 멈추었을때 발생되는 스크롤 상태입니다.
//                //즉 스크롤이 바닦에 닿아 멈춘 상태에 처리를 하겠다는 뜻
//                if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastItemVisibleFlag) {
//                    //TODO 화면이 바닦에 닿을때 처리
//                    startRow++;
//                    UtilClass.logD(TAG,"바닥임, startRow="+startRow);
//                    async_progress_dialog("addBoardInfo");
//                }else{
//
//                }
//            }
//
//        });

        return view;
    }//onCreateView

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "Loading...", true, false);
        dialog.setInverseBackgroundForced(false);

        aq.progress(dialog).ajax(url, JSONObject.class, this, callback);
    }

    public void getBoardInfo(String url, JSONObject object, AjaxStatus status) throws JSONException {

        if(!object.get("count").equals(0)) {
            try {
                arrayList = new ArrayList<>();
                arrayList.clear();
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("key",object.getJSONArray("datas").getJSONObject(i).get("work_key").toString());
                    hashMap.put("data1",object.getJSONArray("datas").getJSONObject(i).get("work_date").toString());
                    hashMap.put("data2",object.getJSONArray("datas").getJSONObject(i).get("worker_nm").toString().trim());
                    hashMap.put("data3",object.getJSONArray("datas").getJSONObject(i).get("work_loc").toString());
                    hashMap.put("data4",object.getJSONArray("datas").getJSONObject(i).get("work_order").toString());
                    arrayList.add(hashMap);
                }

                mAdapter = new BoardAdapter(getActivity(), arrayList, "WorkPlace");
                listView.setAdapter(mAdapter);
            } catch ( Exception e ) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "에러코드 Work 1", Toast.LENGTH_SHORT).show();
            }
        }else{
            Log.d(TAG,"Data is Null");
            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void addBoardInfo(String url, JSONObject object, AjaxStatus status) throws JSONException {
//        Log.d(TAG,url+",콜백 상태");

        if(!object.get("count").equals(0)) {
            try {
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("key",object.getJSONArray("datas").getJSONObject(i).get("work_key").toString());
                    hashMap.put("data1",object.getJSONArray("datas").getJSONObject(i).get("work_date").toString());
                    hashMap.put("data2",object.getJSONArray("datas").getJSONObject(i).get("worker_nm").toString().trim());
                    hashMap.put("data3",object.getJSONArray("datas").getJSONObject(i).get("work_loc").toString());
                    hashMap.put("data4",object.getJSONArray("datas").getJSONObject(i).get("work_order").toString());
                    arrayList.add(hashMap);
                }
                mAdapter.setArrayList(arrayList);
                mAdapter.notifyDataSetChanged();
            } catch ( Exception e ) {
                Toast.makeText(getActivity(), "에러코드 Work 2", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getActivity(), "마지막 데이터 입니다.", Toast.LENGTH_SHORT).show();
            startRow--;
        }
    }

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

    @OnClick(R.id.top_write)
    public void getWriteBoard() {
        Fragment frag = new WorkPlaceWriteFragment();
        Bundle bundle = new Bundle();

        bundle.putString("mode","insert");
        frag.setArguments(bundle);

        FragmentManager fm = getFragmentManager();
        android.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentReplace, frag);
        fragmentTransaction.addToBackStack("작업개소현황작성");
        fragmentTransaction.commit();
    }

    //ListView의 item (상세)
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Fragment frag = null;
            Bundle bundle = new Bundle();

            FragmentManager fm = getFragmentManager();
            android.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new WorkPlaceWriteFragment());
            bundle.putString("title","작업개소현황상세");
            String key= arrayList.get(position).get("key").toString();
            bundle.putString("work_key", key);
            bundle.putString("mode", "update");

            frag.setArguments(bundle);
            fragmentTransaction.addToBackStack("작업개소현황상세");
            fragmentTransaction.commit();
        }
    }

}
