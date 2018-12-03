/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchat.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.messaging.PrivacyManager;
import com.kinstalk.her.qchat.view.ToastCustom;
import com.kinstalk.her.qchat.voice.QRecorderDialog;
import com.kinstalk.her.qchat.voice.QRecorderManager;
import com.kinstalk.her.qchat.voice.QVoicePlayer;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.NetworkUtils;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatcomm.utils.SDCardUtil;

/**
 * Created by Knight.Xu on 2018/4/8.
 * 触发语音录音并进行录音操作的按钮
 */

/**
 * 自定义Button
 */
public class QRecorderButton extends AppCompatRadioButton implements QRecorderManager.AudioStateListener {

    private static final String TAG = QRecorderButton.class.getSimpleName();
    public static final float MAX_TIME = 60+0.4f;//最大录音时间为60s
    public static final float MIN_TIME = 1f;//最小录音时间为1s

    private static final int DISTANCE_Y_CANCEL = 50;    // 触发取消录音的手指滑动距离
    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_TO_CANCEL = 3;
    private int mCurState = STATE_NORMAL;
    private long beforeClickTime, distancTime;

    private boolean isRecording = false;    // 正在录音

    private QRecorderDialog mDialogManager;
    private QRecorderManager mRecorderManager;

    private float mTime;

    private boolean mReady;    // 是否触发 onLongClick

    public QRecorderButton(Context context) {
        this(context, null);
    }

