package com.creative.fines.app.menu;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.creative.fines.app.R;
import com.creative.fines.app.adaptor.WorkAdapter;
import com.creative.fines.app.safe.PeerLoveFragment;
import com.creative.fines.app.safe.PenaltyFragment;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class JungMenuFragment extends Fragment {
    private static final String TAG = "JungMenuFragment";

    private ArrayList<HashMap<String, Object>> workMenuArray;
    private WorkAdapter mAdapter;
    @Bind(R.id.listView1) ListView listView;
    @Bind(R.id.top_title) TextView textTitle;
    private String title;

    @Override
    public void onStart() {
        super.onStart();
    }//onStart

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_work, container, false);
        ButterKnife.bind(this, view);

        title = getArguments().getString("title");
        textTitle.setText(title);
        view.findViewById(R.id.top_home).setVisibility(View.VISIBLE);
        getMenuInfo();

        listView.setOnItemClickListener(new ListViewItemClickListener());

        return view;
    }

    public void onFragment(android.app.Fragment fragment, Bundle bundle, String title){
        FragmentManager fm = getFragmentManager();
        android.app.FragmentTransaction fragmentTransaction = fm.beginTransaction();

        fragmentTransaction.replace(R.id.fragmentReplace, fragment);
        fragmentTransaction.addToBackStack(title);

        fragment.setArguments(bundle);
        fragmentTransaction.commit();
    }

    public void getMenuInfo() {
        workMenuArray = new ArrayList<HashMap<String, Object>>();
        workMenuArray.clear();
        String[] titleList=null;
        if(title.equals("안전")){
            titleList = new String[]{"패널티카드", "동료사랑카드", "작업기준", "위험기계사용점검"};
        }else{

        }
        String[] codeList= new String[titleList.length];
        for (int i = 0; i < titleList.length; i++) {
            codeList[i] = "000"+(i+1);
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("title", titleList[i]);
            hashMap.put("code", codeList[i]);
            workMenuArray.add(hashMap);
        }
        mAdapter = new WorkAdapter(getActivity(), workMenuArray);
        listView.setAdapter(mAdapter);
    }

    @OnClick(R.id.top_home)
    public void goHome() {
        Intent intent = new Intent(getActivity(), MainFragment.class);
        startActivity(intent);
    }

    //ListView의 item을 클릭했을 때.
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(title.equals("안전")){
                if(position==0){
                    Fragment fragment = new PenaltyFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("title","패널티카드");
                    onFragment(fragment, bundle, "패널티카드");
                }else if(position==1){
                    Fragment fragment = new PeerLoveFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("title","동료사랑카드");
                    onFragment(fragment, bundle, "동료사랑카드");
                }else{

                }

            }else if(title.equals("다른거")){
                Log.d(TAG, "dfdfdf");
            }else{

            }


        }
    }

}
