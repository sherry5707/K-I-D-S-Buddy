/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchatmodel.beans;

import android.text.TextUtils;

/**
 * Created by Knight.Xu on 2018/4/9.
 */

public class ImageBean {
    private String url;
    private String localPath;

    public ImageBean(String url, String localPath) {
        this.url = url;
        this.localPath = localPath;
    }

    public String getUrl() {
        return url;
    }

    public String getLocalPath() {
        return localPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageBean)) return false;
        ImageBean imageBean = (ImageBean) o;
        return TextUtils.equals(url, imageBean.url) &&
                TextUtils.equals(localPath, imageBean.localPath);
    }
}
