package com.kinstalk.her.qchat.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.common.AICoreCallbackInterface;
import com.kinstalk.her.qchat.dialog.TaskStarDialog;
import com.kinstalk.qloveaicore.AIManager;
import com.tencent.xiaowei.info.QLoveResponseInfo;
import com.tencent.xiaowei.info.XWResponseInfo;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import com.kinstalk.util.AppUtils;

import static com.kinstalk.her.qchat.activity.AIConstants.*;

/**
 * Created by wangyong on 18/5/23.
 */

public class AIBaseActivity extends Activity implements TaskStarDialog.OnContentClickListener , AICoreCallbackInterface {

    private static final String TAG = "AIBaseActivity";
    private final LocalInfoIntentReceiver pLocalkeyInfoReceiver = new LocalInfoIntentReceiver();

    private final FinishInfoIntentReceiver pFinishkeyInfoReceiver = new FinishInfoIntentReceiver();


    private final String ACTION_ASSIST_KEY = "com.kinstalk.action.assistkey";
    private AudioRecord audioRecord = null;

    private boolean isTtsPlay = true;

    private int nSceneIndex = 0;
    protected boolean isForegroud = false;
    private boolean isPlayingStory;
    private boolean isAiserviceConnected = false;
    /**
     * 用来记录用户是否语音停止故事播放。如果停止了
     * 早起流程就打卡
     * 睡觉流程就停止播故事，即使再进入这个界面也不播故事
     */
    private boolean isVoiceStopStory = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateWindowFlags();
        setAutoActivityTimeout();

        getParamsByIntent();

        initAudioRecord();

