package com.kinstalk.her.qchatmodel.Manager.model;

import com.kinstalk.her.qchatmodel.entity.TaskEntity;

import java.util.List;

/**
 * Created by bean on 2018/7/5.
 */

public interface taskCallback {
    //获取任务成功
    void onTaskChanged(List<TaskEntity> receiveList, int credit);

    //获取任务失败
    void onTaskGetError(int code);

    //领取任务成功
    void onTaskReceive(List<TaskEntity> receiveList, int credit, String action);

    //领取任务失败
    void onTaskReceiveError(int code);
}
