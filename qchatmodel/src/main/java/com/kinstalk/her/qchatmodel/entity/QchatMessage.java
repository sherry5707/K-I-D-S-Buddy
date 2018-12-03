/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchatmodel.entity;

import com.kinstalk.her.qchatmodel.ChatProviderHelper;
import com.kinstalk.her.qchatmodel.beans.EmojiBean;
import com.kinstalk.her.qchatmodel.beans.ImageBean;
import com.kinstalk.her.qchatmodel.beans.VoiceBean;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

/*
 * Created by Knight.Xu on 2018/4/7.
 */
public class QchatMessage implements IMessage,
        MessageContentType.Image, /*this is for default image messages implementation*/
        MessageContentType /*and this one is for custom content type (in this case - voice message)*/ {

    private String id;
    private String text;
    private Date createdAt;
    private WxUserEntity user;
    private ImageBean image;
    private VoiceBean voice;
    private EmojiBean emoji;

    private int messageType;
    private int status;
    private int direction;

    public QchatMessage(String id, WxUserEntity user, String text) {
        this(id, user, text, new Date());
    }

    public QchatMessage(String id, WxUserEntity user, String text, Date createdAt) {
        this.id = id;
        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
    }

    public void setStatus(int status) {
        this.status = status;
    }

//    public void setDirection(int direction) {
//        this.direction = direction;
//    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
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

    public boolean isOnline() {
        return this.user != null && user.isOnline();
    }

    @Override
    public String getImageUrl() {
        return image == null ? null : image.getUrl();
    }

    @Override
    public String getImageLocalPath() {
        return image == null ? null : image.getLocalPath();
    }

    public VoiceBean getVoice() {
        return voice;
    }

    public EmojiBean getEmoji() {
        return emoji;
    }

    public void setEmoji(EmojiBean emoji) {
        this.emoji = emoji;
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

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setImage(ImageBean image) {
        this.image = image;
    }

    public ImageBean getImage() {
        return image;
    }

    public int getDirection() {
        return direction;
    }

    public void setVoice(VoiceBean voice) {
        this.voice = voice;
    }

    public boolean hasVoiceContent (){
        return voice != null && voice.hasContent();
    }
}
