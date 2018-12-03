package com.kinstalk.her.qchat.voice;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchatcomm.base.BaseApplication;
import com.kinstalk.her.qchatcomm.utils.QAILog;

import java.io.IOException;

public class RingPlayer {

    private String TAG = "RingPlayer";

    private MediaPlayer mRingMediaPlayer;
    private static RingPlayer instance;
    private AudioManager mAudioManager;

    public static RingPlayer getInstance() {
        synchronized (AudioFocusController.class) {
            if(instance == null) {
                instance = new RingPlayer();
            }
            return instance;
        }
    }

    private void requestAudioFocus() {
        QAILog.d(TAG, "RingPlayer getAudioFocus");
        mAudioManager = (AudioManager) BaseApplication.getInstance().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
    }

    private void abandonAudioFocus() {
        mAudioManager.abandonAudioFocus(afChangeListener);
    }

    public void startRing() {
        requestAudioFocus();
        final Uri uri = Uri.parse("android.resource://" + QchatApplication.getInstance().getPackageName() + "/" + R.raw.ring_new);
        try {
            mRingMediaPlayer = new MediaPlayer();
            mRingMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mRingMediaPlayer.setLooping(false);
            mRingMediaPlayer.setDataSource(QchatApplication.getInstance(), uri);
            mRingMediaPlayer.prepare();
            mRingMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopRing();
                    abandonAudioFocus();
                }
            });

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRingMediaPlayer.start();
    }

    public void stopRing() {
        QAILog.d(TAG, "RingPlayer stopAlarm.");
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

    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
                QAILog.d(TAG, "RingPlayer AudioFocus.changed:AUDIOFOCUS_LOSS_TRANSIENT");
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // Stop playback
                stopRing();
                QAILog.d(TAG, "RingPlayer AudioFocus.changed:AUDIOFOCUS_LOSS");
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // Lower the volume
                QAILog.d(TAG, "RingPlayer AudioFocus.changed:AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback or Raise it back to normal
                QAILog.d(TAG, "RingPlayer AudioFocus.changed:AUDIOFOCUS_GAIN");
            }
        }
    };
}
