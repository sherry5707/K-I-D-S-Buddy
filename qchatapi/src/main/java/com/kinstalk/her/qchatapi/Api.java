/*
 * Copyright (c) 2017. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchatapi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.kinstalk.her.qchatapi.aws.AWSTransferHelper;
//import com.kinstalk.her.qchatapi.dialog.TaskDilaog;
import com.kinstalk.her.qchatapi.okhttp.OkhttpClientHelper;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.DateUtils;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatcomm.utils.SystemPropertiesProxy;
import com.kinstalk.her.qchatcomm.utils.SystemTool;
import com.kinstalk.her.qchatmodel.CalenderProviderHelper;
import com.kinstalk.her.qchatmodel.ChatProviderHelper;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.util.UIHelp;
import com.kinstalk.her.qchatmodel.entity.CardEntity;
import com.kinstalk.her.qchatmodel.entity.GiftEntity;
import com.kinstalk.her.qchatmodel.entity.HabitEntity;
import com.kinstalk.her.qchatmodel.entity.HabitKidsEntity;
import com.kinstalk.her.qchatmodel.entity.HomeWorkEntity;
import com.kinstalk.her.qchatmodel.entity.PKPetInfo;
import com.kinstalk.her.qchatmodel.entity.PKPropInfo;
import com.kinstalk.her.qchatmodel.entity.PKQuestionInfo;
import com.kinstalk.her.qchatmodel.entity.PKUserInfo;
import com.kinstalk.her.qchatmodel.entity.RemindEntity;
import com.kinstalk.her.qchatmodel.entity.TaskEntity;
import com.kinstalk.her.qchatmodel.entity.VoiceBookBean;
import com.kinstalk.her.qchatmodel.entity.WxUserEntity;
import com.kinstalk.her.qchatmodel.recurrence.RecurrenceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kr.co.namee.permissiongen.internal.Utils;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import retrofit2.Retrofit;

/**
 * Created by Knight.Xu on 2017/3/22.
 * Modified by Knight.Xu on 2018/1/18.
 * Modified by Tracy
 */
public class Api {

    private static final String TAG = "Qchat-Api";

    /**
     * 服务器地址
     */
    // 请求公共部分
    public static String AI_HOST = "api.prod.qspeaker.com";//default api
    private static final String CONFIG_HOST = "api.prod.qspeaker.com";//default config api

    public static final String AI_HOST_PRODUCT = "api.prod.qspeaker.com";
    public static final String AI_HOST_TEST = "api.test.qspeaker.com";
    public static final String AI_HOST_DEV = "api.dev.qspeaker.com";
    public static String HOMEWORK_URL = "https://wx.api.qspeaker.com";
    public static String TEST_URL = "https://wx.api.qspeaker.com/";//测试服务器2.0

    static final String CONFIG_PATH = "config/v1/";
    private static final String QRCODE_PATH = "wx/qrcode/";
    private static final String VOICE_PATH = "wx/message";
    private static final String BAIDU_PATH = "wx/addpush";
    private static final String KIDS_INFO_PATH = "wx/kidinfo";
    private static final String WX_INFO = "wx/info";
    private static final String WX_GIFT = "wx/gift";//礼物
    private static final String WX_CREDIT = "wx/v2/taskCredit";//獲取學分
    private static final String WX_TASK = "wx/v2/taskList";//任务
    private static final String WX_CARD = "wx/v2/game/getCardList";//卡片列表
    private static final String WX_GET_CARD = "wx/v2/game/card/exchange";//卡片列表
    private static final String WX_SHARE_CARD = "wx/v2/game/card/share";//卡片分享
    private static final String WX_PK_INFO = "wx/v2/game/userInfo";//PK用户信息
    private static final String WX_PK_CHOOSE = "wx/v2/game/changePet";//PK选择宠物
    private static final String WX_PK_QUESTION = "wx/v2/game/startGame";//PK选择的题目
    private static final String WX_PK_REWARD = "wx/v2/game/gameOver";//PK领取奖励
    private static final String WX_PK_PROP_USE = "wx/v2/game/useprop";//PK使用道具成功
    private static final String WX_VOICE_BOOK_LIST_PATH = "wx/v2/voicebook";//获取有声书列表


    public static final String UP_MSG_TYPE_VOICE = "voice";
    public static final String UP_MSG_TYPE_TEXT = "text";
    public static final String UP_MSG_TYPE_HOMEWORK = "fetchhomework";
    public static final String UP_MSG_TYPE_REMINDER = "fetchremind";
    private static String prevHabitList = "";

    // 消息头
    private static final String HEADER_X_HB_Client_Type = "X-HB-Client-Type";
    private static final String FROM_ANDROID = "android";

    private static final String APP_ID = "2016szjy";

    private static IApiService service;
    private static Retrofit retrofit;
    private static final boolean SHOW_OKHTTP_LOG = !SystemTool.isUserType() && !BuildConfig.IS_RELEASE;

    public static synchronized IApiService getService() {
        if (service == null) {
            service = getRetrofit().create(IApiService.class);
        }
        return service;
    }

    public static synchronized void restartService() {
        service = null;
        retrofit = null;
        getService();
    }

    /**
     * 拦截器  给所有的请求添加消息头
     */
    private static Interceptor sHeaderInterceptor = new Interceptor() {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request()
                    .newBuilder()
                    .addHeader(HEADER_X_HB_Client_Type, FROM_ANDROID)
                    .build();
            return chain.proceed(request);
        }
    };

    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .client(OkhttpClientHelper.getOkHttpClient())
                    .baseUrl(createBaseHttpUrl())
