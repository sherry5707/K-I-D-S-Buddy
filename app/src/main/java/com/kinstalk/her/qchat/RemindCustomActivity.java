package com.kinstalk.her.qchat;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kinstalk.her.qchat.activity.AIRemindService;
import com.kinstalk.her.qchat.activity.KidsService;
import com.kinstalk.her.qchat.common.AICoreCallbackInterface;
import com.kinstalk.her.qchat.dialog.TaskStarDialog;
import com.kinstalk.her.qchat.utils.CountlyConstant;
import com.kinstalk.her.qchat.view.ToastCustom;
import com.kinstalk.her.qchat.voice.AudioFocusController;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.utils.DateUtils;
import com.kinstalk.her.qchatcomm.utils.NetworkUtils;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.util.UIHelp;
import com.kinstalk.qloveaicore.AIManager;
import com.kinstalk.qloveaicore.TTSListener;
import com.tencent.xiaowei.info.QLoveResponseInfo;
import com.tencent.xiaowei.info.XWResponseInfo;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

import com.kinstalk.util.AppUtils;

import ly.count.android.sdk.Countly;

public class RemindCustomActivity extends AppCompatActivity implements TaskStarDialog.OnContentClickListener, AICoreCallbackInterface {

    private static String TAG = RemindCustomActivity.class.getSimpleName();
    private static MediaPlayer mRingMediaPlayer;
    private static final Long SPACE_TIME = 800L;
    private static Long lastClickTime = 0L;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private PowerManager.WakeLock wl;

    public ImageView starImage_1;
    public ImageView starImage_2;
    public ImageView starImage_3;
    public LinkedList<ImageView> starViewList = new LinkedList<>();

    private ImageButton closeButton;
    private TextView titleView;
    private TextView numDayView;
    private TextView timeView;
    private Button doneButton;
    private static int type;
    private static boolean ttsPlaying = false;
    private long time;
    private String nickName;
    private String content;
    private int star;
    private int status;

    private String[] ttsArray = new String[]{"你真是个好孩子，我为你感到骄傲", "你太棒了，继续坚持，加油", "我真的很喜欢你这样的好孩子"};

