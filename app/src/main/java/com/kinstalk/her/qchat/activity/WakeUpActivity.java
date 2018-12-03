package com.kinstalk.her.qchat.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.kinstalk.her.qchatcomm.utils.StringEncryption;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ly.count.android.sdk.Countly;

public class WakeUpActivity extends AIBaseActivity {

    private List<View> sceneViewList = new ArrayList<View>();
    private AdapterViewFlipper viewFilpper;
    private CircleProgressView circleView;
    private ImageView timerView;
    private AnimationDrawable mTimerAnimation;
    private ImageView timerBtnView;
    private AnimationDrawable mTimerBtnAnimation;
    private ImageView signTimeoutImage;
    private TextView signTimeoutTextView;
    private Button signButton;

    private View bgView;
    private String strBabyName = "宝宝";
    private int nStarCount = 3;


    private final int COUNT_DOWN_MSG = 0x1;
    private final int COUNT_DOWNING_MSG = 0x2;
    private final int COUNT_DOWN_FINISHED_MSG = 0x3;
    private final int COUNT_DOWN_TIMER_OUT_MSG = 0x4;
    private final int COUNT_DOWN_TIMER_RESUME = 0x5;
    private final int COUNT_DOWN_FIVE_METHOD = 0x6;
    private final int COUNT_DOWN_TIEMR_LAST = 0x7;
    private ArrayList<ImageView> starViewList = new ArrayList<ImageView>();

    /**
     * 打卡方式，0：手动；1：语音。
     */
    private int clockMode = 0;

    /**
     * 为了在习惯被删除时finish流程
     */
    private static final String FINISH_ACTION = "wakeup_finish";
    private BroadcastReceiver finishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    private static final String TAG = "WakeUpActivity";

    public static void actionStart(Context context, long time, String nickName, int star) {
        Intent intent = new Intent(context, WakeUpActivity.class);
        intent.putExtra("alarm_time", time);
        intent.putExtra("nick_name", nickName);
        intent.putExtra("star_1", star);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(intent);
        putSceneIndexToSP(context,-1);
        Log.d(TAG, " actionStart(Context context, long time, String nickName, int star_1) ");

    }

