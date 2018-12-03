package com.kinstalk.her.qchat.messaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.common.UIHelper;
import com.kinstalk.her.qchat.greendao.GreenDaoManager;
import com.kinstalk.her.qchat.remind.AutoCheck;
import com.kinstalk.her.qchat.view.ToastCustom;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.CommonDefUtils;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatmodel.ChatProviderHelper;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.util.UIHelp;
import com.kinstalk.her.qchatmodel.entity.WxUserEntity;

import java.util.List;

/**
 * Created by liulinxiang on 4/7/2018.
 * Modified by Tracy
 */

public class QchatBroadcast extends BroadcastReceiver {
    String TAG = QchatBroadcast.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "onReceive: " + intent.toString());
        String action = intent.getAction();
        if (CommonDefUtils.KINSTALK_QCHAT_BIND_STATUS.equals(action)) {
            String bind_typ = intent.getStringExtra(CommonDefUtils.KINSTALK_QCHAT_BIND_TYPE);
            if (!TextUtils.isEmpty(bind_typ)) {
                if (bind_typ.equals(CommonDefUtils.KINSTALK_QCHAT_BIND_LABEL)) {
                    QAIConfig.setBindStatus(true);
                    ToastCustom.makeText(context, context.getResources().getString(R.string.binding_success), Toast.LENGTH_LONG).show();
                    XgPushHelper.getInstance().finishQRCodeActivity();
                    UIHelper.setWorkTimeSharedPreferences();
                    //绑定,0表示未绑定小薇，1表示绑定了小薇
                    if (1 == Settings.Secure.getInt(context.getContentResolver(), "xiaowei_bind_status", 0)) {
                        XgPushHelper.getInstance().startMainActivity();
                    }
                    QchatThreadManager.getInstance().start(new Runnable() {
                        @Override
                        public void run() {
                            String sn = QAIConfig.getMacForSn();
                            String token = StringEncryption.generateToken();
                            Api.reqWxInfo(context, token, sn);
                            Api.fetchStar(context, token, sn);
                        }
                    });
                    if (!TextUtils.isEmpty(QAIConfig.getBaiduID())) {
                        QchatThreadManager.getInstance().start(new Runnable() {
                            @Override
                            public void run() {
                                String token = StringEncryption.generateToken();
                                Api.postChannelID(context, token, QAIConfig.getBaiduID(), true);
                            }
                        });
                    }
                    if (!TextUtils.isEmpty(QAIConfig.getmXingeID())) {
                        QchatThreadManager.getInstance().start(new Runnable() {
                            @Override
                            public void run() {
                                String token = StringEncryption.generateToken();
                                Api.postChannelID(context, token, QAIConfig.getmXingeID(), false);
                            }
                        });
                    }
                    AutoCheck.cancelQRCodeAlarm(context);
                    MessageManager.NotifyReminderToLauncher(context);
                } else if (bind_typ.equals(CommonDefUtils.KINSTALK_QCHAT_UNBIND_LABEL)) {
                    ToastCustom.makeText(context, context.getResources().getString(R.string.unbind_sucess), Toast.LENGTH_LONG).show();
                    ChatProviderHelper.mWxUserMap.clear();
                    MessageManager.eraseAllDB(context);
                    UIHelper.clearAllSharedPreferences();
                    XgPushHelper.getInstance().unbindGetQRCodeAgain();
                    MessageManager.updateHabitKidsInfo(null);
                    UIHelp.sendStarUpdateBroadcast(context, 0);
                    UIHelp.clearWorkAlert(context);
                    GreenDaoManager.getInstance().delDBData(context);
                } else if (bind_typ.equals(CommonDefUtils.KINSTALK_QCHAT_SCAN_LABEL)) {
                    //绑定过的用户再次扫码 某些时候会刷机不解绑，也会从这个逻辑走。
                    XgPushHelper.getInstance().finishQRCodeActivity();
                    AutoCheck.cancelQRCodeAlarm(context);
                    if (1 == Settings.Secure.getInt(context.getContentResolver(), "xiaowei_bind_status", 0)) {
                        XgPushHelper.getInstance().startMainActivity();
                    }
                    QchatThreadManager.getInstance().start(new Runnable() {
                        @Override
                        public void run() {
                            String sn = QAIConfig.getMacForSn();
                            String token = StringEncryption.generateToken();
                            Api.reqWxInfo(context, token, sn);
                            Api.fetchStar(context, token, sn);
                        }
                    });

                    if (!TextUtils.isEmpty(QAIConfig.getBaiduID())) {
                        QchatThreadManager.getInstance().start(new Runnable() {
                            @Override
                            public void run() {
                                String sn = QAIConfig.getMacForSn();
                                String token = StringEncryption.generateToken();
                                Api.postChannelID(context, token, QAIConfig.getBaiduID(), true);
                            }
                        });
                    }
                    if (!TextUtils.isEmpty(QAIConfig.getmXingeID())) {
                        QchatThreadManager.getInstance().start(new Runnable() {
                            @Override
                            public void run() {
                                String token = StringEncryption.generateToken();
                                Api.postChannelID(context, token, QAIConfig.getmXingeID(), false);
                            }
                        });
                    }
                    MessageManager.NotifyReminderToLauncher(context);
                }
            }
        } else if (UIHelper.ACTION_CLEAR_HOME.equals(action)) {
            if (1 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0)) {
                MessageManager.clearAllHomework();
                MessageManager.clearExpireHabit(context);
                MessageManager.asyncNotifyReminderChange(false);
                MessageManager.NotifyReminderToLauncher(context);
            }
            AutoCheck.registerCheckInNight(context);
        } else if (UIHelper.QRCODE_CHECK.equals(action)) {
            if (0 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0))
                XgPushHelper.getInstance().unbindGetQRCodeAgain();
        } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
            AutoCheck.registerCheckInNight(context);
            if (0 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0)) {
                AutoCheck.registerQRCodeRefresh(context);
            } else {
                Api.fetchStar(context, StringEncryption.generateToken(), QAIConfig.getMacForSn());
                MessageManager.clearExpireHabit(context);
                MessageManager.NotifyReminderToLauncher(context);
                MessageManager.getWxUserInfo();
            }
        } else if (UIHelper.SWITCH_WX.equals(action)) {
            if (1 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0)) {
                QAIConfig.setSwitchUser(true);
                XgPushHelper.getInstance().switchWXGetQRCodeAgain();
            } else {
                QAILog.i(TAG, "unbind status needn't switch user");
            }
        } else if (CommonDefUtils.KINSTALK_QCHAT_UPDATE_LAUNCHER.equals(action)) {
            MessageManager.NotifyReminderToLauncher(context);
        } else if (CommonDefUtils.KINSTALK_QCHAT_ACTIVATE_FAILED.equals(action)) {
            XgPushHelper.getInstance().activateFailedGetQRCodeAgain();
        } else {
            String qrcode_action = intent.getStringExtra(CommonDefUtils.KINSTALK_QCHAT_QRCODE);
            Log.d(TAG, "onReceive: qrcode_action " + qrcode_action);
            if (!TextUtils.isEmpty(qrcode_action)) {
                if (qrcode_action.equals("show")) {
                    //系统启动扫码界面
                    //   if(0==Settings.Secure.getInt(context.getContentResolver(),"wechat_bind_status",0))
                    //      XgPushHelper.getInstance().startQRCodeActivity();
                } else {
                    //
                }
            }
        }


    }

}
