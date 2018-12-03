package com.kinstalk.her.qchat.activity;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import com.kinstalk.her.qchat.utils.LogUtil;
import kinstalk.com.qloveaicore.IAICoreInterface;


/**
 * Created by wangyong on 18/6/26.
 * NewBaseAIService
 */

public abstract class NewBaseAIService extends Service implements NewAIDelegate {
    private String TAG = "NewBaseAIService";

    private NewAIManager mAiManager;
    protected Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mAiManager = NewAIManager.getInstance(getApplicationContext());
        mAiManager.setAiDelegate(this);
        mAiManager.setBindParams(getBindParams());
        mAiManager.init();
        mHandler = new Handler(getMainLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        mAiManager.unInit();
        super.onDestroy();
    }

    protected NewAIManager getAImanager() {
        return mAiManager;
    }

    /**
     * 请求AI接口传回调
     *
     * @param requestData
     * @param callback
     */
    public void requestData(String requestData) {
        if (getAImanager() != null && getAImanager().getService() != null) {
            try {
                getAImanager().requestDataWithCallback(requestData);
            } catch (Exception e) {
                LogUtil.d(e.toString());
            }
        }
    }

    /**
     * 请求AI接口传回调
     *
     * @param requestData
     * @param callback
     */
    public void textRequest(String text) {
        if (getAImanager() != null && getAImanager().getService() != null) {
            try {
                getAImanager().textRequest(text);
            } catch (Exception e) {
                LogUtil.d(e.toString());
            }
        }
    }
    /**
     * 获取绑定Ai服务参数
     *
     * @return
     */
    public abstract String getBindParams();

    @Override
    public void onAIServiceConnected(IAICoreInterface aiInterface) {
        LogUtil.d("onAIServiceConnected");

    }

    @Override
    public void onAIServiceDisConnected() {

    }

    @Override
    public void onTTSPlayBegin(String s){

    }

    @Override
    public void onTTSPlayEnd(String s){

    }

    @Override
    public void onTTSPlayError(String s, int i, String s1){

    }
}
