package com.kinstalk.her.qchatmodel.entity;

import com.stfalcon.chatkit.commons.models.IUser;

import java.io.Serializable;

/**
 * Created by wangzhipeng on 2018/4/6.
 * Modified by knight.xu  on 2018/4/14, implements IUser
 */

public class WxUserEntity implements Serializable, IUser {
    public String openid;
    public String nickname;
    public int sex;
    public String country;
    public String province;
    public String city;
    public long subscribe_time;
    public String headimgurl;
    public String language;
    public String remark;
    public int qr_scene;
    public String qr_scene_str;
    public int subscribe;
    public String unionid;
    public int groupid;
    public String subscribe_scene;
    public int status;
    public boolean online;

    public WxUserEntity() {
        this.openid = "";
        this.nickname = "";
        this.sex = 1;
        this.country = "";
        this.province = "";
        this.city = "";
        this.subscribe_time = 0L;
        this.headimgurl = "";
        this.language = "";
        this.remark = "";
        this.qr_scene = 0;
        this.qr_scene_str = "";
        this.subscribe = 1;
        this.unionid = "";
        this.groupid = 0;
        this.subscribe_scene = "";
        this.status = 0;
    }

    public WxUserEntity(String id, String name, String avatar, boolean online) {
        this.openid = id;
        this.nickname = name;
        this.headimgurl = avatar;
        this.online = online;

    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setSubscribe_time(long subscribe_time) {
        this.subscribe_time = subscribe_time;
    }

    public void setHeadimgurl(String headimgurl) {
        this.headimgurl = headimgurl;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setQr_scene(int qr_scene) {
        this.qr_scene = qr_scene;
    }

    public void setQr_scene_str(String qr_scene_str) {
        this.qr_scene_str = qr_scene_str;
    }

    public void setSubscribe(int subscribe) {
        this.subscribe = subscribe;
    }

    public void setUnionid(String unionid) {
        this.unionid = unionid;
    }

    public void setGroupid(int groupid) {
        this.groupid = groupid;
    }

    public void setSubscribe_scene(String subscribe_scene) {
        this.subscribe_scene = subscribe_scene;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOpenid() {
        return openid;
    }

    public String getNickname() {
        return nickname;
    }

    public int getSex() {
        return sex;
    }

    public String getCountry() {
        return country;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public long getSubscribe_time() {
        return subscribe_time;
    }

    public String getHeadimgurl() {
        return headimgurl;
    }

    public String getLanguage() {
        return language;
    }

    public String getRemark() {
        return remark;
    }

    public int getQr_scene() {
        return qr_scene;
    }

    public String getQr_scene_str() {
        return qr_scene_str;
    }

    public int getSubscribe() {
        return subscribe;
    }

    public String getUnionid() {
        return unionid;
    }

    public int getGroupid() {
        return groupid;
    }

    public String getSubscribe_scene() {
        return subscribe_scene;
    }

    public int getStatus() {
        return status;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public String toString() {
        return "WxUserEntity{" +
                "openid='" + openid + '\'' +
                ", nickname='" + nickname + '\'' +
                ", sex=" + sex +
                ", country='" + country + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", subscribe_time=" + subscribe_time +
                ", headimgurl='" + headimgurl + '\'' +
                ", language='" + language + '\'' +
                ", remark='" + remark + '\'' +
                ", qr_scene=" + qr_scene +
                ", qr_scene_str='" + qr_scene_str + '\'' +
                ", subscribe=" + subscribe +
                ", unionid='" + unionid + '\'' +
                ", groupid=" + groupid +
                ", subscribe_scene='" + subscribe_scene + '\'' +
                ", status=" + status +
                ", online=" + online +
                '}';
    }

    @Override
    public String getId() {
        return openid;
    }

    @Override
    public String getName() {
        return nickname;
    }

    @Override
    public String getAvatar() {
        return headimgurl;
    }
}
