package com.kinstalk.her.qchatmodel.entity;

import android.net.wifi.p2p.WifiP2pManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 题目
 * Created by bean on 2018/8/22.
 */

public class PKQuestionInfo implements Serializable {
    private int id;//id
    private String topic;//题目文字
    private String topicUrl;//题目图片地址
    private List<Sections> sections = new ArrayList<>(3);//答案选项
    private int lastNum;//剩余人数
    private String voice;//tts播报内容

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopicUrl() {
        return topicUrl;
    }

    public void setTopicUrl(String topicUrl) {
        this.topicUrl = topicUrl;
    }

    public List<Sections> getSections() {
        return sections;
    }

    public void setSections(List<Sections> sections) {
        this.sections = sections;
    }

    public int getLastNum() {
        return lastNum;
    }

    public void setLastNum(int lastNum) {
        this.lastNum = lastNum;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    @Override
    public String toString() {
        return "PKQuestionInfo{" +
                "id=" + id +
                ", topic='" + topic + '\'' +
                ", topicUrl='" + topicUrl + '\'' +
                ", sections=" + sections.toString() +
                ", lastNum=" + lastNum +
                ", voice='" + voice + '\'' +
                '}';
    }

    public static class Sections implements Serializable {
        private String url;//答案图片地址
        private boolean isAnswer;//答案是否正确
        private String name;//答案名称
        private int num = 0;//用户选择这个答案的份额（供道具卡使用,默认0）

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isAnswer() {
            return isAnswer;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        @Override
        public String toString() {
            return "Sections{" +
                    "url='" + url + '\'' +
                    ", isAnswer=" + isAnswer +
                    ", name='" + name + '\'' +
                    '}';
        }

        public void setAnswer(boolean answer) {
            isAnswer = answer;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
