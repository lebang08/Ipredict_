package com.woyuce.activity.Controller.Store;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.woyuce.activity.BaseActivity;
import com.woyuce.activity.Common.Constants;
import com.woyuce.activity.Controller.Main.MainActivity;
import com.woyuce.activity.Model.Store.StorePayResult;
import com.woyuce.activity.R;
import com.woyuce.activity.Utils.DbUtil;
import com.woyuce.activity.Utils.Http.Volley.HttpUtil;
import com.woyuce.activity.Utils.Http.Volley.RequestInterface;
import com.woyuce.activity.Utils.LogUtil;
import com.woyuce.activity.Utils.PreferenceUtil;
import com.woyuce.activity.Utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/11/21
 */
public class StoreOrderActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout mLlayoutAliPay, mLlayoutWxPay;
    private Button mBtnPay;
    private EditText mEdtOrder, mEdtMoney;
    private TextView mTxtGoods;

    //生成订单
//    private String URL_TO_ORDER = "http://api.iyuce.com/v1/store/order";
//    private String URL_TO_PAY = "http://api.iyuce.com/v1/store/pay";
//    private String URL_TO_CASH_PAY = "http://api.iyuce.com/v1/store/paywithcash?paytype=alipay&id=";
//    private String URL_TO_WXPAY = "http://api.iyuce.com/v1/store/paywithcash?paytype=wxapp&id=";
//    private String URL_TO_VALID = "http://api.iyuce.com/v1/store/validpaybyapp?paytype=alipay";

    private String total_price, local_address_id, local_goods_name, local_skuids, local_store_user_money;
    private String local_user_id, local_order_no, local_order_id, local_alipay_data;

    private static final int SDK_PAY_FLAG = 2;

    private IWXAPI api;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SDK_PAY_FLAG:
                    StorePayResult result = new StorePayResult((Map<String, String>) msg.obj);
                    String resultInfo = result.getResult();// 同步返回需要验证的信息
                    String resultStatus = result.getResultStatus();
                    String resultMemo = result.getMemo();

                    LogUtil.i("resultStatus = " + resultStatus + ",resultMemo = " + resultMemo + ",resultInfo = " + resultInfo);
                    if (TextUtils.equals(resultStatus, "9000")) {
                        // 请求真实结果,该笔订单是否真实支付成功，需要依赖服务端的异步通知。
                        validRequest(resultInfo);
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        new AlertDialog.Builder(StoreOrderActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                .setTitle("支付宝支付结果")
                                .setMessage("支付失败")
                                .setPositiveButton("知道了", null)
                                .show();
                    }
                    break;
            }
        }
    };

    public void back(View view) {
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        HttpUtil.removeTag(Constants.ACTIVITY_STORE_ORDER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_order);

        //先进行微信注册
        api = WXAPIFactory.createWXAPI(this, "wxee1be723a57f9d21");
        initView();
    }

    private void initView() {
        local_user_id = PreferenceUtil.getSharePre(StoreOrderActivity.this).getString("userId", "");

        //从订单列表或者从StorePay界面传过来的
        total_price = getIntent().getStringExtra("total_price");
        local_address_id = getIntent().getStringExtra("address");
        local_skuids = getIntent().getStringExtra("skuids");
        local_store_user_money = getIntent().getStringExtra("discount");
        local_goods_name = getIntent().getStringExtra("goods_name");
        //从订单列表Activity传过来的订单号，不为空则不用去请求生成订单
        local_order_id = getIntent().getStringExtra("local_order_id");
        local_order_no = getIntent().getStringExtra("local_order_no");

        mLlayoutAliPay = (LinearLayout) findViewById(R.id.ll_activity_storeorder_payAli);
        mLlayoutWxPay = (LinearLayout) findViewById(R.id.ll_activity_storeorder_payWx);
        mBtnPay = (Button) findViewById(R.id.btn_activity_storeorder_pay);
        mLlayoutAliPay.setOnClickListener(this);
        mLlayoutWxPay.setOnClickListener(this);
        mBtnPay.setOnClickListener(this);
        mEdtOrder = (EditText) findViewById(R.id.edt_activity_storeorder_order);
        mEdtMoney = (EditText) findViewById(R.id.edt_activity_storeorder_money);
        mTxtGoods = (TextView) findViewById(R.id.txt_activity_storeorder_goods);

        mEdtMoney.setText(total_price);
        mTxtGoods.setText(local_goods_name.substring(1, local_goods_name.length() - 1).replace(",", ",\n"));

        if (TextUtils.isEmpty(local_order_no)) {
            //生成订单请求
            requestOrder();
        } else {
            mEdtOrder.setText(local_order_no);
        }
    }

    /**
     * 生成订单
     */
    private void requestOrder() {
        HashMap<String, String> params = new HashMap<>();
        params.put("discount", local_store_user_money);
        params.put("address", local_address_id);
        params.put("userid", local_user_id);
        params.put("skuids", local_skuids);
        HttpUtil.post(Constants.URL_POST_STORE_ORDER_MAKE_ORDER, params, Constants.ACTIVITY_STORE_ORDER, new RequestInterface() {
            @Override
            public void doSuccess(String result) {
                LogUtil.i("requestOrder = " + result);
                try {
                    JSONObject obj;
                    obj = new JSONObject(result);
                    if (obj.getString("code").equals("0")) {
                        obj = obj.getJSONObject("data");
                        mEdtOrder.setText(obj.getString("order_no"));
                        local_order_no = obj.getString("order_no");
                        local_order_id = obj.getString("id");
                        mEdtMoney.setText(obj.getString("actual_price"));
                        total_price = obj.getString("actual_price");
                    } else {
                        LogUtil.i("生成订单错误" + obj.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 支付请求第一步，金币支付
     */
    public void toPay(final String method, final String url) {
        progressdialogshow(this);
        HttpUtil.post(Constants.URL_POST_STORE_ORDER_TO_PAY + "?id=" + local_order_id + "&userid=" + local_user_id
                , Constants.ACTIVITY_STORE_ORDER, new RequestInterface() {
                    @Override
                    public void doSuccess(String result) {
                        try {
                            JSONObject obj;
                            obj = new JSONObject(result);
                            if (obj.getString("code").equals("0")) {
                                //金币支付成功,结束
                                progressdialogcancel();
                                new AlertDialog.Builder(StoreOrderActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                        .setTitle("支付结果")
                                        .setMessage("支付成功")
                                        .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                startActivity(new Intent(StoreOrderActivity.this, MainActivity.class));
                                                StoreOrderActivity.this.finish();
                                                deleteSql();
                                            }
                                        })
                                        .show();
                            } else if (obj.getString("code").equals("2")) {
                                //金币支付不成功,跳转现金支付
//                                ToastUtil.showMessage(StoreOrderActivity.this, "金币不足抵扣,去调支付宝或者微信");
                                //现金支付请求
                                LogUtil.i("cashrequest url = " + url + local_order_id);
                                cashRequest(method, url + local_order_id);
                            } else {
                                progressdialogcancel();
                                ToastUtil.showMessage(StoreOrderActivity.this, "支付失败");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 支付请求第二步，现金支付宝支付
     */
    private void cashRequest(final String method, String url) {
        HashMap<String, String> params = new HashMap<>();
        params.put("paytype", method);
        params.put("id", local_order_id);
        HttpUtil.post(url, params, Constants.ACTIVITY_STORE_ORDER, new RequestInterface() {
            @Override
            public void doSuccess(String result) {
                LogUtil.i("cashRequest = " + result);
                try {
                    JSONObject obj;
                    obj = new JSONObject(result);
                    progressdialogcancel();
                    if (obj.getString("code").equals("0")) {
                        //**************如果是阿里支付****************
                        if (method.equals("alipay")) {
//                            ToastUtil.showMessage(StoreOrderActivity.this, "去调支付宝吧 = ");
                            obj = obj.getJSONObject("data");
                            local_alipay_data = obj.getString("paydata");
                            //必须异步调用支付宝
                            Runnable payRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    PayTask alipay = new PayTask(StoreOrderActivity.this);
                                    Map<String, String> result = alipay.payV2(local_alipay_data, true);

                                    Message msg = new Message();
                                    msg.what = SDK_PAY_FLAG;
                                    msg.obj = result;
                                    mHandler.sendMessage(msg);
                                }
                            };
                            // 必须异步调用
                            Thread payThread = new Thread(payRunnable);
                            payThread.start();
                        }
                        //************如果是微信支付****************
                        if (method.equals("wxapp")) {
                            obj = obj.getJSONObject("data");
                            String for_wx_validate = obj.getString("orderno");
                            LogUtil.i("for_wx_validate = " + for_wx_validate);
                            //将订单号保存给微信回调做判断
                            PreferenceUtil.save(StoreOrderActivity.this, "for_wx_validate", for_wx_validate);
                            obj = obj.getJSONObject("paydata");
                            PayReq req = new PayReq();
                            req.appId = obj.getString("appid");
                            req.partnerId = obj.getString("partnerid");
                            req.prepayId = obj.getString("prepayid");
                            req.nonceStr = obj.getString("noncestr");
                            req.timeStamp = obj.getString("timestamp");
                            req.packageValue = obj.getString("package");
                            req.sign = obj.getString("sign");
                            // 调起请求之前先将该app注册到微信
                            boolean b = api.sendReq(req);
//                            ToastUtil.showMessage(StoreOrderActivity.this, "去调微信支付吧 = " + b);
                        }
                    } else {
                        ToastUtil.showMessage(StoreOrderActivity.this, "支付唤起失败");
                        LogUtil.i(result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 校验支付宝回调结果
     */
    private void validRequest(final String pay_result) {
        HashMap<String, String> params = new HashMap<>();
        params.put("", pay_result);
        HttpUtil.post(Constants.URL_POST_STORE_ORDER_TO_ALI_VALID, params, Constants.ACTIVITY_STORE_ORDER, new RequestInterface() {
            @Override
            public void doSuccess(String result) {
                LogUtil.i("validRequest = " + result);
                try {
                    JSONObject obj;
                    obj = new JSONObject(result);
                    if (obj.getString("code").equals("0")) {
                        //删除数据库中该表
                        deleteSql();
                        //界面提示处理
                        new AlertDialog.Builder(StoreOrderActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                .setTitle("支付宝支付结果")
                                .setMessage("支付成功")
                                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(StoreOrderActivity.this, MainActivity.class));
                                        StoreOrderActivity.this.finish();
                                    }
                                })
                                .show();
                        LogUtil.i("message" + obj.getString("message"));
                    } else {
                        // 该笔订单真实的支付结果，需要依赖服务端的异步通知。
                        new AlertDialog.Builder(StoreOrderActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                .setTitle("支付宝支付结果")
                                .setMessage("支付失败")
                                .setPositiveButton("知道了", null)
                                .show();
                        LogUtil.i("code !=0" + obj.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 删除数据库中的这张表
     */
    private void deleteSql() {
        SQLiteDatabase mDatabase = DbUtil.getHelper(this, Constants.DATABASE_IYUCE).getWritableDatabase();
        mDatabase.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_CART);
        mDatabase.close();
    }

    //定义用户所选的微信或者支付宝支付
    private String final_method, final_url;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_activity_storeorder_payAli:
                final_url = Constants.URL_POST_STORE_ORDER_TO_ALI_PAY;
                final_method = "alipay";
                LogUtil.i(final_url + final_method);
                mLlayoutAliPay.setBackgroundColor(Color.parseColor("#cccccc"));
                mLlayoutWxPay.setBackgroundColor(Color.parseColor("#ffffff"));
                break;
            case R.id.ll_activity_storeorder_payWx:
                final_url = Constants.URL_POST_STORE_ORDER_TO_WXPAY_PAY;
                final_method = "wxapp";
                LogUtil.i(final_url + final_method);
                mLlayoutWxPay.setBackgroundColor(Color.parseColor("#cccccc"));
                mLlayoutAliPay.setBackgroundColor(Color.parseColor("#ffffff"));
                break;
            case R.id.btn_activity_storeorder_pay:
                if (TextUtils.isEmpty(final_method)) {
                    //判断可否纯金币支付
                    if (Float.parseFloat(total_price) == 0) {
                        toPay("", "");
                        return;
                    }
                    LogUtil.i("total_price = " + total_price);
                    ToastUtil.showMessage(this, "请先选择支付方式哦");
                    return;
                }
                toPay(final_method, final_url);
                break;
        }
    }
}