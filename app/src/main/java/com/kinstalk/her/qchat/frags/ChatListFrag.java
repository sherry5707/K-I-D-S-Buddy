/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchat.frags;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.fragment.NewMessageCallback;
import com.kinstalk.her.qchat.image.GlideImageLoader;
import com.kinstalk.her.qchat.message.fixture.QchatMessagesFixtures;
import com.kinstalk.her.qchat.message.holders.QIncomingEmojiMessageViewHolder;
import com.kinstalk.her.qchat.message.holders.QIncomingImageMessageViewHolder;
import com.kinstalk.her.qchat.message.holders.QIncomingTextMessageViewHolder;
import com.kinstalk.her.qchat.message.holders.QIncomingVoiceMessageViewHolder;
import com.kinstalk.her.qchat.message.holders.QOutcomingEmojiMessageViewHolder;
import com.kinstalk.her.qchat.message.holders.QOutcomingImageMessageViewHolder;
import com.kinstalk.her.qchat.message.holders.QOutcomingTextMessageViewHolder;
import com.kinstalk.her.qchat.message.holders.QOutcomingVoiceMessageViewHolder;
import com.kinstalk.her.qchat.voice.QVoicePlayer;
import com.kinstalk.her.qchat.voice.RingPlayer;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.entity.QchatMessage;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.ChatProviderHelper;
import com.kinstalk.her.qchatmodel.Manager.model.messageCallback;

import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by Knight.Xu on 2018/4/7.
 * 展示聊天列表的fragment
 */
