package com.kinstalk.her.qchatmodel.entity;

import java.io.Serializable;

/**
 * Created by bean on 2018/6/29.
 * 任务
 */

public class TaskEntity implements Serializable {
    private int id;//任务编号
    private String name;// 任务名称
    private int allNum;//总次数
    private int nowNum;//当前次数
    private int credit;//可以获取学分数
    private int type;//任务类型（0表示习惯打卡任务，1表示小微百科任务,2游戏）
    private int status;//0可领取奖励，1已完成，2未完成

    public TaskEntity() {
    }

    public TaskEntity(int id, String name, int allNum, int nowNum, int credit, int type, int status) {
        this.id = id;
        this.name = name;
        this.allNum = allNum;
        this.nowNum = nowNum;
        this.credit = credit;
        this.type = type;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAllNum() {
        return allNum;
    }

    public void setAllNum(int allNum) {
        this.allNum = allNum;
    }

    public int getNowNum() {
        return nowNum;
    }

    public void setNowNum(int nowNum) {
        this.nowNum = nowNum;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TaskEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", allNum=" + allNum +
                ", nowNum=" + nowNum +
                ", credit=" + credit +
                ", type=" + type +
                ", status=" + status +
                '}';
    }
}
