package com.kinstalk.her.qchatmodel.entity;

import java.io.Serializable;

/**
 * 道具
 * Created by bean on 2018/8/22.
 */

public class PKPropInfo implements Serializable {
    private int id;//道具id，跟本地对应
    private String name;//道具名称
    private String action;//概率
    private int num;//道具数量
    private String url;//图片地址
    private String caption;//道具说明

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getAction() {

        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    @Override
    public String toString() {
        return "PKPropInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", action='" + action + '\'' +
                ", num=" + num +
                ", url='" + url + '\'' +
                ", caption='" + caption + '\'' +
                '}';
    }
}
