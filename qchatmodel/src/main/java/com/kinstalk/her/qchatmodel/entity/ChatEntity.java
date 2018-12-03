package com.kinstalk.her.qchatmodel.entity;

import java.io.Serializable;

/**
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 * Created by wangzhipeng on 2018/4/9.
 */

public class ChatEntity implements Serializable {

    public long seq_no;
    public String openid;
    public String message;
    public int direction;
    public long message_time;
    public int message_type;
    public String audio_local_path;
    public String audio_download_path;
    public String audio_md5;
    public String pic_local_path;
    public String pic_url;
    public int status;
    public int duration;

    public ChatEntity() {
        this.seq_no = 1;
        this.openid = "";
        this.message = "";
        this.direction = 1;
        this.message_time = 0L;
        this.message_type = 1;
        this.audio_local_path = "";
        this.audio_download_path = "";
        this.audio_md5 = "";
        this.pic_local_path = "";
        this.pic_url = "";
        this.status = 0;
        this.duration = 0;
    }

    @Override
    public String toString() {
        return "ChatEntity{" +
                "seq_no=" + seq_no +
                ", openid='" + openid + '\'' +
                ", message='" + message + '\'' +
                ", direction=" + direction +
                ", message_time=" + message_time +
                ", message_type=" + message_type +
                ", audio_local_path='" + audio_local_path + '\'' +
                ", audio_download_path='" + audio_download_path + '\'' +
                ", audio_md5='" + audio_md5 + '\'' +
                ", pic_local_path='" + pic_local_path + '\'' +
                ", pic_url='" + pic_url + '\'' +
                ", status=" + status +
                ", duration=" + duration +
                '}';
    }
}
