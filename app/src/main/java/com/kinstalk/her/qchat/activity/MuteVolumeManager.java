package com.kinstalk.her.qchat.activity;

import android.os.Handler;
import android.os.Message;

/**
 * 音量淡入淡出
 * Created by wangyong on 18/5/22.
 */
public class MuteVolumeManager {
    // action
    private static final int ACTION_RESTORE_VALUE = 0x1;
    private static final int ACTION_STOP_VALUE = 0x2;
    // 淡出与淡出时间
    private int restoreTime = 2000; // 恢复 淡入时间
    private int stopTime = 2000; // 静音 淡出时间
    private float muteCurVolume = 0f;
    private float muteTargetVolume = 0f;
    private float unMuteCurVolume = 0f;
    private float unMuteTargetVolume = 0f;
    private boolean isMuteing = false;
    private boolean isUnMuteing = false;
    // listenter
    private VolumeListenter volumeListenter;

    public void setVolumeListenter(VolumeListenter volumeListenter) {
        this.volumeListenter = volumeListenter;
    }

    // handler control
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ACTION_RESTORE_VALUE) {
                if (muteCurVolume < muteTargetVolume) {
                    muteCurVolume += 0.1f;
                    if (muteCurVolume > 1) {
                        muteCurVolume = 1;
                    }


                    if (muteCurVolume < muteTargetVolume) {
                        notifyVolumeChange(muteCurVolume);//最大1.0,是个百分比
                        handler.sendEmptyMessageDelayed(ACTION_RESTORE_VALUE, restoreTime / 10);//2秒恢复音量100%音量
                    } else {
                        muteCurVolume = muteTargetVolume;
                        notifyVolumeChange(muteCurVolume);//最大1.0,是个百分比
                        if (volumeListenter != null && isUnMuteing) {
                            volumeListenter.onCompleteUnMute(muteCurVolume);
                        }
                        isUnMuteing = false;
                    }
                } else if (muteCurVolume >= muteTargetVolume) {
                    notifyVolumeChange(muteTargetVolume);//最大1.0,是个百分比
                    if (volumeListenter != null && isUnMuteing) {
                        volumeListenter.onCompleteUnMute(muteCurVolume);
                    }
                    isUnMuteing = false;
                }
            } else if (msg.what == ACTION_STOP_VALUE) {
                if (unMuteCurVolume > unMuteTargetVolume) {
                    unMuteCurVolume -= 0.1;
                    if (unMuteCurVolume < 0) {
                        unMuteCurVolume = 0;
                    }
                    notifyVolumeChange(unMuteCurVolume);
                    if (unMuteCurVolume > unMuteTargetVolume) {
                        handler.sendEmptyMessageDelayed(ACTION_STOP_VALUE, stopTime / 10);//0.5秒恢复音量100%音量
                    } else {
                        if (volumeListenter != null && isMuteing) {
                            volumeListenter.onCompleteMute(unMuteCurVolume);
                        }
                        isMuteing = false;
                    }
                } else {
                    notifyVolumeChange(0f);//静音
                    if (volumeListenter != null && isMuteing) {
                        volumeListenter.onCompleteMute(unMuteCurVolume);
                    }
                    isMuteing = false;
                }

            }
        }
    };

    /**
     * 恢复音量,淡入效果
     */
    public void unMute(float curVolume, float tagetVolume) {
        if (isUnMuteing) return;

        isUnMuteing = true;
        this.muteTargetVolume = tagetVolume;
        this.muteCurVolume = curVolume;
        handler.sendEmptyMessage(ACTION_RESTORE_VALUE);
    }

    /**
     * 静音,淡出效果
     */
    public void mute(float curVolume) {
        if (isMuteing) return;

        isMuteing = true;
        this.unMuteCurVolume = curVolume;
        this.unMuteTargetVolume = 0f;
        handler.sendEmptyMessage(ACTION_STOP_VALUE);
    }

    private void notifyVolumeChange(float volume) {
        if (volumeListenter != null) {
            volumeListenter.onVolumeChange(volume);
        }
    }

    public interface VolumeListenter {
        public void onCompleteMute(float curVolume);
        public void onCompleteUnMute(float curVolume);
        public void onVolumeChange(float volume);
    }

} 