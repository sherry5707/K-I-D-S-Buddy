package com.kinstalk.her.qchatmodel.entity;

import java.io.Serializable;

/**
 * Created by wangzhipeng on 2018/4/12.
 */

public class HomeWorkEntity implements Serializable {
    public String openid;
    public String homeworkid;
    public String title;
    public String content;
    public String url;
    public String type;
    public long assign_time;
    public long finish_time;
    public int status;


    public HomeWorkEntity(String content) {
        this.content = content;
    }

    public HomeWorkEntity() {
        this.openid = "";
        this.homeworkid = "";
        this.title = "";
        this.content = "";
        this.url = "";
        this.type = "";
        this.assign_time = 0L;
        this.finish_time = 0L;
        this.status = 0;
    }

    public String getOpenid() {
        return openid;
    }

    public String getHomeworkid() {
        return homeworkid;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() { return url; }

    public String getType() {return type;}

    public long getAssign_time() {
        return assign_time;
    }

    public long getFinish_time() {
        return finish_time;
    }

    public int getStatus() {
        return status;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public void setHomeworkid(String homeworkid) {
        this.homeworkid = homeworkid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAssign_time(long assign_time) {
        this.assign_time = assign_time;
    }

    public void setFinish_time(long finish_time) {
        this.finish_time = finish_time;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setUrl(String url) { this.url = url;}

    public void setType(String type) { this.type = type;}

    @Override
    public String toString() {
        return "HomeWorkEntity{" +
                "openid='" + openid + '\'' +
                ", homeworkid='" + homeworkid + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", assign_time=" + assign_time +
                ", finish_time=" + finish_time +
                ", url=" + url +
                ", status=" + status +
                '}';
    }
}


