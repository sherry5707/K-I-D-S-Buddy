package com.kinstalk.her.qchat.messaging;

import android.content.Context;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baidu.android.pushservice.PushMessageReceiver;
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
import com.kinstalk.her.qchatmodel.ChatProviderHelper;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.util.UIHelp;
import com.kinstalk.her.qchatmodel.evtbus.HomeworkMsgEvt;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

import ly.count.android.sdk.Countly;

/*
 * Push消息处理receiver。请编写您需要的回调函数， 一般来说： onBind是必须的，用来处理startWork返回值；
 *onMessage用来接收透传消息； onSetTags、onDelTags、onListTags是tag相关操作的回调；
 *onNotificationClicked在通知被点击时回调； onUnbind是stopWork接口的返回值回调
 * 返回值中的errorCode，解释如下：
 *0 - Success
 *10001 - Network Problem
 *10101  Integrate Check Error
 *30600 - Internal Server Error
 *30601 - Method Not Allowed
 *30602 - Request Params Not Valid
 *30603 - Authentication Failed
 *30604 - Quota Use Up Payment Required
 *30605 -Data Required Not Found
 *30606 - Request Time Expires Timeout
 *30607 - Channel Token Timeout
 *30608 - Bind Relation Not Found
 *30609 - Bind Number Too Many
 * 当您遇到以上返回错误时，如果解释不了您的问题，请用同一请求的返回值requestId和errorCode联系我们追查问题。
 *
 */

public class BaiduPushMessageReceiver extends PushMessageReceiver {

    /**
     * 调用PushManager.startWork后，sdk将对push
     * server发起绑定请求，这个过程是异步的。绑定请求的结果通过onBind返回。 如果您需要用单播推送，需要把这里获取的channel
     * id和user id上传到应用server中，再调用server接口用channel id和user id给单个手机或者用户推送。
     *
     * @param context   BroadcastReceiver的执行Context
     * @param errorCode 绑定接口返回值，0 - 成功
     * @param appid     应用id。errorCode非0时为null
     * @param userId    应用user id。errorCode非0时为null
     * @param channelId 应用channel id。errorCode非0时为null
     * @param requestId 向服务端发起的请求id。在追查问题时有用；
     * @return none
     */
    private Timer mAutoTimer;
    private final String mChannel  = "baidu";

    private class MessageTimerTask extends TimerTask {
        private Context context;

        public MessageTimerTask(Context mContext) {
            context = mContext;
        }

        @Override
        public void run() {
            QAILog.i(TAG, "auto kill-process timer running");
            if (TextUtils.isEmpty(QAIConfig.getmXingeID())) {
                XgPushHelper.beginBaiduGetQRCode();
            }
            this.cancel();
        }
    }

