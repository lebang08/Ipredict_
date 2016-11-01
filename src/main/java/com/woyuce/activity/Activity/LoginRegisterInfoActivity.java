package com.woyuce.activity.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.woyuce.activity.Application.AppContext;
import com.woyuce.activity.R;
import com.woyuce.activity.Utils.LogUtil;
import com.woyuce.activity.Utils.PreferenceUtil;
import com.woyuce.activity.Utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/22.
 */
public class LoginRegisterInfoActivity extends BaseActivity implements View.OnClickListener {

    private EditText edtNickname, edtPassword, edtRepassword, edtUsername, edtEmail, edtTime, edtCity, edtInvitenum;
    private TextView txtback;
    private Button btnfinish, btnCheckUsername, btnCheckEmail;

    private String localtoken, localPhonenum, localtimer;
    private String URL = "http://api.iyuce.com/v1/account/register";
    private String URL_VAILD = "http://api.iyuce.com/v1/account/valid";


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(LoginRegisterInfoActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppContext.getHttpQueue().cancelAll("registerinfo");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginregisterinfo);

        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        localPhonenum = intent.getStringExtra("localPhonenum");

        txtback = (TextView) findViewById(R.id.txt_registerinfo_back);
        edtNickname = (EditText) findViewById(R.id.edt_registerinfo_nickname);
        edtPassword = (EditText) findViewById(R.id.edt_registerinfo_password);
        edtRepassword = (EditText) findViewById(R.id.edt_registerinfo_repassword);
        edtUsername = (EditText) findViewById(R.id.edt_registerinfo_username);
        edtEmail = (EditText) findViewById(R.id.edt_registerinfo_email);
        edtCity = (EditText) findViewById(R.id.edt_registerinfo_city);
        edtTime = (EditText) findViewById(R.id.edt_registerinfo_time);
        edtInvitenum = (EditText) findViewById(R.id.edt_registerinfo_invitednum);
        btnfinish = (Button) findViewById(R.id.btn_registerinfo_tonext);
        btnCheckUsername = (Button) findViewById(R.id.btn_registerinfo_checkusername);
        btnCheckEmail = (Button) findViewById(R.id.btn_registerinfo_checkemail);

