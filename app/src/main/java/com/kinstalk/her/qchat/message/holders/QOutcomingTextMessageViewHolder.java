/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchat.message.holders;

import android.view.View;

import com.kinstalk.her.qchatmodel.entity.QchatMessage;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesListStyle;

/*
 * Created by Knight.Xu on 2018/4/7.
 */
public class QOutcomingTextMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<QchatMessage> {

    public QOutcomingTextMessageViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBind(QchatMessage message, MessagesListStyle messagesListStyle) {
        super.onBind(message, messagesListStyle);

        time.setText(message.getStatus() + " " + time.getText());
    }
}
