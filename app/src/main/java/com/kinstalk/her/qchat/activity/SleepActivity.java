package com.kinstalk.her.qchat.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterViewFlipper;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.progressbar.AnimationState;
import com.kinstalk.her.qchat.progressbar.AnimationStateChangedListener;
import com.kinstalk.her.qchat.progressbar.CircleProgressView;
import com.kinstalk.her.qchat.progressbar.TextMode;
import com.kinstalk.her.qchat.utils.LogUtil;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import ly.count.android.sdk.Countly;

public class SleepActivity extends AIBaseActivity {

    private List<View> sceneViewList = new ArrayList<View>();
    private AdapterViewFlipper viewFilpper;
    private CircleProgressView circleView;
    private ImageView timerView;
    private AnimationDrawable mTimerAnimation;
    private ImageView timerSignBtn;
    private AnimationDrawable mTimerBtnAnimation;
    private ImageView signTimeoutImageView;
    private TextView signTimeoutTextView;
    private ImageView moonImageView;
    private ImageView butterflyImageView;
    private ImageView tentImageView;
    private ImageView storyImageView;
    private TextView signTextView;
    private ImageView joinSleepImageView;
    private Button signButton;

    private int nStarCount;
    private String strBabyName;


    private View bgView;
    private ArrayList<ImageView> starViewList = new ArrayList<ImageView>();

    private final int HANDLE_MSG_NEXT_SCENE = 0x101;
    private final int HANDLE_MSG_PLAY_STORY = 0x102;
    private final int HANDLE_MSG_TIME_OUT = 0x103;
    private final int HANDLE_MSG_TIMER_LAST = 0x104;
    private boolean bStartByPlayStory = false;

    private static final String TAG = "SleepActivity";

    /**
     * 打卡方式，0：手动；1：语音。
     */
    private int clockMode = 0;

    /**
     * 为了在习惯被删除时finish流程
     */
    private static final String FINISH_ACTION = "sleep_finish";
    private BroadcastReceiver finishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    public static void actionStart(Context context, long time, String nickName, int star) {
        Intent intent = new Intent(context, SleepActivity.class);
        intent.putExtra("alarm_time", time);
        intent.putExtra("nick_name", nickName);
        intent.putExtra("star_1", star);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(intent);
        Log.d(TAG, " actionStart(Context context, long time, String nickName, int star_1) ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);
        QchatApplication.getApplicationInstance().setSleepActive(true);

        startWakeUp();
        createSubView();
        registerReceiver();
        Log.d(TAG, "onCreate(Bundle savedInstanceState)");
    }

    private void registerReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FINISH_ACTION);
        registerReceiver(finishReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nHander.removeCallbacksAndMessages(null);
        stopStories();
        unregisterReceiver(finishReceiver);
        QchatApplication.getApplicationInstance().setSleepActive(false);
    }


    @Override
    protected void getParamsByIntent() {

        Intent itent = this.getIntent();
        strBabyName = itent.getStringExtra("nick_name");
        if (TextUtils.isEmpty(strBabyName)) {
            strBabyName = "宝宝";
        }

        nStarCount = itent.getIntExtra("star_1", 3);
        long alarmTime = itent.getLongExtra("alarm_time", 0);

        if (alarmTime == -1) {
            bStartByPlayStory = true;
            apiSignedSucess = true;
        }
    }

