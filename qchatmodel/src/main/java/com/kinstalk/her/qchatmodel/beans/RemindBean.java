/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchatmodel.beans;

import com.kinstalk.her.qchatcomm.utils.DateUtils;

import java.io.Serializable;

/**
 * Created by Knight.Xu on 2018/4/7.
 */
public class RemindBean implements Serializable {

    private String title; // 标题
    private String content; // 内容
    private long remindTime; // 提醒时间
    private  String repeat; // 重复时间

    public RemindBean(String content, long remindTime) {
        this.content = content;
        this.remindTime = remindTime;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return content + "                     " + DateUtils.getRemindTime(remindTime);
    }
}
