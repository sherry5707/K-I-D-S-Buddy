package com.kinstalk.her.qchat.messaging;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.os.Handler;

import com.kinstalk.her.qchat.ChangeWxAccountActivity;
import com.kinstalk.her.qchat.QRCodeActivity;
import com.kinstalk.her.qchat.remind.AutoCheck;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatapi.aws.AWSTransferHelper;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.NetworkUtils;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatcomm.utils.CommonDefUtils;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatmodel.ChatProviderHelper;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.util.UIHelp;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by liulinxiang on 4/6/2018.
 */

public class XgPushHelper {

    private static String TAG = "XgPushHelper";
    private static final int GET_QR_CODE = 1;
    private static final int CHECK_QR_CODE = 2;
    private static int retry_time = 0;
    private static int MAX_RETRY_TIME = 15;
    private static Context mCtx;
    private static String mPushid = "";
    int qrTimeout = QAIConfig.qrcode_expiration;
    private static String qrImageLocalDir = "/data/data/com.kinstalk.her.qchat/images";
    private static String qrImageName = "qr_img";
    private static boolean isSusGetQRCode = false;

    private static Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case GET_QR_CODE:
                    QAILog.i(TAG, "Add Message");
                    beginGetQRCode();
                    break;
                case CHECK_QR_CODE:
                    checkQRCode();
                default:
                    break;

            }
        }
    };

    private static final XgPushHelper ourInstance = new XgPushHelper();

    public static XgPushHelper getInstance() {
        return ourInstance;
    }

    private XgPushHelper() {
    }

    public void onInitParas(Context context) {
        mCtx = context.getApplicationContext();
    }

    public void receivePushId(String pushid) {
        mPushid = pushid;
        if (0 == Settings.Secure.getInt(this.mCtx.getContentResolver(), "wechat_bind_status", 0))
            beginGetQRCode();
    }

    public void unbindGetQRCodeAgain() {
        if ((!TextUtils.isEmpty(mPushid) || !TextUtils.isEmpty(QAIConfig.getBaiduID())) && retry_time == 0) {
            beginGetQRCode();
        }
    }

    public void switchWXGetQRCodeAgain() {
        if ((!TextUtils.isEmpty(mPushid) || !TextUtils.isEmpty(QAIConfig.getBaiduID())) && retry_time == 0) {
            beginGetQRCode();
        }
    }

    public void activateFailedGetQRCodeAgain() {
        if ((!TextUtils.isEmpty(mPushid) || !TextUtils.isEmpty(QAIConfig.getBaiduID())) && retry_time == 0) {
            beginGetQRCode();
        }
    }

    public static void beginBaiduGetQRCode() {
        if (retry_time == 0)
            beginGetQRCode();
    }

    private static void beginGetQRCode() {
        if (!NetworkUtils.isNetworkAvailable(mCtx)) {
            mHandler.sendEmptyMessageDelayed(GET_QR_CODE, 2000);
            return;
        }
        String sn = QAIConfig.getMacForSn();
        String token = StringEncryption.generateToken();
        Log.i(TAG, "onReceive: pushid " + mPushid + " sn " + sn + " token " + token);
        if (retry_time < MAX_RETRY_TIME)
            reqQrCodeAsync(mCtx, token, sn, mPushid, QAIConfig.getBaiduID());
        else
            retry_time = 0;
    }

    private static void checkQRCode() {
        if (0 == Settings.Secure.getInt(mCtx.getContentResolver(), "wechat_bind_status", 0)) {
            AutoCheck.registerQRCodeRefresh(mCtx);
        }
    }

    private void sleep(int l) {
        try {
            Thread.sleep(l);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startWXQRCodeActivity() {
        Intent intent1 = new Intent(mCtx, ChangeWxAccountActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mCtx.startActivity(intent1);


    }

    public void finishQRCodeActivity() {
        Intent intent1 = new Intent(mCtx, QRCodeActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.putExtra("finish", true);
        mCtx.startActivity(intent1);
    }

    public static void finishSwitchWXActivity(Context context) {
        Intent intent1 = new Intent(context, ChangeWxAccountActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.putExtra("finish", true);
        context.startActivity(intent1);
    }

    public void startMainActivity() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        i.addCategory(Intent.CATEGORY_HOME);
        mCtx.startActivity(i);
    }

    public void startHerBootGuideActivity() {
        Intent intent = new Intent();
        ComponentName cn = new ComponentName("com.kinstalk.her.setupwizard",
                "com.kinstalk.her.setupwizard.view.activity.HerBootGuideActivity");
        intent.setComponent(cn);
        mCtx.startActivity(intent);
    }

    public static void reqQrCodeAsync(final Context context, final String token, final String sn, final String pushid, final String baiduID) {
        QAILog.i(TAG, "newFetchConfig: enter");
        com.kinstalk.her.qchatcomm.thread.QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                retry_time++;
                Api.reqQrCode(context, token, sn, pushid, baiduID);
                if (TextUtils.isEmpty(QAIConfig.qrcode)) {
                    mHandler.sendEmptyMessageDelayed(GET_QR_CODE, 2000);
                    return;
                } else {
                    try {
                /*        URL url = new URL(QAIConfig.qrcode);
                        HttpURLConnection conn =  (HttpURLConnection )url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setConnectTimeout(30000);
                        conn.setReadTimeout(30000);
                        Bitmap bmp = BitmapFactory.decodeStream(conn.getInputStream());
                        try {
                            int width = bmp.getWidth();
                */
                        mHandler.removeMessages(CHECK_QR_CODE);
                        mHandler.sendEmptyMessageDelayed(CHECK_QR_CODE, 2 * 60 * 1000);
                        //            saveImage(bmp);
                        retry_time = 0;
                        if (!QAIConfig.getBindStatus())
                            notifyQRCodeShowSts();
                        if (QAIConfig.isSwitchUser()) {
                            QAIConfig.setSwitchUser(false);
                            startWXQRCodeActivity();
                        }
                /*        } catch (Exception e) {
                            QAILog.e(TAG, e.getMessage());
                            mHandler.sendEmptyMessageDelayed(GET_QR_CODE, 2000);
                        }*/
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHandler.sendEmptyMessageDelayed(GET_QR_CODE, 2000);
                    }
                }
            }
        });
    }

    public static void notifyQRCodeShowSts() {

        Log.d(TAG, "notifyQRCodeShowSts: ");
        if (!ChatProviderHelper.isBinded())
            sendQRCodeActionBC("show");
        else {
            sendQRCodeActionBC("no_show");
        }
    }

    public void sendBindBCOuter(String type) {
        Boolean bindStatus = false;
        if (!TextUtils.isEmpty(type)) {
            if (type.equals("sub") || type.equals("scan")) {
                bindStatus = true;
            } else if (type.equals("unsub")) {
                bindStatus = false;
            }
        }
        Intent intent = new Intent();
        intent.setAction(CommonDefUtils.KINSTALK_QCHAT_BIND_STATUS);
        intent.putExtra(CommonDefUtils.EXTRA_QCHAT_BIND_STATUS, bindStatus);
        intent.putExtra(CommonDefUtils.KINSTALK_QCHAT_BIND_TYPE, type);
        mCtx.sendStickyBroadcast(intent);
    }

    public static void sendQRCodeActionBC(String type) {
        Intent intent = new Intent();
        //mCtx, QchatBroadcast.class);
        intent.setAction(CommonDefUtils.KINSTALK_QCHAT_QRCODE);
        intent.putExtra("com.kinstalk.her.qchat.qrcode", type);
        mCtx.sendStickyBroadcast(intent);
    }

    private static void saveImage(Bitmap finalBitmap) {
        File myDir = new File(qrImageLocalDir);
        myDir.mkdirs();
        String fname = qrImageName + ".jpg";
        File file = new File(myDir, fname);
        Log.d(TAG, "SaveImage: myDir " + myDir);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            isSusGetQRCode = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getQRImageFile() {
        File file = new File(qrImageLocalDir + "/" + qrImageName + ".jpg");
        Log.d(TAG, "getQRImageFile: file " + file);
        return file;
    }

    public void refreshQRCodeByHand() {
        beginGetQRCode();
    }

    public void sendMsgToSrv(final String voicePath, final String path, final String type, final String body) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                if (type.equals(Api.UP_MSG_TYPE_VOICE)) {
                    AWSTransferHelper.getInstance().upload(voicePath, path);
                }
                Api.postMessage(mCtx, StringEncryption.generateToken(), QAIConfig.getMacForSn(), type, path, body);
            }
        });
    }

    public void homeworkACK(String id) {
         Api.postHomeworkID(StringEncryption.generateToken(), QAIConfig.getMacForSn(), id);
    }

    public static void switchUserClearDB(Context context) {
        //如果过去有其它绑定者，清除
        Log.i(TAG, "switch user clear db");
        ChatProviderHelper.mWxUserMap.clear();
        MessageManager.eraseAllDB(context);
        UIHelp.clearSharedPreferences(context, UIHelp.SHARED_PREFERENCE_NAME_UI);
        MessageManager.NotifyReminderToLauncher(context);
    }

    public static void asyncFetchHabitKidsInfo(final Context context) {
        Api.fetchHabit(context, StringEncryption.generateToken(), QAIConfig.getMacForSn(), true);
        Api.fetchStar(context, StringEncryption.generateToken(), QAIConfig.getMacForSn());
    }
}