        txtback.setOnClickListener(this);
        edtTime.setOnClickListener(this);
        btnfinish.setOnClickListener(this);
        btnCheckUsername.setOnClickListener(this);
        // btnCheckEmail.setOnClickListener(this);
        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                RequestVaild("email", s.toString());
            }
        });
    }

    private void RequestToNext() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject;
                try {
                    String parseString = new String(response.getBytes("ISO-8859-1"), "utf-8");
                    jsonObject = new JSONObject(parseString);
                    int result = jsonObject.getInt("code");
                    // 成功则Toast，并返回Login界面
                    if (result == 0) {
                        ToastUtil.showMessage(LoginRegisterInfoActivity.this, "恭喜您,注册成功!");
                        Intent intent = new Intent(LoginRegisterInfoActivity.this, LoginActivity.class);
                        intent.putExtra("username_register", edtUsername.getText().toString());
                        intent.putExtra("password_register", edtPassword.getText().toString());
                        intent.putExtra("timer_register", localtimer);
                        startActivity(intent);
                        finish();
                    } else {
                        ToastUtil.showMessage(LoginRegisterInfoActivity.this, "用户名/邮箱/电话已注册，请更换");
                    }
                } catch (JSONException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtil.showMessage(LoginRegisterInfoActivity.this, "网络错误，请稍候重试");
                LogUtil.e("Wrong-Back", "连接错误原因： " + error.getMessage() + "//" + error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                localtoken = PreferenceUtil.getSharePre(LoginRegisterInfoActivity.this).getString("localtoken", "");
                headers.put("Authorization", "Bearer " + localtoken);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("mobile", localPhonenum);
                map.put("username", edtUsername.getText().toString().trim());
                map.put("nickname", edtNickname.getText().toString().trim());
                map.put("password", edtPassword.getText().toString().trim());
                map.put("name", "");
                map.put("email", edtEmail.getText().toString().trim());
                map.put("avatar", "");
                map.put("sex", "");
                map.put("province", "");
                map.put("city", "");
                map.put("aliwangwang", "");
                map.put("qq", "");
                map.put("passwordanswer", "");
                map.put("passwordquestion", "");
                map.put("examtime", "");
                map.put("invite", "");
                LogUtil.e("mobile = " + localPhonenum + "," + edtUsername.getText().toString().trim() + ","
                        + edtNickname.getText().toString().trim() + "," + edtPassword.getText().toString().trim() + ","
                        + edtEmail.getText().toString().trim());

                return map;
            }
        };
        stringRequest.setTag("registerinfo");
        AppContext.getHttpQueue().add(stringRequest);
    }

    /**
     * 检查邮箱是否可用
     */
    private void RequestVaild(final String key, final String value) {
        StringRequest VaildRequest = new StringRequest(Request.Method.POST, URL_VAILD, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LogUtil.e("response2 = " + response);
                JSONObject obj;
                try {
                    obj = new JSONObject(response);
                    int result = obj.getInt("data");
                    if (result == 0) {
                        if (TextUtils.equals(key, "username")) {
                            btnCheckUsername.setText("可以使用");
                        }
                        ToastUtil.showMessage(LoginRegisterInfoActivity.this, obj.getString("message"));
                    } else {
                        if (TextUtils.equals(key, "username")) {
                            btnCheckUsername.setText("不可使用");
                        }
                        ToastUtil.showMessage(LoginRegisterInfoActivity.this, obj.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                LogUtil.e("Wrong-Back", "连接错误原因： " + error.getMessage());
                ToastUtil.showMessage(LoginRegisterInfoActivity.this, "发送失败，请稍候再试");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                localtoken = PreferenceUtil.getSharePre(LoginRegisterInfoActivity.this).getString("localtoken", "");
                headers.put("Authorization", "Bearer " + localtoken);
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("key", key);
                map.put("value", value);
                LogUtil.e("key = " + key + ",,value = " + value);
                return map;
            }
        };
        VaildRequest.setTag("registerinfo");
        AppContext.getHttpQueue().add(VaildRequest);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_registerinfo_back:
                startActivity(new Intent(LoginRegisterInfoActivity.this, LoginActivity.class));
                finish();
                break;
            case R.id.edt_registerinfo_time:
                new DatePickerDialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog, DateSet, 2018, 07, 8).show();
                break;
            case R.id.btn_registerinfo_checkusername:
                String localusername = edtUsername.getText().toString().trim();
                if (TextUtils.isEmpty(localusername)) {
                    ToastUtil.showMessage(LoginRegisterInfoActivity.this, "请输入用户名");
                    break;
                }
                RequestVaild("username", localusername);
                break;
            case R.id.btn_registerinfo_checkemail:
                String localemail = edtEmail.getText().toString().trim();
                if (TextUtils.isEmpty(localemail)) {
                    ToastUtil.showMessage(LoginRegisterInfoActivity.this, "请输入邮箱");
                    break;
                }
                RequestVaild("email", localemail);
                break;
            case R.id.btn_registerinfo_tonext:
                LogUtil.e("alledt = " + edtNickname.getText() + edtUsername.getText() + edtPassword.getText()
                        + edtRepassword.getText() + edtEmail.getText() + localPhonenum + localtoken);
                // 判断是否填入内容
                if (TextUtils.isEmpty(edtNickname.getText().toString().trim())
                        || TextUtils.isEmpty(edtUsername.getText().toString().trim())
                        || TextUtils.isEmpty(edtPassword.getText().toString().trim())
                        || TextUtils.isEmpty(edtRepassword.getText().toString().trim())
                        || TextUtils.isEmpty(edtEmail.getText().toString().trim())) {
                    ToastUtil.showMessage(LoginRegisterInfoActivity.this, "请检查信息是否完整");
                    return;
                }
                // 判断密码一致
                if (!edtPassword.getText().toString().equals(edtRepassword.getText().toString())) {
                    ToastUtil.showMessage(LoginRegisterInfoActivity.this, "您设置的密码不一致");
                    return;
                }
                // 注册
                RequestToNext();
                break;
        }
    }

    // 时间选择器
    DatePickerDialog.OnDateSetListener DateSet = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            edtTime.setText(
                    new StringBuffer().append(year).append("-").append(monthOfYear + 1).append("-").append(dayOfMonth));
            // 将此处的时间设置进数据
            PreferenceUtil.save(LoginRegisterInfoActivity.this, "mtimer", year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
            localtimer = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
        }
    };
}