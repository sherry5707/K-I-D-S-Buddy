/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchat.message.holders;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchatmodel.entity.QchatMessage;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesListStyle;

/*
 * Created by Knight.Xu on 2018/4/7.
 */
public class QIncomingImageMessageViewHolder
        extends MessageHolders.IncomingImageMessageViewHolder<QchatMessage> {

    private ImageView onlineIndicator;

    public QIncomingImageMessageViewHolder(View itemView) {
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

        /*boolean isOnline = message.getUser().isOnline();
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online);
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline);
        }*/

//        onlineIndicator.setVisibility(View.GONE);
    }
}