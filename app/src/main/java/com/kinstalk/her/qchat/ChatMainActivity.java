package com.kinstalk.her.qchat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kinstalk.her.qchat.common.UIHelper;
import com.kinstalk.her.qchat.homework.GuidePageDialog;
import com.kinstalk.her.qchat.dialog.TaskStarDialog;
import com.kinstalk.her.qchat.remind.RemindFragment;
import com.kinstalk.her.qchat.homework.WorkFragment;
import com.kinstalk.her.qchat.fragment.NewMessageCallback;
import com.kinstalk.her.qchat.frags.ChatListFrag;
import com.kinstalk.her.qchat.homework.WorkAlertActivity;
import com.kinstalk.her.qchat.messaging.PrivacyManager;
import com.kinstalk.her.qchat.messaging.XgPushHelper;
import com.kinstalk.her.qchat.utils.CountlyUtils;
import com.kinstalk.her.qchat.dialog.ChatAlertDialog;
import com.kinstalk.her.qchat.utils.EventState;
import com.kinstalk.her.qchat.widget.QRecorderButton;
import com.kinstalk.her.qchat.widget.QViewPager;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatapi.aws.AWSTransferHelper;
import com.kinstalk.her.qchatcomm.base.BaseApplication;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatmodel.ChatProviderHelper;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.util.WorkAlertInfo;
import com.kinstalk.her.qchatmodel.entity.WxUserEntity;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.kinstalk.util.AppUtils;

import io.github.rockerhieu.emojicon.EmojiconTextView;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import ly.count.android.sdk.Countly;

