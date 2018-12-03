/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchatmodel.entity;

import android.text.TextUtils;

import com.kinstalk.her.qchatmodel.ChatProviderHelper;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

/*
 * Created by Knight.Xu on 2018/4/13.
 */
public class QHomeworkMessage implements IMessage,
        MessageContentType.Image, /*this is for default image messages implementation*/
        MessageContentType /*and this one is for custom content type (in this case - voice message)*/ {

    private String id;
    private String text;
    private Date createdAt;
    private WxUserEntity user;
    private HomeWorkEntity homeWorkEntity;

    private int status;
    private int direction;

    public QHomeworkMessage(String id, WxUserEntity user, HomeWorkEntity homework) {
        this(id, user, homework, new Date());
    }

    public QHomeworkMessage(String id, WxUserEntity user, HomeWorkEntity homework, Date createdAt) {
        this.id = id;
        this.homeWorkEntity = homework;
        this.user = user;
        this.createdAt = createdAt;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isOutComming() {
        return direction == ChatProviderHelper.DIRECTION_OUT;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public WxUserEntity getUser() {
        return this.user;
    }

    public String getStatus() {
        return "Sent";
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String getImageLocalPath() {
        return null;
    }

    @Override
    public String getImageUrl() {
        return null;
    }

    public HomeWorkEntity getHomeWorkEntity() {
        return homeWorkEntity;
    }

    public void setUser(WxUserEntity user) {
        this.user = user;
    }

    public void setHomeWorkEntity(HomeWorkEntity homeWorkEntity) {
        this.homeWorkEntity = homeWorkEntity;
    }

    public boolean hasHomework(){
        return homeWorkEntity != null && !TextUtils.isEmpty(homeWorkEntity.content);
    }
}
