/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchatmodel.evtbus;

import com.kinstalk.her.qchatmodel.entity.HomeWorkEntity;

/**
 * Created by Knight.Xu on 2018/4/13.
 */

public class HomeworkMsgEvt  extends MessageEvent{

    private HomeWorkEntity homeWorkEntity;

    public HomeWorkEntity getHomeWorkEntity() {
        return homeWorkEntity;
    }

    public void setHomeWorkEntity(HomeWorkEntity homeWorkEntity) {
        this.homeWorkEntity = homeWorkEntity;
    }
}
