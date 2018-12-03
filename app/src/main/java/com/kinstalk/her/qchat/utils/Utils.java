package com.kinstalk.her.qchat.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.kinstalk.her.qchatcomm.base.BaseApplication;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.kinstalk.util.AppUtils;

public class Utils {

    public static final int NET_NOT_AVAILABLE = 0;
    public static final int NET_WIFI = 1;
    public static final int NET_PROXY = 2;
    public static final int NET_NORMAL = 3;
    private static final String NET_TYPE_WIFI = "WIFI";
    /**
     * 网络类型, 以下的几个方法都是根据TuixinService1中的广播设置的值处理网络连接的
     */
    private volatile static int networkType = NET_NOT_AVAILABLE;

    /**
     * 判断网络连接是否可用
     */
    public static boolean checkNetworkAvailable() {
        int type = getNetworkType(BaseApplication.getInstance());
        return !(type == NET_NOT_AVAILABLE || type == NET_PROXY);
    }

    /**
     * 获取网络连接类型
     */
    public synchronized static int getNetworkType(Context inContext) {
        Context context = inContext.getApplicationContext();
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        if (networkinfo == null || !networkinfo.isAvailable()) {
            // 当前网络不可用
            networkType = NET_NOT_AVAILABLE;
        } else {
            // 如果当前是WIFI连接
            if (NET_TYPE_WIFI.equals(networkinfo.getTypeName())) {
                networkType = NET_WIFI;
            }
            // 非WIFI联网
            else {
                String proxyHost = android.net.Proxy.getDefaultHost();
                // 代理模式
                if (proxyHost != null) {
                    networkType = NET_PROXY;
                }
                // 直连模式
                else {
                    networkType = NET_NORMAL;
                }
            }
        }
        return networkType;
    }

    public static String htmlReplace(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        str = str.replace("&apos;", "'");
        str = str.replace("&ldquo;", "“");
        str = str.replace("&rdquo;", "”");
        str = str.replace("&nbsp;", " ");
        str = str.replace("&amp;", "&");
        str = str.replace("&#39;", "'");
        str = str.replace("&rsquo;", "’");
        str = str.replace("&mdash;", "—");
        str = str.replace("&ndash;", "–");
        return str;
    }

    public static void autoBackLauncher(Activity activity, boolean isBack) {
        Log.d("Utils", "autoBackLauncher isBack:" + isBack);
        try {
            AppUtils.setAutoActivityTimeout(activity.getWindow(), isBack);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static String getUserVipFlagTime(long time) {
        Date date = new Date(time);
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        return format.format(date);
    }


    /**
     * 获得属于桌面的应用的应用包名称
     * 返回包含所有包名的字符串列表数组
     *
     * @return
     */
    public static List<String> getHomeApplicationList(Context context) {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resolveInfos) {
            names.add(resolveInfo.activityInfo.packageName);
        }
        return names;
    }

    /**
     * 判断当前界面是否是桌面
     */
    public static boolean isAtHome(Context context) {
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = mActivityManager.getRunningTasks(1);
        return getHomeApplicationList(context).contains(runningTaskInfos.get(0).topActivity.getPackageName());
    }
}
