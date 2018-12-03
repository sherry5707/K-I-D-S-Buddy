/*
 * Copyright (c) 2017. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchatapi.exception;

import android.util.AndroidException;

/**
 * Created by Knight.Xu on 2018/4/6.
 */

public class ApiException extends AndroidException {
    public int code;
    public String message;

    public ApiException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ApiException{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
