/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchatmodel.beans;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by Knight.Xu on 2018/4/8.
 */

public class VoiceBean implements Serializable {

    private int time;
    private String filePath;
    private String url;
    private int duration; //seconds

    public VoiceBean(String url, int duration) {
        this.url = url;
        this.duration = duration;
    }

    public VoiceBean(int time, String filePath, int duration) {
        this.time = time;
        this.filePath = filePath;
        this.duration = duration;
    }

    public VoiceBean(int time, String filePath) {
        this.time = time;
        this.filePath = filePath;
    }

    public VoiceBean(String url, String filePath, int duration) {
        this.url = url;
        this.filePath = filePath;
        this.duration = duration;
    }


    public void setTime(int time) {
        this.time = time;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getTime() {
        return time;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getUrl() {
        return url;
    }

    public int getDuration() {
        return duration;
    }

    public boolean hasContent() {
        return !TextUtils.isEmpty(url) || !TextUtils.isEmpty(filePath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoiceBean voiceBean = (VoiceBean) o;
        return time == voiceBean.time &&
                duration == voiceBean.duration &&
                TextUtils.equals(filePath, voiceBean.filePath) &&
                TextUtils.equals(url, voiceBean.url);
    }
}
