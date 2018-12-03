package com.kinstalk.her.qchatmodel.entity;

import java.io.Serializable;

/**
 * Created by Tracy on 2018/5/29.
 */

public class HabitKidsEntity implements Serializable {
    public String nick_name;
    public int gender;  //0为男孩， 1为女孩
    public String birthday;
    public int all_star;
    public int all_credit;
    public String grade;
    public String headimgurl;


    public HabitKidsEntity(String name) {
        this.nick_name = name;
    }

    public HabitKidsEntity() {
        this.nick_name = "宝宝";
        this.gender = -1;
        this.birthday = "";
        this.all_star = 0;
        this.all_credit = 0;
        this.grade = "";
        this.headimgurl ="";
    }

    public String getNick_name() {
        return nick_name;
    }

    public int getGender() {
        return gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public int getStar() {
        return all_star;
    }

    public int getCredit() { return all_credit;}

    public String getGrade() {return grade;}

    public String getHeadimgurl() { return  headimgurl;}

    public void setNick_name(String name) {
        this.nick_name = name;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setAll_star(int star) {
        this.all_star = star;
    }

    public void setAll_credit(int credit) { this.all_credit = credit;}

    public void setBirthday(String birthday) { this.birthday = birthday;}

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setHeadimgurl(String url) { this.headimgurl = url; }
    @Override
    public String toString() {
        return "HabitKidsEntity{" +
                "nick_name='" + nick_name + '\'' +
                ", gender='" + gender + '\'' +
                ", all_star='" + all_star + '\'' +
                ", birthday= " + birthday +
                ", grade= " + grade +
                '}';
    }
}

