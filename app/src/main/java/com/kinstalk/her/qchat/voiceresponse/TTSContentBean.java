package com.kinstalk.her.qchat.voiceresponse;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class TTSContentBean {

    private int id;

    private String content;

    @Generated(hash = 1138589405)
    public TTSContentBean(int id, String content) {
        this.id = id;
        this.content = content;
    }

    @Generated(hash = 601293234)
    public TTSContentBean() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
