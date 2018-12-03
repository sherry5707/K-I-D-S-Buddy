package com.kinstalk.her.qchat.library;

public class TitlesBean {

    private String title;

    private boolean isSelected;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public String toString() {
        return "TitlesBean{" +
                "title='" + title + '\'' +
                ", isSelected=" + isSelected +
                '}';
    }
}
