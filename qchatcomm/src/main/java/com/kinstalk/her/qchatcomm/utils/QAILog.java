/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchatcomm.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by Knight.Xu on 2018/4/6.
 */
public class QAILog {

    //当前Debug模式
    public static boolean DE_BUG = true;

    public static void e(String tag, String text) {
        if (DE_BUG) {
            Log.e(tag, text);
        }
    }

    public static void d(String tag, String text) {
        if (DE_BUG) {
            Log.d(tag, text);
        }
    }

    public static void i(String tag, String text) {
        if (DE_BUG) {
            Log.i(tag, text);
        }
    }

    public static void w(String tag, String text) {
        if (DE_BUG) {
            Log.w(tag, text);
        }
    }

    public static void v(String tag, String text) {
        if (DE_BUG) {
            Log.v(tag, text);
        }
    }

    /**
     * TODO 此调用接口不要提交, AI Service 内部查看kpi专用
     * @param tag
     * @param text
     */
    public static void kpi(String tag, String text) {
        //Log.d(tag, "QAI-KPI---" + text);
    }

    private static final String LOG_FORMAT = "%1$s\n%2$s";

    public static void v(String tag, Object... args) {
        log(Log.VERBOSE, null, tag, args);
    }

    public static void d(String tag, Object... args) {
        log(Log.DEBUG, null, tag, args);
    }

    public static void i(String tag, Object... args) {
        log(Log.INFO, null, tag, args);
    }

    public static void w(String tag, Object... args) {
        log(Log.WARN, null, tag, args);
    }

    public static void e(Throwable ex) {
        log(Log.ERROR, ex, null);
    }

    public static void e(String tag, Object... args) {
        log(Log.ERROR, null, tag, args);
    }

    public static void e(Throwable ex, String tag, Object... args) {
        log(Log.ERROR, ex, tag, args);
    }

    private static void log(int priority, Throwable ex, String tag, Object... args) {

        if (QAILog.DE_BUG == false) return;

        String log = "";
        if (ex == null) {
            if (args != null && args.length > 0) {
                for (Object obj : args) {
                    log += String.valueOf(obj);
                }
            }
        } else {
            String logMessage = ex.getMessage();
            String logBody = Log.getStackTraceString(ex);
            log = String.format(LOG_FORMAT, logMessage, logBody);
        }
        Log.println(priority, tag, log);
    }

    public static boolean isDebug() {
        return DE_BUG;
    }

    public static void setDebug(boolean isDebug) {
        QAILog.DE_BUG = isDebug;
    }

    private static String getPrefixFromObject(Object obj) {
        return obj == null ? "<null>" : obj.getClass().getSimpleName();
    }

    public static class QAIHttpLogger implements HttpLoggingInterceptor.Logger {
        private static final String PREFIX = "QOK-";
        private String mTag;

        public QAIHttpLogger(Object object) {
            this(getPrefixFromObject(object));
        }

        public QAIHttpLogger(String tag) {
            mTag = PREFIX + tag;
        }

        @Override
        public void log(String message) {
            QAILog.d(mTag, message);
        }
    }

    /**
     * TODO 调用此接口不要提交
     * @param tag
     * @param msg
     * @param headString
     */
    public static void printFormatJson(String tag, String msg, String headString) {
        String message = JsonUtils.formatJson(msg);

        final String LINE_SEPARATOR = System.getProperty("line.separator");
        message = headString + LINE_SEPARATOR + message;
        String[] lines = message.split(LINE_SEPARATOR);
        for (String line : lines) {
            v(tag, line);
        }
    }

}
