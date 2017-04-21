package com.woyuce.activity.Controller.Free.Net;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.woyuce.activity.Adapter.Free.Net.NetClassCourseAdapter;
import com.woyuce.activity.BaseActivity;
import com.woyuce.activity.Common.Constants;
import com.woyuce.activity.Controller.Store.StoreGoodsActivity;
import com.woyuce.activity.Controller.WebNoCookieActivity;
import com.woyuce.activity.Model.Free.Net.NetBean;
import com.woyuce.activity.R;
import com.woyuce.activity.Utils.Http.Volley.HttpUtil;
import com.woyuce.activity.Utils.Http.Volley.RequestInterface;
import com.woyuce.activity.Utils.LogUtil;
import com.woyuce.activity.Utils.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/9/21
 */

public class NetClassActivity extends BaseActivity
        implements View.OnClickListener, AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {

    private ImageView imgback;
    private TextView txtcontent;
    private Button btn_zhibo, btn_lubo;
    private Spinner spntime, spnclass;
    private GridView gridview;
    private NetClassCourseAdapter webcourseAdapter;

    private ArrayAdapter<String> adapterdate, adapterclass;
    private String localdate, localclass;
    private List<String> dateList = new ArrayList<>();
    private List<String> classnameList = new ArrayList<>();
    private List<String> classidList = new ArrayList<>();

    private List<NetBean> wcgList = new ArrayList<>();
    private String localtoken;

    @Override
    protected void onStop() {
        super.onStop();
        HttpUtil.removeTag(Constants.ACTIVITY_NET);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_class);

        initView();
        getSpinnerJson();
        getWebCourse();
    }

    private void initView() {
        localtoken = PreferenceUtil.getSharePre(NetClassActivity.this).getString("localtoken", "");

        imgback = (ImageView) findViewById(R.id.arrow_back);
        txtcontent = (TextView) findViewById(R.id.txt_wangluoban_content);
        btn_zhibo = (Button) findViewById(R.id.btn_wangluoban_zhibo);
        btn_lubo = (Button) findViewById(R.id.btn_wangluoban_lubo);
        gridview = (GridView) findViewById(R.id.gridview_activity_wangluoban);
        spntime = (Spinner) findViewById(R.id.spn_wangluoban_time);
        spnclass = (Spinner) findViewById(R.id.spn_wangluoban_class);

        imgback.setOnClickListener(this);
        btn_zhibo.setOnClickListener(this);
        btn_lubo.setOnClickListener(this);
        gridview.setOnItemClickListener(this);
    }

    //获取notice的数据，初始传值为null
    private void getNoticeJson() {
        if (localdate == null || localclass == null) {
            return;
        }
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + localtoken);
        HashMap<String, String> params = new HashMap<>();
        params.put("exam_time", localdate);
        params.put("class_type", localclass);
        LogUtil.w(localclass + localdate);
        HttpUtil.post(Constants.URL_POST_NET_NOTICE, headers, params, Constants.ACTIVITY_NET, new RequestInterface() {
            @Override
            public void doSuccess(String result) {
                LogUtil.e("getNoticeJson = ");
                try {
                    JSONObject obj;
                    JSONArray arr;
                    obj = new JSONObject(result);
                    arr = obj.getJSONArray("data");
                    if (arr.length() >= 1) {
                        txtcontent.setText(arr.getString(0));
                    } else {
                        txtcontent.setText("还没有复习计划，敬请关注哦，亲!");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 获取spn的数据
    private void getSpinnerJson() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + localtoken);
        HttpUtil.post(Constants.URL_POST_NET_TIME, headers, null, Constants.ACTIVITY_NET, new RequestInterface() {
            @Override
            public void doSuccess(String result) {
                LogUtil.e("getSpinnerJson = ");
                try {
                    JSONArray arr;
                    JSONArray examtime;
                    JSONObject obj;
                    String examdate;
                    obj = new JSONObject(result);
                    if (obj.getInt("code") == 0) {
                        obj = obj.getJSONObject("data");
                        arr = obj.getJSONArray("class_type");
                        examtime = obj.getJSONArray("exam_time");
                        /* 遍历取网络班时间 */
                        for (int i = 0; i < examtime.length(); i++) {
                            examdate = examtime.getString(i);
                            dateList.add(examdate);
                        }
                        /* 遍历取网络班类型 */
                        for (int i = 0; i < arr.length(); i++) {
                            obj = arr.getJSONObject(i);
                            classnameList.add(obj.getString("wcg_name"));
                            classidList.add(obj.getString("wcg_id"));
                        }
                    }
                    //数据加载完后放入
                    setSpnDate();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 数据加载完后放入 ,spnner初始化
     */
    private void setSpnDate() {
        adapterdate = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dateList);
        adapterdate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spntime.setAdapter(adapterdate);
        spntime.setOnItemSelectedListener(this);

        adapterclass = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classnameList);
        adapterclass.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnclass.setAdapter(adapterclass);
        spnclass.setOnItemSelectedListener(this);
    }

    /**
     * 获取spn的数据
     */
    private void getWebCourse() {
        progressdialogshow(this);
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + localtoken);
        HttpUtil.post(Constants.URL_POST_NET_WEB_COURSE, headers, null, Constants.ACTIVITY_NET, new RequestInterface() {
            @Override
            public void doSuccess(String result) {
                LogUtil.e("getWebCourse = ");
                try {
                    JSONArray arr;
                    JSONObject obj;
                    obj = new JSONObject(result);
                    if (obj.getInt("code") == 0) {
                        arr = obj.getJSONArray("data");
                        NetBean wcgbean;
                        for (int i = 0; i < arr.length(); i++) {
                            wcgbean = new NetBean();
                            obj = arr.getJSONObject(i);
                            wcgbean.setWcg_name(obj.getString("wcg_name"));
                            wcgbean.setWcg_id(obj.getString("wcg_id"));
                            wcgbean.setWcg_powerid(obj.getString("wcg_powerid"));
                            wcgbean.setMonthId(obj.getString("month_id"));
                            wcgbean.setImgUrl(obj.getString("img_url"));
                            wcgList.add(wcgbean);
                        }
                    }
                    // gridview设置adapter
                    webcourseAdapter = new NetClassCourseAdapter(NetClassActivity.this, wcgList);
                    gridview.setAdapter(webcourseAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressdialogcancel();
            }
        });
    }

    /**
     * 获取商城商品信息
     */
    private void getactivegoods() {
        HttpUtil.get(Constants.URL_GetGoods, Constants.ACTIVITY_NET, new RequestInterface() {
            @Override
            public void doSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getString("code").equals("0")) {
                        obj = obj.getJSONObject("data");
                        Intent intent = new Intent(NetClassActivity.this, StoreGoodsActivity.class);
                        intent.putExtra("goods_id", obj.getString("goods_id"));
                        intent.putExtra("goods_sku_id", obj.getString("goods_sku_id"));
                        intent.putExtra("goods_title", obj.getString("goods_title"));
                        intent.putExtra("sales_price", obj.getString("sales_price"));
                        intent.putExtra("can_go_store_back", "yes");
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.arrow_back:
                finish();
                break;
            case R.id.btn_wangluoban_zhibo:
                Intent intent = new Intent(this, WebNoCookieActivity.class);
                intent.putExtra("URL", Constants.URL_WEB_ZHIBO);
                intent.putExtra("TITLE", "网络班直播报名");
                intent.putExtra("COLOR", "#1e87e2");
                startActivity(intent);
                break;
            case R.id.btn_wangluoban_lubo:
                //跳转商城商品
                getactivegoods();
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spn_wangluoban_time:
                localdate = dateList.get(position);
                getNoticeJson();
                break;
            case R.id.spn_wangluoban_class:
                localclass = classidList.get(position);
                getNoticeJson();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(NetClassActivity.this, NetClassLessonActivity.class);
        intent.putExtra("NetBean", wcgList.get(position));
        startActivity(intent);
    }
}