package com.kinstalk.her.qchatmodel.Manager.model;

import com.kinstalk.her.qchatmodel.entity.CardEntity;
import com.kinstalk.her.qchatmodel.entity.PKPropInfo;

import java.util.List;

/**
 * Created by bean on 2018/7/18.
 */

public interface cardCallback {
    //获取卡片成功
    void onCardChanged(List<CardEntity> receiveList, List<CardEntity> propList, int credit, int hasCardNum);

    //获取卡片失败
    void onCardGetError(int code);

    //领取卡片成功
    void onCardReceive(int credit, int id, int type);

    //领取卡片失败
    void onCardReceiveError(int code);

    //分享卡片成功
    void onShareReceive(int credit, int id, int type);

    //分享卡片失败
    void onShareReceiveError(int code);
}
