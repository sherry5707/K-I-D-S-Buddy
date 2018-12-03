/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchat.image;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchatcomm.base.BaseApplication;
import com.stfalcon.chatkit.commons.ImageLoader;

/**
 * Created by Knight.Xu on 2018/4/11.
 * Glide 图片加载器
 */

public class GlideImageLoader implements ImageLoader{
    @Override
    public void loadImage(ImageView imageView, String url) {
        Glide.with(BaseApplication.getInstance().getApplicationContext()).load(url).into(imageView);
    }

    public void loadLocalImage(ImageView imageView, String filePath) {
        Glide.with(BaseApplication.getInstance().getApplicationContext()).load(filePath).into(imageView);
    }
}
