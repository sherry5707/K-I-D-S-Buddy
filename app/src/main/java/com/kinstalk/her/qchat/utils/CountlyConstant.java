package com.kinstalk.her.qchat.utils;

/**
 * 埋点
 */
public class CountlyConstant {
    public static final String SKILL_TYPE = "kidsBuddy";
    /**
     * 聊天
     */
    //点击或长按语音按钮
    public static final String t_chatroom_chatbutton = "t_chatroom_chatbutton";
    //退出聊天页面
    public static final String t_chatroom_turnoff = "t_chatroom_turnoff";
    //(播放语音)
    public static final String t_chatroom_audio_play = "t_chatroom_audio_play";
    //发送语音成功
    public static final String t_chatroom_audio_deliveryed = "t_chatroom_audio_deliveryed";
    //语音消息发送失败
    public static final String t_chatroom_audio_undeliveryed = "t_chatroom_audio_undeliveryed";
    /**
     * 作业
     */
    //点击'完成作业，获得星星'按钮。（可以打卡）
    public static final String t_homework_clockin = "t_homework_clockin";
    //打卡时间已过'
    public static final String t_homework_late = "t_homework_late";
    //点击语音播报按钮
    public static final String t_homework_audio = "t_homework_audio";
    //退出作业面板
    public static final String t_homework_turnoff = "t_homework_turnoff";
    //打开作业详情
    public static final String t_homework_detail = "t_homework_detail";
    //打开作业中的图片(全屏模式)
    public static final String t_homework_detail_pic = "t_homework_detail_pic";
    //'勾选单项作业
    public static final String t_homework_done = "t_homework_done";
    //取消'勾选单项作业
    public static final String t_homework_todo = "t_homework_todo";

    /**
     * 日程表
     */
    //退出日程表
    public static final String t_calendar_turnoff = "t_calendar_turnoff";
    //点击习惯，进入提前打卡页
    public static final String t_calendar_clockin = "t_calendar_clockin";
    //退出提前打卡页
    public static final String t_calendar_clockin_turnoff = "t_calendar_clockin_turnoff";
    //语音消息接收成功
    public static final String t_voice_message_success = "t_chatroom_audio_received";
    //语音消息接收失败
    public static final String getT_voice_message_fail ="t_chatroom_audio_unreceived";

}
