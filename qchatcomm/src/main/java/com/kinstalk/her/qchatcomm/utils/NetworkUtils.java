/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchatcomm.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Knight.Xu on 2018/4/6.
 */

public class NetworkUtils {

    //network type
    public static final int NETWORK_WIFI = 1;
    public static final int NETWORK_MOBILE = 0;
    public static final int NETWORK_NONE = -1;

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (cm.getActiveNetworkInfo() != null) {
                return cm.getActiveNetworkInfo().isAvailable();
            }
        }
        return false;
    }

    public static final int getNetworkState(Context c) {
        int result = NETWORK_NONE;
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        if (ni != null) {
            if (ni.getType() == ConnectivityManager.TYPE_WIFI) result = NETWORK_WIFI;
            else if (ni.getType() == ConnectivityManager.TYPE_MOBILE)
                result = NETWORK_MOBILE;
        }
        return result;
    }
}
