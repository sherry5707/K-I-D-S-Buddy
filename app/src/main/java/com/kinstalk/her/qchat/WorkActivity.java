package com.kinstalk.her.qchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kinstalk.her.qchat.dialog.TaskStarDialog;
import com.kinstalk.her.qchat.homework.HomeWorkVpAdapter;
import com.kinstalk.her.qchat.messaging.XgPushHelper;
import com.kinstalk.her.qchat.utils.CountlyConstant;
import com.kinstalk.her.qchat.utils.SharedPreferencesUtils;
import com.kinstalk.her.qchat.view.ToastCustom;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatapi.fancycoverflow.ScaleTransformer;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.DateUtils;
import com.kinstalk.her.qchatcomm.utils.NetworkUtils;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.model.homeCallback;
import com.kinstalk.her.qchatmodel.Manager.util.UIHelp;
import com.kinstalk.her.qchatmodel.entity.HabitEntity;
import com.kinstalk.her.qchatmodel.entity.HabitKidsEntity;
import com.kinstalk.her.qchatmodel.entity.QHomeworkMessage;
import com.kinstalk.qloveaicore.AIManager;
import com.kinstalk.qloveaicore.TTSListener;
import com.stfalcon.chatkit.messages.MessagesListAdapter;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.kinstalk.util.AppUtils;

import ly.count.android.sdk.Countly;

