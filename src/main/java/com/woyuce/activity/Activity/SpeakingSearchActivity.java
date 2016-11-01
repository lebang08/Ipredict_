package com.woyuce.activity.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.woyuce.activity.Adapter.SpeakingSearchAdapter;
import com.woyuce.activity.Application.AppContext;
import com.woyuce.activity.Bean.SpeakingSearch;
import com.woyuce.activity.R;
import com.woyuce.activity.Utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/22.
 */
public class SpeakingSearchActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {

    //顶部导航条
    private ImageView mImgBack;
    private LinearLayout llback;
    private TextView mTxtToStastis, mTxtToShare;

    private ListView lvSearch;
    private TextView txtNull;

    private String localsearch;
    private String URL_SEARCH = "http://iphone.ipredicting.com/kysubSearch.aspx";
    private List<SpeakingSearch> searchList = new ArrayList<>();

    @Override
    protected void onStop() {
        super.onStop();
        AppContext.getHttpQueue().cancelAll("search");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speakingsearch);

        initView();
        getJson();
    }

    private void initView() {
        //顶部导航条
        mImgBack = (ImageView) findViewById(R.id.img_back);
        llback = (LinearLayout) findViewById(R.id.ll_speaking_share);
        mTxtToStastis = (TextView) findViewById(R.id.txt_speaking_stastis);
        mTxtToShare = (TextView) findViewById(R.id.txt_speaking_share);
        mTxtToStastis.setTextColor(Color.parseColor("#2299cc"));
        mTxtToStastis.setBackgroundResource(R.drawable.buttonstyle_bluestroke);
        mTxtToShare.setTextColor(Color.parseColor("#ffffff"));
        mTxtToShare.setBackgroundResource(R.drawable.buttonstyle_whitestroke);

        //读取数据
        Intent it_search = getIntent();
        localsearch = it_search.getStringExtra("localsearch");

        txtNull = (TextView) findViewById(R.id.txt_search_null);
        lvSearch = (ListView) findViewById(R.id.listview_search);

        mImgBack.setOnClickListener(this);
        llback.setOnClickListener(this);
        lvSearch.setOnItemClickListener(this);
    }

    public void getJson() {
        StringRequest strinRequest = new StringRequest(Request.Method.POST, URL_SEARCH, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                SpeakingSearch search;
                try {
                    jsonObject = new JSONObject(response);
                    int result = jsonObject.getInt("code");
                    if (result == 0) {
                        JSONArray data = jsonObject.getJSONArray("data");
                        if (data.length() == 0) {
                            txtNull.setText("没有找到您需要的内容呢，亲...");
                        }
                        for (int i = 0; i < data.length(); i++) {
                            jsonObject = data.getJSONObject(i);
                            search = new SpeakingSearch();
                            search.subCategoryid = jsonObject.getString("subCategoryid");
                            search.subname = jsonObject.getString("subname");
                            searchList.add(search);
                        }
                    } else {
                        LogUtil.e("code!=0 Data-BACK", "读取页面失败： " + jsonObject.getString("message"));
                        txtNull.setText("您没有输入搜索内容哦，亲!");
                    }
                    // 第二步，将数据放到适配器中
                    SpeakingSearchAdapter adapter = new SpeakingSearchAdapter(SpeakingSearchActivity.this, searchList);
                    lvSearch.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtil.e("Wrong_BACK", "联接错误原因： " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> hashMap = new HashMap<>();
                hashMap.put("key", localsearch);
                return hashMap;
            }
        };
        strinRequest.setTag("search");
        AppContext.getHttpQueue().add(strinRequest);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SpeakingSearch search = searchList.get(position);
        String localsubCategoryid = search.subCategoryid;
        Intent it_subContent = new Intent(this, SpeakingContentActivity.class);
        it_subContent.putExtra("localsubCategoryid", localsubCategoryid);
        startActivity(it_subContent);
    }

    public void back(View view) {
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_speaking_share:
                Intent it_speaking = new Intent(this, SpeakingActivity.class);
                startActivity(it_speaking);
                overridePendingTransition(0, 0);
                break;
            case R.id.img_back:
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }
}