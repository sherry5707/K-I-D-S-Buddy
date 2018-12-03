/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchat;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.kinstalk.her.qchat.activityhelper.VoiceAssistantActivityHelper;
import com.kinstalk.her.qchat.common.UIHelper;
import com.kinstalk.her.qchat.greendao.GreenDaoManager;
import com.kinstalk.her.qchat.messaging.BaiduPushUtils;
import com.kinstalk.her.qchat.messaging.PrivacyManager;
import com.kinstalk.her.qchat.messaging.XgPushHelper;
import com.kinstalk.her.qchat.service.InitService;
import com.kinstalk.her.qchat.utils.LogUtil;
import com.kinstalk.her.qchat.utils.SharedPreferencesUtils;

import com.kinstalk.her.qchat.view.NetworkPrivacyWarningAlertDialog;
import com.kinstalk.her.qchat.voiceresponse.VoiceResponseService;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.base.BaseApplication;
import com.kinstalk.her.qchatcomm.utils.NetworkUtils;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatcomm.utils.SharePreUtils;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatcomm.utils.SystemPropertiesProxy;
import com.kinstalk.her.qchatcomm.utils.SystemTool;
import com.kinstalk.her.qchatmodel.Manager.util.UIHelp;
import com.tencent.tac.TACApplication;
import com.tencent.tac.messaging.TACMessagingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kinstalk.com.countly.CountlyUtils;

import static com.kinstalk.her.qchat.view.NetworkPrivacyWarningAlertDialog.TYPE_WIFI;

/**
 * Created by Knight.Xu on 2018/4/6.
 */

public class QchatApplication extends BaseApplication {
    private static final String TAG = "AI-QchatApplication";
    private int mNetworkType = NetworkUtils.NETWORK_NONE;
    private static final int STOP_COUNTLY_SESSION = 10Privacy01;

    private Timer mAutoKillTimer;
    private static boolean bSleeping = false;
    private static boolean bPreSleeping = false;
    private static boolean bWakeuping = false;
    /* Add to control the warning dialog when CMCC Andlink app running. */
    private static boolean mAndlinkStarted = false;

    private static QchatApplication sInstance = null;

    public static QchatApplication getApplicationInstance() {
        return sInstance;
    }

    public List<Activity> activityList = new ArrayList<>();

    private static NetworkPrivacyWarningAlertDialog mWifiDialog, mPrivacyDialog;
    private static String WIFI_SETTING_STATUS_CHANGE_ACTION = "com.kinstalk.her.qchat.setting.status.change";
    private static String IOT_CONNECT_ACTION = "com.kinstalk.her.iot.action.netconfig";
    private static String ANDLINK_APP_STARTED_ACTION = "com.kinstalk.qloveandlinkapp.APPSTARTED";
    private static String ANDLINK_APP_EXITED_ACTION = "com.kinstalk.qloveandlinkapp.APPEXITED";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static void setWakeupActive(boolean active) {
        UIHelp.setWakeupActive(active);
        bWakeuping = active;
    }

    public static void setSleepActive(boolean active) {
        UIHelp.setSleepActive(active);
        bSleeping = active;
    }

    public static void setPreSleepActive(boolean active) {
        UIHelp.setPreSleepActive(active);
        bPreSleeping = active;
    }

    public static boolean isWakeupActive() {
        return bWakeuping;
    }

    public static boolean isSleepingActive() {
        return (bSleeping || bPreSleeping);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bSleeping = false;
        bWakeuping = false;

        sInstance = this;
        QAILog.setDebug(!isCompleteProject);
        QAILog.e(TAG, "isDebug: " + !isCompleteProject);
        if (shouldInit()) {

            mNetworkType = NetworkUtils.getNetworkState(this);
            QAIConfig.isDebugMode = !SystemTool.isUserType();

            SystemTool.getQLoveProductVersion(getApplicationContext());

            init();
            Api.setHomeworkAndTestUrl(this);
            CountlyUtils.initCountly(this, false, BuildConfig.IS_RELEASE);

            TACApplication.configure(this);
            TACMessagingService.getInstance().start(this);
            XgPushHelper.getInstance().onInitParas(this);
            // 启动百度push
            PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY,
                    BaiduPushUtils.getMetaValue(getApplicationContext(), "api_key"));
            PrivacyManager.getInstance(this);

            GreenDaoManager.getInstance();

            if (SharedPreferencesUtils.getSharedPreferencesWithBool(this, SharedPreferencesUtils.TTS_CONTENT, SharedPreferencesUtils.HAS_INSERT, false)) {

            } else {
                VoiceAssistantActivityHelper helper = VoiceAssistantActivityHelper.instance;
                helper.saveResourceToDB(this);
            }

            this.startService(new Intent(this, InitService.class));
            this.startService(new Intent(this, VoiceResponseService.class));

        }

    }

    public static boolean isCompleteProject = false;

    private void init() {
        SharePreUtils.init(getApplicationContext());
//        ConfigApiHelper.getInstance().startReqConfigTimer(getApplicationContext());
        registerConnectivityChangeReceiver();
        if (UIHelper.wchatBindStatus(getApplicationContext())) {
            QAIConfig.setBindStatus(true);
        }
    }

    private final Handler mHandle = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case STOP_COUNTLY_SESSION:
                    QAILog.d(TAG, "stop countly session");
