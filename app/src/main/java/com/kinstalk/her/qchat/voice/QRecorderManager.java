/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchat.voice;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;

import com.kinstalk.her.qchat.messaging.PrivacyManager;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.SystemTool;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Knight.Xu on 2018/4/8.
 * 录音管理类,用于控制录制状态。
 */

public class QRecorderManager {

    public static final int MAX_DURATION = 1000 * 60+380;// 微信服务号限制：最大录音时长60s;
    public static final int MAX_FILE_SIZE = 1000 * 1000 * 2;// 微信服务号限制：最大录音大小2M;
    public static final String VOICE_DIR = Environment.getExternalStorageDirectory() + "/qchat/voices";

    private MediaRecorder mMediaRecorder;
    private String mCurrentFilePath;//文件储存路径
    private String mCurrentFileName;
    private static QRecorderManager mInstance;
    //表明MediaRecorder是否进入prepare状态（状态为true才能调用stop和release方法）
    private int BASE = 1;
    private boolean isPrepared;

    private QRecorderManager() {
    }

    public String getCurrentFilePath() {
        return mCurrentFilePath;
    }

    public String getCurrentFileName() {
        return mCurrentFileName;
    }

    /**
     * 准备完毕接口
     */
    public interface AudioStateListener {
        void wellPrepared();
    }

    public AudioStateListener mListener;

    public void setOnAudioStateListener(AudioStateListener listener) {
        mListener = listener;
    }

    /**
     * 单例
     *
     * @return QRecorderManager
     */
    public static QRecorderManager getInstance() {
        if (mInstance == null) {
            synchronized (QRecorderManager.class) {
                if (mInstance == null) {
                    mInstance = new QRecorderManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 准备
     */
    public void prepareAudio() {
        try {
            isPrepared = false;
            File dir = new File(VOICE_DIR);//创建文件夹
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = generateFileName();//随机生成文件名
            File file = new File(dir, fileName);//创建文件
            mCurrentFilePath = file.getAbsolutePath();
            mCurrentFileName = fileName;

            /* ①Initial：实例化MediaRecorder对象 */
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置麦克风为音频源
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);//设置音频格式
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);//设置音频编码
            /* ③准备 */
            mMediaRecorder.setOutputFile(file.getAbsolutePath());//设置输出文件
            mMediaRecorder.setMaxDuration(MAX_DURATION);
            mMediaRecorder.setMaxFileSize(MAX_FILE_SIZE);
            AudioFocusController.init().requestFocus();
            mMediaRecorder.prepare();
            /* ④开始 */
            mMediaRecorder.start();

            //准备结束
            isPrepared = true;
            if (mListener != null) {
                mListener.wellPrepared();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 随机生成文件的名称
     *
     * @return
     */
    private String generateFileName() {
        return QAIConfig.getMacForSn() + "_" + UUID.randomUUID().toString() + ".amr";
    }

    /**
     * 获取音量等级
     */
    public int getVoiceLevel(int maxLevel) {
        if (isPrepared) {
            try {
                //mMediaRecorder.getMaxAmplitude() 范围:1-32767
                return maxLevel * mMediaRecorder.getMaxAmplitude() / 32768 + 1;//最大值 * [0,1）+ 1

                /* double ratio = (double) mMediaRecorder.getMaxAmplitude();
                double db = 0;// 分贝
                if (ratio > 1) {
                    db = 20 * Math.log10(ratio);
                }*/
            } catch (Exception e) {
            }
        }
        return 1;
    }

    public double getVoiceLevel() {
        if (mMediaRecorder != null) {
            double ratio = (double) mMediaRecorder.getMaxAmplitude();
            double db = 0;// 分贝
            if (ratio > 1) {
                db = 20 * Math.log10(ratio);
                return db;
            }
        }

        return 0f;
    }

    public void release(Context context) {
        AudioFocusController.init().abandonFocus();
        if (PrivacyManager.getInstance(context).isPrivacy()) {
            QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                if (mMediaRecorder != null) {
                    isPrepared = false;
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                }
            }
        });
        } else {
            try {
                if (mMediaRecorder != null) {
                    isPrepared = false;
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                }
            }catch (Exception e) {
                if(mMediaRecorder != null) {
                    isPrepared = false;
                    mMediaRecorder.reset();
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                }
            }
        }
    }

    public void cancel(Context context) {
        release(context);
        //删除产生的文件
        if (mCurrentFilePath != null) {
            File file = new File(mCurrentFilePath);
            file.delete();
            mCurrentFilePath = null;
        }
    }

    public boolean isPrepard() {
        return isPrepared;
    }

}
