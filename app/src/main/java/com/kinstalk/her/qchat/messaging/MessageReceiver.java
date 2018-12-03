package com.kinstalk.her.qchat.messaging;

import android.app.ActivityManager;
import android.content.Context;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.common.UIHelper;
import com.kinstalk.her.qchat.utils.CountlyConstant;
import com.kinstalk.her.qchat.view.ToastCustom;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatapi.aws.AWSTransferHelper;
import com.kinstalk.her.qchatcomm.base.BaseApplication;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.NetworkUtils;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatcomm.utils.SystemPropertiesProxy;
import com.kinstalk.her.qchatmodel.CalenderProviderHelper;
import com.kinstalk.her.qchatmodel.ChatProvider;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.util.UIHelp;
import com.kinstalk.her.qchatmodel.beans.ImageBean;
import com.kinstalk.her.qchatmodel.evtbus.HomeworkMsgEvt;
import com.kinstalk.her.qchatmodel.evtbus.ImageMsgEvt;
import com.kinstalk.her.qchatmodel.evtbus.TextMsgEvt;
import com.kinstalk.her.qchatmodel.ChatProviderHelper;
import com.tencent.tac.messaging.TACMessagingReceiver;
import com.tencent.tac.messaging.TACMessagingText;
import com.tencent.tac.messaging.TACMessagingToken;
import com.tencent.tac.messaging.TACNotification;
import com.tencent.tac.messaging.type.PushChannel;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import ly.count.android.sdk.Countly;

/**
 * Created by liulinxiang on 4/8/2018.
 * Modified by Tracy
 */

public class MessageReceiver extends TACMessagingReceiver {
    // 启动 Messaging 服务后，会自动向 Messaging 后台注册，注册完成后会回调此接口。

    String TAG = "MessageReceiver";
    private final String mChannel = "xinge";
    private Timer mAutoKillTimer;
    private String preMsgId = "";

    private class MessageTimerTask extends TimerTask {
        private Context context;

        public MessageTimerTask(Context mContext) {
            context = mContext;
        }

        @Override
        public void run() {
            QAILog.i(TAG, "auto kill-process timer running");
            mProcessKiller(context, "com.kinstalk.her.qchat:xg_service_v3");
            this.cancel();
        }
    }

    ;

    private void mProcessKiller(Context context, String packageName) {
        Process.killProcess(Process.myPid());
        /*
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
            method.invoke(mActivityManager, packageName);
        }catch(ClassNotFoundException e) {
            QAILog.e(TAG, e);
        }catch (NoSuchMethodException e) {
            QAILog.e(TAG, e);
        }catch (IllegalAccessException e) {
            QAILog.e(TAG, e);
        }catch (InvocationTargetException e) {
            QAILog.e(TAG, e);
        } */
    }


    @Override
    public void onRegisterResult(final Context context, int errorCode, TACMessagingToken token) {
        String mToken = token.getTokenString();
        Log.i("messaging", "MessageReceiver::OnRegisterResult : code is " + errorCode + ", token is " + mToken);

        if ((mToken == null || mToken.equals("0")) && TextUtils.isEmpty(QAIConfig.getBaiduID())) {
            mAutoKillTimer = new Timer();
            mAutoKillTimer.schedule(new MessageTimerTask(context), 5 * 1000, 5 * 1000);
        } else if (errorCode != 0 && !TextUtils.isEmpty(mToken) && mToken.length() > 1) {
            if(0 == UIHelper.getPropertyForXGActivate(context) ) {
                UIHelper.setPropertyForXGActivate(context);
                mAutoKillTimer = new Timer();
                mAutoKillTimer.schedule(new MessageTimerTask(context), 5 * 1000, 5 * 1000);
                QAIConfig.setKill(true);
            } else {
                UIHelper.ClearPropertyForXGActivate(context);
            }
        } else if (errorCode == 0 && !TextUtils.isEmpty(mToken) && !mToken.equals("0") && mToken.length() > 1) {
            UIHelper.ClearPropertyForXGActivate(context);
            QAIConfig.setmXingeID(mToken);
            if(NetworkUtils.isNetworkAvailable(context)) {
                QchatThreadManager.getInstance().start(new Runnable() {
                    @Override
                    public void run() {
                        String token = StringEncryption.generateToken();
                        Api.postChannelID(context, token, QAIConfig.getmXingeID(), false);
                    }
                });
            }
            XgPushHelper.getInstance().receivePushId(mToken);
        }
//        发送静态广播


    }

    // 反注册后回调此接口。
    @Override
    public void onUnregisterResult(Context context, int code) {
        Log.d("messaging", "MessageReceiver::onUnregisterResult : code is " + code);

    }

