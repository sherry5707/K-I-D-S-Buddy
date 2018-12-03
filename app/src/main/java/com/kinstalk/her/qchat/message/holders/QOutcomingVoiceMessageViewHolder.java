/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */
package com.kinstalk.her.qchat.message.holders;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.image.GlideImageLoader;
import com.kinstalk.her.qchat.utils.CountlyConstant;
import com.kinstalk.her.qchat.voice.AudioFocusController;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.entity.HabitKidsEntity;
import com.kinstalk.her.qchatmodel.entity.QchatMessage;
import com.kinstalk.her.qchat.voice.QVoicePlayer;
import com.kinstalk.her.qchatcomm.utils.DateUtils;
import com.kinstalk.her.qchatcomm.utils.QAILog;
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
 * 发送的语音消息选项holder
 */
public class QOutcomingVoiceMessageViewHolder
        extends MessageHolders.OutcomingTextMessageViewHolder<QchatMessage> {

    private static final String TAG = QOutcomingVoiceMessageViewHolder.class.getSimpleName();

    private TextView tvDuration;
    private TextView tvTime;
    private ImageButton ibPlay;
    private ImageView avatarView;
    private int clickPostion = -1;
    private int position;

    public QOutcomingVoiceMessageViewHolder(View itemView) {
        super(itemView);
        tvDuration = (TextView) itemView.findViewById(R.id.duration);
        tvTime = (TextView) itemView.findViewById(R.id.time);
        ibPlay = (ImageButton) itemView.findViewById(R.id.voice_play);
        avatarView = (ImageView) itemView.findViewById(R.id.avatar);
    }

    @Override
    public void onBind(final QchatMessage message, MessagesListStyle messagesListStyle) {
        super.onBind(message, messagesListStyle);
        position = (int) itemView.getTag();
        Log.d("OOOO","位置："+position+"点击位置"+getClicPosition());
        clickPostion = getClicPosition();
        String duration = DateUtils.getSecondsString2(message.getVoice().getDuration());
        QAILog.d(TAG, "duration = " + duration);
        tvDuration.setText(duration + '"');
        tvTime.setText(DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME));
//        GlideImageLoader glideImageLoader = new GlideImageLoader();
//        glideImageLoader.loadImage(avatarView, message.getUser().getAvatar());
        HabitKidsEntity kidsEntity = MessageManager.getHabitKidsInfo();



      /*  if (kidsEntity != null) {
            int gender = kidsEntity.getGender();
            if (gender == 0) {
                resId = R.mipmap.boy;
            } else if (gender == 1) {
                resId = R.mipmap.girl;
            }
        }*/
        int resId = R.mipmap.deaufl;
        if (kidsEntity != null) {
            if (!TextUtils.isEmpty(kidsEntity.getHeadimgurl() )) {
                Glide.with(QchatApplication.getInstance())
                        .load(kidsEntity.getHeadimgurl())
                        .skipMemoryCache(false)
                        .into(avatarView);

            } else {

                int gender = kidsEntity.getGender();
                if (gender == 0) {
                    resId = R.mipmap.boy;
                } else if (gender == 1) {
                    resId = R.mipmap.girl;
                }
                Glide.with(QchatApplication.getInstance())
                        .load(resId)
                        //.load(message.getUser().getHeadimgurl())
                        .skipMemoryCache(false)
                        .into(avatarView);
            }
        }else {
            Glide.with(QchatApplication.getInstance())
                    .load(resId)
                    //.load(message.getUser().getHeadimgurl())
                    .crossFade()
                    .skipMemoryCache(false)
                    .into(avatarView);
        }


        ibPlay.setId(position);
        AnimationDrawable animationDrawable;
        if (clickPostion == position) {
            ibPlay.setImageResource(R.drawable.voice_play_animation);
            animationDrawable = (AnimationDrawable) ibPlay.getDrawable();
            animationDrawable.start(); //开始的时候调用
            setClickButton(ibPlay);
            Log.d("OOOOO","保存:"+ ibPlay.toString());
        } else {
            ibPlay.setImageResource(R.drawable.voice_play_anim3);
        }

        bubble.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                boolean isClickSamePosition = false;
                if (position == getClicPosition()){
                    isClickSamePosition =true;
                }
                Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE,CountlyConstant.t_chatroom_audio_play);
                Log.d("OOOOO", "点:" + ibPlay.toString());
                EventBus.getDefault().post(new EventIsBack(false));
                VoiceBean voiceBean = message.getVoice();
                String filePath = voiceBean.getFilePath();
                if (new File(filePath).exists()) {
                    ibPlay.setImageResource(R.drawable.voice_play_animation);
                    final AnimationDrawable animationDrawable = (AnimationDrawable) ibPlay.getDrawable();
                    animationDrawable.start(); //开始的时候调用
                    QVoicePlayer.playSound(filePath, new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            EventBus.getDefault().post(new EventIsBack(true));
                            Log.d("OOOOO","完成1："+ ibPlay.toString());
                            if (getClicPosition() != -1 && getClicButton() != null && !ibPlay.toString().equals(getClicButton().toString())) {
                                ImageButton mibPlays = getClicButton();
                                mibPlays.setImageResource(R.drawable.voice_play_anim3);
                                ibPlay = mibPlays;
                            } else {
                                animationDrawable.stop(); //动画结束调用
                                ibPlay.setImageResource(R.drawable.voice_play_anim3);
                            }
                            Log.d("OOOOO","完成2："+ ibPlay.toString());
                            setClickButton(null);
                            setClickPosition(-1);
                            QAILog.d(TAG, "onCompletion");
                            AudioFocusController.init().abandonFocus();
                            QVoicePlayer.setPause(true);
                            //         QVoicePlayer.release();

                        }
                    });
                    if (!isClickSamePosition)
                        setClickPosition(position);
                }

            }
        });
    }
}
