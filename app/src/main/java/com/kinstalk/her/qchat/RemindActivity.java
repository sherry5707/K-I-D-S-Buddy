package com.kinstalk.her.qchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinstalk.her.qchat.messaging.XgPushHelper;
import com.kinstalk.her.qchat.remind.RemindListAdapter;
import com.kinstalk.her.qchat.utils.CountlyConstant;
import com.kinstalk.her.qchatapi.fancycoverflow.ScaleTransformer;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.model.reminderCallback;
import com.kinstalk.her.qchatmodel.Manager.model.starCallback;
import com.kinstalk.her.qchatmodel.entity.HabitKidsEntity;
import com.kinstalk.her.qchatmodel.entity.QReminderMessage;
import com.kinstalk.qloveaicore.AIManager;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ly.count.android.sdk.Countly;

public class RemindActivity extends AppCompatActivity implements MessagesListAdapter.OnLoadMoreListener,
        reminderCallback, starCallback, RemindListAdapter.OnDeleteListener {

    private String TAG = RemindActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private RemindListAdapter adapter;
    private ImageButton closeButton;
    private TextView noTextView;
    private TextView titleView;
    private ImageView line;
    private TextView mStarNum;
    private boolean   mXiaoWei = false;
    private static List<QReminderMessage> reminderMessageList = new ArrayList<>();
    private static List<QReminderMessage> todoDataList = new ArrayList<>();
    private static List<QReminderMessage> doneDataList = new ArrayList<>();
    private String[] ttsArray = new String[]{"看看接下来要做什么", "每天坚持，你是最棒的"};

    private BroadcastReceiver UpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_TIME_CHANGED)) {
                getRemindList(context);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_remind_page);
        MessageManager.readOutKidsInfo();
        mXiaoWei = getIntent().getBooleanExtra("xiaowei", false);
        if(!mXiaoWei)
            playTTSRandom();
        initView();
        initData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mXiaoWei = getIntent().getBooleanExtra("xiaowei", false);
  //      getRemindList(this);
        if(!mXiaoWei)
            playTTSRandom();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        getRemindList(this);
        Countly.sharedInstance().recordEvent("KidsBuddy","t_view_reminders_list");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageManager.unRegisterReminderCallback(this);
        MessageManager.unRegisterStarCallback(this);
        unregisterReceiver(UpdateReceiver);
    }

    public void initData() {
        Log.d(TAG, "initData: ");
        MessageManager.registerReminderCallback(this);
        MessageManager.registerStarCallback(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        registerReceiver(UpdateReceiver, filter);
 //       getRemindList(this);
    }

    public void initView() {
        Log.d("RemindListAdapter", "initView: ");
        recyclerView = (RecyclerView) findViewById(R.id.viewpager);
        LinearLayoutManager ms= new LinearLayoutManager(this);
        ms.setOrientation(LinearLayoutManager.HORIZONTAL);// 设置 recyclerview 布局方式为横向布局
        recyclerView.setLayoutManager(ms);
        adapter = new RemindListAdapter(recyclerView.getContext(), recyclerView);
        recyclerView.setAdapter(adapter);

        closeButton = (ImageButton) findViewById(R.id.remind_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE,CountlyConstant.t_calendar_turnoff);
            }
        });

        noTextView = (TextView) findViewById(R.id.remind_no_view);
        mStarNum = (TextView) findViewById(R.id.star_num);
        titleView = (TextView) findViewById(R.id.title_text);
        line = (ImageView) findViewById(R.id.line);
        HabitKidsEntity habitEntity = MessageManager.getHabitKidsInfo();
        if(null != habitEntity) {
            mStarNum.setText(Integer.toString(habitEntity.all_star));
            titleView.setText(TextUtils.isEmpty(habitEntity.getNick_name())?"宝宝":habitEntity.getNick_name() +"的一天");

        }
    }

    public void startActivity() {
        Intent intent = new Intent();
        intent.setClass(this, RemindCustomActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(intent);
    }

    private void showRemindList(boolean after) {
        if(after) {
            if (null != reminderMessageList) {
                int mSize =  reminderMessageList.size();
                Log.d(TAG, "remind list： " + mSize);
                noTextView.setVisibility(reminderMessageList.size() > 0 ? View.GONE : View.VISIBLE);
                line.setVisibility(mSize > 0 ? View.VISIBLE : View.GONE);
            } else {
                Log.d(TAG, "real null list");
                noTextView.setVisibility(View.VISIBLE);
                line.setVisibility(View.GONE);
            }
        } else {
            noTextView.setVisibility(View.GONE);
        }
        if (adapter != null) {
            adapter.refreshData(todoDataList, doneDataList);
        }
    }

    public void getRemindList(final Context context) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showRemindList(false);
            }
        });
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                reminderMessageList.clear();

                todoDataList = MessageManager.getTodayReminderMessage(context, 1);
                doneDataList = MessageManager.getTodayReminderMessage(context, 2);
                if (null != todoDataList && todoDataList.size() > 0) {
                    int size = todoDataList.size();
                    for(int i = 0; i < size; i++) {
                        if(!reminderMessageList.contains(todoDataList.get(i)))
                            reminderMessageList.add(todoDataList.get(i));
                    }
                }
                if (null != doneDataList && doneDataList.size() > 0) {
                    int size = doneDataList.size();
                    for(int i = 0; i < size; i++) {
                        if(!reminderMessageList.contains(doneDataList.get(i)))
                            reminderMessageList.add(doneDataList.get(i));
                    }
                }
                Log.d(TAG, "todoDataList: " + todoDataList.size());
                Log.d(TAG, "doneDataList: " + doneDataList.size());
                Log.d(TAG, "getRemindList: " + reminderMessageList.size());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showRemindList(true);
                    }
                });
            }
        });
    }

    public void playTTSRandom() {
        Random random = new Random();
        int num = random.nextInt(ttsArray.length);
        String ttsString = ttsArray[num];
        if (!TextUtils.isEmpty(ttsString)) {
            playTTSWithContent(ttsString);
        }
    }

    public void playTTSWithContent(String content) {
        try {
            Log.d(TAG, "playTTS");
            AIManager.getInstance(getApplicationContext()).playTextWithStr(content, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReminderChanged(Boolean status) {
        Log.d(TAG, "onReminderChanged");
        getRemindList(this);
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {

    }

    @Override
    public void onDelete(int index) {
        reminderMessageList.remove(index);
        //todo here is wrong
        adapter.refreshData(reminderMessageList, null);
    }

    @Override
    public void onStarChanged(final int stars) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStarNum.setText(Integer.toString(stars));
                HabitKidsEntity habitEntity = MessageManager.getHabitKidsInfo();
                if(null != habitEntity) {
                    titleView.setText(TextUtils.isEmpty(habitEntity.getNick_name())?"宝宝":habitEntity.getNick_name() +"的一天");

                }
            }
        });
    }
}
