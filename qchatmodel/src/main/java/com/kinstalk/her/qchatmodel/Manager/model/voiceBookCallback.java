package com.kinstalk.her.qchatmodel.Manager.model;

import com.kinstalk.her.qchatmodel.entity.VoiceBookBean;

import java.util.List;

public interface voiceBookCallback {

    void getVoiceBookListSuccess(List<VoiceBookBean> list);

    void getVoiceBookListFail(int errorCode, String errorMsg);
}
