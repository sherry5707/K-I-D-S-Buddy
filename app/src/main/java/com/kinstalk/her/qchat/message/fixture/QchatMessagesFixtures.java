/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchat.message.fixture;

import com.kinstalk.her.qchatmodel.entity.WxUserEntity;
import com.kinstalk.her.qchatmodel.beans.EmojiBean;
import com.kinstalk.her.qchatmodel.beans.ImageBean;
import com.kinstalk.her.qchatmodel.entity.QchatMessage;
import com.kinstalk.her.qchatmodel.beans.VoiceBean;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/*
 * Created by Knight.Xu on 2018/4/7.
 */
public final class QchatMessagesFixtures extends QchatFixturesData {
    private QchatMessagesFixtures() {
        throw new AssertionError();
    }


    public static MessagesListAdapter.Formatter<QchatMessage> getMessageStringFormatter() {
        return new MessagesListAdapter.Formatter<QchatMessage>() {
            @Override
            public String format(QchatMessage message) {
                String createdAt = new SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                        .format(message.getCreatedAt());

                String text = message.getText();
                if (text == null) text = "[attachment]";

                return String.format(Locale.getDefault(), "%s: %s (%s)",
                        message.getUser().getName(), text, createdAt);
            }
        };
    }

    public static QchatMessage getImageMessage() {
        QchatMessage QchatMessage = new QchatMessage(getRandomId(), getUser(), null);
        //TODO
        QchatMessage.setImage(new ImageBean(getRandomImage(), ""));
        return QchatMessage;
    }

    public static QchatMessage getVoiceMessage() {
        QchatMessage QchatMessage = new QchatMessage(getRandomId(), getUser(), null);
        QchatMessage.setVoice(new VoiceBean("http://example.com", rnd.nextInt(200) + 30));
        return QchatMessage;
    }

    public static QchatMessage getEmojiMessage() {
        QchatMessage QchatMessage = new QchatMessage(getRandomId(), getUser(), null);
        QchatMessage.setEmoji(new EmojiBean("\uD83D\uDE03"));
        return QchatMessage;
    }

    public static QchatMessage getTextMessage() {
        return getTextMessage(getRandomMessage());
    }

    public static QchatMessage getTextMessage(String text) {
        return new QchatMessage(getRandomId(), getUser(), text);
    }

    public static ArrayList<QchatMessage> getMessages(Date startDate) {
        ArrayList<QchatMessage> QchatMessages = new ArrayList<>();
        for (int i = 0; i < 10/*days count*/; i++) {
            int countPerDay = rnd.nextInt(5) + 1;

            for (int j = 0; j < countPerDay; j++) {
                QchatMessage QchatMessage;
                if (i % 2 == 0 && j % 3 == 0) {
                    QchatMessage = getImageMessage();
                } else {
                    QchatMessage = getTextMessage();
                }

                Calendar calendar = Calendar.getInstance();
                if (startDate != null) calendar.setTime(startDate);
                calendar.add(Calendar.DAY_OF_MONTH, -(i * i + 1));

                QchatMessage.setCreatedAt(calendar.getTime());
                QchatMessages.add(QchatMessage);
            }
        }
        return QchatMessages;
    }

    public static WxUserEntity getUser() {
        boolean even = rnd.nextBoolean();
        return new WxUserEntity(
                even ? "0" : "1",
                even ? names.get(0) : names.get(1),
                even ? avatars.get(0) : avatars.get(1),
                true);
    }
}
