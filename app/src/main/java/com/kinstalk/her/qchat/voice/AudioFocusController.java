package com.kinstalk.her.qchat.voice;

import android.content.Context;
import android.media.AudioManager;

import com.kinstalk.her.qchatcomm.base.BaseApplication;
import com.kinstalk.her.qchatcomm.utils.QAILog;

/**
 * Created by Tracy on 2018/4/23.
 */

public class AudioFocusController implements AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "AudioFocusController";
    private static AudioFocusController INSTANCE;
    protected boolean mHasAudioFocus = true;
    private AudioManager mAudioManager;
    public boolean mIsPlayingBeforePause = false;

    private AudioFocusController() {
        mAudioManager = (AudioManager) BaseApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
    }

    public static AudioFocusController init() {
        synchronized (AudioFocusController.class) {
            if (INSTANCE == null) {
                INSTANCE = new AudioFocusController();
            }
            return INSTANCE;
        }
    }

    public boolean requestFocus() {
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        QAILog.d(TAG, "requestFocus: result -" + result);
        mHasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        return mHasAudioFocus;
    }

    public void abandonFocus() {
        QAILog.d(TAG, "abandonFocus");
        mAudioManager.abandonAudioFocus(this);
    }

    public boolean isHasAudioFocus() {
        QAILog.d(TAG, "isHasAudioFocus: " + mHasAudioFocus);
        return isHasAudioFocus();
    }

    protected void processAudioFocus(int focusChange) {
        QAILog.d(TAG, "processAudioFocus:" + focusChange);

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                mHasAudioFocus = false;

                QAILog.d(TAG, "Keven Debug: processAudioFocus run.1");

//                QVoicePlayer.stop();

                if (QVoicePlayer.isIsPlaying()) {
                    QVoicePlayer.pause();
                }

                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN:
                mHasAudioFocus = true;

                if (QVoicePlayer.isIsPlaying()) {
                    QVoicePlayer.resume();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                QAILog.d(TAG, "loss focus");
                mHasAudioFocus = false;

                QAILog.d(TAG, "Keven Debug: processAudioFocus run.2");

//                QVoicePlayer.stop();
                mAudioManager.abandonAudioFocus(this);
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mHasAudioFocus = false;
                break;
            default:
                break;
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        QAILog.d(TAG, "onAudioFocusChange:" + focusChange);
        processAudioFocus(focusChange);
    }
}
