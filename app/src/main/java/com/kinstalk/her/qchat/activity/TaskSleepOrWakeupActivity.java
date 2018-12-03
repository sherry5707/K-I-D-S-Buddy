package com.kinstalk.her.qchat.activity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.common.AICoreCallbackInterface;
import com.kinstalk.her.qchat.common.UIHelper;
import com.kinstalk.her.qchat.dialog.TaskStarDialog;
import com.kinstalk.her.qchat.utils.CountlyConstant;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatmodel.Manager.util.UIHelp;
import com.tencent.xiaowei.info.QLoveResponseInfo;
import com.tencent.xiaowei.info.XWResponseInfo;

import org.json.JSONObject;

import java.util.ArrayList;

import ly.count.android.sdk.Countly;

/**
 * 提前睡觉起床界面
 */
public class TaskSleepOrWakeupActivity extends AIBaseActivity implements TaskStarDialog.OnContentClickListener {

    private static final String TAG = TaskSleepOrWakeupActivity.class.getSimpleName();

    private ArrayList<ImageView> starViewList = new ArrayList<ImageView>();

    private boolean bSleep = true;
    private TextView hour1TextView;
    private TextView hour2TextView;
    private TextView minute1TextView;
    private TextView minute2TextView;
    private CountDownTimer countDownTimer;
    private int nStarCount = 3;
    private String strNickName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent itent = this.getIntent();
        bSleep = itent.getBooleanExtra("sleep", true);//是否睡眠


        long featureTimeMillis = System.currentTimeMillis() + 1000 * 12 * 60 * 60;
        featureTimeMillis = itent.getLongExtra("featureTimeMillis", featureTimeMillis);
        nStarCount = itent.getIntExtra("star_1", 3);
        strNickName = itent.getStringExtra("nickName");

        if (bSleep) {
            setContentView(R.layout.activity_task_sleep);
            QchatApplication.getApplicationInstance().setPreSleepActive(true);

            Countly.sharedInstance().recordEvent("Sleep", "t_habit_sleep");

        } else {
            setContentView(R.layout.activity_task_wakeup);
            QchatApplication.getApplicationInstance().setWakeupActive(true);

            Countly.sharedInstance().recordEvent("Wakeup", "t_habit_getup");

        }

        starViewList.add((ImageView) findViewById(R.id.startImageView3));
        starViewList.add((ImageView) findViewById(R.id.startImageView2));
        starViewList.add((ImageView) findViewById(R.id.startImageView1));

        updateStarCount(nStarCount);

        hour1TextView = (TextView) findViewById(R.id.hour1TextView);
        hour2TextView = (TextView) findViewById(R.id.hour2TextView);
        minute1TextView = (TextView) findViewById(R.id.minute1TextView);
        minute2TextView = (TextView) findViewById(R.id.minute2TextView);

        long timeInterval = featureTimeMillis - System.currentTimeMillis();
        updateTime(timeInterval);

        final long tempFeatureTimeMillis = featureTimeMillis;
        countDownTimer = new CountDownTimer(timeInterval, 60 * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // TODO Auto-generated method stub
                long a = tempFeatureTimeMillis - System.currentTimeMillis();
                updateTime(a);
            }

            @Override
            public void onFinish() {
                finish();
            }
        }.start();
    }

    @Override
    protected void setAutoActivityTimeout() {
    }

    private void updateStarCount(int count) {

        for (int i = 0; i < starViewList.size() - count; i++) {
            final ImageView imageView = starViewList.get(i);
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

    private void updateTime(long timeInterval) {

        long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
        long nh = 1000 * 60 * 60;// 一小时的毫秒数
        long nm = 1000 * 60;// 一分钟的毫秒数

        long hour = timeInterval % nd / nh;// 计算差多少小时
        long min = timeInterval % nd % nh / nm;// 计算差多少分钟

        hour1TextView.setText(String.valueOf(hour / 10));
        hour2TextView.setText(String.valueOf(hour % 10));

        minute1TextView.setText(String.valueOf(min / 10));
        minute2TextView.setText(String.valueOf(min % 10));

    }

    private boolean apiSigning = false;
    private boolean signSucess = false;

    public void signButtonClick(View view) {

        if (apiSigning) {
            return;
        }
        Log.d(TAG, "signButtonClick(View view)");

        apiSigning = true;

        if (bSleep) {

            Countly.sharedInstance().recordEvent("Sleep", "t_habit_sleep_clockin_advance");

            UIHelper.setAlreadySleep(getApplicationContext());
        } else {

            Countly.sharedInstance().recordEvent("Wakeup", "t_habit_getup_clockin_advance");

            UIHelper.setAlreadyGetup(getApplicationContext());
        }
        int type = 3;
        if(bSleep)
            type = 4;
        int starCount = UIHelp.checkHabitSignOrNot(this, type);
        if (starCount >= 0) {
            playTtsText("今日已打卡");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

                boolean rec = Api.postHabit(getApplicationContext(), StringEncryption.generateToken(), QAIConfig.getMacForSn(), bSleep ? 4 : 3, nStarCount);
                apiSigning = rec;
                showTaskStarDialog(!rec, nStarCount);
                signSucess = rec;
                if (rec) {
                    if (bSleep) {
                        TaskSleepOrWakeupActivity.this.runOnUiThread(new Runnable() { //imitation of internet connection
                            @Override
                            public void run() {
                                playStory();
                            }
                        });
                    } else {
                        TaskSleepOrWakeupActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playTtsText("你真是个好孩子，我为你感到骄傲");
                            }
                        });
                    }
                }
            }
        }).start();
    }


    private void playStory() {

        playTtsText("恭喜你，完成今天的早睡任务，我们开始听故事吧。");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SleepActivity.actionStart(TaskSleepOrWakeupActivity.this, -1, strNickName, nStarCount);
                finish();
//                long status = -1;
//
//                Intent intent = new Intent(TaskSleepOrWakeupActivity.this, SleepActivity.class);
//                intent.putExtra("alarm_time", status);
//                intent.putExtra("nick_name", strNickName);
//                intent.putExtra("star_1", 3);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                TaskSleepOrWakeupActivity.this.startActivityForResult(intent, 0x010);
            }
        }, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x010) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
        if (bSleep) {
            QchatApplication.getApplicationInstance().setPreSleepActive(false);
        } else {
            QchatApplication.getApplicationInstance().setWakeupActive(false);
        }
    }

    @Override
    protected void localResult(String intentName) {
        if(!TextUtils.isEmpty(intentName) && (intentName.equals("wakeup_checkout") || intentName.equals("wakeup_wakeup")) && !bSleep ) {
            signButtonClick(null);
        } else if (!TextUtils.isEmpty(intentName) && (intentName.equals("wakeup_checkout") || intentName.equals("wakeup_sleep")) && bSleep) {
            signButtonClick(null);
        }
    }

    @Override
    public void onClickTaskStarDialog() {
        if (signSucess && !bSleep) {
            finish();
        }
    }

    @Override
    public void onClickTaskStarClose() {
        if (signSucess && !bSleep) {
            finish();
        }
    }

    @Override
    public void onClickTaskStarRetry() {
        signButtonClick(null);
    }

    public void closeButtonClose(View view) {
        finish();
        Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE,CountlyConstant.t_calendar_clockin_turnoff);
    }

    @Override
    protected void assistInfoIntentReceiver() {
        finish();
        gotoHome();
    }
}