    public static void actionActivite(Context context) {
        Intent intent = new Intent(context, WakeUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        Bundle bundle = new Bundle();
        bundle.putBoolean("mwakeup", true);
        intent.putExtras(bundle);
        context.startActivity(intent);

        Log.d(TAG, " actionStart(Context context, long time, String nickName, int star_1) ");
    }

    Handler hHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case COUNT_DOWN_MSG:
                    //每两分钟 减去一颗星星

                    if (nStarCount > 1) {
                        --nStarCount;
                    }
                    startCountDown(nStarCount);
                    break;
                case COUNT_DOWNING_MSG:
                    viewFilpper.showNext();
//                    circleView.setVisibility(View.VISIBLE);
                    bgView.setBackgroundColor(Color.parseColor("#80000000"));
//                    circleView.setValueAnimated(0, 20, 20 * 1000);
                    startTimer();
                    startPlay(R.raw.wakeup_count_down, 18);
                    hHandler.sendEmptyMessageDelayed(COUNT_DOWN_TIEMR_LAST, 1428 * 13);
                    startFiveScene(COUNT_DOWN_FINISHED_MSG, 20 * 1000 + 2000);
                    break;
                case COUNT_DOWN_TIEMR_LAST:
                    mTimerAnimation.stop();
                    mTimerBtnAnimation.stop();
                    stopMediaPlay();
                    timerView.setBackgroundResource(R.mipmap.wakeup14);
                    timerBtnView.setBackgroundResource(R.mipmap.wakeup_sign_btn_gray);
                    break;
                case COUNT_DOWN_FINISHED_MSG:
                    signButton.setClickable(false);
                    signTimeoutImage.bringToFront();
                    signTimeoutTextView.bringToFront();
                    circleView.setVisibility(View.INVISIBLE);
                    mTimerAnimation.stop();
                    timerView.setVisibility(View.INVISIBLE);
                    mTimerBtnAnimation.stop();
                    timerBtnView.setVisibility(View.INVISIBLE);
                    signTimeoutImage.setVisibility(View.VISIBLE);
                    signTimeoutTextView.setVisibility(View.VISIBLE);

                    if (clockMode == 0) {
                        Countly.sharedInstance().recordEvent("Wakeup", "t_habit_getup_clockin_fail");
                    } else {
                        Countly.sharedInstance().recordEvent("Wakeup", "v_habit_getup_clockin_fail");
                    }

                    clockMode = 0;

                    startFiveScene(COUNT_DOWN_TIMER_OUT_MSG, 3000);
                    break;
                case COUNT_DOWN_TIMER_OUT_MSG:
                    putSceneIndexToSP(WakeUpActivity.this,-1);
                    finish();
                    break;
                case COUNT_DOWN_TIMER_RESUME:
                    playTtsTextFinished();
                    break;
                case COUNT_DOWN_FIVE_METHOD:
                    //每两分钟 减去一颗星星
                    stopStories();
                    startCountDowning();
                    break;

            }
            return false;
        }
    });

    private void startTimer(){
        LogUtil.d(TAG,"startTimer");
        timerView.setVisibility(View.VISIBLE);
        timerBtnView.setVisibility(View.VISIBLE);
        mTimerAnimation = (AnimationDrawable) timerView.getBackground();
        mTimerBtnAnimation = (AnimationDrawable) timerBtnView.getBackground();
        mTimerAnimation.setOneShot(true);
        mTimerBtnAnimation.setOneShot(false);
        mTimerAnimation.start();
        mTimerBtnAnimation.start();
    }

    private void startCountDowning() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startCountDown(nStarCount);
            }
        }, 3 * 1000);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wake_up);
        QchatApplication.getApplicationInstance().setWakeupActive(true);

        startWakeUp();
        switchPrivacy(false);
        createSubView();
        putSceneIndexToSP(this,-1);
        registerReceiver();
        Log.d(TAG, " onCreate(Bundle savedInstanceState)");
    }

    private void registerReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FINISH_ACTION);
        registerReceiver(finishReceiver,intentFilter);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(signButton.isClickable()) {
            signButtonClick(null);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hHandler.removeCallbacksAndMessages(null);
        stopStories();
        unregisterReceiver(finishReceiver);
        QchatApplication.getApplicationInstance().setWakeupActive(false);
    }

    @Override
    protected void getParamsByIntent() {
        Intent itent = this.getIntent();
        strBabyName = itent.getStringExtra("nick_name");
        if (TextUtils.isEmpty(strBabyName)) {
            strBabyName = "宝宝";
        }
        nStarCount = itent.getIntExtra("star_1", 3);
    }

    private void createSubView() {
        timerView = (ImageView) findViewById(R.id.wakeup_timer);
        timerBtnView = (ImageView) findViewById(R.id.wakeup_timer_sign_btn);
        circleView = (CircleProgressView) findViewById(R.id.circleView);
        circleView.setUnitVisible(false);
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

        signButton = (Button) findViewById(R.id.signButton);
        signTimeoutImage = (ImageView) findViewById(R.id.signTimeoutImage);
        signTimeoutTextView = (TextView) findViewById(R.id.signTimeoutText);

        starViewList.add((ImageView) findViewById(R.id.startImageView3));
        starViewList.add((ImageView) findViewById(R.id.startImageView2));
        starViewList.add((ImageView) findViewById(R.id.startImageView1));

        viewFilpper = (AdapterViewFlipper) findViewById(R.id.viewpager);
        viewFilpper.setInAnimation(this, R.animator.push_in);
        viewFilpper.setOutAnimation(this, R.animator.push_out);

        ImageView sceneImageView1 = createImageView(R.mipmap.wakeup_bg1);
        ImageView sceneImageView2 = createImageView(R.mipmap.wakeup_bg2);
        ImageView sceneImageView3 = createImageView(R.mipmap.wakeup_bg3);
        ImageView sceneImageView4 = createImageView(R.mipmap.wakeup_bg4);

        sceneViewList.add(sceneImageView1);
        sceneViewList.add(sceneImageView2);
        sceneViewList.add(sceneImageView3);
        sceneViewList.add(sceneImageView4);


        //
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
                ImageView imageView1 = (ImageView) sceneViewList.get(position);
                return imageView1;
            }
        };

        viewFilpper.setAdapter(adapter);
    }

    private void updateStarCount(int count) {
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

    /**
     * 根据上一次退出时所在的场景，继续下一个场景
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        int index = getSceneIndexFromSP();
        LogUtil.d(TAG, "onResume: index:" + index);
        if (isVoiceStopStory()) {
            stopStories();
            signButtonClick(null);
            return;
        }
        if (index >= 0 && index < 2) {
            switchScene(index + 1);
        } else if (index == 2) {
            startStoriesScene();
        } else if (index == 3) {
            if (QchatApplication.isWakeupActive() && storyPlaying()) {
                LogUtil.d(TAG, "onResume,resumeStories");
                resumeStories();
                return;
            }
        } else if (index == -1) {
            hHandler.removeCallbacksAndMessages(null);
            viewFilpper.setDisplayedChild(0);
            switchScene(0);
        }
        /*if (ttsPlaying() && getUpdateSceneIndex() > 0) {
            hHandler.removeMessages(COUNT_DOWN_TIMER_RESUME);
            startFiveScene(COUNT_DOWN_TIMER_RESUME, 3000);
        } */

    }

    @Override
    protected void switchScene(final int sceneIndex) {

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
            case 3:
                startStoriesScene();
                break;
            case 4:
                break;
            case 5:
//                startFiveScene(COUNT_DOWN_MSG, 3000);
                break;
        }
    }

    private static void putSceneIndexToSP(Context context,int index){
        LogUtil.d(TAG, "putSceneIndexToSP: index:"+index);
        SharedPreferences sp = context.getSharedPreferences("LAST_SCENE",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("wakeup_scene_index",index);
        editor.commit();
    }

    private int getSceneIndexFromSP(){
        SharedPreferences sp = this.getSharedPreferences("LAST_SCENE",MODE_PRIVATE);
        int index = sp.getInt("wakeup_scene_index",0);
        LogUtil.d(TAG, "getSceneIndexFromSP: index:"+index);
        return index;
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
        Log.d(TAG, " startFirstScene");
        String[] stringArray=getResources().getStringArray(R.array.wake_up_first_scene);
        updateStarCount(nStarCount);
        playTtsText(strBabyName + "，"+stringArray[getRadomStringTts(stringArray.length)]);
        putSceneIndexToSP(this,0);
    }

    private void startSecondScene() {
        Log.d(TAG, " startSecondScene");
//        viewFilpper.showNext();
        viewFilpper.setDisplayedChild(1);
        String[] stringArray=getResources().getStringArray(R.array.wake_up_second_scene);
        int index = getRadomStringTts(stringArray.length);
        if (index == 0) {
            playTtsText(stringArray[index]);
        } else if (index == 1) {
            playTtsText(strBabyName + "，" + stringArray[index]);
        }
        updateStarCount(nStarCount);
        startPlay(R.raw.wakeup_bird, 4);
        putSceneIndexToSP(this,1);
    }

    private void startThreeScene() {
        Log.d(TAG, " startThreeScene");
        viewFilpper.setDisplayedChild(2);
        playTtsText(strBabyName + "，"+getString(R.string.wake_up_three_scene));
        updateStarCount(nStarCount);
        putSceneIndexToSP(this,2);
    }

    private void startStoriesScene(){
        viewFilpper.setDisplayedChild(2);
        putSceneIndexToSP(this,3);
        setAlarm(AIConstants.WAKEUP_ALARM_ACTION, 4);
        playStories("播放中国寓言故事");
    }

    private void startFourScene() {
        Log.d(TAG, " startFourScene");

        if (!isForegroud) {
            WakeUpActivity.actionActivite(QchatApplication.getApplicationInstance());
        } else {
            putSceneIndexToSP(this,-2);
            stopStories();
            setStoryPlayingStatus(false);
//            viewFilpper.showNext();
            viewFilpper.setDisplayedChild(2);
            playTtsText(strBabyName + "，"+getString(R.string.wake_up_four_scene));
            updateStarCount(nStarCount);

            hHandler.removeMessages(COUNT_DOWN_FIVE_METHOD);
            startFiveScene(COUNT_DOWN_FIVE_METHOD, 13000);
        }

    }

    private void startFiveScene(int scene, long delayMillis) {
        Log.d(TAG, " startFiveScene");
        Message msg = Message.obtain();
        msg.what = scene;
        hHandler.sendMessageDelayed(msg, delayMillis);
    }

    @Override
    protected void assistInfoIntentReceiver() {
        signButtonClick(null);
    }

    private boolean apiSigning = false;

    public void signButtonClick(View view) {
        if (apiSigning) {
            return;
        }

        stopMediaPlay();

        if (clockMode == 0) {
            Countly.sharedInstance().recordEvent("Wakeup", "t_habit_getup_clockin_ontime");
        } else {
            Countly.sharedInstance().recordEvent("Wakeup", "v_habit_getup_clockin_ontime");
        }

        Log.d(TAG, " signButtonClick(View view)");

        cancelCurrentAlarm();

        hHandler.removeMessages(COUNT_DOWN_FIVE_METHOD);
        hHandler.removeMessages(COUNT_DOWN_MSG);
        hHandler.removeMessages(COUNT_DOWNING_MSG);
        hHandler.removeMessages(COUNT_DOWN_FINISHED_MSG);
        hHandler.removeMessages(COUNT_DOWN_TIMER_OUT_MSG);
        circleView.setVisibility(View.INVISIBLE);
        timerView.setVisibility(View.INVISIBLE);
        timerBtnView.setVisibility(View.INVISIBLE);

        updateSceneIndex(10);
        apiSigning = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean rec = Api.postHabit(getApplicationContext(), StringEncryption.generateToken(), QAIConfig.getMacForSn(), 3, nStarCount);

                WakeUpActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        apiSigning = rec;
                        if (rec) {
                            if (storyPlaying()) {
                                stopStories();
                            }
                            setStoryPlayingStatus(false);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    playTtsText("你真是个好孩子，我为你感到骄傲");
                                }
                            }, 600);
                            clockMode = 0;
                        }
                    }
                });


                showTaskStarDialog(!rec, nStarCount);
            }
        }).start();

    }

    @Override
    protected void localResult(String intentName) {
        try {
            if (!TextUtils.isEmpty(intentName) && intentName.equals("wakeup_wakeup") || intentName.equals("wakeup_checkout")) {
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
        signButtonClick(null);

    }

    @Override
    protected void playTtsTextFinished() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int scene = getUpdateSceneIndex();
                switchScene(++scene);
                updateSceneIndex(scene);
            }
        }, 3 * 1000);
    }

    @Override
    public void onClickTaskStarDialog() {
        playTtsText(" ");

        if (storyPlaying()) {
            stopStories();
        }
        setStoryPlayingStatus(false);
        finish();
        gotoHome();
    }

    @Override
    protected void playCompleteIntentReceiver() {
        super.playCompleteIntentReceiver();
        cancelCurrentAlarm();
        stopStories();
        setStoryPlayingStatus(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startFourScene();
            }
        }, 2000);
    }

    private void startCountDown(int starCount) {
        if (starCount == 2) {
            String[] stringArray = getResources().getStringArray(R.array.wake_up_two_star);
            playTtsText(strBabyName + "，" + stringArray[getRadomStringTts(stringArray.length)]);
        } else if (starCount == 1) {
            String[] stringArray = getResources().getStringArray(R.array.wake_up_one_star);
            playTtsText(strBabyName + "，" + stringArray[getRadomStringTts(stringArray.length)]);
        } else {
            playTtsText("现在打卡可以获得" + starCount + "颗小星星哦");
        }
        updateStarCount(starCount);

        if (starCount == 3) {
            startFiveScene(COUNT_DOWN_MSG, 2 * 60 * 1000);
        } else if (starCount == 2) {
            startFiveScene(COUNT_DOWN_MSG, 2 * 60 * 1000);
        } else if (starCount == 1) {
            startFiveScene(COUNT_DOWNING_MSG, 100 * 1000);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean mwakeup = intent.getBooleanExtra("mwakeup", false);
        if (mwakeup) {

            Log.d(TAG, " onNewIntent(Intent intent)");
            cancelCurrentAlarm();
            stopStories();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (storyPlaying()) {
                        nextStories();
                    }
                    setStoryPlayingStatus(false);
                    startFourScene();
                }
            }, 4000);
        }
    }

    @Override
    protected void cancelCurrentAlarm() {
        Log.d(TAG, "cancelCurrentAlarm(String action, Context context) ");
        cancelAlarm(AIConstants.WAKEUP_ALARM_ACTION, this);
    }

}
