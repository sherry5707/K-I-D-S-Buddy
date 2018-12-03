package com.kinstalk.her.qchatmodel.entity;

import android.text.TextUtils;

import com.kinstalk.her.qchatmodel.ChatProviderHelper;

/**
 * Created by Tracy on 2018/4/20.
 */

public class QReminderMessage {

    private String text;
    private WxUserEntity user;
    private RemindEntity remindEntity;

    private int status;
    private int direction;

    public QReminderMessage(WxUserEntity user, RemindEntity remind) {
        this.remindEntity = remind;
        this.user = user;
    }
    public String getText() {
        return text;
    }

    public WxUserEntity getUser() {
        return this.user;
    }

    public String getStatus() {
        return "remind";
    }

    public void setText(String text) {
        this.text = text;
    }

    public RemindEntity getRemindEntity() {
        return remindEntity;
    }

    public void setUser(WxUserEntity user) {
        this.user = user;
    }

    public void setRemindEntity(RemindEntity remindEntity) {
        this.remindEntity = remindEntity;
    }

    public boolean hasReminder(){
        return remindEntity != null && !TextUtils.isEmpty(remindEntity.content);
    }
}
