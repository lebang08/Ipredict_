package com.woyuce.activity;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;
import com.woyuce.activity.Controller.Login.LoginActivity;
import com.woyuce.activity.Utils.LogUtil;
import com.woyuce.activity.Utils.PreferenceUtil;

import java.util.Map;
import java.util.logging.Level;

/**
 * 全局应用程序类：用于保存和调用全局应用配置及访问网络数据
 *
 * @author LeBang
 * @created 2016-3-22
 */
public class AppContext extends Application {

    //    private static final String TAG = AppContext.class.getSimpleName();
    //    private static final String APP_CACAHE_DIRNAME = "/webcache";
    private static String DEVICE_TOKEN;

    //单例
    private static AppContext appContext = null;
    private Display display;

//    private static RequestQueue mQueue;

    public static AppContext getInstance() {
        return appContext;
    }

//    public static RequestQueue getHttpQueue() {
//        return mQueue;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        //现用网络框架volley并发队列
//        mQueue = Volley.newRequestQueue(getApplicationContext());

        //因Https等原因准备替用的网络框架OkGo
        initOkgo();

        //配置图片加载框架ImageLoader
        initImageLoader(this);

        //初始化友盟推送
        initUmengPush();

        //微博社交模块
        //initWeibo();
    }

    /**
     * 配置ImageLoader
     */
    public static void initImageLoader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY);
        config.denyCacheImageMultipleSizesInMemory();
        config.memoryCacheSize((int) Runtime.getRuntime().maxMemory() / 4);
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(100 * 1024 * 1024); // 100 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        //修改连接超时时间5秒，下载超时时间5秒
        config.imageDownloader(new BaseImageDownloader(appContext, 5 * 1000, 5 * 1000));
        //		config.writeDebugLogs(); // Remove for release app
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

    /**
     * OkGo，基于OkHttp,支持Https
     */
    private void initOkgo() {
        //---------这里给出的是示例代码,告诉你可以这么传,实际使用的时候,根据需要传,不需要就不传-------------//
//        HttpHeaders headers = new HttpHeaders();
//        headers.put("commonHeaderKey1", "commonHeaderValue1");    //header不支持中文
//        headers.put("commonHeaderKey2", "commonHeaderValue2");
//        HttpParams params = new HttpParams();
//        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
//        params.put("commonParamsKey2", "这里支持中文参数");
        //-----------------------------------------------------------------------------------//

        //必须调用初始化
        OkGo.init(this);

        //以下设置的所有参数是全局参数,同样的参数可以在请求的时候再设置一遍,那么对于该请求来讲,请求中的参数会覆盖全局参数
        //好处是全局参数统一,特定请求可以特别定制参数
        try {
            //以下都不是必须的，根据需要自行选择,一般来说只需要 debug,缓存相关,cookie相关的 就可以了
            OkGo.getInstance()

                    // 打开该调试开关,打印级别INFO,并不是异常,是为了显眼,不需要就不要加入该行
                    // 最后的true表示是否打印okgo的内部异常，一般打开方便调试错误
                    .debug("ling", Level.INFO, true)

                    //如果使用默认的 60秒,以下三行也不需要传
                    .setConnectTimeout(OkGo.DEFAULT_MILLISECONDS)  //全局的连接超时时间
                    .setReadTimeOut(OkGo.DEFAULT_MILLISECONDS)     //全局的读取超时时间
                    .setWriteTimeOut(OkGo.DEFAULT_MILLISECONDS)    //全局的写入超时时间

                    //可以全局统一设置缓存模式,默认是不使用缓存,可以不传,具体其他模式看 github 介绍 https://github.com/jeasonlzy/
                    .setCacheMode(CacheMode.NO_CACHE)

                    //可以全局统一设置缓存时间,默认永不过期,具体使用方法看 github 介绍
//                    .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)

                    //可以全局统一设置超时重连次数,默认为三次,那么最差的情况会请求4次(一次原始请求,三次重连请求),不需要可以设置为0
                    .setRetryCount(0);

            //如果不想让框架管理cookie（或者叫session的保持）,以下不需要
//              .setCookieStore(new MemoryCookieStore())            //cookie使用内存缓存（app退出后，cookie消失）
//                    .setCookieStore(new PersistentCookieStore())        //cookie持久化存储，如果cookie不过期，则一直有效

            //可以设置https的证书,以下几种方案根据需要自己设置
//                    .setCertificates()                                  //方法一：信任所有证书,不安全有风险
//              .setCertificates(new SafeTrustManager())            //方法二：自定义信任规则，校验服务端证书
//              .setCertificates(getAssets().open("srca.cer"))      //方法三：使用预埋证书，校验服务端证书（自签名证书）
//              //方法四：使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
//               .setCertificates(getAssets().open("xxx.bks"), "123456", getAssets().open("yyy.cer"))//

            //配置https的域名匹配规则，详细看demo的初始化介绍，不需要就不要加入，使用不当会导致https握手失败
//               .setHostnameVerifier(new SafeHostnameVerifier())

            //可以添加全局拦截器，不需要就不要加入，错误写法直接导致任何回调不执行
//                .addInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        return chain.proceed(chain.request());
//                    }
//                })
            //这两行同上，不需要就不要加入
//                    .addCommonHeaders(headers)  //设置全局公共头
//                    .addCommonParams(params);   //设置全局公共参数
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 友盟推送的全局初始化，这里包含了业务中的推送逻辑
     */
    private void initUmengPush() {
        PushAgent mPushAgent = PushAgent.getInstance(this);
        UmengMessageHandler messageHandler = new UmengMessageHandler() {
            /**
             * 自定义消息的回调方法(透传)，用户无感知
             */
            @Override
            public void dealWithCustomMessage(final Context context, final UMessage msg) {
                //自定义参数
                LogUtil.i("linx", "dealWithCustomMessage----msg:" + msg.custom);
                LogUtil.i("linx", "dealWithCustomMessage----extra:" + msg.extra);
                String sender = "";
                String userid = "";
                for (Map.Entry<String, String> entry : msg.extra.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (TextUtils.equals(key, "sender")) {
                        sender = value;
                    }
                    if (TextUtils.equals(key, "userid")) {
                        userid = value;
                    }
                }
                LogUtil.i("sender = " + sender + "|||userid = " + userid);
                if (!sender.equals(DEVICE_TOKEN) &&
                        userid.equals(PreferenceUtil.getSharePre(context).getString("userId", ""))) {
                    Intent startLogin = new Intent(context, LoginActivity.class);
                    startLogin.putExtra("local_push_code", sender);
                    startLogin.putExtra("local_push_message", msg.custom);
                    startLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(startLogin);
                }
//                boolean isClickOrDismissed = true;
//                if (isClickOrDismissed) {
//                    //自定义消息的点击统计
//                    UTrack.getInstance(getApplicationContext()).trackMsgClick(msg);
//                } else {
//                    //自定义消息的忽略统计
//                    UTrack.getInstance(getApplicationContext()).trackMsgDismissed(msg);
//                }
            }

            /**
             * 当消息推送到用户通知栏时回调
             */
            @Override
            public Notification getNotification(Context context, UMessage uMessage) {
                LogUtil.i("linx", "getNotification----custom:" + uMessage.custom);
//                if (uMessage.custom.equals(PreferenceUtil.getSharePre(context).getString("userId", ""))) {
//                    Intent startLogin = new Intent(context, LoginActivity.class);
//                    startLogin.putExtra("local_push_code", uMessage.custom);
//                    startLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    context.startActivity(startLogin);
//                } else if (uMessage.custom.equals("notification")) {
//                    ToastUtil.showMessage(context, "您收到了一条最新消息");
//                }
                return super.getNotification(context, uMessage);
            }
        };
        mPushAgent.setMessageHandler(messageHandler);

        /**
         * 当用户点击收到的通知时回调
         * UmengNotificationClickHandler是在BroadcastReceiver中被调用，故
         * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
         * */
        UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {
            @Override
            public void dealWithCustomAction(final Context context, UMessage msg) {
                //TODO 接收到推送后自定义打开的界面或者其他
//                ToastUtil.showMessage(context, "收到通知：" + msg.custom + "||" + msg.extra + ",并执行相应操作");
            }
        };
        //使用自定义的NotificationHandler，来结合友盟统计处理消息通知
        //参考http://bbs.umeng.com/thread-11112-1-1.html
        //CustomNotificationHandler notificationClickHandler = new CustomNotificationHandler();
        mPushAgent.setNotificationClickHandler(notificationClickHandler);

        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                //注册成功会返回device token
                LogUtil.i("Appcontext deviceToken = " + deviceToken);
                DEVICE_TOKEN = deviceToken;
            }

            @Override
            public void onFailure(String s, String s1) {

            }
        });
    }

    /**
     * 微博模块相关初始化
     */
    private void initWeibo() {
        //本地图片辅助类初始化(因为6.0后动态权限的原因，将初始化延迟放在MainActivity中做，否则应用无权限时，一打开就崩溃)
//        LocalImageHelper.init(this);

        initImageLoader(getApplicationContext());
        if (display == null) {
            WindowManager windowManager = (WindowManager)
                    getSystemService(Context.WINDOW_SERVICE);
            display = windowManager.getDefaultDisplay();
        }
    }

//    public String getCachePath() {
//        File cacheDir;
//        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
//            cacheDir = getExternalCacheDir();
//        else
//            cacheDir = getCacheDir();
//        if (!cacheDir.exists())
//            cacheDir.mkdirs();
//        return cacheDir.getAbsolutePath();
//    }

    /**
     * 提供全局获取DEVICE_TOKEN的方法
     */
    public static String getDeviceToken() {
        return DEVICE_TOKEN;
    }

    /**
     * @return
     * @Description： 获取当前屏幕的宽度
     */
    public int getWindowWidth() {
        return display.getWidth();
    }

    /**
     * @return
     * @Description： 获取当前屏幕的高度
     */
    public int getWindowHeight() {
        return display.getHeight();
    }

    /**
     * @return
     * @Description： 获取当前屏幕一半宽度
     */
    public int getHalfWidth() {
        return display.getWidth() / 2;
    }

    /**
     * @return
     * @Description： 获取当前屏幕1/4宽度
     */
    public int getQuarterWidth() {
        return display.getWidth() / 4;
    }
}