//                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static void setHomeworkAndTestUrl(Context context) {
        int type = SystemPropertiesProxy.getInt(context, "persist.sys.url", 2);
        if (type == 1) {
            HOMEWORK_URL = "https://wx.api.qspeaker.com";
            TEST_URL = "https://wx.api.qspeaker.com";
        } else if (type == 0) {
            HOMEWORK_URL = "http://wx2.api.qspeaker.com";
            TEST_URL = "http://wx2.api.qspeaker.com";
        } else if (type == 2 && SystemTool.isUserType()) {
            HOMEWORK_URL = "https://wx.api.qspeaker.com";
            TEST_URL = "https://wx.api.qspeaker.com";
        } else if (type == 2 && !SystemTool.isUserType()) {
            HOMEWORK_URL = "http://wx2.api.qspeaker.com";
            TEST_URL = "http://wx2.api.qspeaker.com";
            //TEST_URL = "http://172.20.20.38:9098";
        }
    }

    public static void setHabitJsonArrayString(String list) {
        prevHabitList = list;
    }

    public static String getPrevHabitString() {
        return prevHabitList;
    }

    private static HttpUrl createBaseHttpUrl() {
        String SCHEME = "https";
        if (!AI_HOST.equals(AI_HOST_PRODUCT)) {
            SCHEME = "http";
        }
        return new HttpUrl.Builder()
                .scheme(SCHEME)
                .host(AI_HOST)
                .build();
    }

    protected static String createBaseHttpUrl(boolean isHttps) {
        String SCHEME = "https://";
        if (!AI_HOST.equals(AI_HOST_PRODUCT) || !isHttps) {
            SCHEME = "http://";
        }
        return SCHEME + AI_HOST + "/";
    }

    public static void reqConfigList(Context context, String action) {

        QAILog.d(TAG, "reqConfigList: Enter");
        int rspCode;
        String errorMsg = "";
        okhttp3.Response response;

        try {
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createConfigListRequest(action)).execute();
                //判断请求是否成功
                if (response == null) {
                    QAILog.e(TAG, "reqConfigList response empty");
                    errorMsg = context.getString(R.string.api_error_rsp_EMPTY);
                } else if (!response.isSuccessful()) {
                    QAILog.e(TAG, "reqConfigList response failed");
                    errorMsg = context.getString(R.string.api_error_rsp_SERVER_ERROR);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        QAILog.e(TAG, "reqConfigList rspBody empty");
                        errorMsg = context.getString(R.string.api_error_rsp_EMPTY);
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v(TAG, "reqConfigList body = : " /*+ sBody*/);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                QAILog.v(TAG, "reqConfigList: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    QAIConfig.engine = rspJson.optString("engine");
                                    QAIConfig.current_env = rspJson.optInt("current_env");
                                    if (QAIConfig.current_env == 0) {
                                        AI_HOST = AI_HOST_PRODUCT;
                                    } else if (QAIConfig.current_env == 1) {
                                        AI_HOST = AI_HOST_DEV;
                                    } else if (QAIConfig.current_env == 2) {
                                        AI_HOST = AI_HOST_TEST;
                                    }
                                    QAIConfig.autoTestDevice = rspJson.optBoolean("autotest_device");
                                    QAIConfig.isCrashCookerEnable = !rspJson.optBoolean("strict_log_uploading"); // 默认值为false，取反值
                                    QAIConfig.isAudioCookerEnable = !rspJson.optBoolean("strict_dump_audio"); // 默认值为false，取反值

                                    if (rspJson.has("url_list")) {
                                        JSONObject urlListObject = rspJson.getJSONObject("url_list");
                                        QAIConfig.aiengine_url = urlListObject.optString("aiengine");
                                        QAIConfig.accesskey_url = urlListObject.optString("accesskey");
                                    }
                                    QAILog.v(TAG, "reqConfigList  :  dumpQAIConfig"/* + QAIConfig.dumpString()*/);
                                } else {
                                    errorMsg = context.getString(R.string.api_error_req_CONFIG_LIST_FAILED);
                                }
                            } catch (JSONException e) {
                                errorMsg = context.getString(R.string.api_error_rsp_JSONException);
                                QAILog.e(TAG, "reqConfigList JSONException : " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            errorMsg = context.getString(R.string.api_error_rsp_EMPTY);
                            QAILog.e(TAG, "reqConfigList rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                errorMsg = context.getString(R.string.api_error_rsp_JSONException);
                QAILog.e(TAG, "reqConfigList error : " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Throwable t) {
            QAILog.e(TAG, "reqConfigList error : " + t.getMessage());
            errorMsg = context.getString(R.string.api_error_unknoow);
            t.printStackTrace();
        }

        if (!TextUtils.isEmpty(errorMsg)) {
//            CountlyEvents.reqConfigListErrorMsg(errorMsg, detailMessage);
        }
    }

    private static Request createConfigListRequest(String action) {
        String SCHEME = "https";
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(SCHEME)
                .host(CONFIG_HOST)
                .addEncodedPathSegment(CONFIG_PATH)
                .build();

        JSONObject reqJson = new JSONObject();
        try {
            reqJson.put("sn", QAIConfig.getMacForSn());
            reqJson.put("action", action);
        } catch (JSONException e) {
            e.printStackTrace();
            QAILog.e(TAG, "getConfigList:reqJsonError = " + e.getMessage());
        }
        String postInfoStr = reqJson.toString();
        QAILog.v(TAG, "getConfigList:data = " + postInfoStr);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postInfoStr);

        return new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();
    }

    private static Request createQrCodeRequest(String token, String sn, String pushid, String baiduId) {
        QAILog.v(TAG, "createQrCodeRequest");
        HttpUrl httpUrl = new Request.Builder()
                .url(HOMEWORK_URL)
                .build()
                .url()
                .newBuilder()
                .addPathSegment(QRCODE_PATH)
                .addQueryParameter("token", token)
                .addQueryParameter("sn", sn)
                .addQueryParameter("pushid", pushid)
                .addQueryParameter("baidupush", baiduId)
                .build();

        return new Request.Builder()
                .url(httpUrl)
                .get()
                .build();
    }

    private static Request createWxInfoRequest(String token, String sn) {
        QAILog.v(TAG, "createWxInfoRequest");
        HttpUrl httpUrl = new Request.Builder()
                .url(HOMEWORK_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(WX_INFO)
                .addQueryParameter("token", token)
                .addQueryParameter("sn", sn)
                .build();

        return new Request.Builder()
                .url(httpUrl)
                .get()
                .build();
    }

    public static void reqWxInfo(Context context, String token, String sn) {
        QAILog.d(TAG, "reqWxInfo: Enter");
        int rspCode;
        okhttp3.Response response;

        try {
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createWxInfoRequest(token, sn)).execute();
                //判断请求是否成功
                if (response == null) {
                    QAILog.e(TAG, "reqWxInfo response empty");
                } else if (!response.isSuccessful()) {
                    QAILog.e(TAG, "reqWxInfo response failed");
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        QAILog.e(TAG, "reqWxInfo rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v(TAG, "reqWxInfo body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                QAILog.v(TAG, "reqWxInfo: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    List<WxUserEntity> wxUserList = new ArrayList<>();
                                    JSONArray wxJsonArray = rspJson.optJSONArray("wx");
                                    if (wxJsonArray != null) {
                                        //不可能同时出现两个绑定者，那是bug;
                                        if (wxJsonArray.length() == 1) {
                                            JSONObject jsonObject = wxJsonArray.getJSONObject(0);
                                            WxUserEntity wxUserEntity = new WxUserEntity();
                                            wxUserEntity.setOpenid(jsonObject.optString(ChatProviderHelper.WxUser.COLUMN_OPENID));
                                            wxUserEntity.setNickname(jsonObject.optString(ChatProviderHelper.WxUser.COLUMN_NICKNAME));
                                            wxUserEntity.setSex(jsonObject.optInt(ChatProviderHelper.WxUser.COLUMN_SEX));
                                            wxUserEntity.setCountry(jsonObject.optString(ChatProviderHelper.WxUser.COLUMN_COUNTRY));
                                            wxUserEntity.setProvince(jsonObject.optString(ChatProviderHelper.WxUser.COLUMN_PROVINCE));
                                            wxUserEntity.setCity(jsonObject.optString(ChatProviderHelper.WxUser.COLUMN_CITY));
                                            wxUserEntity.setSubscribe_time(jsonObject.optLong(ChatProviderHelper.WxUser.COLUMN_SUBSCRIBE_TIME));
                                            wxUserEntity.setHeadimgurl(jsonObject.optString(ChatProviderHelper.WxUser.COLUMN_HEADIMGURL));
                                            wxUserEntity.setLanguage(jsonObject.optString(ChatProviderHelper.WxUser.COLUMN_LANGUAGE));
                                            wxUserEntity.setRemark(jsonObject.optString(ChatProviderHelper.WxUser.COLUMN_REMARK));
                                            wxUserEntity.setQr_scene(jsonObject.optInt(ChatProviderHelper.WxUser.COLUMN_QR_SCENE));
                                            wxUserEntity.setQr_scene_str(jsonObject.optString(ChatProviderHelper.WxUser.COLUMN_QR_SCENE_STR));
                                            wxUserEntity.setSubscribe(jsonObject.optInt(ChatProviderHelper.WxUser.COLUMN_SUBSCRIBE));
                                            wxUserEntity.setUnionid(jsonObject.optString(ChatProviderHelper.WxUser.COLUMN_UNIONID));
                                            wxUserEntity.setGroupid(jsonObject.optInt(ChatProviderHelper.WxUser.COLUMN_GROUPID));
                                            wxUserEntity.setSubscribe_scene(jsonObject.optString(ChatProviderHelper.WxUser.COLUMN_SUBSCRIBE_SCENE));
                                            wxUserEntity.setStatus(jsonObject.optInt(ChatProviderHelper.WxUser.COLUMN_STATUS));
                                            wxUserList.add(wxUserEntity);
                                            String openID = jsonObject.optString(ChatProviderHelper.WxUser.COLUMN_OPENID);
                                            if (!ChatProviderHelper.mWxUserMap.isEmpty() && !ChatProviderHelper.mWxUserMap.containsKey((openID))) {
                                            }
                                            ChatProviderHelper.mWxUserMap.put(openID, wxUserEntity);
                                        }
                                        ChatProviderHelper.insertOrUpdateWxUser(wxUserList);
                                    }
                                } else {

                                    QAILog.e(TAG, "reqWxInfo rspCode error srv_code : " + rspCode);
                                }
                            } catch (JSONException e) {
                                QAILog.e(TAG, "reqWxInfo JSONException : " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            QAILog.e(TAG, "reqWxInfo rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "reqQrCode error : " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Throwable t) {
            QAILog.e(TAG, "reqQrCode error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    public static void reqQrCode(Context context, String token, String sn, String pushid, String baiduId) {

        QAILog.i(TAG, "reqQrCode: Enter");
        int rspCode;
        okhttp3.Response response;

        try {
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createQrCodeRequest(token, sn, pushid, baiduId)).execute();
                //判断请求是否成功
                if (response == null) {
                    QAIConfig.qrcode = "";
                    QAILog.e(TAG, "reqQrCode response empty");
                } else if (!response.isSuccessful()) {
                    QAIConfig.qrcode = "";
                    QAILog.e(TAG, "reqQrCode response failed");
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        QAIConfig.qrcode = "";
                        QAILog.e(TAG, "reqQrCode rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v(TAG, "reqQrCode body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                QAILog.v(TAG, "reqQrCode: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    String qrcode = rspJson.optString("qrcode");
                                    QAILog.i(TAG, "qrcode is: " + qrcode);
                                    QAIConfig.qrcode = qrcode;
                                    QAIConfig.qrcode_expiration = rspJson.optInt("expiration");
                                } else {
                                    QAIConfig.qrcode = "";
                                    QAILog.e(TAG, "reqQrCode rspCode error srv_code : " + rspCode);
                                }
                            } catch (JSONException e) {
                                QAIConfig.qrcode = "";
                                QAILog.e(TAG, "reqQrCode JSONException : " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            QAIConfig.qrcode = "";
                            QAILog.e(TAG, "reqQrCode rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAIConfig.qrcode = "";
                QAILog.e(TAG, "reqQrCode error : " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Throwable t) {
            QAIConfig.qrcode = "";
            QAILog.e(TAG, "reqQrCode error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createPostRequest(String token, String sn, String type, String path, String body, String url) {
        HttpUrl httpUrl = new Request.Builder()
                .url(url)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(VOICE_PATH)
                .addQueryParameter("token", token)
                .build();

        JSONObject reqJson = new JSONObject();
        try {
            reqJson.put("sn", sn);
            reqJson.put("type", type);
            reqJson.put("path", path);
            reqJson.put("body", body);
        } catch (JSONException e) {
            e.printStackTrace();
            QAILog.e(TAG, "postMessage:reqJsonError = " + e.getMessage() + " url" + httpUrl);
        }
        String postInfoStr = reqJson.toString();
        QAILog.v(TAG, "postMessage:data = " + postInfoStr + " url" + httpUrl);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postInfoStr);

        return new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();
    }

    public static void postMessage(Context context, String token, String sn, String type, String path, String body) {

        QAILog.d(TAG, "postMessage: Enter");
        int rspCode;
        okhttp3.Response response;

        try {
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createPostRequest(token, sn, type, path, body, HOMEWORK_URL)).execute();
                //判断请求是否成功
                if (response == null) {
                    QAILog.e(TAG, "postMessage response empty");
                } else if (!response.isSuccessful()) {
                    QAILog.e(TAG, "postMessage response failed");
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        QAILog.e(TAG, "postMessage rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v(TAG, "postMessage body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                QAILog.v(TAG, "postMessage: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                } else {
                                    QAILog.e(TAG, "postMessage rspCode error srv_code : " + rspCode);
                                }
                            } catch (JSONException e) {
                                QAILog.e(TAG, "postMessage JSONException : " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            QAILog.e(TAG, "postMessage rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "postMessage error : " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Throwable t) {
            QAILog.e(TAG, "postMessage error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    public static void fetchHomework(final Context context, final String token, final String sn,
                                     final String type, final String path, final String body) {

        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                QAILog.d(TAG, "fetchHomework: Enter");
                int rspCode;
                okhttp3.Response response;

                try {
                    try {
                        response = OkhttpClientHelper.getOkHttpClient().newCall(createPostRequest(token, sn, type, path, body, HOMEWORK_URL)).execute();
                        //判断请求是否成功
                        if (response == null) {
                            QAILog.e(TAG, "fetchHomework response empty");
                        } else if (!response.isSuccessful()) {
                            QAILog.e(TAG, "fetchHomework response failed");
                        } else {
                            if (!HttpHeaders.hasBody(response)) {
                                QAILog.e(TAG, "fetchHomework rspBody empty");
                            } else {
                                //打印服务端返回结果
                                ResponseBody rspBody = response.body();
                                long contentLength = rspBody.contentLength();
                                if (contentLength != 0) {
                                    String sBody = rspBody.string();
                                    QAILog.v(TAG, "fetchHomework body = : " + sBody);
                                    try {
                                        JSONObject rspJson = new JSONObject(sBody);
                                        rspCode = rspJson.optInt("code", -100);
                                        QAILog.v(TAG, "fetchHomework: rspCode: " + rspCode);
                                        QAIConfig.code = rspCode;
                                        if (rspCode == 0) {
                                            List<HomeWorkEntity> homeWorkList = new ArrayList<>();
                                            String alterContent = "";
                                            String id = "";
                                            String url = "";
                                            JSONArray totalWorkListJsonArray = rspJson.optJSONArray("work_list");
                                            if (totalWorkListJsonArray != null) {
                                                for (int i = 0; i < totalWorkListJsonArray.length(); i++) {
                                                    JSONObject workListJsonObject = totalWorkListJsonArray.getJSONObject(i);
                                                    JSONArray userWorkListJsonArray = workListJsonObject.optJSONArray("list");
                                                    if (userWorkListJsonArray != null) {
                                                        for (int n = 0; n < userWorkListJsonArray.length(); n++) {
                                                            JSONObject homeworkJsonObject = userWorkListJsonArray.getJSONObject(n);
                                                            String content = homeworkJsonObject.optString("content");
                                                            String type = homeworkJsonObject.optString("hwtype");
                                                            alterContent = content;
                                                            url = homeworkJsonObject.optString("url");
                                                            id = homeworkJsonObject.optString(ChatProviderHelper.Homework.ID);
                                                            HomeWorkEntity homeworkEntity = new HomeWorkEntity();
                                                            homeworkEntity.setOpenid(homeworkJsonObject.optString(ChatProviderHelper.Homework.COLUMN_OPENID));
                                                            homeworkEntity.setHomeworkid(id);
                                                            homeworkEntity.setType(type);
                                                            homeworkEntity.setTitle(homeworkJsonObject.optString(ChatProviderHelper.Homework.COLUMN_TITLE));
                                                            homeworkEntity.setContent(homeworkJsonObject.optString(ChatProviderHelper.Homework.COLUMN_CONTENT));
                                                            homeworkEntity.setUrl(url);
                                                            String time = homeworkJsonObject.optString(ChatProviderHelper.Homework.COLUMN_ASSIGN_TIME);
                                                            Long milliseconds;
                                                            if (!TextUtils.isEmpty(time))
                                                                milliseconds = DateUtils.strToDate(time);
                                                            else
                                                                milliseconds = 0L;
                                                            QAILog.d(TAG, "Homework content is: " + content);
                                                            homeworkEntity.setAssign_time(milliseconds);
                                                            homeworkEntity.setFinish_time(homeworkJsonObject.optLong(ChatProviderHelper.Homework.COLUMN_FINISH_TIME));
                                                            homeworkEntity.setStatus(homeworkJsonObject.optInt(ChatProviderHelper.Homework.COLUMN_STATUS));
                                                            homeWorkList.add(homeworkEntity);
                                                        }
                                                    }
                                                }
                                                if (!homeWorkList.isEmpty()) {
                                                    ChatProviderHelper.insertOrUpdateHomework(homeWorkList);
                                                    MessageManager.addNewHomeworkList(homeWorkList);
                                                    MessageManager.wakeup(context);
                                                    for (int num = 0; num < homeWorkList.size(); num++) {
                                                        QAILog.v(TAG, "fetchHomework: showWorkAlert: " + alterContent);
                                                        if (!TextUtils.isEmpty(homeWorkList.get(num).getUrl()) && !homeWorkList.get(num).getUrl().equals("null")) {
                                                            AWSTransferHelper.getInstance().download(context, homeWorkList.get(num).getUrl(), 4, 0);
                                                            MessageManager.showWorkAlert(context, "", homeWorkList.get(num).getHomeworkid());
                                                        } else
                                                            MessageManager.showWorkAlert(context, homeWorkList.get(num).getContent(), homeWorkList.get(num).getHomeworkid());
                                                    }
                                                    if ((TextUtils.isEmpty(url) || url.equals("null")) && UIHelp.isTopActivityWorkActivity()) {
                                                        MessageManager.asyncNotifyHomeworkChange(true, homeWorkList);
                                                    }

                                                    Intent intent = new Intent("VOICE_RESPONSE_NEW_HOMEWORK");
                                                    intent.putExtra("voice_id", 501);
                                                    context.sendBroadcast(intent);
                                                }
                                            }
                                        } else {
                                            QAILog.e(TAG, "fetchHomework rspCode error srv_code : " + rspCode);
                                        }
                                    } catch (JSONException e) {
                                        QAILog.e(TAG, "fetchHomework JSONException : " + e.getMessage());
                                        e.printStackTrace();
                                    }
                                } else {
                                    QAILog.e(TAG, "fetchHomework rspBody empty");
                                }
                            }
                        }
                    } catch (IOException e) {
                        QAILog.e(TAG, "fetchHomework error : " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (Throwable t) {
                    QAILog.e(TAG, "fetchHomework error : " + t.getMessage());
                    t.printStackTrace();
                }
            }
        });
    }

    public static void fetchReminder(final Context context, final String token, final String sn,
                                     final String type, final String path, final String body) {

        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                QAILog.d(TAG, "fetchReminder: Enter");
                int rspCode;
                okhttp3.Response response;

                try {
                    try {
                        response = OkhttpClientHelper.getOkHttpClient().newCall(createPostRequest(token, sn, type, path, body, HOMEWORK_URL)).execute();
                        //判断请求是否成功
                        if (response == null) {
                            QAILog.e(TAG, "fetchReminder response empty");
                        } else if (!response.isSuccessful()) {
                            QAILog.e(TAG, "fetchReminder response failed");
                        } else {
                            if (!HttpHeaders.hasBody(response)) {
                                QAILog.e(TAG, "fetchReminder rspBody empty");
                            } else {
                                //打印服务端返回结果
                                ResponseBody rspBody = response.body();
                                long contentLength = rspBody.contentLength();
                                if (contentLength != 0) {
                                    String sBody = rspBody.string();
                                    QAILog.v(TAG, "fetchReminder body = : " + sBody);
                                    try {
                                        JSONObject rspJson = new JSONObject(sBody);
                                        rspCode = rspJson.optInt("code", -100);
                                        QAILog.v(TAG, "fetchReminder: rspCode: " + rspCode);
                                        QAIConfig.code = rspCode;
                                        if (rspCode == 0) {
                                            List<RemindEntity> remindWorkList = new ArrayList<>();
                                            JSONArray totalWorkListJsonArray = rspJson.optJSONArray("remind_list");
                                            if (totalWorkListJsonArray != null) {
                                                for (int i = 0; i < totalWorkListJsonArray.length(); i++) {
                                                    JSONObject workListJsonObject = totalWorkListJsonArray.getJSONObject(i);
                                                    String fromWho = workListJsonObject.optString("from_who");
                                                    QAILog.d(TAG, "fromwho is: " + fromWho);
                                                    JSONArray userWorkListJsonArray = workListJsonObject.optJSONArray("list");
                                                    if (userWorkListJsonArray != null) {
                                                        for (int n = 0; n < userWorkListJsonArray.length(); n++) {
                                                            JSONObject remindJsonObject = userWorkListJsonArray.getJSONObject(n);
                                                            String content = remindJsonObject.optString("content");

                                                            RemindEntity remindEntity = new RemindEntity();
                                                            remindEntity.setOpenid(remindJsonObject.optString(ChatProviderHelper.Reminder.COLUMN_OPENID));
                                                            String reminderID = remindJsonObject.optString("id");
                                                            remindEntity.setRemindid(reminderID);
                                                            remindEntity.setContent(remindJsonObject.optString(ChatProviderHelper.Reminder.COLUMN_REMINDER_CONTENT));
                                                            String time = remindJsonObject.optString(ChatProviderHelper.Reminder.COLUMN_ASSIGN_TIME);
                                                            Long milltime = DateUtils.strToDate(time);
                                                            remindEntity.setAssign_time(milltime);
                                                            String repeat = remindJsonObject.optString("repeat");
                                                            if (repeat.equals("0"))
                                                                repeat = null;
                                                            remindEntity.setRepeat_time(repeat);
                                                            remindEntity.setType(0);
                                                            time = remindJsonObject.optString(ChatProviderHelper.Reminder.TRIGER_TIME);
                                                            if (TextUtils.isEmpty(time)) {
                                                                continue;
                                                            }
                                                            QAILog.d(TAG, "milltime" + milltime);
                                                            if (repeat == null) {
                                                                milltime = DateUtils.strToRemindDate(time);
                                                                QAILog.d(TAG, "null");
                                                                remindEntity.setStatus(0);
                                                                CalenderProviderHelper.insertEventAndReminder(context,
                                                                        content,
                                                                        milltime,
                                                                        null, reminderID, CalenderProviderHelper.ACCOUNT_NORMAL);//new RecurrenceHelper().getRrule("EVERYDAY")
                                                            } else {
                                                                milltime = DateUtils.strToRemindDate(time);
                                                                ;
                                                                remindEntity.setStatus(0);
                                                                String w = "W" + repeat;
                                                                CalenderProviderHelper.insertEventAndReminder(context,
                                                                        content,
                                                                        DateUtils.getReminderTime(milltime, w),
                                                                        new RecurrenceHelper().getRrule(w), reminderID, CalenderProviderHelper.ACCOUNT_NORMAL);//w
                                                            }
                                                            /*only for system test */
                                                            QAILog.d(TAG, "Reminder content is: " + content);
                                                            remindEntity.setRemind_time(milltime);
                                                            remindWorkList.add(remindEntity);
                                                        }
                                                    }
                                                }
                                                ChatProviderHelper.insertOrUpdateRemind(remindWorkList);
                                                MessageManager.notifyReminderChange(true);
                                                MessageManager.NotifyReminderToLauncher(context);
                                            }
                                        } else {
                                            QAILog.e(TAG, "fetchReminder rspCode error srv_code : " + rspCode);
                                        }
                                    } catch (JSONException e) {
                                        QAILog.e(TAG, "fetchReminder JSONException : " + e.getMessage());
                                        e.printStackTrace();
                                    }
                                } else {
                                    QAILog.e(TAG, "fetchReminder rspBody empty");
                                }
                            }
                        }
                    } catch (IOException e) {
                        QAILog.e(TAG, "fetchReminder error : " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (Throwable t) {
                    QAILog.e(TAG, "fetchReminder error : " + t.getMessage());
                    t.printStackTrace();
                }
            }
        });
    }


    private static Request createPostReceipt(String token, String type, String msgid, String pushChannel) {
        HttpUrl httpUrl = new Request.Builder()
                .url(HOMEWORK_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(VOICE_PATH)
                .addQueryParameter("token", token)
                .build();

        JSONObject reqJson = new JSONObject();
        try {
            reqJson.put("type", type);
            reqJson.put("msgid", msgid);
            reqJson.put("pushchannel", pushChannel);
        } catch (JSONException e) {
            e.printStackTrace();
            QAILog.e(TAG, "postMessage:reqJsonError = " + e.getMessage());
        }
        String postInfoStr = reqJson.toString();
        QAILog.v(TAG, "postMessage:data = " + postInfoStr);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postInfoStr);

        return new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();
    }


    public static void postReceipt(Context context, String token, String type, String msgid, String pushchannel) {

        QAILog.d(TAG, "postMessage: Enter");
        int rspCode;
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createPostReceipt(token, type, msgid, pushchannel)).execute();
                //判断请求是否成功
                if (response == null) {
                    QAILog.e(TAG, "postMessage response empty");
                } else if (!response.isSuccessful()) {
                    QAILog.e(TAG, "postMessage response failed");
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        QAILog.e(TAG, "postMessage rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v(TAG, "postMessage body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                QAILog.v(TAG, "postMessage: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                } else {
                                    QAILog.e(TAG, "postMessage rspCode error srv_code : " + rspCode);
                                }
                            } catch (JSONException e) {
                                QAILog.e(TAG, "postMessage JSONException : " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            QAILog.e(TAG, "postMessage rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "postMessage error : " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Throwable t) {
            QAILog.e(TAG, "postMessage error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createPostChannelID(String token, String sn, String channelID, boolean type) {
        HttpUrl httpUrl = new Request.Builder()
                .url(HOMEWORK_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(BAIDU_PATH)
                .addQueryParameter("token", token)
                .build();

        JSONObject reqJson = new JSONObject();
        try {
            reqJson.put("sn", sn);
            if (type) {
                reqJson.put("pushtype", "baidu");
            } else {
                reqJson.put("pushtype", "xinge");
            }
            reqJson.put("pushid", channelID);
        } catch (JSONException e) {
            e.printStackTrace();
            QAILog.e(TAG, "reqJsonError = " + e.getMessage());
        }
        String postInfoStr = reqJson.toString();
        QAILog.v(TAG, "PostBaiduChannelID:data = " + postInfoStr);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postInfoStr);

        return new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();
    }

    public static void postChannelID(Context context, String token, String channelID, boolean type) {

        QAILog.d(TAG, "postChannelID: Enter");
        String sn = QAIConfig.getMacForSn();
        int rspCode;
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createPostChannelID(token, sn, channelID, type)).execute();
                //判断请求是否成功
                if (response == null) {
                    QAILog.e(TAG, "postChannelID response empty");
                } else if (!response.isSuccessful()) {
                    QAILog.e(TAG, "postChannelID response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        QAILog.e(TAG, "postChannelID rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v(TAG, "postChannelID body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                QAILog.v(TAG, "postChannelID: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {

                                    QAILog.d(TAG, "postChannelID successful");
                                } else {
                                    QAILog.e(TAG, "postChannelID rspCode error srv_code : " + rspCode);
                                }
                            } catch (JSONException e) {
                                QAILog.e(TAG, "postChannelID JSONException : " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            QAILog.e(TAG, "postChannelID rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "postChannelID error : " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Throwable t) {
            QAILog.e(TAG, "postChannelID error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createPostHomeworkID(String token, String sn, String id) {
        HttpUrl httpUrl = new Request.Builder()
                .url(HOMEWORK_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(VOICE_PATH)
                .addQueryParameter("token", token)
                .build();

        JSONObject reqJson = new JSONObject();
        try {
            reqJson.put("id", id);
            reqJson.put("sn", sn);
            reqJson.put("type", "readhomework");
        } catch (JSONException e) {
            e.printStackTrace();
            QAILog.e(TAG, "reqJsonError = " + e.getMessage());
        }
        String postInfoStr = reqJson.toString();
        QAILog.v(TAG, "postHomework:data = " + postInfoStr);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postInfoStr);

        return new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();
    }

    public static void postHomeworkID(String token, String sn, String id) {

        QAILog.d(TAG, "postHomeworkID: Enter");
        int rspCode;
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createPostHomeworkID(token, sn, id)).execute();
                //判断请求是否成功
                if (response == null) {
                    QAILog.e(TAG, "postHomeworkID response empty");
                } else if (!response.isSuccessful()) {
                    QAILog.e(TAG, "postHomeworkID response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        QAILog.e(TAG, "postHomeworkID rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v(TAG, "postHomeworkID body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                QAILog.v(TAG, "postHomeworkID: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {

                                    QAILog.d(TAG, "postHomeworkID successful");
                                } else {
                                    QAILog.e(TAG, "postHomeworkID rspCode error srv_code : " + rspCode);
                                }
                            } catch (JSONException e) {
                                QAILog.e(TAG, "postHomeworkID JSONException : " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            QAILog.e(TAG, "postHomeworkID rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "postHomeworkID error : " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Throwable t) {
            QAILog.e(TAG, "postHomeworkID error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    //get请求(获取礼物列表)
    public static void postGift(String token, String sn) {

        QAILog.d(TAG, "postGift: Enter");
        int rspCode;
        int allStar;
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createPostGift(token, sn)).execute();
                //判断请求是否成功
                if (response == null) {
                    MessageManager.addGiftListError(-100);
                    QAILog.e(TAG, "postGift response empty");
                } else if (!response.isSuccessful()) {
                    MessageManager.addGiftListError(-101);
                    QAILog.e(TAG, "postGift response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        MessageManager.addGiftListError(-102);
                        QAILog.e(TAG, "postGift rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v("GiftActivityLog", "postGift body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                allStar = rspJson.optInt("all_star");
                                QAILog.v(TAG, "postHomeworkID: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    JSONArray array = rspJson.optJSONArray("gifts");
                                    List<GiftEntity> receiveList = new ArrayList<>();
                                    List<GiftEntity> receivedList = new ArrayList<>();
                                    if (null != array && array.length() > 0) {
                                        for (int i = 0; i < array.length(); i++) {
                                            JSONObject jsonObject = array.getJSONObject(i);
                                            GiftEntity entity = new GiftEntity();
                                            entity.setId(jsonObject.optInt("id"));
                                            entity.setDate(jsonObject.optString("got_time"));
                                            entity.setGiftName(jsonObject.optString("name"));
                                            entity.setStarCount(jsonObject.optInt("star_num"));
                                            //表示是否已领取,0可领取或者不可领取，2已领取，3表示领取中，只能有一个
                                            if (2 == jsonObject.optInt("flag")) {
                                                entity.setStatus(2);
                                                receivedList.add(entity);
                                            } else if (3 == jsonObject.optInt("flag")) {
                                                entity.setStatus(3);
                                                receiveList.add(entity);
                                            } else if (0 == jsonObject.optInt("flag")) {
                                                //可领取
                                                if (allStar >= jsonObject.optInt("star_num")) {
                                                    entity.setStatus(0);
                                                    receiveList.add(entity);
                                                    //不可领取
                                                } else {
                                                    entity.setStatus(1);
                                                    receiveList.add(entity);
                                                }
                                            }
                                        }
                                    }

                                    //已领取的需要根据时间排序
                                    Collections.sort(receivedList);
                                    MessageManager.addGiftList(receiveList, receivedList, allStar);
                                } else {
                                    MessageManager.addGiftListError(rspCode);
                                }
                            } catch (JSONException e) {
                                MessageManager.addGiftListError(-103);
                                e.printStackTrace();
                            }
                        } else {
                            MessageManager.addGiftListError(-104);
                            QAILog.e(TAG, "postHomeworkID rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "postGift error : " + e.getMessage());
                MessageManager.addGiftListError(-105);
                e.printStackTrace();
            }
        } catch (Throwable t) {
            MessageManager.addGiftListError(-106);
            QAILog.e(TAG, "postGift error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createPostGift(String token, String sn) {
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(WX_GIFT)
                .addQueryParameter("token", token)
                .addQueryParameter("sn", sn)
                .build();
        Log.i("GiftActivityLog", "url=" + httpUrl.url());
        return new Request.Builder()
                .url(httpUrl)
                .build();
    }

    /**
     * * post，根据不同的action值，做不同的处理
     *
     * @param action "got"领取中，点击红星的操作    "finished"点击已领取礼物   "cancel"取消领取礼物
     */

    public static void postReceiveGift(String token, String sn, int id, int star, String action) {

        QAILog.d(TAG, "postGift: Enter");
        int rspCode;
        int allStar;
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createPostReceiveGift(token, sn, id, star, action)).execute();
                //判断请求是否成功
                if (response == null) {
                    MessageManager.receiveGiftError(-100);
                    QAILog.e(TAG, "postGift response empty");
                } else if (!response.isSuccessful()) {
                    MessageManager.receiveGiftError(-101);
                    QAILog.e(TAG, "postGift response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        MessageManager.receiveGiftError(-102);
                        QAILog.e(TAG, "postGift rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v("GiftActivityLog", "postGift body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                allStar = rspJson.optInt("all_star");
                                QAILog.v(TAG, "postHomeworkID: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    JSONArray array = rspJson.optJSONArray("gifts");
                                    List<GiftEntity> receiveList = new ArrayList<>();
                                    List<GiftEntity> receivedList = new ArrayList<>();
                                    if (null != array && array.length() > 0) {
                                        for (int i = 0; i < array.length(); i++) {
                                            JSONObject jsonObject = array.getJSONObject(i);
                                            GiftEntity entity = new GiftEntity();
                                            entity.setId(jsonObject.optInt("id"));
                                            entity.setDate(jsonObject.optString("got_time"));
                                            entity.setGiftName(jsonObject.optString("name"));
                                            entity.setStarCount(jsonObject.optInt("star_num"));
                                            //表示是否已领取,0可领取或者不可领取，2已领取，3表示领取中，只能有一个
                                            if (2 == jsonObject.optInt("flag")) {
                                                entity.setStatus(2);
                                                receivedList.add(entity);
                                            } else if (3 == jsonObject.optInt("flag")) {
                                                entity.setStatus(3);
                                                receiveList.add(entity);
                                            } else if (0 == jsonObject.optInt("flag")) {
                                                //可领取
                                                if (allStar >= jsonObject.optInt("star_num")) {
                                                    entity.setStatus(0);
                                                    receiveList.add(entity);
                                                    //不可领取
                                                } else {
                                                    entity.setStatus(1);
                                                    receiveList.add(entity);
                                                }
                                            }
                                        }
                                    }
                                    Collections.sort(receivedList);
                                    MessageManager.receiveGift(receiveList, receivedList, allStar, action);
                                } else {
                                    MessageManager.receiveGiftError(rspCode);
                                }
                            } catch (JSONException e) {
                                MessageManager.receiveGiftError(-103);
                                e.printStackTrace();
                            }
                        } else {
                            MessageManager.receiveGiftError(-104);
                            QAILog.e(TAG, "postHomeworkID rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "postGift error : " + e.getMessage());
                MessageManager.receiveGiftError(-105);
                e.printStackTrace();
            }
        } catch (Throwable t) {
            MessageManager.receiveGiftError(-106);
            QAILog.e(TAG, "postGift error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createPostReceiveGift(String token, String sn, int id, int star, String action) {
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(WX_GIFT)
                .addQueryParameter("token", token)
                .build();

        JSONObject reqJson = new JSONObject();
        try {
            reqJson.put("sn", sn);
            reqJson.put("action", action);
            reqJson.put("id", id);
            reqJson.put("star", star);
        } catch (JSONException e) {
            e.printStackTrace();
            QAILog.e(TAG, "reqJsonError = " + e.getMessage());
        }
        String postInfoStr = reqJson.toString();
        Log.i("GiftActivityLog", "token=" + token + "  sn=" + sn + "  postInfoStr=" + postInfoStr);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postInfoStr);

        return new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();
    }

    private static Request createHabitRequest(Context context, String token, String sn, boolean full) {
        QAILog.v(TAG, "createHabitRequest");
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(VOICE_PATH)
                .addQueryParameter("token", token)
                .build();

        JSONObject reqJson = new JSONObject();
        try {
            reqJson.put("sn", sn);
            reqJson.put("type", "fetchhabitv2");
            if (!full) {
                String time = UIHelp.getUpdateTime(context);
                if (!TextUtils.isEmpty(time)) {
                    reqJson.put("update_time", time);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            QAILog.e(TAG, "reqJsonError = " + e.getMessage());
        }
        String postInfoStr = reqJson.toString();
        QAILog.v(TAG, "createHabitRequest:data = " + postInfoStr);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postInfoStr);

        return new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();
    }

    public static void fetchHabit(final Context context, final String token, final String sn, final boolean full) {

        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                QAILog.i(TAG, "fetchHabitv2: Enter");
                int rspCode;
                okhttp3.Response response;

                try {
                    try {
                        response = OkhttpClientHelper.getOkHttpClient().newCall(createHabitRequest(context, token, sn, full)).execute();
                        //判断请求是否成功
                        if (response == null) {
                            QAILog.e(TAG, "fetchHabit response empty");
                        } else if (!response.isSuccessful()) {
                            QAILog.e(TAG, "fetchHabit response failed");
                        } else {
                            if (!HttpHeaders.hasBody(response)) {
                                QAILog.e(TAG, "fetchHabit rspBody empty");
                            } else {
                                //打印服务端返回结果
                                ResponseBody rspBody = response.body();
                                long contentLength = rspBody.contentLength();
                                if (contentLength != 0) {
                                    String sBody = rspBody.string();
                                    QAILog.i(TAG, "fetchHabit2 body = : " + sBody);
                                    try {
                                        JSONObject rspJson = new JSONObject(sBody);
                                        rspCode = rspJson.optInt("code", -100);
                                        QAILog.v(TAG, "fetchHabit: rspCode: " + rspCode);
                                        QAIConfig.code = rspCode;
                                        if (rspCode == 0) {
                                            List<HabitEntity> habitList = new ArrayList<>();
                                            JSONArray habitListJsonArray = rspJson.optJSONArray("habit_list");
                                            if (habitListJsonArray != null) {
                                                Long updateTime = 0L;
                                                String updateTimeString = "";
                                                String habitListString = habitListJsonArray.toString();
                                                if (!TextUtils.isEmpty(habitListString) && !TextUtils.isEmpty(prevHabitList) && habitListString.equals(prevHabitList))
                                                    return;
                                                setHabitJsonArrayString(habitListString);
                                                for (int i = 0; i < habitListJsonArray.length(); i++) {
                                                    JSONObject habitListJsonObject = habitListJsonArray.getJSONObject(i);
                                                    String fromWho = habitListJsonObject.optString("from_who");
                                                    QAILog.d(TAG, "fromwho is: " + fromWho);
                                                    JSONArray habitJsonArray = habitListJsonObject.optJSONArray("list");
                                                    if (habitJsonArray != null) {
                                                        for (int n = 0; n < habitJsonArray.length(); n++) {
                                                            JSONObject habitJsonObject = habitJsonArray.getJSONObject(n);
                                                            String name = habitJsonObject.optString("name");
                                                            int star = habitJsonObject.optInt("star");
                                                            int id = habitJsonObject.optInt("id");
                                                            int type = habitJsonObject.optInt("type");
                                                            int habitID = type;
                                                            String assignTime = habitJsonObject.optString("add_time");
                                                            String endTime = habitJsonObject.optString("end_time");
                                                            boolean isSelected = habitJsonObject.optBoolean("is_selected");
                                                            if (!isSelected)
                                                                continue;
                                                            QAILog.d(TAG, "get habit type: " + type);
                                                            Long last_checkin_time = habitJsonObject.optLong("last_checkin", 0L);
                                                            int duration = habitJsonObject.optInt("delta_time");
                                                            String habitUpdateTime = habitJsonObject.optString("update_time");
                                                            Long habitUpdateMillis = 0L;
                                                            if (!TextUtils.isEmpty(habitUpdateTime))
                                                                habitUpdateMillis = DateUtils.strDateToLong(habitUpdateTime);
                                                            QAILog.d(TAG, "habit update Time: " + habitUpdateTime + " habit checkin time " + last_checkin_time);
                                                            if (habitUpdateMillis > updateTime) {
                                                                updateTime = habitUpdateMillis;
                                                                updateTimeString = habitUpdateTime;
                                                            }
                                                            Long assignMillis = DateUtils.strToHabitStartDate(assignTime);
                                                            Long endMillis = 0L;
                                                            if (!TextUtils.isEmpty(endTime))
                                                                endMillis = DateUtils.strToEndDate(endTime);
                                                            if (System.currentTimeMillis() > endMillis) {
                                                                continue;
                                                            }
                                                            JSONArray alarmArray = habitJsonObject.optJSONArray("alarm_json");
                                                            if (type == CalenderProviderHelper.TYPE_GETUP || type == CalenderProviderHelper.TYPE_SLEEP)
                                                                CalenderProviderHelper.deleteAllEvents(context, type);
                                                            else if (type > CalenderProviderHelper.TYPE_WORK) {
                                                                CalenderProviderHelper.deleteEvent(context, Integer.toString(habitID), CalenderProviderHelper.TYPE_SELFHABIT);
                                                                CalenderProviderHelper.deleteEvent(context, Integer.toString(CalenderProviderHelper.TYPE_INTERVAL + habitID), CalenderProviderHelper.TYPE_SELFHABIT);
                                                            }
                                                            MessageManager.deleteHabitFromLocalDB(type);
                                                            if (alarmArray != null) {
                                                                for (int j = 0; j < alarmArray.length(); j++) {
                                                                    JSONObject alarmObject = alarmArray.getJSONObject(j);
                                                                    String when = alarmObject.optString("when");
                                                                    boolean isOpened = alarmObject.optBoolean("isOpened", false);
                                                                    when = when + ":00";

                                                                    String repeat = alarmObject.getString("repeat");
                                                                    Long start_millis = DateUtils.strToDateTime(when);
                                                                    Long startReminder = DateUtils.strToDateTime(assignTime, when);
                                                                    if (start_millis < startReminder)
                                                                        start_millis = startReminder;
                                                                    String w = "W" + repeat;
                                                                    if (type == CalenderProviderHelper.TYPE_GETUP || type == CalenderProviderHelper.TYPE_SLEEP)
                                                                        CalenderProviderHelper.insertEventAndReminder(context,
                                                                                name,
                                                                                DateUtils.getReminderTime(start_millis, w),
                                                                                new RecurrenceHelper().getRrule(w, 0), Integer.toString(id++), type);
                                                                    else if (type > CalenderProviderHelper.TYPE_WORK) {
                                                                        CalenderProviderHelper.insertEventAndReminder(context,
                                                                                name,
                                                                                DateUtils.getReminderTime(start_millis, w),
                                                                                new RecurrenceHelper().getRrule(w, 0), Integer.toString(habitID), CalenderProviderHelper.TYPE_SELFHABIT);
                                                                        habitID += CalenderProviderHelper.TYPE_INTERVAL;
                                                                    }
                                                                    //   Long beginTime = DateUtils.strToHabitStartDate(assignTime, when);
                                                                    HabitEntity habitEntity = new HabitEntity();
                                                                    habitEntity.setOpenid("");
                                                                    habitEntity.setStatus(star);
                                                                    habitEntity.setEnd_time(endMillis);
                                                                    habitEntity.setRemind_time(startReminder);
                                                                    habitEntity.setType(type);
                                                                    habitEntity.setRepeat_time(repeat);
                                                                    habitEntity.setName(name);
                                                                    habitEntity.setRemindid(Integer.toString(id));
                                                                    habitEntity.setAssign_time(assignMillis);
                                                                    habitList.add(habitEntity);

                                                                }
                                                            }
                                                            if (last_checkin_time != 0) {
                                                                UIHelp.setAlreadyHabitSign(context, type, star, last_checkin_time * 1000);
                                                            }
                                                        }
                                                    }
                                                }
                                                if (!habitList.isEmpty()) {
                                                    int status = ChatProviderHelper.insertOrUpdateHabit(habitList);
                                                    if (status == -1) {
                                                        fetchHabit(context, StringEncryption.generateToken(), QAIConfig.getMacForSn(), false);
                                                        return;
                                                    }
                                                    MessageManager.asyncNotifyReminderChange(true);
                                                    MessageManager.NotifyReminderToLauncher(context);
                                                } else
                                                    QAILog.d(TAG, "habit list is null");
                                                if (updateTime != 0) {
                                                    UIHelp.setUpdateTime(context, updateTimeString);
                                                }
                                            }
                                        } else {
                                            QAILog.e(TAG, "habitListJsonArray rspCode error srv_code : " + rspCode);
                                        }
                                    } catch (JSONException e) {
                                        QAILog.e(TAG, "habitListJsonArray JSONException : " + e.getMessage());
                                        e.printStackTrace();
                                    }
                                } else {
                                    QAILog.e(TAG, "habitListJsonArray rspBody empty");
                                }
                            }
                        }
                    } catch (IOException e) {
                        QAILog.e(TAG, "habitListJsonArray error : " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (Throwable t) {
                    QAILog.e(TAG, "habitListJsonArray error : " + t.getMessage());
                    t.printStackTrace();
                }
            }
        });
    }

    //习惯打卡
    private static Request createPostHabit(String token, String sn, String type, int star) {
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(VOICE_PATH)
                .addQueryParameter("token", token)
                .build();

        JSONObject reqJson = new JSONObject();
        try {
            reqJson.put("sn", sn);
            reqJson.put("type", "checkin");
            reqJson.put("typecode", Integer.parseInt(type));
            reqJson.put("id", 103);
            reqJson.put("star", star);
        } catch (JSONException e) {
            e.printStackTrace();
            QAILog.e(TAG, "reqJsonError = " + e.getMessage());
        }
        String postInfoStr = reqJson.toString();
        QAILog.v(TAG, "postHabit:data = " + postInfoStr);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postInfoStr);

        return new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();
    }

    public static boolean postHabit(Context context, String token, String sn, int type, int star) {

        QAILog.d(TAG, "postHabit: Enter");
        int rspCode;
        int all_star;
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                UIHelp.setAlreadySign(context, type, star);
                MessageManager.NotifyReminderToLauncher(context);
                response = OkhttpClientHelper.getOkHttpClient().newCall(createPostHabit(token, sn, Integer.toString(type), star)).execute();
                //判断请求是否成功
                if (response == null) {
                    QAILog.e(TAG, "postHabit response empty");
                } else if (!response.isSuccessful()) {
                    QAILog.e(TAG, "postHabit response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        QAILog.e(TAG, "postHabit rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v(TAG, "postHabit body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                all_star = rspJson.optInt("all_star", -100);
                                QAILog.v(TAG, "postHabit: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    QAILog.d(TAG, "postHabit successful");
                                    if (all_star != -100) {
                                        //          MessageManager.asyncNotifyStarChange(all_star);
                                        //           MessageManager.notifyStarToLauncher(context, all_star);
                                    }
                                    return true;
                                } else {
                                    UIHelp.setAlreadySign(context, type, -2);
                                    QAILog.e(TAG, "postHabit rspCode error srv_code : " + rspCode);
                                }
                            } catch (JSONException e) {
                                QAILog.e(TAG, "postHabit JSONException : " + e.getMessage());
                                e.printStackTrace();
                            }
                        } else {
                            QAILog.e(TAG, "postHabit rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "postHabit error : " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Throwable t) {
            QAILog.e(TAG, "postHabit error : " + t.getMessage());
            t.printStackTrace();
        }
        UIHelp.setAlreadySign(context, type, -2);
        return false;
    }

    private static Request createStarRequest(String token, String sn) {
        QAILog.v(TAG, "createStarRequest");
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(KIDS_INFO_PATH)
                .addQueryParameter("token", token)
                .addQueryParameter("sn", sn)
                .build();

        QAILog.v(TAG, "createHabitRequest:data = " + httpUrl.toString());

        return new Request.Builder()
                .url(httpUrl)
                .get()//requestBody)
                .build();
    }

    public static void fetchStar(final Context context, final String token, final String sn) {

        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                QAILog.d(TAG, "fetchStar: Enter");
                int rspCode;
                okhttp3.Response response;

                try {
                    try {
                        response = OkhttpClientHelper.getOkHttpClient().newCall(createStarRequest(token, sn)).execute();
                        //判断请求是否成功
                        if (response == null) {
                            QAILog.e(TAG, "fetchStar response empty");
                        } else if (!response.isSuccessful()) {
                            QAILog.e(TAG, "fetchStar response failed");
                        } else {
                            if (!HttpHeaders.hasBody(response)) {
                                QAILog.e(TAG, "fetchStar rspBody empty");
                            } else {
                                //打印服务端返回结果
                                ResponseBody rspBody = response.body();
                                long contentLength = rspBody.contentLength();
                                if (contentLength != 0) {
                                    String sBody = rspBody.string();
                                    QAILog.v(TAG, "fetchStar body = : " + sBody);
                                    try {
                                        JSONObject rspJson = new JSONObject(sBody);
                                        rspCode = rspJson.optInt("code", -100);
                                        QAILog.v(TAG, "fetchStar: rspCode: " + rspCode);
                                        QAIConfig.code = rspCode;
                                        if (rspCode == 0) {
                                            List<HabitEntity> kidsList = new ArrayList<>();
                                            JSONArray kidsListJsonArray = rspJson.optJSONArray("kid_list");
                                            int star = rspJson.optInt("all_star");
                                            int credit = rspJson.optInt("all_credit");
                                            HabitKidsEntity entity = new HabitKidsEntity();
                                            if (kidsListJsonArray != null && kidsListJsonArray.length() > 0) {
                                                // 因为目前只会有一个小孩信息，所以此处不用for
                                                JSONObject kidsListJsonObject = kidsListJsonArray.getJSONObject(0);
                                                String name = kidsListJsonObject.optString("nick_name");
                                                star = kidsListJsonObject.optInt("all_star");
                                                boolean gender = kidsListJsonObject.optBoolean("gender");
                                                String grade = kidsListJsonObject.optString("grade");
                                                String birthday = kidsListJsonObject.optString("birthday");
                                                String url = kidsListJsonObject.optString("avatar");
                                                entity.setNick_name(name);
                                                entity.setGrade(grade);
                                                entity.setAll_star(star);
                                                entity.setAll_credit(credit);
                                                if (gender)
                                                    entity.setGender(0);
                                                else
                                                    entity.setGender(1);
                                                entity.setBirthday(birthday);
                                                entity.setHeadimgurl(url);
                                                QAILog.d(TAG, "Kids list is not null");
                                                UIHelp.setChildNameToSettings(context, name);
                                            }
                                            if (star >= 0) {
                                                entity.setAll_star(star);
                                                MessageManager.updateHabitKidsInfo(entity);
                                                MessageManager.asyncNotifyStarChange(star);
                                                MessageManager.notifyStarToLauncher(context, star);
                                                MessageManager.saveKidsInfoToDB(entity);
                                                UIHelp.setChildNameToSettings(context, entity.getNick_name());
                                            }
                                        } else {
                                            QAILog.e(TAG, "kidsListJsonArray rspCode error srv_code : " + rspCode);
                                        }
                                    } catch (JSONException e) {
                                        QAILog.e(TAG, "kidsListJsonArray JSONException : " + e.getMessage());
                                        e.printStackTrace();
                                    }
                                } else {
                                    QAILog.e(TAG, "kidsListJsonArray rspBody empty");
                                }
                            }
                        }
                    } catch (IOException e) {
                        QAILog.e(TAG, "kidsListJsonArray error : " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (Throwable t) {
                    QAILog.e(TAG, "kidsListJsonArray error : " + t.getMessage());
                    t.printStackTrace();
                }
            }
        });
    }

    //get请求(获取任务列表)
    public static void postTask(String token, String sn) {
        int rspCode;
        int allCredit;
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createPostTask(token, sn)).execute();
                //判断请求是否成功
                if (response == null) {
                    MessageManager.addTaskListError(-100);
                    QAILog.e(TAG, "postTask response empty");
                } else if (!response.isSuccessful()) {
                    MessageManager.addTaskListError(-101);
                    QAILog.e(TAG, "postTask response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        MessageManager.addTaskListError(-102);
                        QAILog.e(TAG, "postTask rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v("GiftActivityLog", "postTask body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                allCredit = rspJson.optInt("all_credit");
                                QAILog.v(TAG, "postTask: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    List<TaskEntity> taskList = new ArrayList<>();//总数据
                                    List<TaskEntity> taskList0 = new ArrayList<>();//status=0。待领取状态
                                    List<TaskEntity> taskList2 = new ArrayList<>();//status=2。未完成状态
                                    List<TaskEntity> taskList1 = new ArrayList<>();//status=1。已领取状态
                                    JSONArray tasksArray = rspJson.optJSONArray("tasks");
                                    if (null != tasksArray && tasksArray.length() > 0) {
                                        for (int i = 0; i < tasksArray.length(); i++) {
                                            JSONObject jsonObject = tasksArray.getJSONObject(i);
                                            TaskEntity taskEntity = new TaskEntity();
                                            taskEntity.setId(jsonObject.optInt("id"));
                                            taskEntity.setName(jsonObject.optString("name"));
                                            taskEntity.setAllNum(jsonObject.optInt("all_num"));
                                            taskEntity.setNowNum(jsonObject.optInt("now_num"));
                                            taskEntity.setCredit(jsonObject.optInt("credit"));
                                            int status = jsonObject.optInt("status");
                                            taskEntity.setStatus(status);
                                            taskEntity.setType(jsonObject.optInt("task_type"));
                                            if (0 == status) {//待领取
                                                taskList0.add(taskEntity);
                                            } else if (2 == status) {//未完成
                                                taskList2.add(taskEntity);
                                            } else if (1 == status) {//已完成
                                                taskList1.add(taskEntity);
                                            }
                                        }
                                        taskList.addAll(taskList0);
                                        taskList.addAll(taskList2);
                                        taskList.addAll(taskList1);
                                        MessageManager.addTaskList(taskList, allCredit);
                                    }
                                } else {
                                    MessageManager.addTaskListError(rspCode);
                                }

                            } catch (JSONException e) {
                                MessageManager.addTaskListError(-103);
                                e.printStackTrace();
                            }
                        } else {
                            MessageManager.addTaskListError(-104);
                            QAILog.e(TAG, "postTask rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "postTask error : " + e.getMessage());
                MessageManager.addTaskListError(-105);
                e.printStackTrace();
            }
        } catch (Throwable t) {
            MessageManager.addTaskListError(-106);
            QAILog.e(TAG, "postTask error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createPostTask(String token, String sn) {
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(WX_TASK)
                .addQueryParameter("token", token)
                .addQueryParameter("sn", sn)
                .build();
        Log.i("GiftActivityLog", "url=" + httpUrl.url());
        return new Request.Builder()
                .url(httpUrl)
                .build();
    }

    /**
     * 完成任务，获取学分
     *
     * @param token
     * @param sn
     * @param id     任务编号
     * @param credit 任务学分
     * @param type   任务类型
     * @param action 传”got”
     */
    public static void postReceiveTask(final Context context, String token, String sn, int id, int credit, int type, String action) {

        QAILog.d(TAG, "postTask: Enter");
        int rspCode;
        final int allCredit;//总学分
        final int gotCredit;//获取到的学分，用于弹框
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createPostReceiveTask(token, sn, id, credit, type, action)).execute();
                //判断请求是否成功
                if (response == null) {
                    MessageManager.receiveTaskError(-100);
                    QAILog.e(TAG, "postTask response empty");
                } else if (!response.isSuccessful()) {
                    MessageManager.receiveTaskError(-101);
                    QAILog.e(TAG, "postTask response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        MessageManager.receiveTaskError(-102);
                        QAILog.e(TAG, "postTask rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v("GiftActivityLog", "postReceiveTask body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                allCredit = rspJson.optInt("all_credit");
                                gotCredit = rspJson.optInt("credit_got");
                                if (0 == gotCredit) {
                                    return;
                                }
                                QAILog.v(TAG, "postTask: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    List<TaskEntity> taskList = new ArrayList<>();//总数据
                                    List<TaskEntity> taskList0 = new ArrayList<>();//status=0。待领取状态
                                    List<TaskEntity> taskList2 = new ArrayList<>();//status=2。未完成状态
                                    List<TaskEntity> taskList1 = new ArrayList<>();//status=1。已领取状态
                                    JSONArray tasksArray = rspJson.optJSONArray("tasks");
                                    if (null != tasksArray && tasksArray.length() > 0) {
                                        for (int i = 0; i < tasksArray.length(); i++) {
                                            JSONObject jsonObject = tasksArray.getJSONObject(i);
                                            TaskEntity taskEntity = new TaskEntity();
                                            taskEntity.setId(jsonObject.optInt("id"));
                                            taskEntity.setName(jsonObject.optString("name"));
                                            taskEntity.setAllNum(jsonObject.optInt("all_num"));
                                            taskEntity.setNowNum(jsonObject.optInt("now_num"));
                                            taskEntity.setCredit(jsonObject.optInt("credit"));
                                            int status = jsonObject.optInt("status");
                                            taskEntity.setStatus(status);
                                            taskEntity.setType(jsonObject.optInt("task_type"));
                                            if (0 == status) {//待领取
                                                taskList0.add(taskEntity);
                                            } else if (2 == status) {//未完成
                                                taskList2.add(taskEntity);
                                            } else if (1 == status) {//已完成
                                                taskList1.add(taskEntity);
                                            }
                                        }
                                        taskList.addAll(taskList0);
                                        taskList.addAll(taskList2);
                                        taskList.addAll(taskList1);
                                        MessageManager.receiveTask(taskList, allCredit, action);
                                        if (0 == type) {//打卡
                                            //跳转activity样式dialog
                                            UIHelp.showTaskDialog(context, gotCredit, true);
                                        } else if (1 == type) {//百科
                                            UIHelp.showBaikeDialog(context, gotCredit);
                                        } else if (2 == type) {
                                            UIHelp.showTaskDialog(context, gotCredit, true);
                                        }

                                    }
                                } else {
                                    MessageManager.receiveTaskError(rspCode);
                                }
                            } catch (JSONException e) {
                                MessageManager.receiveTaskError(-103);
                                e.printStackTrace();
                            }
                        } else {
                            MessageManager.receiveTaskError(-104);
                            QAILog.e(TAG, "postTask rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "postTask error : " + e.getMessage());
                MessageManager.receiveTaskError(-105);
                e.printStackTrace();
            }
        } catch (Throwable t) {
            MessageManager.receiveTaskError(-106);
            QAILog.e("GiftActivityLog", "postTask error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createPostReceiveTask(String token, String sn, int id, int credit, int type, String action) {
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(WX_CREDIT)
                .addQueryParameter("token", token)
                .build();

        JSONObject reqJson = new JSONObject();
        try {
            reqJson.put("sn", sn);
            reqJson.put("action", action);
            reqJson.put("id", id);
            reqJson.put("credit", credit);
            reqJson.put("type", type);
        } catch (JSONException e) {
            e.printStackTrace();
            QAILog.e(TAG, "reqJsonError = " + e.getMessage());
        }
        String postInfoStr = reqJson.toString();
        Log.i("GiftActivityLog", "token=" + token + "  sn=" + sn + "  postInfoStr=" + postInfoStr);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postInfoStr);

        return new Request.Builder()
                .url(httpUrl)
                .post(requestBody)
                .build();
    }

    //get请求(获取卡片列表)
    public static void postCard(String token, String sn) {
        int rspCode;
        int allCredit;
        int hasCardNum = 0;//拥有的普通卡片数量;
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createPostCard(token, sn)).execute();
                //判断请求是否成功
                if (response == null) {
                    MessageManager.addCardListError(-100);
                    QAILog.e(TAG, "postCard response empty");
                } else if (!response.isSuccessful()) {
                    MessageManager.addCardListError(-101);
                    QAILog.e(TAG, "postCard response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        MessageManager.addCardListError(-102);
                        QAILog.e(TAG, "postCard rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v("GiftActivityLog", "postCard body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                allCredit = rspJson.optInt("all_credit");
                                QAILog.v(TAG, "postCard: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    List<CardEntity> cardList = new ArrayList<>();//宠物卡
                                    List<CardEntity> propList = new ArrayList<>(3); //道具卡

                                    JSONArray propArray = rspJson.getJSONArray("prop_list");
                                    if (null != propArray && propArray.length() > 0) {
                                        for (int i = 0; i < propArray.length(); i++) {
                                            JSONObject jsonObject = propArray.getJSONObject(i);
                                            CardEntity cardEntity = new CardEntity();
                                            cardEntity.setId(jsonObject.optInt("id"));
                                            cardEntity.setUrl(jsonObject.optString("img_url"));
                                            cardEntity.setName(jsonObject.optString("card_name"));
                                            cardEntity.setPerson(jsonObject.optInt("persion_nums"));
                                            cardEntity.setCardType(jsonObject.optInt("card_type"));
                                            cardEntity.setCardNums(jsonObject.optInt("card_nums"));
                                            cardEntity.setPersonUrl(jsonObject.optString("person_url"));
                                            cardEntity.setCardDesp(jsonObject.optString("card_desp"));
                                            cardEntity.setStatus(jsonObject.optInt("status"));
                                            cardEntity.setShareCredit(jsonObject.optInt("share_credit"));
                                            cardEntity.setGetCredit(jsonObject.optInt("exchg_need_credit"));
                                            cardEntity.setCardNum(jsonObject.optInt("exchg_need_cardnums"));
                                            cardEntity.setUpdateTime(jsonObject.optString("update_time"));
                                            propList.add(cardEntity);
                                        }
                                    }

                                    JSONArray cardArray = rspJson.getJSONArray("card_list");
                                    if (null != cardArray && cardArray.length() > 0) {
                                        for (int i = 0; i < cardArray.length(); i++) {
                                            JSONObject jsonObject = cardArray.getJSONObject(i);
                                            CardEntity cardEntity = new CardEntity();
                                            cardEntity.setId(jsonObject.optInt("id"));
                                            cardEntity.setUrl(jsonObject.optString("img_url"));
                                            cardEntity.setName(jsonObject.optString("card_name"));
                                            cardEntity.setPerson(jsonObject.optInt("persion_nums"));
                                            cardEntity.setCardType(jsonObject.optInt("card_type"));
                                            cardEntity.setCardNums(jsonObject.optInt("card_nums"));
                                            cardEntity.setPersonUrl(jsonObject.optString("person_url"));
                                            cardEntity.setCardDesp(jsonObject.optString("card_desp"));
                                            cardEntity.setStatus(jsonObject.optInt("status"));
                                            cardEntity.setShareCredit(jsonObject.optInt("share_credit"));
                                            cardEntity.setGetCredit(jsonObject.optInt("exchg_need_credit"));
                                            cardEntity.setCardNum(jsonObject.optInt("exchg_need_cardnums"));
                                            cardEntity.setUpdateTime(jsonObject.optString("update_time"));
                                            cardList.add(cardEntity);
                                            if (1 == jsonObject.optInt("status") && 1 == jsonObject.optInt("card_type")) {//已拥有的普通卡片数量
                                                hasCardNum++;
                                            }
                                        }
                                    }
                                    MessageManager.addCardList(cardList, propList, allCredit, hasCardNum);
                                } else {
                                    MessageManager.addCardListError(rspCode);
                                }
                            } catch (JSONException e) {
                                MessageManager.addCardListError(-103);
                                e.printStackTrace();
                            }
                        } else {
                            MessageManager.addCardListError(-104);
                            QAILog.e(TAG, "postCard rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "postCard error : " + e.getMessage());
                MessageManager.addCardListError(-105);
                e.printStackTrace();
            }
        } catch (Throwable t) {
            MessageManager.addCardListError(-106);
            QAILog.e(TAG, "postCard error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createPostCard(String token, String sn) {
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(WX_CARD)
                .addQueryParameter("token", token)
                .addQueryParameter("sn", sn)
                .build();
        Log.i("GiftActivityLog", "url=" + httpUrl.url());
        return new Request.Builder()
                .url(httpUrl)
                .build();
    }

    /**
     * @param token
     * @param sn
     * @param id    卡片编号
     */
    public static void postReceiveCard(String token, String sn, int id, int type) {

        QAILog.d(TAG, "postCard: Enter");
        int rspCode;
        int allCredit;
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createPostReceiveCard(token, sn, id, type)).execute();
                //判断请求是否成功
                if (response == null) {
                    MessageManager.receiveCardError(-100);
                    QAILog.e(TAG, "postCard response empty");
                } else if (!response.isSuccessful()) {
                    MessageManager.receiveCardError(-101);
                    QAILog.e(TAG, "postCard response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        MessageManager.receiveCardError(-102);
                        QAILog.e(TAG, "postCard rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v("GiftActivityLog", "postCard body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                allCredit = rspJson.optInt("all_credit");
                                QAILog.v(TAG, "postCard: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    MessageManager.receiveCard(allCredit, id, type);
                                } else {
                                    MessageManager.receiveCardError(rspCode);
                                }
                            } catch (JSONException e) {
                                MessageManager.receiveCardError(-103);
                                e.printStackTrace();
                            }
                        } else {
                            MessageManager.receiveCardError(-104);
                            QAILog.e(TAG, "postCard rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "postCard error : " + e.getMessage());
                MessageManager.receiveCardError(-105);
                e.printStackTrace();
            }
        } catch (Throwable t) {
            MessageManager.receiveCardError(-106);
            QAILog.e(TAG, "postCard error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createPostReceiveCard(String token, String sn, int id, int type) {
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(WX_GET_CARD)
                .addQueryParameter("token", token)
                .addQueryParameter("sn", sn)
                .addQueryParameter("card_id", id + "")
                .addQueryParameter("card_type", type + "")
                .build();
        Log.i("GiftActivityLog", "url=" + httpUrl.url());
        return new Request.Builder()
                .url(httpUrl)
                .build();
    }

    /**
     * 卡片分享接口
     *
     * @param token
     * @param sn
     * @param id    卡片编号
     */
    public static void postShareCard(String token, String sn, int id, int type) {
        int rspCode;
        int addCredit;
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createShareReceiveCard(token, sn, id)).execute();
                //判断请求是否成功
                if (response == null) {
                    MessageManager.shareCardError(-100);
                    QAILog.e(TAG, "shareCard response empty");
                } else if (!response.isSuccessful()) {
                    MessageManager.shareCardError(-101);
                    QAILog.e(TAG, "shareCard response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        MessageManager.shareCardError(-102);
                        QAILog.e(TAG, "shareCard rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v("GiftActivityLog", "shareCard body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                addCredit = rspJson.optInt("add_credit");
                                QAILog.v(TAG, "shareCard: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    MessageManager.shareCard(addCredit, id, type);
                                } else {
                                    MessageManager.shareCardError(rspCode);
                                }
                            } catch (JSONException e) {
                                MessageManager.shareCardError(-103);
                                e.printStackTrace();
                            }
                        } else {
                            MessageManager.shareCardError(-104);
                            QAILog.e(TAG, "shareCard rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "shareCard error : " + e.getMessage());
                MessageManager.shareCardError(-105);
                e.printStackTrace();
            }
        } catch (Throwable t) {
            MessageManager.shareCardError(-106);
            QAILog.e(TAG, "shareCard error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createShareReceiveCard(String token, String sn, int id) {
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(WX_SHARE_CARD)
                .addQueryParameter("token", token)
                .addQueryParameter("sn", sn)
                .addQueryParameter("card_id", id + "")
                .build();
        Log.i("GiftActivityLog", "url=" + httpUrl.url());
        return new Request.Builder()
                .url(httpUrl)
                .build();
    }

    //get请求(获取PK用户，宠物信息s)
    public static void postPKInfo(String token, String sn) {
        int rspCode;
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createPostPKInfo(token, sn)).execute();
                //判断请求是否成功
                if (response == null) {
                    MessageManager.addPKInfoListError(-100);
                    QAILog.e(TAG, "PKInfo response empty");
                } else if (!response.isSuccessful()) {
                    MessageManager.addPKInfoListError(-101);
                    QAILog.e(TAG, "PKInfo response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        MessageManager.addPKInfoListError(-102);
                        QAILog.e(TAG, "PKInfo rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v("PKActivityLog", "PKInfo body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                QAILog.v(TAG, "PKInfo: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    PKUserInfo userInfo = new PKUserInfo();
                                    userInfo.setAllCredit(rspJson.optInt("all_credit"));
                                    userInfo.setAllTimes(rspJson.optInt("total_times"));
                                    userInfo.setNowTimes(rspJson.optInt("times"));
                                    userInfo.setAvatar(rspJson.optString("avatar"));
                                    userInfo.setNickName(rspJson.optString("nickname"));
                                    userInfo.setPetId(rspJson.optInt("pet_id"));
                                    userInfo.setPetUrl(rspJson.optString("pet_url"));
                                    JSONArray jsonArray = rspJson.optJSONArray("pets");
                                    List<PKPetInfo> list = new ArrayList<>();
                                    if (jsonArray != null && jsonArray.length() > 0) {
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                                            PKPetInfo info = new PKPetInfo();
                                            info.setCaption(jsonObject.optString("caption"));
                                            info.setId(jsonObject.optInt("id"));
                                            info.setImgUrl(jsonObject.optString("img_url"));
                                            if (0 == jsonObject.optInt("if_own")) {//0未拥有，1已拥有
                                                info.setIfOwn(false);
                                            } else if (1 == jsonObject.optInt("if_own")) {
                                                info.setIfOwn(true);
                                            }
                                            info.setName(jsonObject.optString("name"));
                                            list.add(info);
                                        }
                                    }
                                    MessageManager.addPKInfoList(list, userInfo);
                                } else {
                                    MessageManager.addPKInfoListError(rspCode);
                                }
                            } catch (JSONException e) {
                                MessageManager.addPKInfoListError(-103);
                                e.printStackTrace();
                            }
                        } else {
                            MessageManager.addPKInfoListError(-104);
                            QAILog.e(TAG, "PKInfo rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "PKInfo error : " + e.getMessage());
                MessageManager.addPKInfoListError(-105);
                e.printStackTrace();
            }
        } catch (Throwable t) {
            MessageManager.addPKInfoListError(-106);
            QAILog.e(TAG, "PKInfo error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createPostPKInfo(String token, String sn) {
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(WX_PK_INFO)
                .addQueryParameter("token", token)
                .addQueryParameter("sn", sn)
                .build();
        Log.i("PKActivityLog", "url=" + httpUrl.url());
        return new Request.Builder()
                .url(httpUrl)
                .build();
    }

    /**
     * 选择宠物
     *
     * @param token
     * @param sn
     * @param id    宠物id
     */
    public static void postChoosePet(String token, String sn, int id) {
        int rspCode;
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createChoosePet(token, sn, id)).execute();
                //判断请求是否成功
                if (response == null) {
                    MessageManager.addPKChoosePetError(-100);
                    QAILog.e(TAG, "PKChoosePet response empty");
                } else if (!response.isSuccessful()) {
                    MessageManager.addPKChoosePetError(-101);
                    QAILog.e(TAG, "PKChoosePet response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        MessageManager.addPKChoosePetError(-102);
                        QAILog.e(TAG, "PKChoosePet rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v("PKActivityLog", "PKChoosePet body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                QAILog.v(TAG, "PKChoosePet: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    MessageManager.addPKChoosePet(id);
                                } else {
                                    MessageManager.addPKChoosePetError(rspCode);
                                }
                            } catch (JSONException e) {
                                MessageManager.addPKChoosePetError(-103);
                                e.printStackTrace();
                            }
                        } else {
                            MessageManager.addPKChoosePetError(-104);
                            QAILog.e(TAG, "PKChoosePet rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "PKChoosePet error : " + e.getMessage());
                MessageManager.addPKChoosePetError(-105);
                e.printStackTrace();
            }
        } catch (Throwable t) {
            MessageManager.addPKChoosePetError(-106);
            QAILog.e(TAG, "PKChoosePet error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createChoosePet(String token, String sn, int id) {
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(WX_PK_CHOOSE)
                .addQueryParameter("token", token)
                .addQueryParameter("sn", sn)
                .addQueryParameter("pet_id", id + "")
                .build();
        Log.i("PKActivityLog", "url=" + httpUrl.url());
        return new Request.Builder()
                .url(httpUrl)
                .build();
    }

    //get请求(获取PK题目信息)
    public static void postPKQuestion(String token, String sn) {
        int rspCode;
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createPostPKQuestion(token, sn)).execute();
                //判断请求是否成功
                if (response == null) {
                    MessageManager.addPKQuestionError(-100);
                    QAILog.e(TAG, "PKQuestion response empty");
                } else if (!response.isSuccessful()) {
                    MessageManager.addPKQuestionError(-101);
                    QAILog.e(TAG, "PKQuestion response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        MessageManager.addPKQuestionError(-102);
                        QAILog.e(TAG, "PKQuestion rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v("PKActivityLog", "PKQuestion body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                QAILog.v(TAG, "PKQuestion: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    List<PKQuestionInfo> questionInfo = new ArrayList<>(10);
                                    List<PKPropInfo> propInfo = new ArrayList<>(3);
                                    //先解析道具卡
                                    JSONArray prpoArray = rspJson.optJSONArray("propList");
                                    if (null != prpoArray && prpoArray.length() > 0) {
                                        for (int i = 0; i < prpoArray.length(); i++) {
                                            JSONObject jsonObject = prpoArray.getJSONObject(i);
                                            PKPropInfo info = new PKPropInfo();
                                            info.setAction(jsonObject.optString("action"));
                                            info.setName(jsonObject.optString("name"));
                                            info.setId(jsonObject.optInt("id"));
                                            info.setNum(jsonObject.optInt("num"));
                                            info.setUrl(jsonObject.optString("icon_url"));
                                            info.setCaption(jsonObject.optString("caption"));
                                            propInfo.add(info);
                                        }
                                    }

                                    //解析题目
                                    JSONArray questionArray = rspJson.optJSONArray("questionList");
                                    if (null != questionArray && questionArray.length() > 0) {
                                        for (int i = 0; i < questionArray.length(); i++) {
                                            JSONObject jsonObject = questionArray.optJSONObject(i);
                                            PKQuestionInfo info = new PKQuestionInfo();
                                            info.setId(jsonObject.optInt("id"));
                                            info.setLastNum(jsonObject.optInt("last_num"));
                                            info.setTopic(jsonObject.optString("topic"));
                                            info.setTopicUrl(jsonObject.optString("topic_url"));
                                            info.setVoice(jsonObject.optString("voice"));
                                            //解析答案
                                            List<PKQuestionInfo.Sections> sections = new ArrayList<>(3);
                                            JSONArray sectionsArray = jsonObject.optJSONArray("sections");
                                            for (int j = 0; j < sectionsArray.length(); j++) {
                                                JSONObject sectionObject = sectionsArray.optJSONObject(j);
                                                PKQuestionInfo.Sections section = new PKQuestionInfo.Sections();
                                                section.setUrl(sectionObject.optString("url"));
                                                section.setName(sectionObject.optString("name"));
                                                section.setAnswer(sectionObject.optBoolean("is_answer"));
                                                sections.add(section);
                                            }
                                            info.setSections(sections);

                                            questionInfo.add(info);
                                        }
                                    }

                                    MessageManager.addPKQuestion(questionInfo, propInfo);
                                } else {
                                    MessageManager.addPKInfoListError(rspCode);
                                }
                            } catch (JSONException e) {
                                MessageManager.addPKQuestionError(-103);
                                e.printStackTrace();
                            }
                        } else {
                            MessageManager.addPKQuestionError(-104);
                            QAILog.e(TAG, "PKQuestion rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "PKQuestion error : " + e.getMessage());
                MessageManager.addPKQuestionError(-105);
                e.printStackTrace();
            }
        } catch (Throwable t) {
            MessageManager.addPKQuestionError(-106);
            QAILog.e(TAG, "PKQuestion error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createPostPKQuestion(String token, String sn) {
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(WX_PK_QUESTION)
                .addQueryParameter("token", token)
                .addQueryParameter("sn", sn)
                .build();
        Log.i("PKActivityLog", "url=" + httpUrl.url());
        return new Request.Builder()
                .url(httpUrl)
                .build();
    }

    /**
     * 获取PK奖励
     *
     * @param token
     * @param sn
     * @param num_topic  答对题目数量
     * @param num_people 当前剩余人数
     */

    public static void postPKRewrd(String token, String sn, int num_topic, int num_people) {
        int rspCode;
        int ranking;//排名
        int credit;//送的学分
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createReward(token, sn, num_topic, num_people)).execute();
                //判断请求是否成功
                if (response == null) {
                    MessageManager.addPKRewardError(-100);
                    QAILog.e(TAG, "PKReward response empty");
                } else if (!response.isSuccessful()) {
                    MessageManager.addPKRewardError(-101);
                    QAILog.e(TAG, "PKReward response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        MessageManager.addPKRewardError(-102);
                        QAILog.e(TAG, "PKReward rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v("PKActivityLog", "PKReward body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                ranking = rspJson.optInt("ranking");
                                credit = rspJson.optInt("credit");
                                QAILog.v(TAG, "PKReward: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    //解析道具卡
                                    List<PKPropInfo> propInfoList = new ArrayList<>();
                                    JSONArray propArray = rspJson.optJSONArray("pros");
                                    if (null != propArray && propArray.length() > 0) {
                                        for (int i = 0; i < propArray.length(); i++) {
                                            PKPropInfo propInfo = new PKPropInfo();
                                            JSONObject jsonObject = propArray.optJSONObject(i);
                                            propInfo.setUrl(jsonObject.optString("icon_url"));
                                            propInfo.setId(jsonObject.optInt("id"));
                                            propInfo.setCaption(jsonObject.optString("caption"));
                                            propInfo.setNum(jsonObject.optInt("num"));
                                            propInfo.setAction(jsonObject.optString("action"));
                                            propInfo.setName(jsonObject.optString("name"));
                                            propInfoList.add(propInfo);
                                        }
                                    }

                                    //解析宠物卡
                                    List<CardEntity> cardEntityList = new ArrayList<>();
                                    JSONArray cardArray = rspJson.optJSONArray("card");
                                    if (null != cardArray && cardArray.length() > 0) {
                                        for (int i = 0; i < cardArray.length(); i++) {
                                            CardEntity card = new CardEntity();
                                            JSONObject jsonObject = cardArray.optJSONObject(i);
                                            card.setCardDesp(jsonObject.optString("card_desp"));
                                            card.setName(jsonObject.optString("card_name"));
                                            card.setId(jsonObject.optInt("id"));
                                            card.setUrl(jsonObject.optString("img_url"));
                                            cardEntityList.add(card);
                                        }
                                    }

                                    MessageManager.addPKReward(credit, propInfoList, cardEntityList, ranking);
                                } else {
                                    MessageManager.addPKRewardError(rspCode);
                                }
                            } catch (JSONException e) {
                                MessageManager.addPKRewardError(-103);
                                e.printStackTrace();
                            }
                        } else {
                            MessageManager.addPKRewardError(-104);
                            QAILog.e(TAG, "PKReward rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "PKReward error : " + e.getMessage());
                MessageManager.addPKRewardError(-105);
                e.printStackTrace();
            }
        } catch (Throwable t) {
            MessageManager.addPKRewardError(-106);
            QAILog.e(TAG, "PKReward error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createReward(String token, String sn, int num_topic, int num_people) {
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(WX_PK_REWARD)
                .addQueryParameter("token", token)
                .addQueryParameter("sn", sn)
                .addQueryParameter("num_topic", num_topic + "")
                .addQueryParameter("num_people", num_people + "")
                .build();
        Log.i("PKActivityLog", "url=" + httpUrl.url());
        return new Request.Builder()
                .url(httpUrl)
                .build();
    }

    /**
     * 使用道具成功
     */
    public static void postPKPropUse(String token, String sn, PKPropInfo pkPropInfo) {
        int rspCode;
        okhttp3.Response response;

        try {
            //发送请求获取响应
            try {
                response = OkhttpClientHelper.getOkHttpClient().newCall(createPropUse(token, sn, pkPropInfo.getId())).execute();
                //判断请求是否成功
                if (response == null) {
                    MessageManager.usePropError(-100);
                    QAILog.e(TAG, "PKPropUse response empty");
                } else if (!response.isSuccessful()) {
                    MessageManager.usePropError(-101);
                    QAILog.e(TAG, "PKPropUse response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        MessageManager.usePropError(-102);
                        QAILog.e(TAG, "PKPropUse rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v("PKActivityLog", "PKPropUse body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -100);
                                QAILog.v(TAG, "PKPropUse: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    MessageManager.usePropSuccess(pkPropInfo);
                                } else {
                                    MessageManager.usePropError(rspCode);
                                }
                            } catch (JSONException e) {
                                MessageManager.usePropError(-103);
                                e.printStackTrace();
                            }
                        } else {
                            MessageManager.usePropError(-104);
                            QAILog.e(TAG, "PKPropUse rspBody empty");
                        }
                    }
                }
            } catch (IOException e) {
                QAILog.e(TAG, "PKPropUse error : " + e.getMessage());
                MessageManager.usePropError(-105);
                e.printStackTrace();
            }
        } catch (Throwable t) {
            MessageManager.usePropError(-106);
            QAILog.e(TAG, "PKPropUse error : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request createPropUse(String token, String sn, int id) {
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(WX_PK_PROP_USE)
                .addQueryParameter("token", token)
                .addQueryParameter("sn", sn)
                .addQueryParameter("prop_id", id + "")
                .build();
        Log.i("PKActivityLog", "url=" + httpUrl.url());
        return new Request.Builder()
                .url(httpUrl)
                .build();
    }

    public static void getVoiceBooks(String token, String sn) {
        int rspCode;
        okhttp3.Response response;

        try {

            try {

                response = OkhttpClientHelper.getOkHttpClient().newCall(creatGetVoiceBooks(token, sn)).execute();

                //判断请求是否成功
                if (response == null) {
                    MessageManager.getVoiceBookListFail(-1000, "");
                    QAILog.e("getVoiceBooks", "getVoiceBooks response empty");
                } else if (!response.isSuccessful()) {
                    MessageManager.getVoiceBookListFail(-1000, "");
                    QAILog.e("getVoiceBooks", "getVoiceBooks response failed" + response);
                } else {
                    if (!HttpHeaders.hasBody(response)) {
                        MessageManager.getVoiceBookListFail(-1000, "");
                        QAILog.e("getVoiceBooks", "getVoiceBooks rspBody empty");
                    } else {
                        //打印服务端返回结果
                        ResponseBody rspBody = response.body();
                        long contentLength = rspBody.contentLength();
                        if (contentLength != 0) {
                            String sBody = rspBody.string();
                            QAILog.v("getVoiceBooks", "getVoiceBooks body = : " + sBody);
                            try {
                                JSONObject rspJson = new JSONObject(sBody);
                                rspCode = rspJson.optInt("code", -1000);
                                QAILog.v("getVoiceBooks", "getVoiceBooks: rspCode: " + rspCode);
                                QAIConfig.code = rspCode;
                                if (rspCode == 0) {
                                    List<VoiceBookBean> list = new ArrayList<>();
                                    JSONArray array = rspJson.optJSONArray("book_list");

                                    if (null != array && array.length() > 0) {
                                        int size = array.length();
                                        for (int i = 0; i < size; i++) {

                                            String parentName = array.optJSONObject(i).optString("type_name");

                                            JSONArray arrayList = array.optJSONObject(i).optJSONArray("list");

                                            for (int j = 0; j < arrayList.length(); j++) {

                                                VoiceBookBean bean = new VoiceBookBean();

                                                bean.setParentName(parentName);

                                                JSONObject jsonObject = arrayList.optJSONObject(j);

                                                QAILog.d("getVoiceBooks", "jsonObject -> " + jsonObject.toString());

                                                bean.setBookName(jsonObject.optString("name"));
                                                bean.setIconUrl(jsonObject.optString("img_url"));
                                                bean.setCommand(jsonObject.optString("tts_text"));
                                                QAILog.d("getVoiceBooks", "bean -> " + bean.toString());
                                                list.add(bean);
                                            }
                                        }
                                    }

                                    MessageManager.getVoiceBookListSuccess(list);
                                } else {
                                    MessageManager.getVoiceBookListFail(rspCode, rspJson.optString("msg"));
                                }
                            } catch (JSONException e) {
                                MessageManager.getVoiceBookListFail(-1000, "");
                                e.printStackTrace();
                            }
                        } else {
                            MessageManager.getVoiceBookListFail(-1000, "");
                            QAILog.e("getVoiceBooks", "PKPropUse rspBody empty");
                        }
                    }
                }

            } catch (Exception e) {
                QAILog.e("getVoiceBooks", "getVoiceBooks Exception : " + e.getMessage());
                MessageManager.getVoiceBookListFail(-1000, "");
                e.printStackTrace();
            }


        } catch (Throwable t) {
            MessageManager.getVoiceBookListFail(-1000, "");
            QAILog.e("getVoiceBooks", "getVoiceBooks Throwable : " + t.getMessage());
            t.printStackTrace();
        }
    }

    private static Request creatGetVoiceBooks(String token, String sn) {
        HttpUrl httpUrl = new Request.Builder()
                .url(TEST_URL)
                .build()
                .url()
                .newBuilder()
                .addEncodedPathSegment(WX_VOICE_BOOK_LIST_PATH)
                .addQueryParameter("token", token)
                .addQueryParameter("sn", sn)
                .build();
        Log.i(TAG, "creatGetVoiceBooks url=" + httpUrl.url());
        return new Request.Builder()
                .url(httpUrl)
                .build();
    }

}
