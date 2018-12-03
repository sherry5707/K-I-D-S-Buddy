/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchat.message.holders;

import android.view.View;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchatmodel.entity.QchatMessage;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesListStyle;

import io.github.rockerhieu.emojicon.EmojiconTextView;

/**
 * Created by Knight.Xu on 2018/4/7.
 */
public class QIncomingEmojiMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<QchatMessage> {

    private EmojiconTextView tvEmoji;

    public QIncomingEmojiMessageViewHolder(View itemView) {
        super(itemView);
        tvEmoji = (EmojiconTextView) itemView.findViewById(R.id.messageEmoji);
    }

    @Override
    public void onBind(QchatMessage message, MessagesListStyle messagesListStyle) {
        super.onBind(message, messagesListStyle);
        tvEmoji.setText(message.getEmoji().getTag());
    }
}
