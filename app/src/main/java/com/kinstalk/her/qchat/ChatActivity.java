package com.kinstalk.her.qchat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kinstalk.her.qchat.common.UIHelper;
import com.kinstalk.her.qchat.frags.QBaseFragment;
import com.kinstalk.her.qchat.image.GlideImageLoader;
import com.kinstalk.her.qchat.message.holders.QIncomingEmojiMessageViewHolder;
import com.kinstalk.her.qchat.message.holders.QIncomingImageMessageViewHolder;
import com.kinstalk.her.qchat.message.holders.QIncomingTextMessageViewHolder;
import com.kinstalk.her.qchat.message.holders.QIncomingVoiceMessageViewHolder;
import com.kinstalk.her.qchat.message.holders.QOutcomingEmojiMessageViewHolder;
import com.kinstalk.her.qchat.message.holders.QOutcomingImageMessageViewHolder;
import com.kinstalk.her.qchat.message.holders.QOutcomingTextMessageViewHolder;
import com.kinstalk.her.qchat.message.holders.QOutcomingVoiceMessageViewHolder;
import com.kinstalk.her.qchat.messaging.XgPushHelper;
import com.kinstalk.her.qchat.utils.CountlyConstant;
import com.kinstalk.her.qchat.voice.QVoicePlayer;
import com.kinstalk.her.qchat.widget.QRecorderButton;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatapi.aws.AWSTransferHelper;
import com.kinstalk.her.qchatcomm.base.BaseApplication;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.FileUtils;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatmodel.ChatProviderHelper;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.model.messageCallback;
import com.kinstalk.her.qchatmodel.Manager.util.UIHelp;
import com.kinstalk.her.qchatmodel.entity.QchatMessage;
import com.kinstalk.her.qchatmodel.evtbus.EventIsBack;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.kinstalk.util.AppUtils;
import java.util.List;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import ly.count.android.sdk.Countly;

public class ChatActivity extends AppCompatActivity implements messageCallback, MessageHolders.ContentChecker<QchatMessage>, MessagesListAdapter.OnLoadMoreListener, QRecorderButton.RecorderFinishListener, MessagesListAdapter.ScrollToEndListenner, AWSTransferHelper.UploadFinishListener {
    private static final String TAG = QBaseFragment.class.getSimpleName();

    protected static final int TOTAL_MESSAGES_COUNT = 100;
    private final int SEND_MESSAGE_TAG = 1;
    // Chat
    protected static final byte CONTENT_TYPE_VOICE = 1;//语音类型
    protected static final byte CONTENT_TYPE_EMOJI = 2;//表情类型
    // HOMEWORK
    protected static final byte CONTENT_TYPE_HOMEWORK = 3;//作业类型

    private String ACTION_AICORE_WINDOW_SHOWN = "kinstalk.com.aicore.action.window_shown";
    private String EXTRA_AICORE_WINDOW_SHOWN = "isShown";
    protected Context mContext;
    protected ImageLoader imageLoader;
    private RelativeLayout layoutView;
    private MessagesList messagesList;
    private MessagesListAdapter<QchatMessage> messagesAdapter;
    private List<QchatMessage> chatMessageList;
    private TextView sendView;


