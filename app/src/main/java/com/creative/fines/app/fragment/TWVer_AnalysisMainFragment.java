package com.creative.fines.app.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.creative.fines.app.R;
import com.creative.fines.app.adaptor.BaseExpandableAdapter;
import com.creative.fines.app.menu.MainFragment;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TWVer_AnalysisMainFragment extends Fragment {

    private static final String TAG = "AnalysisMainActivity";
    private String url = "http://59.23.191.35/taewoonApp/API/mobile/an_safe_history.php";

    private String[] mGroupList = null;
    private String[][] mChildListContent = null;
    private String[][] mChildListUrl = null;
    private String menuUrl;

    @Bind(R.id.top_title)
    TextView textTitle;
    @Bind(R.id.listView1)
    ExpandableListView exListView;
    private WebView webView;
    private ProgressDialog dialog;

    public TWVer_AnalysisMainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.menu_analysis, container, false);
        ButterKnife.bind(this, view);

//        textTitle.setText(getActivity().getIntent().getStringExtra("title"));
        textTitle.setText(getArguments().getString("title"));
        view.findViewById(R.id.top_home).setVisibility(View.VISIBLE);

        getGroupMenuList();
        exListView.setAdapter(new BaseExpandableAdapter(getActivity(), mGroupList, mChildListContent));

        // 차일드 클릭 했을 경우 이벤트
        exListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                menuUrl = mChildListUrl[groupPosition][childPosition];
                //Intent intent = new Intent(getActivity(), AnalysisMenuActivity.class);
                //intent.putExtra("menuUrl", menuUrl);
                //intent.putExtra("title", mChildListContent[groupPosition][childPosition]);
                //startActivity(intent);

                return false;
            }
        });

        View headerView = getActivity().getLayoutInflater().inflate(R.layout.basic_view, null, false);
        exListView.addHeaderView(headerView);
        webView = (WebView) view.findViewById(R.id.webView1);

        final Context myApp = getActivity();
        //자바스크립트 Alert,confirm 사용
        webView.setWebChromeClient(new WebChromeClient() {
            ProgressBar pb = (ProgressBar)view.findViewById(R.id.progressBar1);

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
                new android.app.AlertDialog.Builder(myApp)
                        .setTitle("경고")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new android.app.AlertDialog.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                // TODO Auto-generated method stub
                //return super.onJsConfirm(view, url, message, result);
                new android.app.AlertDialog.Builder(view.getContext())
                        .setTitle("알림")
                        .setMessage(message)
                        .setPositiveButton("네",
                                new android.app.AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("아니오",
                                new android.app.AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.cancel();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }

            public void onProgressChanged(WebView webView, int paramInt) {
                this.pb.setProgress(paramInt);
                if (paramInt == 100)
                {
                    this.pb.setVisibility(View.GONE);
                    return;
                }
                this.pb.setVisibility(View.VISIBLE);
            }
        });//setWebChromeClient 재정의

        WebSettings wSetting = webView.getSettings();
        webView.setWebViewClient(new WebViewClient()); // 이걸 안해주면 새창이 뜸
        webView.setWebViewClient(new MyWebViewClient());
        wSetting.setJavaScriptEnabled(true);      // 웹뷰에서 자바 스크립트 사용
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.loadUrl(url);

        return view;
    }

    public void getGroupMenuList(){
        mGroupList = new String[4];
        mGroupList[0] = "운송관리";
        mGroupList[1] = "정비관리";
        mGroupList[2] = "자재관리";
        mGroupList[3] = "안전관리";

        mChildListContent = new String[4][];
        mChildListContent[0] = new String[]{"운송현황", "일상점검실적"};
        mChildListContent[1] = new String[]{"일일정비현황", "자가정비현황"};
        mChildListContent[2] = new String[]{"재고현황", "자재,정비비", "부족분 리스트"};
        mChildListContent[3] = new String[]{"안전점검현황"};

        mChildListUrl = new String[][] { { "an_trans_status", "an_trans_result" }, { "an_repair_day", "an_repair_self" },
                { "an_materi_status", "an_materi_cost", "an_materi_lack" }, { "an_safe_status" } };
    }

    @OnClick(R.id.top_home)
    public void goHome() {
        Intent intent = new Intent(getActivity(), MainFragment.class);
        startActivity(intent);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
//            Log.d("shouldOveride","웹뷰클릭 됨="+url);
            view.loadUrl(url);

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            dialog = new ProgressDialog(getActivity());
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Loading...");
            dialog.setProgress(0);
            dialog.setMax(100);
            dialog.setCancelable(false);
            dialog.show();
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            try {
                if (dialog.isShowing()) {
                    dialog.cancel();
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        }

        @Override
        public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.d("onReceivedError", "errorCode=" + errorCode);
            switch(errorCode) {
                case ERROR_AUTHENTICATION:              // 서버에서 사용자 인증 실패
                case ERROR_BAD_URL:                     // 잘못된 URL
                case ERROR_CONNECT:                     // 서버로 연결 실패
                case ERROR_FAILED_SSL_HANDSHAKE:     	// SSL handshake 수행 실패
                case ERROR_FILE:                        // 일반 파일 오류
                case ERROR_FILE_NOT_FOUND:              // 파일을 찾을 수 없습니다
                case ERROR_HOST_LOOKUP:            		// 서버 또는 프록시 호스트 이름 조회 실패
                case ERROR_IO:                          // 서버에서 읽거나 서버로 쓰기 실패
                case ERROR_PROXY_AUTHENTICATION:    	// 프록시에서 사용자 인증 실패
                case ERROR_REDIRECT_LOOP:               // 너무 많은 리디렉션
                case ERROR_TIMEOUT:                     // 연결 시간 초과
                case ERROR_TOO_MANY_REQUESTS:           // 페이지 로드중 너무 많은 요청 발생
                case ERROR_UNKNOWN:                     // 일반 오류
                case ERROR_UNSUPPORTED_AUTH_SCHEME:  	// 지원되지 않는 인증 체계
                case ERROR_UNSUPPORTED_SCHEME:			// URI가 지원되지 않는 방식

                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(view.getContext());
                    builder.setTitle("Error");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finish();
                        }
                    });
                    builder.setMessage("네트워크 상태가 원활하지 않습니다. 잠시 후 다시 시도해 주세요.");
                    builder.show();

                    break;
            }
        }
    }//MyWebViewClient
}
