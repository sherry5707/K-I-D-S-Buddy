/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchat.frags;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.kinstalk.her.qchat.image.GlideImageLoader;
import com.kinstalk.her.qchatcomm.base.BaseApplication;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.ChatProviderHelper;
import com.stfalcon.chatkit.commons.ImageLoader;

/**
 * Created by Knight.Xu on 2018/4/13.
 */

public class QBaseFragment extends Fragment{

    private static final String TAG = QBaseFragment.class.getSimpleName();

    protected static final int TOTAL_MESSAGES_COUNT = 100;
    // Chat
    protected static final byte CONTENT_TYPE_VOICE = 1;//语音类型
    protected static final byte CONTENT_TYPE_EMOJI = 2;//表情类型
    // HOMEWORK
    protected static final byte CONTENT_TYPE_HOMEWORK = 3;//作业类型
    protected Context mContext;
    protected ImageLoader imageLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QAILog.d(TAG, "onCreate");
        imageLoader = new GlideImageLoader();
        mContext = BaseApplication.getInstance().getApplicationContext();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        QAILog.d(TAG, "onCreate");
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
