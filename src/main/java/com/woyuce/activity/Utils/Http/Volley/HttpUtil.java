package com.woyuce.activity.Utils.Http.Volley;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;
import com.woyuce.activity.Utils.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by LeBang on 2017/2/16
 */
public class HttpUtil {

    public static void removeTag(String tag) {
        OkGo.getInstance().cancelTag(tag);
//        AppContext.getHttpQueue().cancelAll(tag);
    }

    /**
     * Get请求
     */
    public static void get(String url, String tag, final RequestInterface requestInterface) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        OkGo.get(url).tag(tag).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, okhttp3.Response response) {
                requestInterface.doSuccess(s);
            }
        });

//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                requestInterface.doSuccess(response);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//                //TODO 网络请求错误时可以在这里设置全局回调,其他几个方法同理。
//                //然而实践证明，其实请求失败时一直转圈就好，最终还是取消了这里的事件。
//                //如果要加事件，建议在这里做Toast，告诉用户发生了什么
//            }
//        });
//        stringRequest.setTag(tag);
//        AppContext.getHttpQueue().add(stringRequest);
    }

    /**
     * Get请求
     */
    public static void get(String url, String tag, final HashMap<String, String> headers, final RequestInterface requestInterface) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        HttpHeaders httpHeaders = transferHttpHeaders(headers);
        OkGo.get(url).tag(tag).headers(httpHeaders).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, okhttp3.Response response) {
                requestInterface.doSuccess(s);
            }
        });

//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                requestInterface.doSuccess(response);
//            }
//        }, null) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                return headers;
//            }
//        };
//        stringRequest.setTag(tag);
//        AppContext.getHttpQueue().add(stringRequest);
    }


    /**
     * Post请求
     */
    public static void post(String url, final HashMap<String, String> headers, final HashMap<String, String> params, String tag, final RequestInterface requestInterface) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        HttpHeaders httpHeaders = transferHttpHeaders(headers);
        HttpParams httpParams = transferHttpParams(params);
        OkGo.post(url).tag(tag).headers(httpHeaders).params(httpParams).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, okhttp3.Response response) {
                requestInterface.doSuccess(s);
            }
        });
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                requestInterface.doSuccess(response);
//            }
//        }, null) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                return headers;
//            }
//
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                return params;
//            }
//        };
//        stringRequest.setTag(tag);
//        //TODO 默认请求失败后会重复请求,此处设置为不重复请求
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        AppContext.getHttpQueue().add(stringRequest);
    }

    /**
     * Post请求
     */
    public static void post(String url, final HashMap<String, String> param, String tag, final RequestInterface requestInterface) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        HttpParams httpParams = transferHttpParams(param);
        OkGo.post(url).tag(tag).params(httpParams).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, okhttp3.Response response) {
                requestInterface.doSuccess(s);
            }
        });

//        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                requestInterface.doSuccess(response);
//            }
//        }, null) {
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                return param;
//            }
//        };
//        stringRequest.setTag(tag);
//        //TODO 默认请求失败后会重复请求,此处设置为不重复请求
//        stringRequest.setRetryPolicy(new DefaultRetryPolicy(1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        AppContext.getHttpQueue().add(stringRequest);
    }

    /**
     * POST请求
     */
    /**
     * Get请求
     */
    public static void post(String url, String tag, final RequestInterface requestInterface) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        OkGo.post(url).tag(tag).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, okhttp3.Response response) {
                requestInterface.doSuccess(s);
            }
        });
    }

    /**
     * 转换HashMap成 httpHeaders,为OkGo服务
     */
    @NonNull
    private static HttpHeaders transferHttpHeaders(HashMap<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            Iterator iter_header = headers.entrySet().iterator();
            while (iter_header.hasNext()) {
                Map.Entry entry = (Map.Entry) iter_header.next();
                httpHeaders.put(entry.getKey().toString(), entry.getValue().toString());
                LogUtil.w(entry.getKey().toString() + "--------------" + entry.getValue().toString());
            }
        }
        return httpHeaders;
    }

    /**
     * 转换HashMap成 HttpParams,为OkGo服务
     */
    @NonNull
    private static HttpParams transferHttpParams(HashMap<String, String> params) {
        HttpParams httpParams = new HttpParams();
        //TODO 如果有请求头，就在请求头中加入 user-Agent
//        httpParams.put(HttpHeaders.HEAD_KEY_USER_AGENT, System.getProperty("http.agent") + "; woyuce/" + localVersion);
        if (params != null) {
            Iterator iter_param = params.entrySet().iterator();
            while (iter_param.hasNext()) {
                Map.Entry entry = (Map.Entry) iter_param.next();
                httpParams.put(entry.getKey().toString(), entry.getValue().toString());
                LogUtil.w(entry.getKey().toString() + "--------------" + entry.getValue().toString());
            }
        }
        return httpParams;
    }
}