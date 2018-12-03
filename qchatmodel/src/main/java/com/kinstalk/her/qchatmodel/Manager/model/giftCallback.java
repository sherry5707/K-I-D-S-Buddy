package com.kinstalk.her.qchatmodel.Manager.model;

import com.kinstalk.her.qchatmodel.entity.GiftEntity;

import java.util.List;

/**
 * Created by bean on 2018/5/22.
 */

public interface giftCallback {
    /**
     * 礼物列表请求成功
     *
     * @param receiveList
     * @param receivedList
     * @param star
     */
    void onGiftChanged(List<GiftEntity> receiveList, List<GiftEntity> receivedList, int star);

    /**
     * 礼物列表请求失败
     *
     * @param code
     */

    void onGiftGetError(int code);

    /**
     * 礼物d各种点击事件成功(直接返回最新数据，刷新列表)
     *
     * @param action "got"领取中，点击红星的操作    "finished"点击已领取礼物   "cancel"取消领取礼物s
     */
    void onGiftReceive(List<GiftEntity> receiveList, List<GiftEntity> receivedList, int star, String action);

    /**
     * 礼物领取失败
     *
     * @param code
     */
    void onGiftReceiveError(int code);

}