    private TaskStarDialog taskStarDialog;
    private AIRemindService.AIResultBinder aiResultBinder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_remind_custom);
        initView();
        initData();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.FULL_WAKE_LOCK, AlarmActivity.class.getSimpleName());
    }

    public void onCallback(Object obj) {
        if (TextUtils.isEmpty(obj.toString()))
            return;
        QLoveResponseInfo rspData = (QLoveResponseInfo) obj;
        XWResponseInfo xwResponseInfo = rspData.xwResponseInfo;
        if (xwResponseInfo == null)
            return;
        Log.d(TAG, "QLoveResponseInfo" + rspData.toString());
        try {
            JSONObject object = new JSONObject(rspData.xwResponseInfo.responseData);
            String intentName = object.optString("intentName");
            if (!TextUtils.isEmpty(intentName) && intentName.equals("wakeup_checkout")) {
                clickDoneButton();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    //status 1 ----提前10分钟   //status 0 ====到点  //status 2 ,过时三十分钟
    public static void actionStart(Context context, long time, String nickName, String content, int star, int status, int habitType) {
        Intent intent = new Intent(context, RemindCustomActivity.class);
        intent.putExtra("alarm_time", time);
        intent.putExtra("nick_name", nickName);
        intent.putExtra("content", content);
        intent.putExtra("star", star);
        intent.putExtra("status", status);
        intent.putExtra("habitType", habitType);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(intent);
        Log.d(TAG, " actionStart(Context context, long time, String nickName, int star) ");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        super.onNewIntent(intent);
        stopAlarm();
        initData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        KidsService.addToCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (type != -1 && status != 100) {
            wl.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (type != -1 && status != 100) {
            wl.release();
        }
        mHandler.removeCallbacksAndMessages(null);
        abandonAudioFocus();
        stopAlarm();
        UIHelp.setSelfDefineHabitAlarming(this, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        mHandler.removeCallbacksAndMessages(null);
        KidsService.removeFromCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsPlaying) {
            playTTSWithContent("");
        }
        mHandler.removeCallbacksAndMessages(null);
        Log.d(TAG, "onDestroy: ");
    }

    public void initData() {
        String ttsContent = "";
        Log.d(TAG, "initData: ");

        Intent intent = this.getIntent();
        if (null != intent) {
            time = intent.getLongExtra("alarm_time", 0);
            nickName = intent.getStringExtra("nick_name");
            content = intent.getStringExtra("content");
            star = intent.getIntExtra("star", 0);
            status = intent.getIntExtra("status", 100);

            type = intent.getIntExtra("habitType", -1);
            Log.d(TAG, "type " + type + " status " + status);
        }

        if (star > 0) {
            for (int i = 0; i < 3; i++) {
                if (i >= star) {
                    ImageView starView = starViewList.get(i);
                    starView.setVisibility(View.GONE);
                }
            }
        }

        if (!TextUtils.isEmpty(content)) {
            titleView.setText(content);
        }

        if (time > 0) {
            String hhMMSS = DateUtils.getHHmmssTime(time);
            String hourMTime = hhMMSS.substring(0, 5);
            timeView.setText("开始时间： " + hourMTime);
            if (doneButton.getText().toString().equals("打卡")) {
                Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_calendar_clockin);
            }
        }
        if (status != 100) {
            UIHelp.setSelfDefineHabitAlarming(this, 1);
        }
        switch (status) {
            case 0:
                ttsContent = nickName + " " + content + "的时间已经到了   记得按时完成哦";
                playTTSWithContent(ttsContent);
                break;
            case 1:
                ttsContent = nickName + " " + content + "的时间快到了   记得按时完成哦";
                playTTSWithContent(ttsContent);
                break;
            case 2:
                ttsContent = nickName + " " + content + "的时间已经过啦   今天还没有打卡哦";
                playTTSWithContent(ttsContent);
                break;
            case 100:
                //列表进入
                break;
            default:
                Log.w(TAG, "WORNING");
                break;
        }

        if (type != -1) {
            int num = MessageManager.getHabitDurationTillNow(type);
            numDayView.setText(String.valueOf(num));
            int starCount = UIHelp.checkHabitSignOrNot(this, type);
            if (starCount >= 0) {
                setGrayFinishBtn();
            } else {
                setUnGrayFinishBtn();
            }

            if (status != 100) {
                setAutoSwitchLauncher(false);
                //            startAlarm();

                //当新的提醒开始时，清空之前的倒计时，重新开始
                mHandler.removeCallbacksAndMessages(null);
                Log.i(TAG, "content " + ttsContent.length());
                if (ttsContent.length() <= 24) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startAlarm();
                        }
                    }, 6 * 1000);
                } else if (ttsContent.length() >= 24 && ttsContent.length() < 26) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startAlarm();
                        }
                    }, 7 * 1000);
                } else if (ttsContent.length() >= 26 && ttsContent.length() < 30) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startAlarm();
                        }
                    }, 7500);
                } else if (ttsContent.length() >= 30 && ttsContent.length() < 32) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startAlarm();
                        }
                    }, 8 * 1000);
                } else if (ttsContent.length() >= 32) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startAlarm();
                        }
                    }, 9 * 1000);
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopAlarm();
                        finish();
                    }
                }, 5 * 60 * 1000);

            }
        }
    }

    public void initView() {
        Log.d(TAG, "initView: ");
        titleView = (TextView) findViewById(R.id.titleText);
        numDayView = (TextView) findViewById(R.id.numDayText);
        timeView = (TextView) findViewById(R.id.timeText);

        doneButton = (Button) findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAlarm();
                mHandler.removeCallbacksAndMessages(null);
                clickDoneButton();
            }
        });

        closeButton = (ImageButton) findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                if (doneButton.getText().toString().equals("打卡")) {
                    Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_calendar_clockin_turnoff);
                }
            }
        });

        starImage_1 = (ImageView) findViewById(R.id.startImageView1);
        starImage_2 = (ImageView) findViewById(R.id.startImageView2);
        starImage_3 = (ImageView) findViewById(R.id.startImageView3);
        starViewList.add(starImage_1);
        starViewList.add(starImage_2);
        starViewList.add(starImage_3);
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

    public void playTTSWithContent(String content) {
        try {
            AIManager.getInstance(getApplicationContext()).playTextWithStr(content, mCommonTTSCb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

 /*   public String getFormartDateStr(String pattern, Date date) {
        SimpleDateFormat s = new SimpleDateFormat(pattern);
        String str = s.format(date);
        return str;
    }
*/
    //ring

    private void startAlarm() {
        AudioFocusController.init().requestFocus();

        if (isStateInCall((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE))) {
            return;
        }

        final Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.alarm_ring);
        try {
            mRingMediaPlayer = new MediaPlayer();
            mRingMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mRingMediaPlayer.setLooping(true);
            mRingMediaPlayer.setDataSource(this, uri);
            mRingMediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (null != mRingMediaPlayer) {
            mRingMediaPlayer.start();
        } else {
            Log.w(TAG, "RingPlayer == null");
        }
    }

    private static void stopAlarm() {
        QAILog.d(TAG, "AlarmActivity stopAlarm.");
        AudioFocusController.init().abandonFocus();
        try {
            if (null != mRingMediaPlayer) {
                mRingMediaPlayer.stop();
                mRingMediaPlayer.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mRingMediaPlayer = null;
        }
    }

    private boolean isStateInCall(AudioManager audioManager) {
        if (audioManager == null) {
            return false;
        }
        return ((audioManager.getMode() == AudioManager.MODE_IN_CALL) ||
                (audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION));
    }

    private void abandonAudioFocus() {
        QAILog.d(TAG, "AlarmActivity release audioFocus.");
        AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(afChangeListener);
    }

    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
                QAILog.d(TAG, "AlarmActivity AudioFocus.changed:AUDIOFOCUS_LOSS_TRANSIENT");
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // Stop playback
                stopAlarm();
                finish();
                QAILog.d(TAG, "AlarmActivity AudioFocus.changed:AUDIOFOCUS_LOSS");
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // Lower the volume
                QAILog.d(TAG, "AlarmActivity AudioFocus.changed:AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback or Raise it back to normal
                QAILog.d(TAG, "AlarmActivity AudioFocus.changed:AUDIOFOCUS_GAIN");
            }
        }
    };

    public void clickDoneButton() {
        if(isFastClick())
            return;
        int starCount = UIHelp.checkHabitSignOrNot(this, type);
        if (starCount >= 0) {
                playTTSWithContent("今日已打卡，明天再来吧");
        } else {
            Context context = getApplicationContext();
            if (NetworkUtils.isNetworkAvailable(context)) {
                postHabit();
            } else {
                ToastCustom.makeText(context, context.getResources().getString(R.string.need_neetwork), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void postHabit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean rec = Api.postHabit(getApplicationContext(), StringEncryption.generateToken(), QAIConfig.getMacForSn(), type, star);
                RemindCustomActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (null != taskStarDialog && taskStarDialog.isShowing()) {

                        } else {
                            taskStarDialog = new TaskStarDialog.Builder(QchatApplication.getInstance())
                                    .setOnContentClickListener(RemindCustomActivity.this)
                                    .create();
                            taskStarDialog.show();
                        }

                        if (rec) {
                            playTTSRandom();
                            taskStarDialog.showSuccess(star);
                            setGrayFinishBtn();
                        } else {
                            taskStarDialog.showFail();
                            //     setGrayFinishBtn();
                        }
                        UIHelp.setSelfDefineHabitAlarming(getApplicationContext(), 0);
                    }
                });
            }
        }).start();
    }

    public void setGrayFinishBtn() {
        doneButton.setText("已打卡");
        doneButton.setBackgroundResource(R.drawable.btn_remind_custom_finish_gray);
    }

    public void setUnGrayFinishBtn() {
        doneButton.setText("打卡");
        doneButton.setBackgroundResource(R.mipmap.wakeup_sign);
    }

    public void playTTSRandom() {
        Random random = new Random();
        int num = random.nextInt(ttsArray.length);
        String ttsString = ttsArray[num];
        if (!TextUtils.isEmpty(ttsString)) {
            playTTSWithContent(ttsString);
        }
    }

    @Override
    public void onClickTaskStarDialog() {

    }

    @Override
    public void onClickTaskStarClose() {

    }

    @Override
    public void onClickTaskStarRetry() {
        clickDoneButton();
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

    private static boolean isFastClick() {
        long currentTime = System.currentTimeMillis();//当前系统时间
        boolean isFastClick;//是否允许点击
        if (currentTime - lastClickTime < SPACE_TIME) {
            isFastClick = true;
        } else {
            isFastClick = false;
            lastClickTime = currentTime;
        }
        return isFastClick;
    }

}
