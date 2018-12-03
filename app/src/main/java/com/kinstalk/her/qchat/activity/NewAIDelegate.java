package com.kinstalk.her.qchat.activity;

import com.tencent.xiaowei.info.QLoveResponseInfo;

import kinstalk.com.qloveaicore.IAICoreInterface;

/**
 * Created by wangyong on 18/6/26.
 * NewAIDelegate
 */

public interface NewAIDelegate {

    /**
     * AI识别结果回调
     *
     * @param jsonResult json结果
     */
    void onJsonResult(String jsonResult);

    void handleQLoveResponseInfo(String voiceId, QLoveResponseInfo rspData, byte[] extendData);

    /**
     * 唤醒后语音精灵动画的控制接口
     *
     * @param command 指令
     * @param data    具体处理数据
     * @return void
     */
    void handleWakeupEvent(int command, String data);

    void onAIServiceConnected(IAICoreInterface aiInterface);

    void onAIServiceDisConnected();

    void onTTSPlayBegin(String s);

    void onTTSPlayEnd(String s);

    void onTTSPlayError(String s, int i, String s1);


}
