package com.kinstalk.her.qchatmodel.Manager.model;

import com.kinstalk.her.qchatmodel.entity.CardEntity;
import com.kinstalk.her.qchatmodel.entity.PKPetInfo;
import com.kinstalk.her.qchatmodel.entity.PKPropInfo;
import com.kinstalk.her.qchatmodel.entity.PKQuestionInfo;
import com.kinstalk.her.qchatmodel.entity.PKUserInfo;

import java.util.List;

/**
 * Created by bean on 2018/8/21.
 */

public interface pkInfoCallback {
    //获取PK详情成功
    void onPKInfoChanged(List<PKPetInfo> petList, PKUserInfo user);

    //获取PK详情失败
    void onPKInfoGetError(int code);

    //选择宠物成功
    void onPKChoosePet(int id);

    //选择宠物失败
    void onPKChoosePetError(int code);

    //获取PK题目成功
    void onPKQuestionChanged(List<PKQuestionInfo> questionList, List<PKPropInfo> propList);

    //获取PK题目失败
    void onPKQuestionGetError(int code);

    //获取PK结果成功
    void onPKRewardChanged(int credit, List<PKPropInfo> propInfos, List<CardEntity> cardEntities, int ranking);

    //获取PK结果失败
    void onPKRewardGetError(int code);

    //使用PK道具成功
    void onPKPropUse(PKPropInfo pkPropInfo);

    //使用PK道具失败
    void onPKPropUseError(int code);
}
