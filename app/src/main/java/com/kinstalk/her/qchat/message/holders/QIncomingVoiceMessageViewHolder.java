/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchat.message.holders;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.utils.CountlyConstant;
import com.kinstalk.her.qchat.voice.AudioFocusController;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.entity.QchatMessage;
import com.kinstalk.her.qchat.voice.QVoicePlayer;
import com.kinstalk.her.qchatcomm.utils.DateUtils;
import com.kinstalk.her.qchatmodel.beans.VoiceBean;
import com.kinstalk.her.qchatmodel.evtbus.EventIsBack;
import com.stfalcon.chatkit.messages.MessageHolders;
import com.stfalcon.chatkit.messages.MessagesListStyle;
import com.stfalcon.chatkit.utils.DateFormatter;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import ly.count.android.sdk.Countly;

/**
 * Created by Knight.Xu on 2018/4/7.
 * 收到的语音消息选项holder
 */
public class QIncomingVoiceMessageViewHolder
        extends MessageHolders.IncomingTextMessageViewHolder<QchatMessage> {

    private static final String TAG = "QInComingVoiceMessageHolder";
    private TextView tvDuration;
    private TextView tvTime;
    private ImageButton ibPlay;
    private ImageView avatarView;
    private int clickPostion = -1;
    private ImageButton clickButton = null;

    public QIncomingVoiceMessageViewHolder(View itemView) {
        super(itemView);
        tvDuration = (TextView) itemView.findViewById(R.id.duration);
        tvTime = (TextView) itemView.findViewById(R.id.time);
        ibPlay = (ImageButton) itemView.findViewById(R.id.voice_play);
        avatarView = (ImageView) itemView.findViewById(R.id.avatar);
    }

    @Override
    public void onBind(final QchatMessage message, MessagesListStyle messagesListStyle) {
        super.onBind(message, messagesListStyle);
        final int position = (int) itemView.getTag();
        clickPostion = getClicPosition();
        Log.d("CCCCCCC", position + ":" + clickPostion + ibPlay.toString());
        String duration = DateUtils.getSecondsString2(message.getVoice().getDuration());
        tvDuration.setText(duration + '"');
        tvTime.setText(DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME));

        if (null != message && null != message.getUser() && null != message.getUser().getHeadimgurl()) {
            Glide.with(QchatApplication.getInstance())
                    .load(message.getUser().getHeadimgurl())
                    .thumbnail(0.5f)
                    .skipMemoryCache(false)
                    .into(avatarView);
        }

//        //TODO 根据消息状态是否要显示未读，message.status =
//        ibPlay.setImageResource(R.drawable.voice_play_anim3);
        AnimationDrawable animationDrawable;
        if (position == clickPostion) {
            ibPlay.setImageResource(R.drawable.voice_play_in_animation);
            animationDrawable = (AnimationDrawable) ibPlay.getDrawable();
            animationDrawable.start(); //开始的时候调用
            setClickButton(ibPlay);

        } else {
            ibPlay.setImageResource(R.drawable.voice_play_in_anim3);
        }
        ibPlay.setId(position);

        bubble.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                boolean isClicSamePosition = false;
                if (getClicPosition() == position) {
                    isClicSamePosition = true;
                }
                Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE,CountlyConstant.t_chatroom_audio_play);
                EventBus.getDefault().post(new EventIsBack(false));
                VoiceBean voiceBean = message.getVoice();
                String filePath = voiceBean.getFilePath();
                if (new File(filePath).exists()) {
                    ibPlay.setImageResource(R.drawable.voice_play_in_animation);
                    final AnimationDrawable animationDrawable = (AnimationDrawable) ibPlay.getDrawable();
                    animationDrawable.start(); //开始的时候调用
                    QVoicePlayer.playSound(filePath, new MediaPlayer.OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            EventBus.getDefault().post(new EventIsBack(true));
                            if (getClicPosition() != -1 && getClicButton() != null && !ibPlay.toString().equals(getClicButton().toString())) {
                                ImageButton mibPlays = getClicButton();
                                mibPlays.setImageResource(R.drawable.voice_play_in_anim3);
                            } else {
                                animationDrawable.stop(); //动画结束调用
                                ibPlay.setImageResource(R.drawable.voice_play_in_anim3);
                            }
                            setClickButton(null);
                            setClickPosition(-1);
                            //TODO 刷新消息状态
                            AudioFocusController.init().abandonFocus();
                            QAILog.d(TAG, "onCompletion");
                            QVoicePlayer.setPause(true);
                            //           QVoicePlayer.release();
                        }
                    });
                    //记录点击位置，如果文件损坏，并不会记录位置，所以移到此。
                    if (!isClicSamePosition)
                        setClickPosition(position);
                }

            }
        });
    }
}
