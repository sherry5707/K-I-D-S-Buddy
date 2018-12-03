/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchat.message.holders;

import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.utils.MessageUtils;
import com.kinstalk.her.qchatmodel.entity.QchatMessage;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesListStyle;

/*
 * Created by Knight.Xu on 2018/4/7.
 */
public class QIncomingTextMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<QchatMessage> {

    private ImageView onlineIndicator;

    public QIncomingTextMessageViewHolder(View itemView) {
        super(itemView);
        onlineIndicator = (ImageView) itemView.findViewById(R.id.avatar);
    }

    @Override
    public void onBind(QchatMessage message, MessagesListStyle messagesListStyle) {
        super.onBind(message, messagesListStyle);

        if (null != message && null != message.getUser() && null != message.getUser().getHeadimgurl()) {
            Glide.with(QchatApplication.getInstance())
                    .load(message.getUser().getHeadimgurl())
                    .crossFade()
                    .skipMemoryCache(false)
                    .into(onlineIndicator);
        }

        bMultiLine = MessageUtils.bMultiLineMsg(message.getText());
        ViewCompat.setBackground(bubble, bMultiLine ? messagesListStyle.getIncomingBubbleMultiLineDrawable() : messagesListStyle.getIncomingBubbleSingleLineDrawable());

        /*boolean isOnline = message.isOnline();
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online);
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline);
        }*/
//        onlineIndicator.setVisibility(View.GONE);
    }
}