    @Override
    public void onBind(final Context context, int errorCode, String appid,
                       String userId, String channelId, String requestId) {
        String responseString = "onBind errorCode=" + errorCode + " appid="
                + appid + " userId=" + userId + " channelId=" + channelId
                + " requestId=" + requestId;
        QAILog.d(TAG, responseString);

        if (errorCode == 0) {
            // 绑定成功
            QAILog.d(TAG, "Baidu push enable");
        }
        //Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
        if (TextUtils.isEmpty(channelId) || TextUtils.equals("0", channelId)) {
            QAILog.d(TAG, "channelID is null");
        } else {
            QAILog.d(TAG, "Baidu channel ID is " + channelId);
            QAIConfig.setBaiduID(channelId);

            mAutoTimer = new Timer();
            if (0 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0))
                mAutoTimer.schedule(new MessageTimerTask(context), 5 * 1000, 5 * 1000);
            if(NetworkUtils.isNetworkAvailable(context)) {
                QchatThreadManager.getInstance().start(new Runnable() {
                    @Override
                    public void run() {
                        String token = StringEncryption.generateToken();
                        Api.postChannelID(context, token, QAIConfig.getBaiduID(), true);
                    }
                });
            }
        }
    }

    /**
     * 接收透传消息的函数。
     *
     * @param context             上下文
     * @param message             推送的消息
     * @param customContentString 自定义内容,为空或者json字符串
     */
    @Override
    public void onMessage(Context context, String message,
                          String customContentString) {
        String messageString = "透传消息 onMessage=\"" + message
                + "\" customContentString=" + customContentString;
        QAILog.d(TAG, messageString);

        // 自定义内容获取方式，mykey和myvalue对应透传消息推送时自定义内容中设置的键和值
        if (!TextUtils.isEmpty(customContentString)) {
            JSONObject customJson = null;
            try {
                customJson = new JSONObject(customContentString);
                String myvalue = null;
                if (!customJson.isNull("mykey")) {
                    myvalue = customJson.getString("mykey");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(message)) {
            // 应用请在这里加入自己的处理逻辑
            parseContent(context, message);
            updateContent(context, messageString);
        }
    }

    /**
     * 接收通知到达的函数。
     *
     * @param context             上下文
     * @param title               推送的通知的标题
     * @param description         推送的通知的描述
     * @param customContentString 自定义内容，为空或者json字符串
     */

    @Override
    public void onNotificationArrived(Context context, String title,
                                      String description, String customContentString) {

        String notifyString = "通知到达 onNotificationArrived  title=\"" + title
                + "\" description=\"" + description + "\" customContent="
                + customContentString;
        QAILog.d(TAG, notifyString);

        // 自定义内容获取方式，mykey和myvalue对应通知推送时自定义内容中设置的键和值
        if (!TextUtils.isEmpty(customContentString)) {
            JSONObject customJson = null;
            try {
                customJson = new JSONObject(customContentString);
                String myvalue = null;
                if (!customJson.isNull("mykey")) {
                    myvalue = customJson.getString("mykey");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
        // 你可以參考 onNotificationClicked中的提示从自定义内容获取具体值
        updateContent(context, notifyString);
    }

    /**
     * 接收通知点击的函数。
     *
     * @param context             上下文
     * @param title               推送的通知的标题
     * @param description         推送的通知的描述
     * @param customContentString 自定义内容，为空或者json字符串
     */
    @Override
    public void onNotificationClicked(Context context, String title,
                                      String description, String customContentString) {
        String notifyString = "通知点击 onNotificationClicked title=\"" + title + "\" description=\""
                + description + "\" customContent=" + customContentString;
        QAILog.d(TAG, notifyString);

        // 自定义内容获取方式，mykey和myvalue对应通知推送时自定义内容中设置的键和值
        if (!TextUtils.isEmpty(customContentString)) {
            JSONObject customJson = null;
            try {
                String newContent = customContentString.replace('\"', '"');
                QAILog.d(TAG, newContent);
                customJson = new JSONObject(customContentString);
                String myvalue = null;
                if (!customJson.isNull("mykey")) {
                    myvalue = customJson.getString("mykey");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
        updateContent(context, notifyString);
    }

    /**
     * setTags() 的回调函数。
     *
     * @param context     上下文
     * @param errorCode   错误码。0表示某些tag已经设置成功；非0表示所有tag的设置均失败。
     * @param successTags 设置成功的tag
     * @param failTags    设置失败的tag
     * @param requestId   分配给对云推送的请求的id
     */
    @Override
    public void onSetTags(Context context, int errorCode,
                          List<String> successTags, List<String> failTags, String requestId) {
        String responseString = "onSetTags errorCode=" + errorCode
                + " successTags=" + successTags + " failTags=" + failTags
                + " requestId=" + requestId;
        QAILog.d(TAG, responseString);

        // Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
        updateContent(context, responseString);
    }

    /**
     * delTags() 的回调函数。
     *
     * @param context     上下文
     * @param errorCode   错误码。0表示某些tag已经删除成功；非0表示所有tag均删除失败。
     * @param successTags 成功删除的tag
     * @param failTags    删除失败的tag
     * @param requestId   分配给对云推送的请求的id
     */
    @Override
    public void onDelTags(Context context, int errorCode,
                          List<String> successTags, List<String> failTags, String requestId) {
        String responseString = "onDelTags errorCode=" + errorCode
                + " successTags=" + successTags + " failTags=" + failTags
                + " requestId=" + requestId;
        QAILog.d(TAG, responseString);

        // Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
        updateContent(context, responseString);
    }

    /**
     * listTags() 的回调函数。
     *
     * @param context   上下文
     * @param errorCode 错误码。0表示列举tag成功；非0表示失败。
     * @param tags      当前应用设置的所有tag。
     * @param requestId 分配给对云推送的请求的id
     */
    @Override
    public void onListTags(Context context, int errorCode, List<String> tags,
                           String requestId) {
        String responseString = "onListTags errorCode=" + errorCode + " tags="
                + tags;
        QAILog.d(TAG, responseString);

        // Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
        updateContent(context, responseString);
    }

    /**
     * PushManager.stopWork() 的回调函数。
     *
     * @param context   上下文
     * @param errorCode 错误码。0表示从云推送解绑定成功；非0表示失败。
     * @param requestId 分配给对云推送的请求的id
     */
    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {
        String responseString = "onUnbind errorCode=" + errorCode
                + " requestId = " + requestId;
        QAILog.d(TAG, responseString);

        if (errorCode == 0) {
            // 解绑定成功
            QAILog.d(TAG, "解绑成功");
        }
        // Demo更新界面展示代码，应用请在这里加入自己的处理逻辑
        updateContent(context, responseString);
    }

    private void updateContent(Context context, String content) {
        QAILog.d(TAG, "updateContent");
        String logText = "" + BaiduPushUtils.logStringCache;

        if (!logText.equals("")) {
            logText += "\n";
        }

        SimpleDateFormat sDateFormat = new SimpleDateFormat("HH-mm-ss");
        logText += sDateFormat.format(new Date()) + ": ";
        logText += content;

        BaiduPushUtils.logStringCache = logText;

//        Intent intent = new Intent();
//        intent.setClass(context.getApplicationContext(), PushDemoActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.getApplicationContext().startActivity(intent);
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

    void parseContent(Context context, String content) {
        try {
            JSONObject jsonObject = new JSONObject(content);
            String type = jsonObject.optString("type");
            String data = jsonObject.optString("data");
            String opt = jsonObject.optString("opt");
            String openid = jsonObject.optString("who");
            String msgid = jsonObject.optString("msgid");
            Long timestamp = jsonObject.optLong("timestamp");
            boolean needSync = jsonObject.optBoolean("full_sync", false);
            boolean alreadyBind = jsonObject.optBoolean("already_bind", false);
            QAILog.d(TAG, "msgid" + msgid);
            if(!TextUtils.isEmpty(UIHelper.getMsgID()) && !TextUtils.isEmpty(msgid) && UIHelper.getMsgID().equals(msgid)){
                QAILog.i(TAG, "we got same msg id");
                responseAck(context, "receipt", msgid, mChannel);
                return;
            }
            responseAck(context, "receipt", msgid, mChannel);
            UIHelper.setMsgID(msgid);

            if (type.equals("voice") && MessageManager.chatMsgCheck(msgid)) {
                MessageManager.saveIncomingMsg(context, openid, "", data, msgid, ChatProviderHelper.Chat.MSG_TYPE_VOICE, 0);
                AWSTransferHelper.getInstance().download(context, data, ChatProviderHelper.Chat.MSG_TYPE_VOICE, 0);
                UIHelper.wakeupAndDong(context);
                Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE,CountlyConstant.t_voice_message_success);
            } else if (type.equals("text") && MessageManager.chatMsgCheck(msgid)) {
                MessageManager.saveIncomingMsg(context, openid, data, "", msgid, ChatProviderHelper.Chat.MSG_TYPE_TEXT, 0);
                UIHelper.wakeupAndDong(context);
                MessageManager.notifyLauncher(context);
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
                    if (needSync && (0 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0) || !alreadyBind))
                        XgPushHelper.asyncFetchHabitKidsInfo(context);
                } else if (type.equals("scan")) {
                    QAIConfig.setBindStatus(true);

                    if (alreadyBind && 1 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0)) {
                        QAILog.d(TAG, "need finish");
                        ToastCustom.makeText(context, context.getResources().getString(R.string.already_binded), Toast.LENGTH_LONG).show();
                        //      XgPushHelper.finishSwitchWXActivity(context);   bug 15624 no need to finish activity
                    } else  if(1 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0)) {
                        if (!ChatProviderHelper.mWxUserMap.isEmpty() && !ChatProviderHelper.mWxUserMap.containsKey((openid))) {
                            XgPushHelper.switchUserClearDB(context);
                        }
                    }

                    if (!alreadyBind) {
                        QchatApplication.getApplicationInstance().finishAllActivity();
                    }
                    if (needSync && (!alreadyBind ||0 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0))) {
                        XgPushHelper.asyncFetchHabitKidsInfo(context);
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
                    UIHelper.clearSignSharedPrefrences(context, id);
                    MessageManager.asyncNotifyReminderChange(false);
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
            } else if (type.equals("star")) {
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
                QchatThreadManager.getInstance().start(new Runnable() {
                    @Override
                    public void run() {
                        String sn = QAIConfig.getMacForSn();
                        String token = StringEncryption.generateToken();
                        Api.postTask(token, sn);
                    }
                });
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
    }

    void responseAck(final Context context, final String type, final String msgid, final String channelID) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                Api.postReceipt(context, StringEncryption.generateToken(), type, msgid, channelID);
            }
        });
    }
}