        IntentFilter filter = new IntentFilter();
        filter.addAction(SLEEP_OR_WAKEUP_STOP_ACTION);
        filter.addAction(ACTION_PLAY_COMPLETE);
        registerReceiver(pFinishkeyInfoReceiver, filter);
        isPlayingStory = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(pLocalkeyInfoReceiver);
        LockManager.getInstance(this).releaseWakeLock();
        isForegroud = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerLocalReceiver();
        isForegroud = true;
        if(!(this instanceof TaskSleepOrWakeupActivity)) {
            LockManager.getInstance(this).requireScreenOn();
        }
    }

    protected boolean ttsPlaying() {
        return isTtsPlay;
    }

    private void updateWindowFlags() {
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                        // bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
    }

    private class LocalInfoIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (TextUtils.equals(action, ACTION_ASSIST_KEY)) {
                assistInfoIntentReceiver();
            } else if (TextUtils.equals(action, AICoreDefine.ACTION_TXSDK_TTS)) {
                String state = intent.getStringExtra(AICoreDefine.ACTION_TXSDK_EXTRA_TTS_STATE);
                if (AICoreDefine.ACTION_TXSDK_EXTRA_TTS_STOP.equals(state)) {
                    isTtsPlay = false;
                    playTtsTextFinished();
                }
            }
        }
    }

    private class FinishInfoIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(TextUtils.equals(action, SLEEP_OR_WAKEUP_STOP_ACTION)){
                cancelCurrentAlarm();
                finish();
            }else if (TextUtils.equals(action, ACTION_PLAY_COMPLETE)) {
                playCompleteIntentReceiver();
            }
        }
    }

    protected void playCompleteIntentReceiver() {
        isPlayingStory = false;
        //ui do
    }

    protected void assistInfoIntentReceiver() {
        //ui do
    }

    protected void playTtsTextFinished() {
        //ui do
    }

    protected int getUpdateSceneIndex() {
        return nSceneIndex;
    }

    protected void updateSceneIndex(int index) {
        nSceneIndex = index;
    }

    //Override
    protected void switchScene(final int sceneIndex) {
        //
    }

    public void gotoHome() {
        Intent intent1 = new Intent(Intent.ACTION_MAIN, null);
        intent1.addCategory(Intent.CATEGORY_HOME);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        this.startActivity(intent1);
    }

    private void registerLocalReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ASSIST_KEY);
        filter.addAction(AICoreDefine.ACTION_TXSDK_TTS);
        registerReceiver(pLocalkeyInfoReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCurrentAlarm();
        releaseRecord();
        unregisterReceiver(pFinishkeyInfoReceiver);
        releaseMediaPlayer();
    }

    protected void setAutoActivityTimeout() {
        try {
            AppUtils.setAutoActivityTimeout(getWindow(), false);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    public void onCallback(Object obj) {
        if(TextUtils.isEmpty(obj.toString()))
            return;
        QLoveResponseInfo rspData = (QLoveResponseInfo)obj;
        XWResponseInfo xwResponseInfo = rspData.xwResponseInfo;
        if (xwResponseInfo == null)
            return;
        Log.d(TAG, "QLoveResponseInfo" +rspData.toString());
        try {
            JSONObject object = new JSONObject(rspData.xwResponseInfo.responseData);
            String intentName = object.optString("intentName");
            if ("wakeup_stopstory".equals(intentName)){
                stopStories();
                setStoryPlayingStatus(false);
                setVoiceStopStory(true);
            }else {
                localResult(intentName);
            }

        }catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        KidsService.addToCallback(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        KidsService.removeFromCallback(this);
    }


    //override
    protected void localResult(String string) {
    }



    protected void getParamsByIntent(){
        isAiserviceConnected = false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        isForegroud = true;
    }

    protected void switchPrivacy(boolean privacyMode) {
        try {
            AppUtils.setPrivacyMode(getApplicationContext(), privacyMode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void resumeStories(){
        isPlayingStory = true;
        sendBroadcast(new Intent("com.kinstalk.audio.action.play"));
    }

    protected void playStories(String text){

        isPlayingStory = true;
        sendBroadcast(new Intent("com.kinstalk.audio.action.stop"));
        Intent intent = new Intent("com.kinstalk.audio.action.playtext");
        intent.putExtra("request_text", text);
        sendBroadcast(intent);
    }

    protected void stopStories(){
        sendBroadcast(new Intent("com.kinstalk.audio.action.stop"));
    }

    protected void setStoryPlayingStatus(boolean status){
        isPlayingStory = status;
    }

    protected void setVoiceStopStory(boolean status) {
        isVoiceStopStory = status;
    }

    protected boolean storyPlaying(){
        return isPlayingStory;
    }

    protected boolean isVoiceStopStory(){
        return  isVoiceStopStory;
    }

    public static void delayStopStories(final Context context) {

        new Handler().postDelayed(new Runnable() { //imitation of internet connection
            @Override
            public void run() {
                context.sendBroadcast(new Intent("com.kinstalk.audio.action.stop"));
            }
        }, 8000);
        Log.d(TAG, " actionStart(Context context, long time, String nickName, int star_1) ");
    }


    protected void nextStories(){
        sendBroadcast(new Intent("com.kinstalk.audio.action.next"));
    }

    protected void startWakeUp() {
        try {
            AppUtils.wakeupDevice(getApplicationContext(), SystemClock.uptimeMillis());
        } catch (Exception e) {
        }
    }

    protected void gotoSleep() {
        try {
            AppUtils.goToSleep(getApplicationContext(), SystemClock.uptimeMillis());
        } catch (Exception e) {
        }

    }

    protected void playTtsText(final String text) {
        isTtsPlay = true;
        AIManager.getInstance(getApplicationContext()).playTextWithStr(text, null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (text.contains("你好小微")) {
                    try {
                        startRecord();
                        Thread.sleep(8000);
                        stopRecord();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    private void initAudioRecord(){
        //16K采集率
        int frequency = 16000;
        //格式
        int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
    }

    public void startRecord() {
        audioRecord.startRecording();
    }

    private void releaseRecord(){
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    public void stopRecord() {
        if (audioRecord != null) {
            audioRecord.stop();
        }
    }

    private TaskStarDialog taskStarDialog = null;

    public void showTaskStarDialog(final boolean error, final int startCount) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != taskStarDialog && taskStarDialog.isShowing()) {

                } else {
                    taskStarDialog = new TaskStarDialog.Builder(QchatApplication.getInstance())
                            .setOnContentClickListener(AIBaseActivity.this)
                            .create();
                    taskStarDialog.show();
                }

                if (error) {
                    taskStarDialog.showFail();
                } else {
                    taskStarDialog.showSuccess(startCount);
                }
            }
        });
    }

    @Override
    public void onClickTaskStarDialog() {
    }

    protected ImageView createImageView(int resID) {

        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        imageView.setImageResource(resID);
        FrameLayout.LayoutParams subFrameLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        subFrameLayout.setMargins(0, 0, 0, 0);
        imageView.setLayoutParams(subFrameLayout);
        return imageView;
    }

    @Override
    public void onClickTaskStarClose() {
         playTtsText(" ");
         stopStories();
         finish();
    }

    @Override
    public void onClickTaskStarRetry() {
    }

    private MediaPlayer mediaPlayer = null;
    int nPlayCount = 0;

    protected void startPlay(int res, int playCount){

        nPlayCount = playCount;

        final Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + res);
        try {

            if(mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setDataSource(this, uri);
            mediaPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                -- nPlayCount;
                if(nPlayCount > 0) {
                    mediaPlayer.start();
                }
            }
        });

    }

    protected void stopMediaPlay(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            nPlayCount = 0;
            mediaPlayer.stop();
        }
    }

    protected void releaseMediaPlayer(){
        if (mediaPlayer != null) {
            nPlayCount = 0;
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    protected void setAlarm(String action, int amount) {
        Log.d(TAG, "setAlarm(String action, int amount)");

        cancelAlarm(action, this);
        AlarmManager am;
        am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setClass(this, SleepAlarmReceiver.class);


        PendingIntent pi = PendingIntent.getBroadcast(this, 0x101,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, amount);
        calendar.add(Calendar.SECOND, 10);

        if (am != null) {
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        }

    }

    protected void cancelAlarm(String action, Context context) {
        Log.d(TAG, "cancelAlarm(String action, Context context) ");

        Intent intent = new Intent();
        intent.setAction(action);
        intent.setClass(this, SleepAlarmReceiver.class);

        PendingIntent sender = PendingIntent.getBroadcast(
                context, 0x101, intent, PendingIntent.FLAG_NO_CREATE);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }

    protected void cancelCurrentAlarm() {
        Log.d(TAG, "cancelAlarm(String action, Context context) ");
    }

    public static void stopPlay(Context context) {
        stopSleepOrWakeup();

        String ACTION_TXSDK_PLAY_TTS = "kingstalk.action.wateranimal.playtts";
        Intent intent = new Intent(ACTION_TXSDK_PLAY_TTS);
        Bundle bundle = new Bundle();
        bundle.putString("text", " ");
        intent.putExtras(bundle);
        QchatApplication.getInstance().sendBroadcast(intent);

    }

    public static void stopSleepOrWakeup() {
        QchatApplication.getInstance().sendBroadcast(new Intent("com.kinstalk.audio.action.stop"));
        QchatApplication.getInstance().sendBroadcast(new Intent("com.kinstalk.her.qchat.stop_sleep_or_wakeup"));
    }
}