    // 收到通知栏消息后回调此接口。
    @Override
    public void onNotificationArrived(Context context, TACNotification notification, PushChannel notificationId) {
        Log.d("messaging", "MessageReceiver::OnNotificationShowed : notification is " + notification + " PushChannel id is " + notificationId);
        Log.d("messaging", "onNotificationShowed: text " + notification.getText().toString());
//        XgPushHelper.getInstance().startMainActivity();
    }

    // 用户处理通知栏消息后回调此接口，如用户点击或滑动取消通知。
    @Override
    public void onNotificationClicked(Context context, TACNotification notification, PushChannel pushChannel) {
        Log.d("messaging", "MessageReceiver::onNotificationClicked : notification is " + notification + " pushChannel is " + pushChannel);
    }

    // 收到应用内消息后回调此接口。
    @Override
    public void onMessageArrived(Context context, TACMessagingText message, PushChannel var3) {
        Log.i("messaging", "MessageReceiver::OnTextMessaged : message is " + message);
        String title = message.getTitle();
        String content = message.getContent();
        //     String customContent = message.getCustomContent();

        parseContent(context, content);

    }

    @Override
    public void onNotificationDeleted(Context var1, TACNotification var2, PushChannel var3) {

    }

    @Override
    public void onBindTagResult(Context var1, int var2, String var3) {

    }

    @Override
    public void onUnbindTagResult(Context var1, int var2, String var3) {

    }

    private String jsonGetID(String data) {
        String id = "";
        try {
            JSONObject obj = new JSONObject(data);
            id = obj.optString("id");
            QAILog.d(TAG, "id " + id);
        } catch (JSONException e) {
            QAILog.d(TAG, "JsonException" + e);
        }
        return id;
    }

    private int jsonGetType(String data) {
        int id = -1;
        try {
            JSONObject obj = new JSONObject(data);
            id = obj.optInt("typecode");
            QAILog.d(TAG, "typecode " + id);
        } catch (JSONException e) {
            QAILog.d(TAG, "JsonException" + e);
        }
        return id;
    }

