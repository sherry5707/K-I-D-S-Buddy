package com.kinstalk.her.qchat.translation;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

@Entity()
public class TranslationBean {

    @Property(nameInDb = "id")
    @Id(autoincrement = true)
    private Long id;

    private int userId;

    private String input;

    private String translation;


    @Generated(hash = 686955163)
    public TranslationBean(Long id, int userId, String input, String translation) {
        this.id = id;
        this.userId = userId;
        this.input = input;
        this.translation = translation;
    }


    @Generated(hash = 104445283)
    public TranslationBean() {
    }


    @Override
    public String toString() {
        return "TranslationBean{" +
                ", userId=" + userId +
                ", input='" + input + '\'' +
                ", translation='" + translation + '\'' +
                '}';
    }


    public Long getId() {
        return this.id;
    }


    public void setId(long id) {
        this.id = id;
    }


    public int getUserId() {
        return this.userId;
    }


    public void setUserId(int userId) {
        this.userId = userId;
    }


    public String getInput() {
        return this.input;
    }


    public void setInput(String input) {
        this.input = input;
    }


    public String getTranslation() {
        return this.translation;
    }


    public void setTranslation(String translation) {
        this.translation = translation;
    }


    public void setId(Long id) {
        this.id = id;
    }


}
