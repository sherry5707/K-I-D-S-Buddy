package com.kinstalk.her.qchat.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.kinstalk.her.qchat.utils.LogUtil;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.tencent.xiaowei.info.QLoveResponseInfo;

import kinstalk.com.qloveaicore.IAICoreInterface;
import kinstalk.com.qloveaicore.ICmdCallback;
import kinstalk.com.qloveaicore.ITTSCallback;

/**
 * Created by wangyong on 18/6/26.
 * NewAIManager
 */

public class NewAIManager {

    private static final String TAG = "AIManager";

    private IAICoreInterface mController;
    final Context mContext;
    private static NewAIManager sInstance;

    public static final String remoteSvcPkg = "kinstalk.com.qloveaicore";
    public static final String remoteSvcCls = "kinstalk.com.qloveaicore.QAICoreService";

    private HandlerThread mHandlerThread;
    private InternalHandler mHandler;

    private static final int MSG_RECONNECT_REMOTE = 0x1;
    private static final int MSG_HANDLE_CMD = 0x2;

    private String bindParams;
    private NewAIDelegate aiDelegate;

    public static synchronized NewAIManager getInstance(Context c) {
        if (sInstance == null) {
            sInstance = new NewAIManager(c.getApplicationContext());
        }
        return sInstance;
    }

    private NewAIManager(Context mContext) {
        this.mContext = mContext.getApplicationContext();
    }

    public void init() {
        mContext.bindService(getServiceIntent(), sc, Context.BIND_AUTO_CREATE);
        mHandlerThread = new HandlerThread("slh_handler_thread");
        mHandlerThread.start();
        mHandler = new InternalHandler(mHandlerThread.getLooper());
    }

    public void unInit() {

        QAILog.d(TAG, "unInit RUN");

        if (mController != null) {
            try {
                mController.unRegisterService(bindParams);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mController = null;
        mContext.unbindService(sc);
        mHandler.removeCallbacksAndMessages(null);
        mHandlerThread.quit();
    }

    public void registerClient() {
        try {
            LogUtil.d("registerClient regist ai callback ... ");
            mController.registerService(bindParams, mCb);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class InternalHandler extends Handler {
        InternalHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RECONNECT_REMOTE:
                    bindService();
                    break;
                case MSG_HANDLE_CMD:
                    try {
                        SpeakModel model = (SpeakModel) msg.obj;
                        mController.playText(AIUtils.buildPlayTextJson(model.text, model.speed, model.role));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private ICmdCallback mCb = new ICmdCallback.Stub() {
        @Override
        public String processCmd(String json) throws RemoteException {
            LogUtil.d(TAG, "processCmd");
            if (aiDelegate != null) {
                aiDelegate.onJsonResult(json);
            }
            return "";
        }

        @Override
        public void handleQLoveResponseInfo(String voiceId, QLoveResponseInfo rspData, byte[] extendData) throws RemoteException {
            LogUtil.d(TAG, "handleQLoveResponseInfo");
            if (aiDelegate != null) {
                aiDelegate.handleQLoveResponseInfo(voiceId, rspData, extendData);
            }
        }

        /**
         * 唤醒后语音精灵动画的控制接口
         * @param command 指令
         * @param data 具体处理数据
         * @return void
         */
        @Override
        public void handleWakeupEvent(int command, String data) {
            LogUtil.d(TAG, "handleWakeupEvent");
            if (aiDelegate != null) {
                aiDelegate.handleWakeupEvent(command, data);
            }

        }
    };


    /**
     * 请求AI接口传回调
     *
     * @param requestData
     */
    public void requestDataWithCallback(String requestData) {
        try {
            LogUtil.d(TAG, "requestDataWithCallback");
            mController.requestDataWithCb(requestData, mCb);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求AI接口传回调
     */
    public void textRequest(String text) {
        try {
            mController.textRequest(text);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.d("onServiceConnected ... ");
            mController = IAICoreInterface.Stub.asInterface(service);
            if (aiDelegate != null) {
                aiDelegate.onAIServiceConnected(mController);
            }
            registerClient();
            mHandler.removeMessages(MSG_RECONNECT_REMOTE);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mController = null;

            if (aiDelegate != null) {
                aiDelegate.onAIServiceDisConnected();
            }
//            rebindRemoteService(3000);

            mHandler.removeMessages(MSG_RECONNECT_REMOTE);
            rebindRemoteService(10000);
        }
    };

    /**
     * 服务绑定
     */
    private void bindService() {
        LogUtil.d("bindService ... ");
        mContext.bindService(getServiceIntent(), sc, Context.BIND_AUTO_CREATE);
    }

    private void rebindRemoteService(int milliSeconds) {
        LogUtil.d("rebindRemoteService ... ");
        mController = null;

        // re-bind to the service if disconnected
        Message m = Message.obtain();
        m.what = MSG_RECONNECT_REMOTE;
        mHandler.sendMessageDelayed(m, milliSeconds);
    }

    private Intent getServiceIntent() {
        ComponentName cn = new ComponentName(remoteSvcPkg, remoteSvcCls);
        Intent i = new Intent();
        i.setComponent(cn);
        return i;
    }

    /////TODO remove belows
    public IAICoreInterface getService() {
        return mController;
    }

    public void setAiDelegate(NewAIDelegate aiDelegate) {
        this.aiDelegate = aiDelegate;
    }

    public void setBindParams(String bindParams) {
        this.bindParams = bindParams;
    }

    /**
     * 朗读
     */
    public void ttsSpeakText(String text) {
        ttsSpeakText(text, 1, 2);
    }

    private ITTSCallback ittsCallback = new ITTSCallback.Stub() {

        @Override
        public void onTTSPlayBegin(String s) throws RemoteException {
            if (aiDelegate != null) {
                aiDelegate.onTTSPlayBegin(s);
            }
        }

        @Override
        public void onTTSPlayEnd(String s) throws RemoteException {
            if (aiDelegate != null) {
                aiDelegate.onTTSPlayEnd(s);
            }
        }

        @Override
        public void onTTSPlayProgress(String s, int i) throws RemoteException {

        }

        @Override
        public void onTTSPlayError(String s, int i, String s1) throws RemoteException {
            if (aiDelegate != null) {
                aiDelegate.onTTSPlayError(s, i, s1);
            }
        }
    };

    /**
     * 朗读
     */
    public void ttsSpeakText1(String voiceId) throws RemoteException {
        mController.playTextWithId(voiceId, ittsCallback);
    }

    /**
     * 朗读
     */
    public void ttsSpeakText(String text, int speed, int role) {
        Message msg = Message.obtain();
        msg.what = MSG_HANDLE_CMD;
        SpeakModel speakModel = new SpeakModel();
        speakModel.text = text;
        speakModel.speed = speed;
        speakModel.role = role;
        msg.obj = speakModel;

        if (!mHandler.getLooper().getThread().isAlive()) {
            mHandlerThread.start();

            mHandler = new InternalHandler(mHandlerThread.getLooper());

        }

        mHandler.sendMessage(msg);
    }

    private final class SpeakModel {
        String text;
        int speed;
        int role;
    }
}
