/*
 * Copyright (c) 2017. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchatapi;

import android.text.TextUtils;

import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatcomm.utils.SharePreUtils;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by Knight.Xu on 2017/3/22.
 * Modified by Knight.Xu on 2018/1/18.
 * Api类的包装
 */
public class ApiWrapper extends Api {

    private static final String TAG = "AI-ApiWrapper";
    private static ApiWrapper sApiWrapper;

    private static final String ENGINE_URL = "engine/";

    public static ApiWrapper getInstance() {
        if (sApiWrapper == null) {
            sApiWrapper = new ApiWrapper();
        }
        return sApiWrapper;
    }

    private ApiWrapper() {
        sApiWrapper = new ApiWrapper();
    }

    /**
     * 调用{@link ApiWrapper#ENGINE_URL } 接口
     */
    public static Call<ResponseBody> engineApi(RequestBody body) {
        QAILog.d(TAG, "engineApi");
        String engineStrUrl = TextUtils.isEmpty(QAIConfig.aiengine_url) ?
                createBaseHttpUrl(false) + ENGINE_URL: QAIConfig.aiengine_url;
        HttpUrl engineUrl = new Request.Builder()
                .url(engineStrUrl)
                .build()
                .url()
                .newBuilder()
//                .addQueryParameter(ACCESS_KEY_PARA, SharePreUtils.getUserToken())
                .build();

        return getService().engineApi(engineUrl, body);
    }

}
