/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchatcomm.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Knight.Xu on 2018/4/6.
 */

public class JsonUtils {

    /**
     * 格式化json数据
     */
    public static String formatJson(String jsonMsg) {

        String message;
        try {
            if (jsonMsg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(jsonMsg);
                message = jsonObject.toString(4);//最重要的方法，就一行，返回格式化的json字符串，其中的数字4是缩进字符数
            } else if (jsonMsg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(jsonMsg);
                message = jsonArray.toString(4);
            } else {
                message = jsonMsg;
            }
        } catch (JSONException e) {
            message = jsonMsg;
        }
        return message;
    }
}
