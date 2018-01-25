package com.creative.fines.app.inno;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class IdeaScribFragment extends Fragment {
    private static final String TAG = "IdeaScribFragment";
    private String url = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Inno/ideaScribList";

    private ArrayList<HashMap<String,Object>> arrayList;
    private BoardAdapter mAdapter;
    @Bind(R.id.listView1) ListView listView;
    @Bind(R.id.top_title) TextView textTitle;

    private AQuery aq = new AQuery(getActivity());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.basic_list, container, false);
        ButterKnife.bind(this, view);

        textTitle.setText(getArguments().getString("title"));
        view.findViewById(R.id.top_write).setVisibility(View.VISIBLE);

        async_progress_dialog("getBoardInfo");

        listView.setOnItemClickListener(new ListViewItemClickListener());

        return view;
    }//onCreateView

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "Loading...", true, false);
        dialog.setInverseBackgroundForced(false);

        aq.progress(dialog).ajax(url, JSONObject.class, this, callback);
    }

    public void getBoardInfo(String url, JSONObject object, AjaxStatus status) throws JSONException {
//        UtilClass.logD(TAG, "object= "+object);

        if(!object.get("count").equals(0)) {
            try {
                arrayList = new ArrayList<>();
                arrayList.clear();
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("key",object.getJSONArray("datas").getJSONObject(i).get("idea_key").toString());
                    hashMap.put("data1",object.getJSONArray("datas").getJSONObject(i).get("input_date").toString());
                    hashMap.put("data2",object.getJSONArray("datas").getJSONObject(i).get("input_nm").toString().trim());
                    hashMap.put("data3",object.getJSONArray("datas").getJSONObject(i).get("idea_title").toString());
                    hashMap.put("data4",object.getJSONArray("datas").getJSONObject(i).get("idea_etc").toString());
                    arrayList.add(hashMap);
                }

                mAdapter = new BoardAdapter(getActivity(), arrayList, "Idea");
                listView.setAdapter(mAdapter);
            } catch ( Exception e ) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "에러코드 Idea 1", Toast.LENGTH_SHORT).show();
            }
        }else{
            Log.d(TAG,"Data is Null");
            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

    @OnClick(R.id.top_write)
    public void getWriteBoard() {
        Fragment frag = new IdeaScribWriteFragment();
        Bundle bundle = new Bundle();

        bundle.putString("mode","insert");
        Log.d(TAG,"bundle="+bundle);
        frag.setArguments(bundle);

        FragmentManager fm = getFragmentManager();
        android.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentReplace, frag);
        fragmentTransaction.addToBackStack("아이디어낙서방작성");
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
            fragmentTransaction.replace(R.id.fragmentReplace, frag = new IdeaScribWriteFragment());
            bundle.putString("title","아이디어낙서방상세");
            String key= arrayList.get(position).get("key").toString();
            bundle.putString("idea_key", key);
            bundle.putString("mode", "update");

            frag.setArguments(bundle);
            fragmentTransaction.addToBackStack("아이디어낙서방상세");
            fragmentTransaction.commit();
        }
    }

}
