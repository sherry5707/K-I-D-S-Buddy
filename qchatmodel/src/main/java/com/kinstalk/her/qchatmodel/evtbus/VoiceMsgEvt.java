package com.kinstalk.her.qchatmodel.evtbus;

import com.kinstalk.her.qchatmodel.beans.VoiceBean;

/**
 * Created by liulinxiang on 4/6/2018.
 */

public class VoiceMsgEvt extends MessageEvent {
     private VoiceBean vocieBean;

    public VoiceBean getVocieBean() {
        return vocieBean;
    }

    public void setVocieBean(VoiceBean vocieBean) {
        this.vocieBean = vocieBean;
    }
}
