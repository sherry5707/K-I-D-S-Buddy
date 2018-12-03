package com.kinstalk.her.qchatmodel.evtbus;

import com.kinstalk.her.qchatmodel.beans.ImageBean;
import com.kinstalk.her.qchatmodel.beans.VoiceBean;

/**
 * Created by liulinxiang on 4/6/2018.
 */

public class ImageMsgEvt extends MessageEvent {
     private ImageBean imgBean;

    public ImageBean getImgBean() {
        return imgBean;
    }

    public void setImgBean(ImageBean imgBean) {
        this.imgBean = imgBean;
    }
}
