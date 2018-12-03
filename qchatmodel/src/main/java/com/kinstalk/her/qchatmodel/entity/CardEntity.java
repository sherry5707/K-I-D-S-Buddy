package com.kinstalk.her.qchatmodel.entity;

import java.io.Serializable;

/**
 * Created by bean on 2018/6/29.
 * 卡片
 */

public class CardEntity implements Serializable {
    private int id;//图片id
    private String url;//图片地址
    private String name;//图片名称
    private int person;//已获取的人数
    private String personUrl;//已获取的人图片
    private int shareCredit;//分享可以得到的学分（已分享过的这里返回0即可）
    private int getCredit;//换取需要的学分 (稀有卡片返回0即可，设备端会先根据card_num判断)
    private int status;//状态 (0未拥有，1.已拥有)
    private int cardNum;//卡片需要合成的次数 （0次表示普通卡片，稀有卡片返回相应的次数）
    private String cardDesp;//卡片描述
    private int cardType;// 1, # 卡类型： 1普通卡 2稀有卡 3道具卡
    private int cardNums;// 卡片数量
    private String updateTime;

    public CardEntity() {
    }

    public CardEntity(int id, String url, String name, int person, String personUrl, int shareCredit, int getCredit, int status, int cardNum, String cardDesp, int cardType, int cardNums, String updateTime) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.person = person;
        this.personUrl = personUrl;
        this.shareCredit = shareCredit;
        this.getCredit = getCredit;
        this.status = status;
        this.cardNum = cardNum;
        this.cardDesp = cardDesp;
        this.cardType = cardType;
        this.cardNums = cardNums;
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "CardEntity{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", person=" + person +
                ", personUrl='" + personUrl + '\'' +
                ", shareCredit=" + shareCredit +
                ", getCredit=" + getCredit +
                ", status=" + status +
                ", cardNum=" + cardNum +
                ", cardDesp='" + cardDesp + '\'' +
                ", cardType=" + cardType +
                ", cardNums=" + cardNums +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public int getCardType() {
        return cardType;
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
    }

    public int getCardNums() {
        return cardNums;
    }

    public void setCardNums(int cardNums) {
        this.cardNums = cardNums;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPerson() {
        return person;
    }

    public void setPerson(int person) {
        this.person = person;
    }

    public String getPersonUrl() {
        return personUrl;
    }

    public void setPersonUrl(String personUrl) {
        this.personUrl = personUrl;
    }

    public int getShareCredit() {
        return shareCredit;
    }

    public void setShareCredit(int shareCredit) {
        this.shareCredit = shareCredit;
    }

    public int getGetCredit() {
        return getCredit;
    }

    public void setGetCredit(int getCredit) {
        this.getCredit = getCredit;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getCardNum() {
        return cardNum;
    }

    public void setCardNum(int cardNum) {
        this.cardNum = cardNum;
    }

    public String getCardDesp() {
        return cardDesp;
    }

    public void setCardDesp(String cardDesp) {
        this.cardDesp = cardDesp;
    }

}