    private void createSubView() {
        timerView = (ImageView) findViewById(R.id.sleep_timer);
        timerSignBtn = (ImageView) findViewById(R.id.sleep_timer_sign_btn);
        circleView = (CircleProgressView) findViewById(R.id.circleView);
        circleView.setUnitVisible(false);
        circleView.setSpinSpeed(4.5f);
        circleView.setMaxValue(60);
        circleView.setValue(2);
        circleView.setTextSize(44);
        circleView.setText("打卡");
        circleView.setTextMode(TextMode.TEXT); // show text while spinning
        circleView.setOnAnimationStateChangedListener(
                new AnimationStateChangedListener() {
                    @Override
                    public void onAnimationStateChanged(AnimationState _animationState) {
                        switch (_animationState) {
                            case IDLE:
                            case ANIMATING:
                            case START_ANIMATING_AFTER_SPINNING:
                                circleView.setText("打卡");
                                break;
                            case SPINNING:
                                circleView.setText("打卡中...");
                            case END_SPINNING:
                                break;
                            case END_SPINNING_START_ANIMATING:
                                break;

                        }
                    }
                }
        );

        bgView = findViewById(R.id.bgView);
        moonImageView = (ImageView) findViewById(R.id.moonImageView);
        moonImageView.setImageResource(R.mipmap.sleep_moon);
        butterflyImageView = (ImageView) findViewById(R.id.butterflyImageView);
        tentImageView = (ImageView) findViewById(R.id.tentImageView);
        storyImageView = (ImageView) findViewById(R.id.storyImageView);
        signTimeoutImageView = (ImageView) findViewById(R.id.signTimeoutImage);
        signTimeoutTextView = (TextView) findViewById(R.id.signTimeoutText);
        joinSleepImageView = (ImageView) findViewById(R.id.joinSleepImageView);
        signButton = (Button) findViewById(R.id.signButton);

        signTextView = (TextView) findViewById(R.id.signTextView);
        starViewList.add((ImageView) findViewById(R.id.startImageView3));
        starViewList.add((ImageView) findViewById(R.id.startImageView2));
        starViewList.add((ImageView) findViewById(R.id.startImageView1));


        viewFilpper = (AdapterViewFlipper) findViewById(R.id.viewpager);
        viewFilpper.setInAnimation(this, R.animator.push_in);
        viewFilpper.setOutAnimation(this, R.animator.push_out);
        viewFilpper.setFlipInterval(10 * 1000);

        ImageView sceneImageView1 = createImageView(R.mipmap.sleep_xiaowei_talk1);
        ImageView sceneImageView2 = createImageView(R.mipmap.sleep_xiaowei_talk2);
        ImageView sceneImageView3 = createImageView(R.mipmap.sleep_xiaowei_talk3);
        ImageView sceneImageView4 = createImageView(R.mipmap.sleep_xiaowei_talk4);
        ImageView sceneImageView5 = createImageView(R.mipmap.sleep_xiaowei_talk5);

        sceneViewList.add(sceneImageView1);
        sceneViewList.add(sceneImageView2);
        sceneViewList.add(sceneImageView3);
        sceneViewList.add(sceneImageView4);
        sceneViewList.add(sceneImageView5);


        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return sceneViewList.size();
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ImageView imageView = (ImageView) sceneViewList.get(position);
                return imageView;
            }
        };

        viewFilpper.setAdapter(adapter);
        viewFilpper.startFlipping();

        new Handler().postDelayed(new Runnable() { //imitation of internet connection
            @Override
            public void run() {
                viewFilpper.stopFlipping();
            }
        }, 45 * 1000);

    }

    private void updateStarCount(int count) {
        if (nStarCount == 2) {
            String[] stringArray = getResources().getStringArray(R.array.wake_up_two_star);
            playTtsText(strBabyName + "，" + stringArray[getRadomStringTts(stringArray.length)]);
        } else if (nStarCount == 1) {
            String[] stringArray = getResources().getStringArray(R.array.wake_up_one_star);
            playTtsText(strBabyName + "，" + stringArray[getRadomStringTts(stringArray.length)]);
        }
        for (int i = 0; i < starViewList.size() - count; i++) {


            final ImageView imageView = starViewList.get(i);

            if (imageView.getVisibility() == View.INVISIBLE)
                continue;

            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.f);
            alphaAnimation.setDuration(300);
            alphaAnimation.setRepeatCount(0);//设置重复次数
            alphaAnimation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
            alphaAnimation.setStartOffset(0);//执行前的等待时间
            alphaAnimation.setInterpolator(new DecelerateInterpolator());
            imageView.startAnimation(alphaAnimation);

            alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imageView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    protected void onResume() {
        super.onResume();
        if (!bStartByPlayStory) {
            switchScene(0);
        } else {
            if (QchatApplication.isSleepingActive() && storyPlaying()) {
                LogUtil.d(TAG,"onResume,resumeStories");
                resumeStories();
                return;
            } else if (isVoiceStopStory()) {
                LogUtil.d(TAG, "onResume: story not playing");
                return;
            }
            startStoriesScene();
        }
    }

    @Override
    protected void switchScene(final int sceneIndex) {

        if (!apiSignedSucess) {
            switch (sceneIndex) {
                case 0:
                    startFirstScene();
                    break;
                case 1:
                    startSecondScene();
                    break;
                case 2:
                    startThreeScene();
                    break;
            }
        }
    }

    private void startStoriesScene() {
        //TODO 讲个故事
        Log.d(TAG, "startStoriesScene");

        nHander.removeMessages(HANDLE_MSG_TIME_OUT);
        nHander.removeMessages(HANDLE_MSG_NEXT_SCENE);
        nHander.removeMessages(HANDLE_MSG_PLAY_STORY);

        if (bStartByPlayStory) {
            Log.i(TAG, "startStoriesScene: delay");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    playTtsText("  ");
                    playStories("播放儿童睡前故事");
                }
            }, 4000);
        } else {
            Log.i(TAG, "startStoriesScene: normal");
            playTtsText("  ");
            playStories("播放儿童睡前故事");
        }

        moonImageView.setImageResource(R.mipmap.sleep_moon_sleep);
        butterflyImageView.setVisibility(View.VISIBLE);
        tentImageView.setVisibility(View.VISIBLE);
        signTextView.setVisibility(View.INVISIBLE);
        viewFilpper.setVisibility(View.INVISIBLE);
        storyImageView.setVisibility(View.VISIBLE);
        signButton.setEnabled(false);

        circleView.setVisibility(View.INVISIBLE);
        timerView.setVisibility(View.INVISIBLE);
        timerSignBtn.setVisibility(View.INVISIBLE);
        updateStarCount(0);

        startGotoSleep();
        setAlarm(AIConstants.SLEEP_ALARM_ACTION, 10);

    }


    private void startGotoSleep() {
        Log.d(TAG, "startGotoSleep");

        WindowManager.LayoutParams lp = SleepActivity.this.getWindow().getAttributes();
        ValueAnimator anim = ValueAnimator.ofFloat(lp.screenBrightness, 0.0f);
        anim.setDuration(4000);
        anim.setStartDelay(0);
        anim.setRepeatCount(0);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setLight(SleepActivity.this, (Float) animation.getAnimatedValue());
            }
        });
        anim.start();
    }

    private void setLight(Activity context, float brightness) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.screenBrightness = -1 * brightness * 255.0f;
        context.getWindow().setAttributes(lp);
    }

    private int getRadomStringTts(int length) {
        double random = Math.random();
        int index = (int) (random * length);
        LogUtil.d(TAG,"getRadomStringTts,index:"+index);
        if (index == length)
            return length - 1;
        return index;
    }

    private void startFirstScene() {
        Log.d(TAG, "startFirstScene");

        updateStarCount(nStarCount);
        String[] stringArray=getResources().getStringArray(R.array.sleep_first_scene);
        playTtsText(strBabyName + "，"+stringArray[getRadomStringTts(stringArray.length)]);


        if (nStarCount == 1) {
            int scene = getUpdateSceneIndex();
            updateSceneIndex(++scene);
            nextScene(60 * 1000, HANDLE_MSG_NEXT_SCENE);//
        } else {
            nextScene(2 * 60 * 1000, HANDLE_MSG_NEXT_SCENE);//
        }
    }

    private void startSecondScene() {
        Log.d(TAG, "startSecondScene");

        playTtsText(strBabyName + "，今天的故事很有趣哦，快来听我讲故事吧。");

        if (nStarCount == 2) {
            nextScene(2 * 60 * 1000, HANDLE_MSG_NEXT_SCENE);//
        } else {
            nextScene(60 * 1000, HANDLE_MSG_NEXT_SCENE);//
        }
    }

    private Handler nHander = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case HANDLE_MSG_NEXT_SCENE:
                    if (nStarCount > 1) {
                        --nStarCount;
                    }
                    updateStarCount(nStarCount);

                    int scene = getUpdateSceneIndex();
                    switchScene(++scene);
                    updateSceneIndex(scene);
                    break;
                case HANDLE_MSG_PLAY_STORY:
                    startStoriesScene();
                    break;
                case HANDLE_MSG_TIMER_LAST:
                    mTimerAnimation.stop();
                    mTimerBtnAnimation.stop();
                    stopMediaPlay();
                    timerView.setBackgroundResource(R.mipmap.sleep21);
                    timerSignBtn.setBackgroundResource(R.mipmap.sleep_sign_btn_gray);
                    break;
                case HANDLE_MSG_TIME_OUT:
                    startFourScene();
                    break;
            }
            return false;
        }
    });

    private void nextScene(int millis, int type) {
        Message msg = Message.obtain();
        msg.what = type;
        nHander.sendMessageDelayed(msg, millis);
    }

    private void startThreeScene() {
        Log.d(TAG, "startThreeScene");

//        circleView.setVisibility(View.VISIBLE);
        bgView.setBackgroundColor(Color.parseColor("#80000000"));
//        circleView.setValueAnimated(0, 60, 60 * 1000);
        startTimer();
        startPlay(R.raw.wakeup_count_down, 45);
        nHander.sendEmptyMessageDelayed(HANDLE_MSG_TIMER_LAST, 2857 * 20);
        nextScene(61 * 1000, HANDLE_MSG_TIME_OUT);
    }

    private void startTimer(){
        timerView.setVisibility(View.VISIBLE);
        timerSignBtn.setVisibility(View.VISIBLE);
        mTimerAnimation = (AnimationDrawable) timerView.getBackground();
        mTimerBtnAnimation = (AnimationDrawable) timerSignBtn.getBackground();
        mTimerAnimation.setOneShot(true);
        mTimerBtnAnimation.setOneShot(false);
        mTimerAnimation.start();
        mTimerBtnAnimation.start();
    }

    private void startFourScene() {
        Log.d(TAG, "startFourScene");

        circleView.setVisibility(View.INVISIBLE);
        timerView.setVisibility(View.INVISIBLE);
        timerSignBtn.setVisibility(View.INVISIBLE);
        signTimeoutImageView.setVisibility(View.VISIBLE);
        signTimeoutTextView.setVisibility(View.VISIBLE);
        signButton.setClickable(false);

        if (clockMode == 0) {
            Countly.sharedInstance().recordEvent("Sleep", "t_habit_sleep_clockin_fail");
        } else {
            Countly.sharedInstance().recordEvent("Sleep", "v_habit_sleep_clockin_fail");
        }

        clockMode = 0;

        new Handler().postDelayed(new Runnable() { //imitation of internet connection
            @Override
            public void run() {
                finish();
            }
        }, 3000);
    }

    @Override
    protected void assistInfoIntentReceiver() {
        if (!apiSignedSucess) {

            QAILog.e(TAG, "assistInfoIntentReceiver DEBUG CLICK");

            signButtonClick(null);
        } else {
            playTtsText(" ");
            stopStories();
            setStoryPlayingStatus(false);
            finish();
            gotoHome();
            AIBaseActivity.delayStopStories(QchatApplication.getApplicationInstance());
        }
    }

    private boolean apiSigning = false;
    private boolean apiSignedSucess = false;

    public void signButtonClick(View view) {
        if (apiSigning || apiSignedSucess) {
            return;
        }

        Log.d(TAG, "signButtonClick(View view)");

        stopMediaPlay();

        if (clockMode == 0) {
            Countly.sharedInstance().recordEvent("Sleep", "t_habit_sleep_clockin_ontime");
        } else {
            Countly.sharedInstance().recordEvent("Sleep", "v_habit_sleep_clockin_ontime");

        }

        apiSigning = true;

        new Thread(new Runnable() {
            @Override
            public void run() {

                final boolean rec = Api.postHabit(getApplicationContext(), StringEncryption.generateToken(), QAIConfig.getMacForSn(), 4, nStarCount);

                apiSigning = false;
                apiSignedSucess = rec;
                SleepActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        circleView.setVisibility(View.INVISIBLE);
                        timerView.setVisibility(View.INVISIBLE);
                        timerSignBtn.setVisibility(View.INVISIBLE);
                        if (rec) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    playTtsText("恭喜你，完成今天的早睡任务，我们开始听故事吧。");
                                }
                            },600);
                            nextScene(8000, HANDLE_MSG_PLAY_STORY);

                            clockMode = 0;
                        }
                    }
                });

                showTaskStarDialog(!rec, nStarCount);
            }
        }).start();

    }

    @Override
    protected void playTtsTextFinished() {
//        viewFilpper.showNext();
    }

    @Override
    protected void localResult(String intentName) {
        try {
        if (intentName.equals("wakeup_sleep") || intentName.equals("wakeup_checkout")) {
            QAILog.e(TAG, "localServiceJsonResult DEBUG CLICK");
            clockMode = 1;
            signButtonClick(null);
        }
    } catch (Exception e) {
        e.printStackTrace();
            }
    }

    @Override
    public void onClickTaskStarRetry() {
        apiSigning = false;

        QAILog.e(TAG, "onClickTaskStarRetry DEBUG CLICK");

        signButtonClick(null);
    }

    @Override
    public void onClickTaskStarDialog() {
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        boolean msleep = intent.getBooleanExtra("msleep", false);

        if (msleep) {
            Log.d(TAG, "onNewIntent(Intent intent)");

            cancelCurrentAlarm();
            joinToSleepMode();
            QchatApplication.getApplicationInstance().setSleepActive(true);
        }
    }

    private void joinToSleepMode() {

        playTtsText(" ");
        joinSleepImageView.setVisibility(View.VISIBLE);
        stopStories();


        Log.d(TAG, "joinToSleepMode");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (storyPlaying()) {
                    nextStories();
                }
                setStoryPlayingStatus(false);

                switchPrivacy(true);
                gotoSleep();
                finish();
                gotoHome();
            }
        }, 4000);
    }

    @Override
    protected void switchPrivacy(boolean privacyMode) {
        super.switchPrivacy(privacyMode);
        removePricacy();
    }

    @Override
    protected void playCompleteIntentReceiver() {
        super.playCompleteIntentReceiver();
        if (!isForegroud) {
            actionActivite(QchatApplication.getApplicationInstance());
        } else {
            cancelCurrentAlarm();
            joinToSleepMode();
        }
    }

    public static void actionActivite(Context context) {
        Intent intent = new Intent(context, SleepActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        Bundle bundle = new Bundle();
        bundle.putBoolean("msleep", true);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private void removePricacy() {

        AlarmManager am;
        Intent intent = new Intent();
        intent.setAction(AIConstants.SLEEP_REMOVE_PRIVACY_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0x102,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);


        long firstTime = SystemClock.elapsedRealtime();
        long systemTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.add(Calendar.SECOND, 10);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long selectTime = calendar.getTimeInMillis();

        if (systemTime > selectTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar.getTimeInMillis();
        }

        long time = selectTime - systemTime;// 计算现在时间到设定时间的时间差
        long my_Time = firstTime + time;

        am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, my_Time, AlarmManager.INTERVAL_DAY, pi);
    }

    @Override
    protected void cancelCurrentAlarm() {
        Log.d(TAG, "cancelAlarm(String action, Context context) ");
        cancelAlarm(AIConstants.SLEEP_ALARM_ACTION, this);
    }
}


