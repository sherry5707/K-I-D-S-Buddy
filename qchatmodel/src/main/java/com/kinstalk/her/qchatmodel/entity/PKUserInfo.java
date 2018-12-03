package com.kinstalk.her.qchatmodel.entity;

import java.io.Serializable;

/**
 * PK用户信息
 * Created by bean on 2018/8/20.
 */

public class PKUserInfo implements Serializable {
    private int allCredit;//总学分
    private String nickName;//用户名
    private String avatar;//头像
    private int nowTimes;//当前玩的次数
    private int allTimes;//总次数
    private int petId;//宠物ID
    private String petUrl;//宠物地址

    public int getAllCredit() {
        return allCredit;
    }

    public void setAllCredit(int allCredit) {
        this.allCredit = allCredit;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getNowTimes() {
        return nowTimes;
    }

    public void setNowTimes(int nowTimes) {
        this.nowTimes = nowTimes;
    }

    public int getAllTimes() {
        return allTimes;
    }

    public void setAllTimes(int allTimes) {
        this.allTimes = allTimes;
    }

    public int getPetId() {
        return petId;
    }

    public void setPetId(int petId) {
        this.petId = petId;
    }

    public String getPetUrl() {
        return petUrl;
    }

    public void setPetUrl(String petUrl) {
        this.petUrl = petUrl;
    }

    @Override
    public String toString() {
        return "PKUserInfo{" +
                "allCreggit=" + allCredit +
                ", nickName='" + nickName + '\'' +
                ", avatar='" + avatar + '\'' +
                ", nowTimes=" + nowTimes +
                ", allTimes=" + allTimes +
                ", petId=" + petId +
                ", petUrl='" + petUrl + '\'' +
                '}';
    }
}