public class ChatListFrag extends QBaseFragment implements
        MessageHolders.ContentChecker<QchatMessage>,
        MessagesListAdapter.OnLoadMoreListener,
        messageCallback {

    private static final String TAG = ChatListFrag.class.getSimpleName();

    private ImageLoader imageLoader;
    private MessagesList messagesList;
    private MessagesListAdapter<QchatMessage> messagesAdapter;
    private Date lastLoadedDate;
    private NewMessageCallback newMessageCallback;
    private ArrayList<QchatMessage> messages;
    private List<QchatMessage> chatMessageList;
    private String ACTION_AICORE_WINDOW_SHOWN = "kinstalk.com.aicore.action.window_shown";
    private String EXTRA_AICORE_WINDOW_SHOWN = "isShown";
    private BroadcastReceiver mAiUIRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_AICORE_WINDOW_SHOWN.equals(action)) {
                boolean isShow = intent.getBooleanExtra(EXTRA_AICORE_WINDOW_SHOWN, false);
                QAILog.d(TAG, "AI UI wake up isShow " + isShow);
                if (isShow) {
                    QVoicePlayer.stop();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        QAILog.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        imageLoader = new GlideImageLoader();
        registerAIReceiver();
    }

    private final String senderId = "0";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        QAILog.d(TAG, "onCreateView");
        View layout = inflater.inflate(R.layout.frag_chat_list, container, false);

        messagesList = (MessagesList) layout.findViewById(R.id.qchat_messagesList);
        initAdapter();

//        getActivity().getContentResolver().registerContentObserver(ChatProviderHelper.Chat.CONTENT_URI, true, mContentObserver);
        MessageManager.registerMessageCallback(this);
        findAllChats();
        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageManager.unRegisterMessageCallback(this);
        unRegisterAIReceiver();
//        getContext().getContentResolver().unregisterContentObserver(mContentObserver);
        super.onDestroy();
    }

    @Override
    public boolean hasContentFor(QchatMessage message, byte type) {
        switch (type) {
            case CONTENT_TYPE_VOICE:
                return message.hasVoiceContent();
            case CONTENT_TYPE_EMOJI:
                return message.getEmoji() != null
                        && !TextUtils.isEmpty(message.getEmoji().getTag());
        }
        return false;
    }


    @Override
    public void onLoadMore(int page, int totalItemsCount) {
        if (totalItemsCount < TOTAL_MESSAGES_COUNT) {
//            loadMessages();
        }
    }

    private void initAdapter() {
        MessageHolders holders = new MessageHolders()
                .registerContentType(
                        CONTENT_TYPE_VOICE,
                        QIncomingVoiceMessageViewHolder.class,
                        R.layout.q_item_incoming_voice_message,
                        QOutcomingVoiceMessageViewHolder.class,
                        R.layout.q_item_outcoming_voice_message,
                        this);

        holders.registerContentType(
                CONTENT_TYPE_EMOJI,
                QIncomingEmojiMessageViewHolder.class,
                R.layout.q_item_incoming_emoji_message,
                QOutcomingEmojiMessageViewHolder.class,
                R.layout.q_item_outcoming_emoji_message,
                this);

        holders.setIncomingTextConfig(
                QIncomingTextMessageViewHolder.class,
                R.layout.q_item_incoming_text_message)
                .setOutcomingTextConfig(
                        QOutcomingTextMessageViewHolder.class,
                        R.layout.q_item_outcoming_text_message)
                .setIncomingImageConfig(
                        QIncomingImageMessageViewHolder.class,
                        R.layout.q_item_incoming_image_message)
                .setOutcomingImageConfig(
                        QOutcomingImageMessageViewHolder.class,
                        R.layout.q_item_outcoming_image_message);

        messagesAdapter = new MessagesListAdapter<>(senderId, holders, imageLoader);
//        super.messagesAdapter.enableSelectionMode(this);
        messagesAdapter.setLoadMoreListener(this);
        messagesList.setAdapter(messagesAdapter);
    }


    protected void loadMessages() {
        new Handler().postDelayed(new Runnable() { //imitation of internet connection
            @Override
            public void run() {
                messages = QchatMessagesFixtures.getMessages(lastLoadedDate);
                lastLoadedDate = messages.get(messages.size() - 1).getCreatedAt();
                messagesAdapter.addToEnd(messages, false);
            }
        }, 1000);
    }

    @Override
    public void onMessageChanged(Boolean status) {
        Log.d(TAG, "onMessageChanged: ");
        findAllChats();

        if (status && null != chatMessageList && chatMessageList.size() > 0) {
            if (!QchatApplication.isWakeupActive() || !QchatApplication.isSleepingActive()) {
                //非起床睡觉播放故事
                RingPlayer.getInstance().startRing();
            }
            if (null != chatMessageList.get(chatMessageList.size() - 1).getUser()) {
                newMessageCallback.receiverNewMessage(ChatListFrag.class.getSimpleName(), chatMessageList.get(chatMessageList.size() - 1).getUser().getNickname());
            } else {
                newMessageCallback.receiverNewMessage(ChatListFrag.class.getSimpleName(), "");
            }
        }
    }

    private void findAllChats() {

        chatMessageList = MessageManager.getAllChatMessage();
        getActivity().runOnUiThread(new Runnable() { //imitation of internet connection
            @Override
            public void run() {
                messagesAdapter.clear();
                messagesAdapter.addToEnd(chatMessageList, true);
                messagesAdapter.notifyDataSetChanged();

                messagesList.smoothScrollToPosition(0);
            }
        });
    }

    public void setNewMessageCallback(NewMessageCallback newMessageCallback) {
        this.newMessageCallback = newMessageCallback;
    }

    public void unSetNewMessageCallback() {
        this.newMessageCallback = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(getUserVisibleHint()) {
            // 可视
        } else {
            // 不可视
            QVoicePlayer.stop();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        QVoicePlayer.stop();
        super.onPause();
    }

    private void registerAIReceiver() {
        IntentFilter intentFilter = new IntentFilter(ACTION_AICORE_WINDOW_SHOWN);
        this.getContext().registerReceiver(mAiUIRecevier, intentFilter);
    }

    private void unRegisterAIReceiver() {
        this.getContext().unregisterReceiver(mAiUIRecevier);
    }

}