    void parseContent(final Context context, String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            String type = jsonObject.optString("type");
            String data = jsonObject.optString("data");
            String opt = jsonObject.optString("opt");
            String openid = jsonObject.optString("who");
            String msgid = jsonObject.optString("msgid");
            Long timestamp = jsonObject.optLong("timestamp");
            QAILog.d(TAG, "msgid" + msgid);
            if(!TextUtils.isEmpty(UIHelper.getMsgID()) && !TextUtils.isEmpty(msgid) && UIHelper.getMsgID().equals(msgid)){
                QAILog.i(TAG, "we got same msg id");
                responseAck(context, "receipt", msgid, mChannel);
                return;
            }
            boolean needSync = jsonObject.optBoolean("full_sync", false);
            boolean alreadyBind = jsonObject.optBoolean("already_bind", false);
            responseAck(context, "receipt", msgid, mChannel);
            UIHelper.setMsgID(msgid);
            if (type.equals("voice") && MessageManager.chatMsgCheck(msgid)) {
                MessageManager.saveIncomingMsg(context, openid, "", data, msgid, ChatProviderHelper.Chat.MSG_TYPE_VOICE, 0);
                AWSTransferHelper.getInstance().download(context, data, ChatProviderHelper.Chat.MSG_TYPE_VOICE, 0);
                UIHelper.wakeupAndDong(context);
                Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE,CountlyConstant.t_voice_message_success);
            } else if (type.equals("text") && MessageManager.chatMsgCheck(msgid)) {
                QAILog.d(TAG, "Text content is " + data);
                MessageManager.saveIncomingMsg(context, openid, data, "", msgid, ChatProviderHelper.Chat.MSG_TYPE_TEXT, 0);
                MessageManager.notifyLauncher(context);
                UIHelper.wakeupAndDong(context);
            } else if (type.equals("image") && MessageManager.chatMsgCheck(msgid)) {
                MessageManager.saveIncomingMsg(context, openid, "", data, msgid, ChatProviderHelper.Chat.MSG_TYPE_PIC, 0);
                AWSTransferHelper.getInstance().download(context, data, ChatProviderHelper.Chat.MSG_TYPE_PIC, 0);
            } else if (type.equals("sub") || type.equals("unsub") || type.equals("scan")) {
                if(timestamp < UIHelper.getTimestamp()) {
                    QAILog.i(TAG, "timestamp is earlier than last control message, just ignore");
                }
                UIHelper.setTimestamp(timestamp);
                if (type.equals("sub")) {
                    QAIConfig.setBindStatus(true);
                    if(1 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0)) {
                        if (!ChatProviderHelper.mWxUserMap.isEmpty() && !ChatProviderHelper.mWxUserMap.containsKey((openid))) {
                            XgPushHelper.switchUserClearDB(context);
                        }
                    }
                    if (needSync && (!alreadyBind ||0 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0)))
                        XgPushHelper.asyncFetchHabitKidsInfo(context);
                    //asyncPostGift();
                } else if (type.equals("scan")) {
                    QAIConfig.setBindStatus(true);

                    if (alreadyBind && 1 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0)) {
                        QAILog.d(TAG, "need finish");
                        ToastCustom.makeText(context, context.getResources().getString(R.string.already_binded), Toast.LENGTH_LONG).show();
                        //         XgPushHelper.finishSwitchWXActivity(context); bug 15624 no need to finish activity
                    } else  if(1 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0)) {
                        if (!ChatProviderHelper.mWxUserMap.isEmpty() && !ChatProviderHelper.mWxUserMap.containsKey((openid))) {
                            XgPushHelper.switchUserClearDB(context);
                        }
                    }
                    if (needSync && (!alreadyBind ||0 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0))) {
                        XgPushHelper.asyncFetchHabitKidsInfo(context);
                    }
                    if(!alreadyBind) {
                        QchatApplication.getApplicationInstance().finishAllActivity();
                    }
                } else if (type.equals("unsub")) {
                    QAIConfig.setBindStatus(false);
                    Api.setHabitJsonArrayString("");
                    QchatApplication.getApplicationInstance().finishAllActivity();
                }
                QAIConfig.setSwitchUser(false);
                if ((0 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0)) || !alreadyBind)
                    XgPushHelper.getInstance().sendBindBCOuter(type);
            } else if (type.equals("homework")) {
                if (opt.equals("add") || opt.equals("mod")) {
                    Api.fetchHomework(context,
                            StringEncryption.generateToken(), QAIConfig.getMacForSn(), Api.UP_MSG_TYPE_HOMEWORK, "", "");
                } else if (opt.equals("del")) {
                    String id = jsonGetID(data);
                    MessageManager.deleteHomework(context, id);
                    UIHelp.notifyWorkAlert(context, id);
                    UIHelper.checkHomeworkAnimation(context);
                }
            } else if (type.equals("remind")) {
                if (opt.equals("add") || opt.equals("mod")) {
                    Api.fetchReminder(context, StringEncryption.generateToken(), QAIConfig.getMacForSn(), Api.UP_MSG_TYPE_REMINDER, "", "");
                } else if (opt.equals("del")) {
                    String id = jsonGetID(data);
                    MessageManager.deleteReminder(context, id);
                    MessageManager.NotifyReminderToLauncher(context);
                }
            } else if (type.equals("habit")) {
                if (opt.equals("add") || opt.equals(("mod"))) {
                    Api.fetchHabit(context, StringEncryption.generateToken(), QAIConfig.getMacForSn(), false);
                    Api.fetchStar(context, StringEncryption.generateToken(), QAIConfig.getMacForSn());
                } else if (opt.equals("del")) {
                    int id = jsonGetType(data);
                    MessageManager.deleteHabit(context, id);
                    MessageManager.asyncNotifyReminderChange(false);
                    UIHelper.clearSignSharedPrefrences(context, id);
                    MessageManager.NotifyReminderToLauncher(context);
                    QAILog.d(TAG, "del habit: " + id);
                } else if (opt.equals("close")) {
                    int id = jsonGetType(data);
                    MessageManager.deleteHabit(context, id);
                    UIHelper.clearSignSharedPrefrences(context, id);
                    MessageManager.asyncNotifyReminderChange(false);
                    MessageManager.NotifyReminderToLauncher(context);
                    QAILog.d(TAG, "close habit: " + id);
                }
            } else if (type.equals("star") || type.equals("kidinfo")) {
                Api.fetchStar(context, StringEncryption.generateToken(), QAIConfig.getMacForSn());
            } else if (type.equals("gift")) {
                //接受到gift的push
                QchatThreadManager.getInstance().start(new Runnable() {
                    @Override
                    public void run() {
                        String sn = QAIConfig.getMacForSn();
                        String token = StringEncryption.generateToken();
                        Api.postGift(token, sn);
                    }
                });
            } else if (type.equals("task")) {
                //接受到任务的push
                asyncPostTask();
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    void responseAck(final Context context, final String type, final String msgid, final String pushChannel) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                Api.postReceipt(context, StringEncryption.generateToken(), type, msgid, pushChannel);
            }
        });
    }

    private void asyncPostGift() {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                String sn = QAIConfig.getMacForSn();
                String token = StringEncryption.generateToken();
                Api.postGift(token, sn);
                Api.postTask(token, sn);
            }
        });
    }

    private void asyncPostTask() {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                String sn = QAIConfig.getMacForSn();
                String token = StringEncryption.generateToken();
                Api.postTask(token, sn);
            }
        });
    }


}
