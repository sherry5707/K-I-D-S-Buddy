/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchatcomm.utils;

import android.text.TextUtils;

import com.kinstalk.her.qchatcomm.base.BaseApplication;

/**
 * Created by knight.xu on 2018/4/6.
 */

public class QAIConfig {
    private static final String TAG = "QAIConfig";
    private static boolean bind_status = false;
    private static String mBaiduChannelID = "";
    private static String mXingeID = "";
    public static boolean isDebugMode = !SystemTool.isUserType();
    public static boolean isCrashCookerEnable = true;
    public static boolean isAudioCookerEnable = true;
    public static boolean isAiPromptEnable = false;
    public static boolean isNetworkGood = true; //标志网络是否ok
    public static String host_url = "";
    public static String aiengine_url = "";
    public static String accesskey_url = "";
    public static String engine = "";
    public static String qrcode = "";
    public static String sn;
    public static int qrcode_expiration = 14000;
    public static int code = 0;
    public static int current_env = 0;
    public static boolean autoTestDevice = false;
    public static int qLoveProductVersionNum = -1;
    private static boolean mSwitch = false;
    private static boolean mBind = false;
    private static boolean mKill = false;
    public static final String CONFIG_ACTION_ALL = "all";
    public static final String CONFIG_ACTION_SWITCH_TO_DEV = "switch_to_dev";
    public static final String CONFIG_ACTION_SWITCH_TO_TEST = "switch_to_test";
    public static final String CONFIG_ACTION_SWITCH_TO_PROD = "switch_to_prod";

    public static int logLevel = -1;
    public static int logType = -1;

    public static final int MODEL_ARMSTRONG = 0; // armstrong/discovery
    public static final int MODEL_COLUMBUS = 1; // 哥伦布
    public static final int MODEL_MAGELLAN_M10 = 3; // 麦哲伦 M10
    public static final int MODEL_MAGELLAN_M7 = 5; // 麦哲伦 M7

    public static void setBindStatus(boolean status) {
        bind_status = status;
    }

    public static boolean getBindStatus() {
        return bind_status;
    }

    private static String getQloveSN() {
        String qlovesn = SystemPropertiesProxy.getString(BaseApplication.getInstance().getApplicationContext(),
                "ro.serialno");
        String qloveSn = TextUtils.isEmpty(qlovesn) ? "" : qlovesn;

        return qloveSn;
    }

    public static void setSwitchUser(boolean status) {mSwitch = status;}

    public static boolean isSwitchUser(){
        return mSwitch;
    }

    public  static void setKill(boolean status ) {
        mKill = status;
    }
    public static boolean getKill() {
        return  mKill;
    }

    public  static void setBaiduID(String id) {
        mBaiduChannelID = id;
    }

    public static String getBaiduID() {
        return  mBaiduChannelID;
    }
    public  static void setmXingeID(String id) {
        mXingeID = id;
    }

    public static String getmXingeID() {
        return  mXingeID;
    }

    public static String getMacForSn() {
        String serialNum = getQloveSN();
        boolean isSnGot = false;
        if (!TextUtils.isEmpty(serialNum)) {
            QAILog.d(TAG, "getSn: serialNum = " + serialNum);
            if (serialNum.length() == 18) {
                sn = serialNum.substring(2, 18);
                isSnGot = true;
                QAILog.d(TAG, "getSn: sn = " + sn);
            } else {
                sn = "1234567890";
                QAILog.e(TAG, "getSn: wrong serial number");
            }
        } else {
            QAILog.e(TAG, "getSn: empty serial number ");
            String macAddr = SystemTool.getLocalMacAddress();
            if (!TextUtils.isEmpty(macAddr)) {
                QAILog.d(TAG, "getSn: mac = " + macAddr);
                if (macAddr.length() == 17) {
                    sn = macAddr.substring(0, 2) + macAddr.substring(3, 17);
                    isSnGot = true;
                    QAILog.d(TAG, "getSn: mac sn = " + sn);
                } else {
                    sn = "1234567890";
                    QAILog.e(TAG, "getSn: wrong mac Addr");
                }
            } else {
                sn = "1234567890";
                QAILog.e(TAG, "getSn: macAddr is empty");
            }
        }
        return sn;
    }

    public static String dumpString() {
        return "dumpQAIConfig: \n" +
                " isCrashCookerEnable: " + isCrashCookerEnable +
                " isAudioCookerEnable: " + isAudioCookerEnable +
                " aiengine_url: " + aiengine_url +
                " accesskey_url: " + accesskey_url +
                " engine: " + engine +
                " code: " + code +
                " current_env: " + current_env +
                " autoTestDevice: " + autoTestDevice;
    }
}
