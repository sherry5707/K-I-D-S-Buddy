package com.kinstalk.her.qchatmodel.entity;

import java.io.Serializable;

public class VoiceBookBean implements Serializable {

    private String parentName;

    private String iconUrl;

    private String bookName;

    private String command;

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "VoiceBookBean{" +
                "parentName='" + parentName + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", bookName='" + bookName + '\'' +
                ", command='" + command + '\'' +
                '}';
    }
}