//                    Countly.sharedInstance().onStop();
                    break;
            }
            return false;
        }
    });

    private void registerConnectivityChangeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mConnChangeReceiver, filter);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WIFI_SETTING_STATUS_CHANGE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(IOT_CONNECT_ACTION);
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        intentFilter.addAction(ANDLINK_APP_STARTED_ACTION);
        intentFilter.addAction(ANDLINK_APP_EXITED_ACTION);
        registerReceiver(mNetworkChangeReceiver,intentFilter);
    }

    private void unConnectivityChangeRegisterReceivers() {
        unregisterReceiver(mConnChangeReceiver);
        unregisterReceiver(mNetworkChangeReceiver);
    }

    private final BroadcastReceiver mConnChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            QAILog.d(TAG, "onReceive: Connectivity changed.");
            if (TextUtils.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {


                int connectionStatus = intent.getIntExtra("inetCondition", 0);
                QAILog.d(TAG, "connectionStatus =  " + connectionStatus);
                QAIConfig.isNetworkGood = connectionStatus > 50;

                mNetworkType = NetworkUtils.getNetworkState(QchatApplication.this);

                if (mNetworkType == NetworkUtils.NETWORK_NONE) {
                    QAILog.d(TAG, "Network disconnected.");
                } else {
                    QAILog.d(TAG, "Network connected.");
                    if (0 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0)) {
                        mAutoKillTimer = new Timer();
                        mAutoKillTimer.schedule(new MessageTimerTask(context), 8 * 1000, 8 * 1000);
                    }
                    if(1 == UIHelper.getPropertyForXGClear(context)) {
                        UIHelper.ClearPropertyForXGClear(context);

                        mAutoKillTimer = new Timer();
                        mAutoKillTimer.schedule(new XingeKillerTimerTask(context), 60 * 1000, 60 * 1000);
                    }
                    if (1 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0)) {
                        Api.fetchHomework(BaseApplication.mApplication.getApplicationContext(),
                                StringEncryption.generateToken(), QAIConfig.getMacForSn(), Api.UP_MSG_TYPE_HOMEWORK, "", "");
                    }
                }
            }
        }
    };

    private final BroadcastReceiver mNetworkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isSetupFinished(context)) {
                return;
            }
            LogUtil.d("mNetworkChangeReceiver", "onReceive: action:" + intent.getAction());
            if (WIFI_SETTING_STATUS_CHANGE_ACTION.equals(intent.getAction())
                    || "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())
                    || IOT_CONNECT_ACTION.equals(intent.getAction())) {
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    LogUtil.d(TAG, "Andlink app status: " + mAndlinkStarted);
                    if (!mAndlinkStarted && !isSettingActivityVisible(context) && !isIotActivityVisible(context)) {
                        getWarningDlg(context);
                        if (!mWifiDialog.isShowing()) {
                            mWifiDialog.show();
                        }
                    } else {
                        if (mWifiDialog != null) {
                            mWifiDialog.dismiss();
                            mWifiDialog = null;
                        }
                    }
                } else {
                    if (mWifiDialog != null) {
                        mWifiDialog.dismiss();
                        mWifiDialog = null;
                    }
                }
	} else if (("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) ||
			(ANDLINK_APP_EXITED_ACTION.equals(intent.getAction()))){
                if(ANDLINK_APP_EXITED_ACTION.equals(intent.getAction())){
                    LogUtil.d(TAG, "Andlink app exited!");
                    mAndlinkStarted = false;
                }

                if (isNetworkAvailable(intent, context) == 0 && !NetworkUtils.isNetworkAvailable(context)) {
                    LogUtil.d(TAG, "Andlink app status: " + mAndlinkStarted);
                    if(!mAndlinkStarted) {
                        getWarningDlg(context);
                        if (!mWifiDialog.isShowing() && !isSettingActivityVisible(context) && !isIotActivityVisible(context)) {
                            mWifiDialog.show();
                        } else if (isSettingActivityVisible(context) || isIotActivityVisible(context)) {
                            mWifiDialog.dismiss();
                            mWifiDialog = null;
                        }
                    }
                } else if (isNetworkAvailable(intent, context) == 1) {
                    LogUtil.d(TAG, "network available");
                    if (mWifiDialog != null) {
                        mWifiDialog.dismiss();
                        mWifiDialog = null;
                    }
                }
           } else if(ANDLINK_APP_STARTED_ACTION.equals(intent.getAction())){
                    LogUtil.d(TAG, "Andlink app started!");
                    mAndlinkStarted = true;
                    if (mWifiDialog != null) {
                        LogUtil.d(TAG, "Warning dialog is showing, dismiss it!");
                        mWifiDialog.dismiss();
                        mWifiDialog = null;
                    }
                    LogUtil.d(TAG, "Andlink app status: " + mAndlinkStarted);
              }
        }

        private void getWarningDlg(Context context) {
            if (mWifiDialog == null) {
                mWifiDialog = new NetworkPrivacyWarningAlertDialog(context, TYPE_WIFI);
                mWifiDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
                mWifiDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            }
        }

        /* 判断当前界面是否是iot设备连接界面
         */
        private boolean isIotActivityVisible(Context context) {
            int result = Settings.System.getInt(context.getContentResolver(), "persist.iot.status", 0);
            LogUtil.d(TAG, "isIotActivityVisible: result:"+result);
            if (result == 1) {
                return true;
            } else {
                return false;
            }
        }

        /* 判断当前界面是否是Setting界面
         */
        private boolean isSettingActivityVisible(Context context) {
            int result = SystemPropertiesProxy.getInt(context, "persist.sys.wifi.status", 0);
            if (result == 1) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * 判断当前是否激活完成,激活没有完成就不弹出
         */
        private boolean isSetupFinished(Context context) {
            PackageManager pm = context.getPackageManager();
            ComponentName guide = new ComponentName("com.kinstalk.her.setupwizard", "com.kinstalk.her.setupwizard.view.activity.HerBootGuideActivity");
            ComponentName guideXiaowei = new ComponentName("com.kinstalk.her.setupwizard", "com.kinstalk.her.setupwizard.view.activity.HerBootGuideXiaoWeiActivity");
            int guideCode = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            int guideXiaoweiCode = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

            try {
                pm.getActivityInfo(guide, 0);
                guideCode = pm.getComponentEnabledSetting(guide);
            } catch (PackageManager.NameNotFoundException e) {
            }

            try {
                pm.getActivityInfo(guideXiaowei, 0);
                guideCode = pm.getComponentEnabledSetting(guideXiaowei);
            } catch (PackageManager.NameNotFoundException e) {
            }
            boolean isFinished = (guideCode == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) && (guideXiaoweiCode == PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
            return isFinished;

        }

        private int isNetworkAvailable(Intent intent, Context context) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            LogUtil.d(TAG, "isNetworkAvailable: network:" + networkInfo);
            if (networkInfo != null) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    WifiInfo wifiInfo = getWifiInfo(context);
                    if (wifiInfo != null && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                        LogUtil.d(TAG, "wifi:" + wifiInfo.getSSID() + "连接成功");
                        return 1;
                    }
                } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                    LogUtil.d(TAG, "wifi网络连接断开");
                    return 0;
                } else if (networkInfo.getState() == NetworkInfo.State.CONNECTING) {
                    LogUtil.d(TAG, "wifi网络连接ing...");
                    return 2;
                }
            }
            return 0;
        }

        public WifiInfo getWifiInfo(Context context) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getSSID()) && !TextUtils.equals("<unknown ssid>", wifiInfo.getSSID())) {
                return wifiInfo;
            }
            return null;
        }
    };

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = android.os.Process.myPid();

        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            QAILog.i(TAG, "my.pid -> " + myPid + ",mainProcessName -> " + mainProcessName);
            QAILog.i(TAG, "info.pid -> " + info.pid + ",info.processName -> " + info.processName);

            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    private class MessageTimerTask extends TimerTask {
        private Context context;

        public MessageTimerTask(Context mContext) {
            context = mContext;
        }

        @Override
        public void run() {
            QAILog.i(TAG, "auto kill-process timer running");
            if (TextUtils.isEmpty(QAIConfig.getmXingeID()) && TextUtils.isEmpty(QAIConfig.getBaiduID()))
                mProcessKiller(context, "com.kinstalk.her.qchat:xg_service_v3");
            this.cancel();
        }
    }

    ; private class XingeKillerTimerTask extends TimerTask {
        private Context context;

        public XingeKillerTimerTask(Context mContext) {
            context = mContext;
        }

        @Override
        public void run() {
            QAILog.i(TAG, "XingeKillderTimerTask kill-process timer running");
            if (TextUtils.isEmpty(QAIConfig.getmXingeID()))
                mProcessKiller(context, null);
            this.cancel();
        }
    }

    private void mProcessKiller(Context context, String packageName) {
        Process.killProcess(Process.myPid());
    }

    public void removeActivity(Activity activity) {
        if (null != activity) {
            if (activityList.contains(activity)) {
                activityList.remove(activity);
            }
        }
    }

    public void addActivity(Activity activity) {
        if (null != activity) {
            activityList.add(activity);
        }
    }

    public void finishAllActivity() {
        //做个延迟，bug16190 [100%][ST][好童学-开机引导]礼物中心页面解绑好童学，会先回到首页，然后才跳出好童学二维码界面
        Log.i("GiftActivityLog","finishAllActivity");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activityList.size() > 0) {
                    for (Activity bean : activityList) {
                        bean.finish();
                    }
                }
                activityList.clear();
            }
        },2000);
    }
}
