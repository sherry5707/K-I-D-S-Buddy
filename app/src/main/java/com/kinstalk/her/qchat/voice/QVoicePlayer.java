/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchat.voice;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;

import com.kinstalk.her.qchatcomm.utils.QAILog;

/**
 * Created by Knight.Xu on 2018/4/8.
 * MediaManger,用于播放音频
 * Tracy fixed bug-- if click two bean at same time, on animation will not stop
 */

public class QVoicePlayer {

    private static final String TAG = QVoicePlayer.class.getSimpleName();

    private static MediaPlayer mMediaPlayer;

    private static boolean isPause = true;//是否是暂停
    private static MediaPlayer.OnCompletionListener mOnCompletionListener;
    private static String mFilepath = "";

    private static boolean isPlaying = false;

    /**
     * 播放音频
     * TODO 后面需要处理audio focus，未读状态，播放动画
     */
    public static void playSound(String filePath, MediaPlayer.OnCompletionListener onCompletionListener) {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mMediaPlayer.reset();
                    return false;
                }
            });
        } else {
            QAILog.d("filePath" + filePath + isPause);
            if (!TextUtils.isEmpty(filePath) && filePath.equals(mFilepath) && !isPause) {

                QAILog.d(TAG, "Keven Debug: playSound run.");

                if (mMediaPlayer.isPlaying())
                    mMediaPlayer.stop();
                if (mOnCompletionListener != null)
                    mOnCompletionListener.onCompletion(mMediaPlayer);
                isPause = true;
                return;
            }
            if (mOnCompletionListener != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
            mMediaPlayer.reset();
        }

        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnCompletionListener(onCompletionListener);
            mOnCompletionListener = onCompletionListener;
            mMediaPlayer.setDataSource(filePath);
            mFilepath = filePath;
            mMediaPlayer.prepare();
            AudioFocusController.init().requestFocus();
            mMediaPlayer.start();
            isPause = false;
            setIsPlaying(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 開始播放
     */
    public static void start() {

        QAILog.d(TAG, "Keven Debug: start run.");

        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            isPause = false;
        }
    }

    /**
     * 暂停
     */
    public static void pause() {

        QAILog.d(TAG, "Keven Debug: pause run.");

        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            isPause = true;
           // setIsPlaying(false);
        }
    }

    /**
     * 继续
     */
    public static void resume() {

        QAILog.d(TAG, "Keven Debug: resume run.");

        if (mMediaPlayer != null && isPause) {
            mMediaPlayer.start();
            isPause = false;
         //   setIsPlaying(true);
        }
    }

    //停止
    public static void stop() {
        if (mMediaPlayer != null) {
            if (mOnCompletionListener != null && mMediaPlayer.isPlaying()) {

                QAILog.d(TAG, "Keven Debug: stop run.");
                setIsPlaying(false);
                mMediaPlayer.stop();
                mOnCompletionListener.onCompletion(mMediaPlayer);
            }
            mMediaPlayer.reset();
        }
    }

    public static void setPause(Boolean status) {

        QAILog.d(TAG, "Keven Debug: setPause run.");

        isPause = status;
    }

    /**
     * 释放资源
     */
    public synchronized static void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            isPause = true;
            mMediaPlayer = null;
            setIsPlaying(false);
        }
    }

    public static boolean isIsPlaying() {
        return isPlaying;
    }

    public static void setIsPlaying(boolean isPlaying) {
        QVoicePlayer.isPlaying = isPlaying;
    }
}
