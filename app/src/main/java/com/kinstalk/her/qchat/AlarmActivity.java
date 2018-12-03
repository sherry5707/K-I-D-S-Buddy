package com.kinstalk.her.qchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


import com.kinstalk.her.qchatcomm.utils.DateUtils;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.constant.RemindConstant;
import com.kinstalk.her.qchatmodel.entity.HabitKidsEntity;
import com.kinstalk.qloveaicore.AIManager;

import java.io.IOException;

import com.kinstalk.util.AppUtils;


/**
 * Created by Tracy on 2018/4/23.
 * partially introduced from reminder
 */

public class AlarmActivity extends AlarmBaseActivity {
    private String TAG = "AlarmActivity";
    private String ACTION_AICORE_WINDOW_SHOWN = "kinstalk.com.aicore.action.window_shown";
    private String EXTRA_AICORE_WINDOW_SHOWN = "isShown";
    private String ACTION_ASSIST_KEY = "com.kinstalk.action.assistkey";
    private BroadcastReceiver mAiUIRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_AICORE_WINDOW_SHOWN.equals(action)) {
                boolean isShow = intent.getBooleanExtra(EXTRA_AICORE_WINDOW_SHOWN, false);
                QAILog.d(TAG, "AI UI wake up isShow " + isShow);
                if (isShow) {
                    stopAlarm();
                    finish();
                }
            } else if (ACTION_ASSIST_KEY.equals(action)) {
                QAILog.d(TAG, "assist_key ");
                stopAlarm();
                finish();
            }
        }
    };

    private TextView reminderDate;
    private TextView reminderContent;

    private MediaPlayer mRingMediaPlayer;
    private PowerManager.WakeLock wl;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public static void actionStart(Context context, long time, String title) {
        Intent intent = new Intent(context, AlarmActivity.class);
        intent.putExtra(RemindConstant.ParamTime.KEY_TIME, time);
        intent.putExtra(RemindConstant.ParamTime.KEY_TITLE, title);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QAILog.d(TAG, "AlarmActivity is enter onCreate.");
        setContentView(R.layout.activity_alarm);
        reminderDate = (TextView) findViewById(R.id.reminder_time);
        reminderContent = (TextView) findViewById(R.id.reminder_text);
        findViewById(R.id.reminder_ignore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAlarm();
                finish();
            }
        });
        registerAIReceiver();
        initData();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.SCREEN_DIM_WAKE_LOCK, AlarmActivity.class.getSimpleName());

        setWindowNotAutoClose();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        QAILog.d(TAG, "AlarmActivity is enter onNewIntent.");
        setIntent(intent);
        stopAlarm();
        initData();
    }

    private void setWindowNotAutoClose() {
        try {
            AppUtils.setAutoActivityTimeout(getWindow(), false);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void initData() {
        QAILog.d(TAG, "AlarmActivity startAlarm.");
        String title = getIntent().getStringExtra(RemindConstant.ParamTime.KEY_TITLE);
        long time = getIntent().getLongExtra(RemindConstant.ParamTime.KEY_TIME, 0);
        StringBuilder timeBuilder = new StringBuilder();
        timeBuilder.append(DateUtils.getTimeApm(this, time));
        timeBuilder.append(" ");
        timeBuilder.append(DateUtils.getFormatTime(time));

        reminderContent.setText(TextUtils.isEmpty(title) ? "提醒" : title);
        reminderDate.setText(timeBuilder.toString());
        HabitKidsEntity kidsEntity = MessageManager.getHabitKidsInfo();
        playTTSWithContent("嗨,小微来啦"+ ((kidsEntity != null) ? kidsEntity.nick_name : "宝宝") + " " + (TextUtils.isEmpty(title) ? "提醒" : title) + "的时间到了");
        pauseAudioPlayback();
        startAlarm();

        //当新的提醒开始时，清空之前的倒计时，重新开始
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopAlarm();
                finish();
            }
        }, 10 * 60 * 1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        wl.acquire();
    }

    @Override
    protected void onPause() {
        super.onPause();
        wl.release();
    }

    @Override
    protected void onStop() {
        QAILog.d(TAG, "AlarmActivity AlarmActivity is enter onStop.");
        abandonAudioFocus();
        super.onStop();
        stopAlarm();
    }

    @Override
    public void onDestroy() {
        QAILog.d(TAG, "AlarmActivity AlarmActivity is enter onDestroy.");
        mHandler.removeCallbacksAndMessages(null);
        unRegisterAIReceiver();
        super.onDestroy();
    }

    private void startAlarm() {
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
        mRingMediaPlayer.start();
    }

    private void stopAlarm() {
        QAILog.d(TAG, "AlarmActivity stopAlarm.");
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

    private void pauseAudioPlayback() {
        QAILog.d(TAG, "AlarmActivity gain audioFocus.");
        AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(afChangeListener, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
    }

    private boolean isStateInCall(AudioManager audioManager) {
        if (audioManager == null) {
            return false;
        }
        return ((audioManager.getMode() == AudioManager.MODE_IN_CALL) ||
                (audioManager.getMode() == AudioManager.MODE_IN_COMMUNICATION));
    }


    /*
     * when complete the recording we should abandon the audio focus
     */
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


    private void registerAIReceiver() {
        IntentFilter intentFilter = new IntentFilter(ACTION_AICORE_WINDOW_SHOWN);
        intentFilter.addAction(ACTION_ASSIST_KEY);
        registerReceiver(mAiUIRecevier, intentFilter);
    }

    private void unRegisterAIReceiver() {
        unregisterReceiver(mAiUIRecevier);
    }

    public void playTTSWithContent(String content) {
        try {
            AIManager.getInstance(getApplicationContext()).playTextWithStr(content, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

