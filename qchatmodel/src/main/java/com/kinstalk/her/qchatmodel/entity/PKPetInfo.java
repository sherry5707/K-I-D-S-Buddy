package com.kinstalk.her.qchatmodel.entity;

import java.io.Serializable;

/**
 * Created by bean on 2018/8/20.
 */

public class PKPetInfo implements Serializable {
    private int id;//宠物id
    private String name;//宠物名称
    private String caption;//宠物功能
    private String imgUrl;//宠物图片地址
    private boolean ifOwn;//是否拥有

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

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public boolean isIfOwn() {
        return ifOwn;
    }

    public void setIfOwn(boolean ifOwn) {
        this.ifOwn = ifOwn;
    }

    @Override
    public String toString() {
        return "PKPetInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", caption='" + caption + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", ifOwn=" + ifOwn +
                '}';
    }
}