public class ChatMainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener,
        QRecorderButton.RecorderFinishListener, NewMessageCallback, ChatAlertDialog.OnContentClickListener,
        AWSTransferHelper.UploadFinishListener, TaskStarDialog.OnContentClickListener {

    private static final String TAG = ChatMainActivity.class.getSimpleName();
    public static String TARGET_TAB_EXTRA_KEY = "QCHAT_TARGET_TAB";
    private final int SEND_MESSAGE_TAG = 1;
    private QViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private WorkFragment workFragment = new WorkFragment();
    private RemindFragment remindFragment = new RemindFragment();
    private ChatListFrag chatFrag = new ChatListFrag();
    private final BroadcastReceiver updateTimeReceiver = new UpdateTimeReceiver();

    private RadioGroup inputEntrance;
    private RadioButton radioWork;
    private RadioButton radioRemind;
    private RadioButton radioChat;
    private QRecorderButton radioVoice;
    private TextView sendView;

    private static final int TAB_INDEX_HOMEWORK = 0;
    private static final int TAB_INDEX_REMIND = 1;
    private static final int TAB_INDEX_CHAT = 2;
    private int currentIndex;
    private int chatUnreadCount;
    private Drawable workUnreadView;
    private Drawable remindUnreadView;
    private Drawable chatUnreadView;

    private TextView dateView;
    private boolean isForeground;

    private ChatAlertDialog chatAlertDialog;
    private TaskStarDialog taskStarDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory(LayoutInflater.from(this), new LayoutInflaterFactory() {
            @Override
            public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
                if (name.equals("TextView")) {
                    return new EmojiconTextView(context, attrs);
                }
                return null;
            }
        });

        Log.d(TAG, "onCreate: this :" + this + "    taskID:   " + getTaskId());
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_main);
        setAutoSwitchLauncher(false);
        initViews();
        guideShow();
        setListeners();
        setViewPagerListeners();
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                List<WxUserEntity> records = ChatProviderHelper.WxUser.getWxUserEntities(null, null);
                if (records != null && records.size() > 0) {
                    for (WxUserEntity entity : records) {
                        if (entity != null && !TextUtils.isEmpty(entity.openid)) {
                            ChatProviderHelper.mWxUserMap.put(entity.openid, entity);
                        }
                    }
                }
            }
        });

        showWorkUnreadView(false);
        showRemindUnreadView(false);
        showChatUnreadView(false);
        isForeground = true;
        clearChatUnreadCount();
        PrivacyManager.getInstance(this);
        // 拉取作业列表
        Api.fetchHomework(BaseApplication.mApplication.getApplicationContext(),
                StringEncryption.generateToken(), QAIConfig.getMacForSn(), Api.UP_MSG_TYPE_HOMEWORK, "", "");

    }

    @Override
    protected void onPause() {
        isForeground = false;
        radioVoice.resetToNormal();
        Log.d(TAG, "onPause:foreground false ");
        super.onPause();

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        Countly.sharedInstance().onStop();

        super.onStop();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
        Countly.sharedInstance().onStart(CountlyUtils.skillName, this);
    }

    @Override
    protected void onResume() {
        isForeground = true;
        Log.d(TAG, "onResume: foreground true");
        super.onResume();
        updateTime();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        QAILog.d(TAG, "onNewIntent " + intent);
        if (intent.hasExtra(TARGET_TAB_EXTRA_KEY)) {
            int extra = intent.getIntExtra(TARGET_TAB_EXTRA_KEY, 0);
            switch (extra) {
                case 0:
                    radioWork.setChecked(true);
                    workFragment.scrollToTop();
                    break;
                case 1:
                    radioRemind.setChecked(true);
                    break;
                case 2:
                    radioChat.setChecked(true);
                    clearChatUnreadCount();
                    break;
            }
        } else if (currentIndex == TAB_INDEX_CHAT) {
            clearChatUnreadCount();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        isForeground = false;
        workFragment.unSetNewMessageCallback();
        chatFrag.unSetNewMessageCallback();
        unregisterReceiver(updateTimeReceiver);
    }

    private void initViews() {
        Log.d(TAG, "initViews: ");
        mViewPager = (QViewPager) findViewById(R.id.viewpager);
        inputEntrance = (RadioGroup) findViewById(R.id.entrance_board);
        radioWork = (RadioButton) findViewById(R.id.work_button);
        radioRemind = (RadioButton) findViewById(R.id.remind_button);
        radioChat = (RadioButton) findViewById(R.id.chat_button);
        radioVoice = (QRecorderButton) findViewById(R.id.voiceButton);

        dateView = (TextView) findViewById(R.id.chat_time);
        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTaskStarDialog();
            }
        });

        sendView = (TextView) findViewById(R.id.send_message_text);
        sendView.setOnClickListener(null);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(updateTimeReceiver, filter);
    }

    private class UpdateTimeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_TIME_TICK) ||
                    TextUtils.equals(intent.getAction(), Intent.ACTION_TIME_CHANGED)) {
                updateTime();
            }
        }
    }

    private void updateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String dateString = dateFormat.format(new Date());
        dateView.setText(dateString);

        EventBus.getDefault().post(EventState.EVENT_STATE_UPDATE_TIME);
    }

    private void setListeners() {
        radioVoice.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                QAILog.d(TAG, "onLongClick");
                permissionGen();
                return false;
            }
        });
        radioVoice.setRecorderFinishListener(this);
        AWSTransferHelper.setUploadFinishListener(this);
        inputEntrance.setOnCheckedChangeListener(this);
        inputEntrance.check(0);

        workFragment = new WorkFragment();
        remindFragment = new RemindFragment();
        chatFrag = new ChatListFrag();

        // 作业tab
        workFragment.setNewMessageCallback(this);
        mFragments.add(workFragment);
        LayerDrawable workLayers = (LayerDrawable) radioWork.getBackground();
        workUnreadView = workLayers.findDrawableByLayerId(R.id.red_point);

        // 提醒tab
        remindFragment.setNewMessageCallback(this);
        mFragments.add(remindFragment);
        LayerDrawable remindLayers = (LayerDrawable) radioRemind.getBackground();
        remindUnreadView = remindLayers.findDrawableByLayerId(R.id.red_point);

        // 聊天tab，包括表情和语音
        chatFrag.setNewMessageCallback(this);
        mFragments.add(chatFrag);
        LayerDrawable chatLayers = (LayerDrawable) radioChat.getBackground();
        chatUnreadView = chatLayers.findDrawableByLayerId(R.id.red_point);
    }

    private void setViewPagerListeners() {
        mViewPager.setOffscreenPageLimit(2);
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public Fragment getItem(int arg0) {
                return mFragments.get(arg0);
            }
        };

        mViewPager.setAdapter(mAdapter);
        mViewPager.setCanScroll(false);

        int extra = getIntent().getIntExtra(TARGET_TAB_EXTRA_KEY, 0);
        switch (extra) {
            case 0:
                radioWork.setChecked(true);
                break;
            case 1:
                radioRemind.setChecked(true);
                break;
            case 2:
                radioChat.setChecked(true);
                clearChatUnreadCount();
                break;
        }
    }

    private void setShowVoiceButton(boolean show) {
        if (show) {
            radioVoice.setVisibility(View.VISIBLE);
            radioChat.setVisibility(View.GONE);
        } else {
            radioVoice.setVisibility(View.GONE);
            radioChat.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {
        if (id == R.id.work_button) {
            onWorkEnter();
            showWorkUnreadView(false);
            this.setShowVoiceButton(false);
            currentIndex = TAB_INDEX_HOMEWORK;
            Countly.sharedInstance().recordEvent("KidsBuddy", "t_chat_homework");
            workFragment.notifyRead();
        } else if (id == R.id.remind_button) {
            onRemindEnter();
            showRemindUnreadView(false);
            this.setShowVoiceButton(false);
            currentIndex = TAB_INDEX_REMIND;
            Countly.sharedInstance().recordEvent("KidsBuddy", "t_chat_reminder");
        } else if (id == R.id.chat_button) {
            onChatEnter();
            showChatUnreadView(false);
            this.setShowVoiceButton(true);
            currentIndex = TAB_INDEX_CHAT;
            clearChatUnreadCount();
        }
    }

    //    @Override
    public void onWorkEnter() {

        if (currentIndex != TAB_INDEX_HOMEWORK) {
            mViewPager.setCurrentItem(TAB_INDEX_HOMEWORK, false);
        }
    }

    //    @Override
    public void onRemindEnter() {
            /*RemindDialogFrag dialog = new RemindDialogFrag();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            dialog.show(ft, "remind_dialog");*/
        if (currentIndex != TAB_INDEX_REMIND) {
            mViewPager.setCurrentItem(TAB_INDEX_REMIND, false);
        }
    }

    //    @Override
    public void onChatEnter() {
        if (currentIndex != TAB_INDEX_CHAT) {
            mViewPager.setCurrentItem(TAB_INDEX_CHAT, false);
        }
    }

    /**
     * 是否显示作业红点
     */
    private void showWorkUnreadView(boolean isShow) {
        if (workUnreadView != null) {
            workUnreadView.setAlpha(isShow ? 255 : 0);
        }
    }

    /**
     * 是否显示提醒红点
     */
    private void showRemindUnreadView(boolean isShow) {
        if (remindUnreadView != null)
            remindUnreadView.setAlpha(isShow ? 255 : 0);
    }

    /**
     * 是否显示聊天红点
     */
    private void showChatUnreadView(boolean isShow) {
        if (chatUnreadView != null)
            chatUnreadView.setAlpha(isShow ? 255 : 0);
    }

    /**
     * 语音
     */

    private static final int REQ_RECORD_SUCCESS_CODE = 0;

    private void permissionGen() {
        //处理需要动态申请的权限
        PermissionGen.with(ChatMainActivity.this)
                .addRequestCode(REQ_RECORD_SUCCESS_CODE)
                .permissions(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .request();
    }

    // 申请权限结果的返回
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
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
        Toast.makeText(ChatMainActivity.this
                , getText(R.string.req_record_permission_failed)
                , Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onChatRecordFinish(final float seconds, final String filePath, final String fileName) {

        /*if (PrivacyManager.getInstance(this).isPrivacy()) {
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
                MessageManager.saveOutcomingMsg(BaseApplication.getInstance().getApplicationContext(), "", filePath, fileName, ChatProviderHelper.Chat.MSG_TYPE_VOICE, Float.valueOf(seconds).intValue());
            }
        });
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

    private void sendViewStatus(int status) {
        mHandler.removeCallbacksAndMessages(null);
        if (status == 999) {
            sendView.setVisibility(View.VISIBLE);
            sendView.setText("发送中");
        } else if (status == 0) {
            sendView.setText("发送成功");
            mHandler.sendEmptyMessageDelayed(SEND_MESSAGE_TAG, 1000);
            QAILog.d(TAG, "Voice message upload success");
            Countly.sharedInstance().recordEvent("KidsBuddy", "t_chat_audio");
        } else {
            sendView.setText("网络开小差了，请再尝试一次");
            mHandler.sendEmptyMessageDelayed(SEND_MESSAGE_TAG, 1000);
        }
    }

    @Override
    public void onUploadFinish(int status) {
        final int mStatus = status;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendViewStatus(mStatus);
            }
        });
    }

    @Override
    public void receiverNewMessage(String className, String content) {

        Log.d(TAG, "receiverNewMessage: " + className + " currentIndex: " + currentIndex + "  currentThread()  +  " + Thread.currentThread());

        if (currentIndex == TAB_INDEX_HOMEWORK) {
            workFragment.notifyRead();
        }

        if (TextUtils.equals(className, WorkFragment.class.getSimpleName()) && currentIndex != TAB_INDEX_HOMEWORK) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showWorkUnreadView(true);
                }
            });
        } else if (TextUtils.equals(className, ChatListFrag.class.getSimpleName()) && currentIndex != TAB_INDEX_CHAT) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showChatUnreadView(true);
                }
            });
        }

        if (!isForeground && !UIHelper.isTopActivity2("com.kinstalk.her.settings")) {
            if (TextUtils.equals(className, WorkFragment.class.getSimpleName())) {
                showWorkAlert(content);
            } else if (TextUtils.equals(className, ChatListFrag.class.getSimpleName())) {
                showChatAlert(content);
            }
        }
        UIHelper.wakeUp(this.getApplicationContext());
    }

    public void showWorkAlert(String content) {
        Log.d(TAG, "showWorkAlert: content" + content);

        WorkAlertInfo workAlertInfo = new WorkAlertInfo();
        workAlertInfo.setWorkString(content);

        Intent i = new Intent(this, WorkAlertActivity.class);
        i.putExtra("WorkContent", workAlertInfo);
        this.startActivity(i);
    }

    public void showChatAlert(String name) {
        Log.d(TAG, "showChatAlert: ");
        final String string;
        if (TextUtils.isEmpty(name)) {
            string = "收到" + addChatUnreadCount() + "条消息";
            ;
        } else {
            string = name + "发来了" + addChatUnreadCount() + "条消息";
        }

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != chatAlertDialog && chatAlertDialog.isShowing()) {

                } else {
                    chatAlertDialog = new ChatAlertDialog.Builder(QchatApplication.getInstance())
                            .setOnContentClickListener(ChatMainActivity.this)
                            .create();
                    chatAlertDialog.show();
                }
                chatAlertDialog.setContent(string);
            }
        });
    }

    public void showTaskStarDialog() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != taskStarDialog && taskStarDialog.isShowing()) {

                } else {
                    taskStarDialog = new TaskStarDialog.Builder(QchatApplication.getInstance())
                            .setOnContentClickListener(ChatMainActivity.this)
                            .create();
                    taskStarDialog.show();
                }
                taskStarDialog.showSuccess(3);
            }
        });
    }

    public void clearChatUnreadCount() {
        chatUnreadCount = 0;
    }

    public int addChatUnreadCount() {
        return ++chatUnreadCount;
    }

    @Override
    public void onClickChatAlert() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.putExtra(TARGET_TAB_EXTRA_KEY, 2);
        this.startActivity(intent);
    }

    @Override
    public void onClickTaskStarDialog() {
        Log.d(TAG, "onClickTaskStarDialog: ");
    }

    @Override
    public void onClickTaskStarClose() {

    }

    @Override
    public void onClickTaskStarRetry() {

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

    public void guideShow() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int currentVersion = info.versionCode;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        int lastVersion = sp.getInt("VERSION_KEY", 0);

        if (currentVersion > lastVersion) {
            sp.edit().putInt("VERSION_KEY", currentVersion).commit();
            GuidePageDialog guidePageDialog = new GuidePageDialog(this);
            guidePageDialog.show();
        }
    }

    float x1 = 0, y1 = 0;
    float x2 = 0, y2 = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("q12345", "onTouchEvent");
        //继承了Activity的onTouchEvent方法，直接监听点击事件
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下的时候
            x1 = event.getX();
            y1 = event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //当手指离开的时候
            x2 = event.getX();
            y2 = event.getY();
            if (y1 - y2 > 50) {
                //向上滑
                Log.d("HerSettingsActivity", "a22417 up");
            } else if (y2 - y1 > 50) {
                //向下滑
                Log.d("HerSettingsActivity", "a22417 down");
            } else if (x1 - x2 > 50) {
                //向左滑
                Log.d("HerSettingsActivity", "a22417 left");
                Intent openAllAppIntent = new Intent();
                openAllAppIntent.setPackage("com.kinstalk.her.setupwizard");
                openAllAppIntent.setClassName("com.kinstalk.her.setupwizard", "com.kinstalk.her.setupwizard.allapp.AllAPPList");
                startActivity(openAllAppIntent);
            } else if (x2 - x1 > 50) {
                //向右滑
                Log.d("HerSettingsActivity", "a22417 right");
            }
        }
        return super.onTouchEvent(event);
    }

}
