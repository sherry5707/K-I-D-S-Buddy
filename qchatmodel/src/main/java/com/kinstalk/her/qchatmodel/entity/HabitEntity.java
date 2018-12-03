package com.kinstalk.her.qchatmodel.entity;

import java.io.Serializable;

/**
 * Created by Tracy on 2018/5/22.
 */

public class HabitEntity implements Serializable {
    public String openid;
    public String remindid;
    public String name;
    public Long assign_time;
    public Long remind_time;
    public Long end_time;
    public String repeat_time;
    public int type;
    public int status;


    public HabitEntity(String name) {
        this.name = name;
    }

    public HabitEntity() {
        this.openid = "";
        this.remindid = "";
        this.name = "";
        this.assign_time = 0L;
        this.remind_time = 0L;
        this.end_time = 0L;
        this.repeat_time = "";
        this.type = 0;
        this.status = 0;
    }

    public String getOpenid() {
        return openid;
    }

    public String getRemindid() {
        return remindid;
    }

    public String getName() {
        return name;
    }

    public Long getAssign_time() {
        return assign_time;
    }
    public String getRepeat_time() {return repeat_time;}

    public Long getEnd_time() {return end_time;}

    public Long getRemind_time() {
        return remind_time;
    }

    public int getType() { return type;}

    public int getStatus() {
        return status;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public void setRemindid(String remindid) {
        this.remindid = remindid;
    }

    public void setName(String content) {
        this.name = content;
    }

    public void setEnd_time(Long end_time) {this.end_time = end_time;}

    public void setAssign_time(Long assign_time) {
        this.assign_time = assign_time;
    }

    public void setRemind_time(Long remind_time) {
        this.remind_time = remind_time;
    }

    public void setRepeat_time(String repeat_time) { this.repeat_time = repeat_time;}

    public void setType(int type) { this.type = type;}

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "HabitEntity{" +
                "openid='" + openid + '\'' +
                ", remindid='" + remindid + '\'' +
                ", content='" + name + '\'' +
                ", add_time=" + assign_time +
                ", remind_time=" + remind_time +
                ", end_time=" + end_time +
                ", repeat_time=" + repeat_time +
                ", type=" + type +
                ", status=" + status +
                '}';
    }
}
