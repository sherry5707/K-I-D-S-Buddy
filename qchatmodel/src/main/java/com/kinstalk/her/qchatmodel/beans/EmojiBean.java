/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchatmodel.beans;

import android.text.TextUtils;

/**
 * Created by Knight.Xu on 2018/4/8.
 */

public class EmojiBean {

    private String tag;

    public EmojiBean(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        EmojiBean bean = (EmojiBean) obj;
        return TextUtils.equals(tag, bean.tag);
    }
}
