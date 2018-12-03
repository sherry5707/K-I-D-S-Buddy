package com.kinstalk.her.qchatmodel.entity;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by bean on 2018/5/22.
 */

public class GiftEntity implements Serializable, Comparable<GiftEntity> {
    private int id;//商品唯一标示
    private String giftName;//礼物名称
    private int starCount;//兑换星星数量
    private String date;//已领取的日期
    private int status;//0可领取，1不可领取， 2已领取, 3正在获取中的礼物，有且只有一个

    public GiftEntity() {
    }

    public GiftEntity(int id, String giftName, int starCount, String date, int status) {
        this.id = id;
        this.giftName = giftName;
        this.starCount = starCount;
        this.date = date;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public int getStarCount() {
        return starCount;
    }

    public void setStarCount(int starCount) {
        this.starCount = starCount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "GiftEntity{" +
                "id=" + id +
                ", giftName='" + giftName + '\'' +
                ", starCount=" + starCount +
                ", date='" + date + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public int compareTo(@NonNull GiftEntity bean) {
        return bean.getDate().compareTo(this.date);
    }
}
