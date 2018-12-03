/**
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchatmodel.datadiff;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.kinstalk.her.qchatmodel.entity.QchatMessage;
import com.kinstalk.her.qchatmodel.ChatProviderHelper;

import java.util.List;

/**
 * Created by knight.xu on 2018/4/14.
 * 用来判断新旧Item是否相同
 * https://blog.csdn.net/zxt0601/article/details/52562770
 * //TODO DiffUtil的高级用法
 */
public class ChatDiffCallBack extends DiffUtil.Callback {
    private List<QchatMessage> mOldDatas, mNewDatas;//看名字

    public ChatDiffCallBack(List<QchatMessage> mOldDatas, List<QchatMessage> mNewDatas) {
        this.mOldDatas = mOldDatas;
        this.mNewDatas = mNewDatas;
    }

    // 老数据集size
    @Override
    public int getOldListSize() {
        return mOldDatas != null ? mOldDatas.size() : 0;
    }

    // 新数据集size
    @Override
    public int getNewListSize() {
        return mNewDatas != null ? mNewDatas.size() : 0;
    }

    /**
     * Called by the DiffUtil to decide whether two object represent the same Item.
     * For example, if your items have unique ids, this method should check their id equality.
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list
     * @return True if the two items represent the same object or false if they are different.
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return TextUtils.equals(mOldDatas.get(oldItemPosition).getId(), mNewDatas.get(newItemPosition).getId());
    }

    /**
     * Called by the DiffUtil when it wants to check whether two items have the same data.
     * DiffUtil uses this information to detect if the contents of an item has changed.
     * DiffUtil uses this method to check equality instead of {@link Object#equals(Object)}
     * so that you can change its behavior depending on your UI.
     * For example, if you are using DiffUtil with a
     * {@link android.support.v7.widget.RecyclerView.Adapter RecyclerView.Adapter}, you should
     * return whether the items' visual representations are the same.
     * This method is called only if {@link #areItemsTheSame(int, int)} returns
     * {@code true} for these items.
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list which replaces the
     *                        oldItem
     * @return True if the contents of the items are the same or false if they are different.
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        QchatMessage beanOld = mOldDatas.get(oldItemPosition);
        QchatMessage beanNew = mNewDatas.get(newItemPosition);
        int msgType = beanOld.getMessageType();
        if (msgType == ChatProviderHelper.Chat.MSG_TYPE_VOICE) {
            return beanOld.getVoice().equals(beanNew.getVoice());
        } else if (msgType == ChatProviderHelper.Chat.MSG_TYPE_EMOJI) {
            return false;
        } else if (msgType == ChatProviderHelper.Chat.MSG_TYPE_PIC) {
            return beanOld.getImage().equals(beanNew.getImage());
        } else if (msgType == ChatProviderHelper.Chat.MSG_TYPE_TEXT) {
            return TextUtils.equals(beanOld.getText(), beanNew.getText());
        }

        return true; //默认两个data内容是相同的
    }

    /**
     * When {@link #areItemsTheSame(int, int)} returns {@code true} for two items and
     * {@link #areContentsTheSame(int, int)} returns false for them, DiffUtil
     * calls this method to get a payload about the change.
     *
     * 当{@link #areItemsTheSame(int, int)} 返回true，且{@link #areContentsTheSame(int, int)} 返回false时，DiffUtils会回调此方法，
     * 去得到这个Item（有哪些）改变的payload。
     *
     * For example, if you are using DiffUtil with {@link RecyclerView}, you can return the
     * particular field that changed in the item and your
     * {@link android.support.v7.widget.RecyclerView.ItemAnimator ItemAnimator} can use that
     * information to run the correct animation.
     *
     * 例如，如果你用RecyclerView配合DiffUtils，你可以返回  这个Item改变的那些字段，
     * {@link android.support.v7.widget.RecyclerView.ItemAnimator ItemAnimator} 可以用那些信息去执行正确的动画
     *
     * Default implementation returns {@code null}.\
     * 默认的实现是返回null
     *
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list
     * @return A payload object that represents the change between the two items.
     * 返回 一个 代表着新老item的改变内容的 payload对象，
     */
     /*@Nullable
    @Override
   public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // 定向刷新中的部分更新
        // 效率最高
        // 只是没有了ItemChange的白光一闪动画，（反正我也觉得不太重要）
        QchatMessage oldBean = mOldDatas.get(oldItemPosition);
        QchatMessage newBean = mNewDatas.get(newItemPosition);

        // 这里就不用比较核心字段了,一定相等
        Bundle payload = new Bundle();
        if (!oldBean.getDesc().equals(newBean.getDesc())) {
            payload.putString("KEY_DESC", newBean.getDesc());
        }
        if (oldBean.getPic() != newBean.getPic()) {
            payload.putInt("KEY_PIC", newBean.getPic());
        }

        if (payload.size() == 0)//如果没有变化 就传空
            return null;
        return payload;//
    }*/
}
