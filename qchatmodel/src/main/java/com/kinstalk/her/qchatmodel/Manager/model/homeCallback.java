package com.kinstalk.her.qchatmodel.Manager.model;

import com.kinstalk.her.qchatmodel.entity.QHomeworkMessage;

import java.util.List;

/**
 * Created by Tracy on 2018/4/19.
 */

public interface homeCallback {
    void onHomeworkChanged(Boolean status, List<QHomeworkMessage> msg);
}
