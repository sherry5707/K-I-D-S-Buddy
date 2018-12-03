package com.kinstalk.her.qchat.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;

//开始播放声音
public class MusicUtil {
    private static MusicUtil sInstance;
    private MediaPlayer mediaPlayer; // 媒体播放器

    private MusicUtil() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置媒体流类型
        //mediaPlayer.setVolume(0.3f, 0.3f);
    }

    public static MusicUtil getInstance() {
        if (sInstance == null) {
            sInstance = new MusicUtil();
        }
        return sInstance;
    }

    // 停止
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        sInstance = null;
    }

    public void playUrl(Context context, int id) {
        try {
            Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + id);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(context, uri); // 设置数据源
            mediaPlayer.prepare(); // prepare自动播放
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                }
            });
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            //mediaPlayer.setOnCompletionListener(null);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 暂停
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }
}