public class WorkActivity extends AppCompatActivity implements MessagesListAdapter.OnLoadMoreListener,
        homeCallback, TaskStarDialog.OnContentClickListener {

    private static String TAG = WorkActivity.class.getSimpleName();
    //    private TouchableRecyclerView recyclerView;
//    private WorkListAdapter adapter;
    private List<QHomeworkMessage> workMessageList;
    private ImageButton closeButton;
    private TextView noTextView;
    private ViewGroup workBottomView;
    private ViewGroup workRemindView;
    private TextView contentView;
    private ImageView starImage;
    private TextView statusView;
    private TextView finishView;
    private TaskStarDialog taskStarDialog;
    private HabitEntity habitEntity;
    private final BroadcastReceiver updateTimeReceiver = new WorkActivity.UpdateTimeReceiver();
    private boolean receiverUnregistered = false;
    private boolean goHome = true;
    private static final int WORK_SPACE_TIME = 30;
    private static boolean ttsPlaying = false;
    private LinearLayout yetCard;

    private ViewPager viewPager;
    private HomeWorkVpAdapter adapter;

    private BroadcastReceiver UpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_TIME_CHANGED)) {
                getWorkList();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        setContentView(R.layout.activity_work);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(updateTimeReceiver, filter);
        initView();
        initData();

        reloadSPTime();
        if (goHome) {
            setAutoSwitchLauncher(true);
        } else {
            //TODO 30s finish
            setAutoSwitchLauncher(true);
        }
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadSPTime();
        getWorkList();
        UIHelp.sendEnterHomeworkBroadcast(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reloadSPTime();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if(ttsPlaying) {
            playWorkContent(" ");
        }
        unregisterReceiver(UpdateReceiver);
        MessageManager.unRegisterHomeworkCallback(this);
        if (!receiverUnregistered) {
            unregisterReceiver(updateTimeReceiver);
            receiverUnregistered = true;
        }
    }

    public void initView() {
        Log.d(TAG, "initView: ");
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        adapter = new HomeWorkVpAdapter(WorkActivity.this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setPageTransformer(false, new ScaleTransformer());


        workBottomView = (ViewGroup) findViewById(R.id.work_bottom_layout);
        workRemindView = (ViewGroup) findViewById(R.id.work_remind_layout);
        contentView = (TextView) findViewById(R.id.work_content_text);
        starImage = (ImageView) findViewById(R.id.work_content_image);
        statusView = (TextView) findViewById(R.id.work_status_text);
        finishView = (TextView) findViewById(R.id.work_finish_btn);
        yetCard = (LinearLayout) findViewById(R.id.yet_card);
        finishView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: finish");
                clickFinish();
            }
        });


        closeButton = (ImageButton) findViewById(R.id.work_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                if (goHome) {
                    overridePendingTransition(R.anim.fullscreen_enter, R.anim.fullscreen_exit);
                    XgPushHelper.getInstance().startMainActivity();
                }
                Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_homework_turnoff);
            }
        });

        noTextView = (TextView) findViewById(R.id.work_no_view);
    }

    public void initData() {
        Log.d(TAG, "initData: ");
        MessageManager.registerHomeworkCallback(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        registerReceiver(UpdateReceiver, filter);

        goHome = getIntent().getBooleanExtra("goHome", true);

        habitEntity = MessageManager.homeHabitOrNot();
        setInfo(habitEntity);
        getWorkList();
        clearSPTime();
        updateTime();
        checkWorkFinish();
    }

    public void setInfo(HabitEntity habitEntity) {
        if (null != habitEntity) {
            //习惯
            String hhTime = DateUtils.getHHmmTime(habitEntity.getRemind_time());
            setRemindInfo(hhTime, habitEntity.getStatus());
            workBottomView.setVisibility(View.VISIBLE);
        } else {
            //不是习惯
            workBottomView.setVisibility(View.GONE);
        }
    }

    public void setRemindInfo(String hhTime, int status) {
        contentView.setText(hhTime + "前");
        statusView.setText(status + "个");
        Log.d(TAG, "setRemindInfo: ---> time: " + hhTime + "   ---> status: " + status + "个");
    }


    public void getWorkList() {

        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                workMessageList = MessageManager.getAllHomeworkMessage();
                Log.d(TAG, "getWorkList: " + workMessageList);

                runOnUiThread(new Runnable() { //imitation of internet connection
                    @Override
                    public void run() {
                        if (null != workMessageList) {
                            noTextView.setVisibility(workMessageList.size() > 0 ? View.GONE : View.VISIBLE);
                        } else {
                            noTextView.setVisibility(View.VISIBLE);
                        }
                        adapter.refreshData(workMessageList);

                    }
                });
                if(null != workMessageList) {
                    Log.d(TAG, "WorkContent " +workMessageList.get(0).getHomeWorkEntity().getContent());
                    XgPushHelper.getInstance().homeworkACK(workMessageList.get(0).getHomeWorkEntity().getHomeworkid());
                }
            }
        });
    }

    /**
     * 现在时间跟打卡时间比对
     *
     * @return 小于0   时间未过
     */
    public Long diffTime() {
        if (null != habitEntity) {
            String hhMMSS = DateUtils.getHHmmssTime(habitEntity.getRemind_time());
            String yearMD = DateUtils.getYearMonthDay();
            Long time1 = DateUtils.strDateToLong(yearMD + " " + hhMMSS);
            Long curTime = System.currentTimeMillis();
            long diff = curTime - time1;
            if (diff < 0) {
                return -1L;
            }
            Long min = diff / (60 * 1000); //以分钟为单位取整
            return min;
        }
        return Long.valueOf(0);
    }

    /**
     * @return 此时打卡可获得星星数量。
     */
    public int starCount() {
        if (null != habitEntity) {
            Long time = diffTime();
            if (time < 0) {
                //时间未到
                return habitEntity.getStatus();
            }
            //习惯任务设置的星星数量。
            //风无声，沙不语。凝一汪清泉起涟漪.
            int starCount = habitEntity.getStatus();
            //time>=0;
            int i = (int) (time / WORK_SPACE_TIME);
            Log.d(TAG, "time:" + time + "bei:" + i);
            if (i > 0) {
                if ((int) (time % WORK_SPACE_TIME) == 0) {
                    starCount = starCount - i;
                } else
                    starCount = starCount - i - 1;
            } else {
                starCount = starCount - 1;
            }
            if (starCount < 0) {
                starCount = 0;
            }

          /*  for (int i = 1; i < starCount; i++) {
                if (time >= 0 && time <= i * WORK_SPACE_TIME) {
                    starCount = habitEntity.getStatus() - i;
                    Log.d(TAG, "time: " + time + "habitEntity.getStatus() - i: " + starCount);
                    break;
                } else if (time > (habitEntity.getStatus() - 1) * WORK_SPACE_TIME) {
                    starCount = 0;
                    Log.d(TAG, "time: " + time + "habitEntity.getStatus() * WORK_SPACE_TIME: " + habitEntity.getStatus() * WORK_SPACE_TIME);
                    break;
                }
                Log.d(TAG, "time: " + time + "i: " + i);
            }
*/
            Log.d(TAG, "starCount: 当前应获得星星数" + starCount);
            return starCount;
        }
        return habitEntity.getStatus();
    }

    /**
     * 打卡：设置 已打卡界面
     * 未打卡：设置
     * 1、如果可获得星星数量大于0，显示打卡时间
     * 2、星星为0，显示打卡时间已过
     */
    public void updateTime() {
        if (null != habitEntity) {
            int isfinish = UIHelp.checkWorkSignOrNot(this);
            if (isfinish == -1) {
                Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_calendar_clockin);
                //未打卡
                int starCount = starCount();

                Date newDate2 = new Date(habitEntity.getRemind_time() + (long) (habitEntity.getStatus() - starCount) * WORK_SPACE_TIME * 60 * 1000);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                String dateOk = simpleDateFormat.format(newDate2);

                if (starCount != 0) {
                    setRemindInfo(dateOk, starCount);
                } else {
                    //未打卡，但打卡时间已过
                    hiddenStarInfo();
                    setGrayFinishView();
                    if (!receiverUnregistered) {
                        unregisterReceiver(updateTimeReceiver);
                        receiverUnregistered = true;
                    }
                }
            } else {
                Log.d(TAG, "updateTime: setGrayFinishBtn");
//                setGrayFinishView();
                setYetFinishView();
                if (!receiverUnregistered) {
                    unregisterReceiver(updateTimeReceiver);
                    receiverUnregistered = true;
                }
            }
        }
    }

    public void checkWorkFinish() {
        int isfinish = UIHelp.checkWorkSignOrNot(this);
        Log.d(TAG, "checkWorkFinish: " + isfinish);
        if (isfinish == -1) {
            //没有打卡
            updateTime();
        } else {
//            setGrayFinishView();
            setYetFinishView();
        }
    }

    /**
     * 设置打卡 ，打卡时间已过
     */
    public void setGrayFinishView() {
        hiddenStarInfo();
        finishView.setText("打卡时间已过");
        finishView.setTextColor(Color.parseColor("#ffffff"));
        finishView.setBackgroundResource(R.drawable.btn_work_finish_gray_pic);
    }

    /**
     * 设置已打卡--->view
     */
    public void setYetFinishView() {
        hiddenStarInfo();
        finishView.setVisibility(View.GONE);
        yetCard.setVisibility(View.VISIBLE);


    }

    public void hiddenStarInfo() {
        workRemindView.setVisibility(View.GONE);
        contentView.setVisibility(View.GONE);
        statusView.setVisibility(View.GONE);
        starImage.setVisibility(View.GONE);
    }

    /**
     * 打卡-->判断是否已打(UIHelp.checkWorkSignOrNot(this))-->
     * 1、
     */
    public void clickFinish() {
        if (null != habitEntity) {
            HabitKidsEntity habitKidsEntity = MessageManager.getHabitKidsInfo();
            String name = "宝宝";

            if (null != habitKidsEntity) {
                if (!TextUtils.isEmpty(habitKidsEntity.getNick_name())) {
                    name = habitKidsEntity.getNick_name();
                }
            }

            int isfinish = UIHelp.checkWorkSignOrNot(this);
            if (isfinish > 0) {
                //打卡。。。。
                playWorkContent("嗨" + name + "今日打卡时间已过，明天继续坚持哦");
                Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_homework_late);
                Log.d(TAG, "clickFinish: " + isfinish);

            } else {
                int starCount = starCount();
                Log.d(TAG, "clickFinish: " + isfinish + "   startCount:" + starCount);
                if (starCount != 0) {
                    Context mContext = getApplicationContext();
                    if(NetworkUtils.isNetworkAvailable(mContext)) {
                        postHabit(starCount);
                    } else {
                        ToastCustom.makeText(mContext, mContext.getResources().getString(R.string.need_neetwork), Toast.LENGTH_LONG).show();
                    }
                    Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_homework_clockin);
                } else {
                    playWorkContent("嗨" + name + "今日打卡时间已过，明天继续坚持哦");
                    Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_homework_late);
                }
            }
        }
    }

    public void playWorkContent(String content) {
        try {
            AIManager.getInstance(getApplicationContext()).playTextWithStr(content, mCommonTTSCb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static TTSListener mCommonTTSCb = new TTSListener() {

        @Override
        public void onTTSPlayBegin(String s) {
            ttsPlaying = true;
        }

        @Override
        public void onTTSPlayEnd(String s) {
            Log.d(TAG, "onTTSPlayEnd");
            ttsPlaying = false;
        }

        @Override
        public void onTTSPlayProgress(String s, int i) {

        }

        @Override
        public void onTTSPlayError(String s, int i, String s1) {
            Log.d(TAG, "onTTSPlayError");
        }
    };

    public void postHabit(final int starCount) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean rec = Api.postHabit(getApplicationContext(), StringEncryption.generateToken(), QAIConfig.getMacForSn(), 5
                        , starCount);
                WorkActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (null != taskStarDialog && taskStarDialog.isShowing()) {

                        } else {
                            taskStarDialog = new TaskStarDialog.Builder(QchatApplication.getInstance())
                                    .setOnContentClickListener(WorkActivity.this)
                                    .create();
                            taskStarDialog.show();
                        }

                        if (rec) {
                            taskStarDialog.showSuccess(starCount);
                            playWorkContent("你真是个好孩子，我为你感到骄傲");
                            setYetFinishView();
                        } else {
                            taskStarDialog.showFail();
                        }

                    }
                });
            }
        }).start();
    }

    public void reloadSPTime() {
        SharedPreferencesUtils.setSharedPreferencesWithLong(this, SharedPreferencesUtils.WORK_TIME, SharedPreferencesUtils.WORK_ALERT_TIME, (workMessageList != null && workMessageList.size() >0)? workMessageList.get(0).getHomeWorkEntity().assign_time:System.currentTimeMillis());
    }

    public void clearSPTime() {
        SharedPreferencesUtils.clearSharedPreferences(this, SharedPreferencesUtils.WORK_TIME);
    }

    @Override
    public void onHomeworkChanged(Boolean status, List<QHomeworkMessage> workMsg) {
        getWorkList();
/*        if (status) {
            if (!QchatApplication.isWakeupActive() || !QchatApplication.isSleepingActive()) {
                //非起床睡觉播放故事
                RingPlayer.getInstance().startRing();
            }
        }
        */
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {

    }

    @Override
    public void onClickTaskStarDialog() {
        Log.d(TAG, "onClickTaskStarDialog: ");
    }

    @Override
    public void onClickTaskStarClose() {
        Log.d(TAG, "onClickTaskStarClose: ");
    }

    @Override
    public void onClickTaskStarRetry() {
        Log.d(TAG, "onClickTaskStarRetry: ");
        clickFinish();
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

}
