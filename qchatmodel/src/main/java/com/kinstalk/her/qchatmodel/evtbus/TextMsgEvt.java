package com.kinstalk.her.qchatmodel.evtbus;

/**
 * Created by liulinxiang on 4/6/2018.
 */

public class TextMsgEvt extends MessageEvent {
    String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