    public QRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDialogManager = new QRecorderDialog(getContext());
        mRecorderManager = QRecorderManager.getInstance();
        mRecorderManager.setOnAudioStateListener(this);

    }

    OnClickListener onClickListener;

    public void setOnClicListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                /**过滤连续按压
                 */
                if (System.currentTimeMillis() - beforeClickTime > distancTime) {
                    Log.d("EVENT_ACTION", "press");
                    if (onClickListener != null)
                        onClickListener.onClick(this);
                } else
                    //不處理該事件序列。
                    return false;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("EVENT_ACTION", "move");
                if (isRecording) {
                    //根据想x,y的坐标，判断是否想要取消
                    if (wantToCancel(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                    } else {
                        changeState(STATE_RECORDING);
                    }

                    if (mTime >= MAX_TIME) {
                        Log.d(TAG + "1", "MAX");
                        mDialogManager.dismissDialog();
                        mRecorderManager.release(this.getContext());
                        asyncNotifyRecorderFinish(mTime, mRecorderManager.getCurrentFilePath(), mRecorderManager.getCurrentFileName());
                        reset();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                beforeClickTime = System.currentTimeMillis();
                distancTime =400;
                Log.d("EVENT_ACTION", "up");
                //如果onLongClick 没触发
                if (!mReady) {
                    reset();
                    return super.onTouchEvent(event);
                }
                performCickUp();
                break;
        }
        return true;
    }

    private void performCickUp() {

        //触发了onlongclick 没准备好，但是已经prepared 已经start
        //所以消除文件夹
        if (!isRecording || mTime < MIN_TIME) {
            Log.d(TAG, "min time");
            mHandler.sendEmptyMessage(MSG_VOICE_TIMESHORT);
            /*  mDialogManager.tooShort();*/
            mRecorderManager.cancel(getContext());
            distancTime = 1350;
            mHandler.sendEmptyMessageDelayed(MSG_DIALOG_DIMISS, 1300);
        } else if (mCurState == STATE_RECORDING) {//正常录制结束
            isRecording = false;
            mDialogManager.dismissDialog();
            mRecorderManager.release(this.getContext());
            asyncNotifyRecorderFinish(mTime, mRecorderManager.getCurrentFilePath(), mRecorderManager.getCurrentFileName());

        } else if (mCurState == STATE_WANT_TO_CANCEL) {
            isRecording = false;
            mDialogManager.dismissDialog();
            mRecorderManager.cancel(getContext());
        }
        Log.d(TAG + "1", "up");
        reset();
    }

    public void removeAllMessageWhenFinish() {
        mHandler.removeCallbacksAndMessages(null);
    }
    /**
     * 按钮长按 准备录音 包括start
     */
    public boolean doOnLongClick() {
        Log.d(TAG + "1", "isRe" + isRecording);
        if (isRecording) {
            mDialogManager.dismissDialog();
            mRecorderManager.release(this.getContext());
            reset();
        }
        mReady = true;
        QVoicePlayer.stop();

/*        if (PrivacyManager.getInstance(getContext()).isPrivacy()) {
            PrivacyManager.getInstance(getContext()).showPrivacyDialog(getContext(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    QAILog.d(TAG, "Privacy");
                    PrivacyManager.getInstance(getContext()).closePrivacy();
                    dialog.dismiss();
                }
            });
            return false;// super.onTouchEvent(event);
        }*/

        if (!NetworkUtils.isNetworkAvailable(getContext())) {
            ToastCustom.makeText(getContext(), getContext().getResources().getString(R.string.no_network), Toast.LENGTH_LONG).show();
            return false;
        }
        // 判断 sdcard 是否存在， 是否可读, 空间是否足
        if (!SDCardUtil.isSDMounted()) {
            Toast.makeText(getContext()
                    , getContext().getText(R.string.warning_sd_not_found)
                    , Toast.LENGTH_SHORT).show();
            Log.e(TAG, "sd card not mounted");
            return false;
        }
        mRecorderManager.prepareAudio();
        /*isRecording = true;
        changeState(STATE_RECORDING);*/
        return false;
    }

    /**
     * 定时器，下拉状态栏，各种提醒打断录音情况下调用
     */
    public void setResetRes() {
        /* performCickUp();*/
        if (isRecording) {
            reset();
            mDialogManager.dismissDialog();
            mRecorderManager.cancel(getContext());
        }
    }

    /**
     * 录音完成后的回调
     */
    public interface RecorderFinishListener {
        //时长  和 文件
        void onChatRecordFinish(float seconds, String filePath, String fileName);
    }

    private RecorderFinishListener mListener;

    public void setRecorderFinishListener(RecorderFinishListener listener) {
        mListener = listener;
    }

    public void asyncNotifyRecorderFinish(final float seconds, final String filePath, final String fileName) {
        Log.d("FileUtils","mTime:"+mTime);
        if (mListener != null)
            mListener.onChatRecordFinish(seconds, filePath, fileName);
    }

    // 获取音量大小的Runnable
    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRecording) {
                try {
                    Thread.sleep(200);
                    mTime += 0.2;
                    mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static final int MSG_AUDIO_PREPARED = 0X110;
    private static final int MSG_VOICE_CHANGED = 0X111;
    private static final int MSG_DIALOG_DIMISS = 0X112;
    /*录音时间太短*/
    private static final int MSG_VOICE_TIMESHORT = 0X113;

    private Handler mHandler = new ButtonHandler();

    private class ButtonHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    //TODO 真正现实应该在audio end prepared以后
                    mDialogManager.showRecordingDialog();
                    isRecording = true;

                    new Thread(mGetVoiceLevelRunnable).start();
                    break;
                case MSG_VOICE_CHANGED:
                    mDialogManager.updateVoiceLevel(mRecorderManager.getVoiceLevel(7));

                    break;
                case MSG_DIALOG_DIMISS:
                    mDialogManager.dismissDialog();
                    break;
                case MSG_VOICE_TIMESHORT:
                    mDialogManager.tooShort();
                    break;
            }
        }
    }

    @Override
    public void wellPrepared() {
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }

    /**
     * 恢复状态 标志位
     */
    private void reset() {
        isRecording = false;
        mReady = false;
        changeState(STATE_NORMAL);
        mTime = 0;

    }

    public void resetToNormal() {
        if (isRecording) {
            mRecorderManager.release(getContext());
            isRecording = false;
            mDialogManager.dismissDialog();
            mReady = false;
            changeState(STATE_NORMAL);
            mTime = 0;
        }
    }

    private boolean wantToCancel(int x, int y) {
        //如果左右滑出 button
        if (x < 0 || x > getWidth()) {
            return true;
        }
        //如果上下滑出 button  加上我们自定义的距离
        if (y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }
        return false;
    }

    private void changeState(int state) {
        if (mCurState != state) {
            mCurState = state;
            switch (state) {
                case STATE_NORMAL:
//                    setBackgroundResource(R.drawable.shape_recorder_btn_normal);
//                    setText(R.string.recorder_normal);
                    break;
                case STATE_RECORDING:
//                    setBackgroundResource(R.drawable.shape_recorder_btn_press);
//                    setText(R.string.recorder_recording);

                    if (isRecording) {
                        mDialogManager.showRecording();
                    }
                    break;
                case STATE_WANT_TO_CANCEL:
//                    setBackgroundResource(R.drawable.shape_recorder_btn_press);
//                    setText(R.string.recorder_want_cancel);
                    mDialogManager.wantToCancel();
                    break;
            }
        }
    }
}