    private final String senderId = "0";
    private BroadcastReceiver mAiUIRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_AICORE_WINDOW_SHOWN.equals(action)) {
                boolean isShow = intent.getBooleanExtra(EXTRA_AICORE_WINDOW_SHOWN, false);
                QAILog.d(TAG, "AI UI wake up isShow " + isShow);
                if (isShow) {

                    QAILog.d(TAG, "Keven Debug: onReceive run.");

                    QVoicePlayer.stop();
                }
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        EventBus.getDefault().register(this);
        initVoiceTest();
        MessageManager.getWxUserInfoSync();
        Api.fetchStar(this, StringEncryption.generateToken(), QAIConfig.getMacForSn());
        imageLoader = new GlideImageLoader();
        registerAIReceiver();
        mContext = BaseApplication.getInstance().getApplicationContext();
        layoutView = (RelativeLayout) findViewById(R.id.layout_view);
        sendView = (TextView) findViewById(R.id.send_message_text);
        sendView.setOnClickListener(null);

        layoutView.bringToFront();
        layoutView.getBackground().setAlpha(0);
        messagesList = (MessagesList) findViewById(R.id.qchat_messagesList);
        ImageButton close = (ImageButton) findViewById(R.id.chat_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_chatroom_turnoff);
                overridePendingTransition(R.anim.fullscreen_enter, R.anim.fullscreen_exit);
                XgPushHelper.getInstance().startMainActivity();
            }
        });
        initAdapter();
        MessageManager.registerMessageCallback(this);
        findAllChats();


    }

    public void layoutView(View v) {

    }

    /*******************本类测试录音代码******************************/
    private QRecorderButton radioVoice;
    private static final int REQ_RECORD_SUCCESS_CODE = 0;

    private void initVoiceTest() {
        radioVoice = (QRecorderButton) findViewById(R.id.voiceButton);
        AWSTransferHelper.setUploadFinishListener(this);

        radioVoice.setOnClicListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QAILog.d(TAG, "onLongClick");
                permissionGen();
                Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_chatroom_chatbutton);

            }
        });
        radioVoice.setRecorderFinishListener(this);
    }

    @Override
    public void onChatRecordFinish(final float seconds, final String filePath, final String fileName) {
/*        if (PrivacyManager.getInstance(this).isPrivacy()) {
            PrivacyManager.getInstance(this).showPrivacyDialog(this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    QAILog.d(TAG, "Privacy");
                    PrivacyManager.getInstance(getApplicationContext()).closePrivacy();
                    dialog.dismiss();
                }
            });
            return;
        }*/
        sendViewStatus(999);
        //TODO sendmessage state
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                XgPushHelper.getInstance().sendMsgToSrv(filePath, fileName, Api.UP_MSG_TYPE_VOICE, "");
                MessageManager.saveOutcomingMsg(BaseApplication.getInstance().getApplicationContext(), "", filePath, fileName, ChatProviderHelper.Chat.MSG_TYPE_VOICE, FileUtils.getDurationOutMessage(filePath));
            }
        });
        UIHelper.Dong(getApplicationContext());
    }

    private void permissionGen() {
        //处理需要动态申请的权限
        PermissionGen.with(this)
                .addRequestCode(REQ_RECORD_SUCCESS_CODE)
                .permissions(
                        android.Manifest.permission.RECORD_AUDIO,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .request();
    }

    // 权限申请成功
    @PermissionSuccess(requestCode = REQ_RECORD_SUCCESS_CODE)
    public void startChatRecord() {
        //在这个方法中做一些权限申请成功的事情
        Log.d(TAG, "startChatRecord: ");
        radioVoice.doOnLongClick();
    }

    // 申请失败
    @PermissionFail(requestCode = REQ_RECORD_SUCCESS_CODE)
    public void startChatRecordFailed() {
        Log.d(TAG, "startChatRecordFailed: ");
        Toast.makeText(this
                , getText(R.string.req_record_permission_failed)
                , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        UIHelp.sendEnterMessageBroadcast(this);
    }

    @Override
    public void onPause() {
        QVoicePlayer.stop();
        radioVoice.setResetRes();
        super.onPause();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(EventIsBack isBack) {
        Log.d(TAG, "isback" + isBack);
        setAutoSwitchLauncher(isBack.getIsBack());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        radioVoice.removeAllMessageWhenFinish();
        MessageManager.unRegisterMessageCallback(this);
        unRegisterAIReceiver();
//        getContext().getContentResolver().unregisterContentObserver(mContentObserver);
    }

    private void findAllChats() {
        chatMessageList = MessageManager.getAllChatMessage();
        runOnUiThread(new Runnable() { //imitation of internet connection
            @Override
            public void run() {
                messagesAdapter.clear();
                messagesAdapter.addToEnd(chatMessageList, true);
                messagesAdapter.notifyData();


            }
        });
    }

    private void initAdapter() {
        /**声音
         */
        MessageHolders holders = new MessageHolders()
                .registerContentType(
                        CONTENT_TYPE_VOICE,
                        QIncomingVoiceMessageViewHolder.class,
                        R.layout.q_item_incoming_voice_message,
                        QOutcomingVoiceMessageViewHolder.class,
                        R.layout.q_item_outcoming_voice_message,
                        this);

        holders.registerContentType(
                CONTENT_TYPE_EMOJI,
                QIncomingEmojiMessageViewHolder.class,
                R.layout.q_item_incoming_emoji_message,
                QOutcomingEmojiMessageViewHolder.class,
                R.layout.q_item_outcoming_emoji_message,
                this);

        holders.setIncomingTextConfig(
                QIncomingTextMessageViewHolder.class,
                R.layout.q_item_incoming_text_message)
                .setOutcomingTextConfig(
                        QOutcomingTextMessageViewHolder.class,
                        R.layout.q_item_outcoming_text_message)
                .setIncomingImageConfig(
                        QIncomingImageMessageViewHolder.class,
                        R.layout.q_item_incoming_image_message)
                .setOutcomingImageConfig(
                        QOutcomingImageMessageViewHolder.class,
                        R.layout.q_item_outcoming_image_message);

        messagesAdapter = new MessagesListAdapter<>(senderId, holders, imageLoader);
//        super.messagesAdapter.enableSelectionMode(this);
        messagesAdapter.setLoadMoreListener(this);
        messagesList.setAdapter(messagesAdapter);
        messagesAdapter.setScrollToEndListenner(this);
    }


    @Override
    public void onMessageChanged(Boolean status) {
        Log.d(TAG, "onMessageChanged: ");

        findAllChats();
    }


    @Override
    public boolean hasContentFor(QchatMessage message, byte type) {
        switch (type) {
            case CONTENT_TYPE_VOICE:
                return message.hasVoiceContent();
            case CONTENT_TYPE_EMOJI:
                return message.getEmoji() != null
                        && !TextUtils.isEmpty(message.getEmoji().getTag());
        }
        return false;
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {

    }

    private void registerAIReceiver() {
        IntentFilter intentFilter = new IntentFilter(ACTION_AICORE_WINDOW_SHOWN);
        registerReceiver(mAiUIRecevier, intentFilter);
    }

    private void unRegisterAIReceiver() {
        unregisterReceiver(mAiUIRecevier);
    }


    @Override
    public void scrollToposition(int items) {
        if (items > 0) {
            messagesList.scrollToPosition(items);
        }
    }

    /**
     * 取消自动回到首页方法
     *
     * @param auto true 30s自动回到首页 false 取消自动回到首页
     */
    public void setAutoSwitchLauncher(boolean auto) {
        try {
            AppUtils.setAutoActivityTimeout(getWindow(), auto);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onUploadFinish(int status) {
        final int mStatus = status;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              /*  if (mStatus == -1) {
                    ToastCustom.makeText(getApplicationContext(), "网络开小差了，请再尝试一次", Toast.LENGTH_LONG).show();

                }*/
                sendViewStatus(mStatus);
            }
        });
    }

    private void sendViewStatus(int status) {
        mHandler.removeCallbacksAndMessages(null);
        if (status == 999) {
            sendView.setVisibility(View.VISIBLE);
            sendView.setText("发送中");
        } else if (status == 0) {
            sendView.setText("发送成功");
            mHandler.sendEmptyMessageDelayed(SEND_MESSAGE_TAG, 1000);
            QAILog.d(TAG, "Voice message upload success");
            Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_chatroom_audio_deliveryed);
            Countly.sharedInstance().recordEvent("KidsBuddy", "t_chat_audio");
        } else {
            sendView.setText("网络开小差了，请再尝试一次");
            mHandler.sendEmptyMessageDelayed(SEND_MESSAGE_TAG, 1000);
            Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_chatroom_audio_undeliveryed);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SEND_MESSAGE_TAG) {
                Log.d(TAG, "dispatchMessage: sendView.GONE");
                sendView.setVisibility(View.GONE);
            }
        }
    };
}
