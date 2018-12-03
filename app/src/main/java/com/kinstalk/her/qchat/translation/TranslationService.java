package com.kinstalk.her.qchat.translation;

import com.kinstalk.her.qchat.activity.AIUtils;
import com.kinstalk.her.qchat.activity.NewBaseAIService;
import com.tencent.xiaowei.info.QLoveResponseInfo;

public class TranslationService extends NewBaseAIService {

    private static final String TAG = TranslationService.class.getSimpleName();

    public static final String TYPE_GIFT = "generic";

    public static final String localSvcPkg = "com.kinstalk.her.qchat";
    public static final String localSvcCls = "kinstalk.her.qchat.translation.TranslationService";

    @Override
    public String getBindParams() {
        return AIUtils.buildJson(TYPE_GIFT, localSvcPkg, localSvcCls);
    }

    @Override
    public void onJsonResult(String jsonResult) {
        //TODO Need complete
        textRequest(jsonResult);
    }

    @Override
    public void handleQLoveResponseInfo(String voiceId, QLoveResponseInfo rspData, byte[] extendData) {

    }

    @Override
    public void handleWakeupEvent(int command, String data) {

    }

}

