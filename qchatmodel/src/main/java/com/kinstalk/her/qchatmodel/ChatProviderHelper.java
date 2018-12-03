package com.kinstalk.her.qchatmodel;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.kinstalk.her.qchatcomm.base.BaseApplication;
import com.kinstalk.her.qchatcomm.utils.DateUtils;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.beans.EmojiBean;
import com.kinstalk.her.qchatmodel.beans.ImageBean;
import com.kinstalk.her.qchatmodel.beans.VoiceBean;
import com.kinstalk.her.qchatmodel.entity.ChatEntity;
import com.kinstalk.her.qchatmodel.entity.HabitEntity;
import com.kinstalk.her.qchatmodel.entity.HabitKidsEntity;
import com.kinstalk.her.qchatmodel.entity.HomeWorkEntity;
import com.kinstalk.her.qchatmodel.entity.QchatMessage;
import com.kinstalk.her.qchatmodel.entity.RemindEntity;
import com.kinstalk.her.qchatmodel.entity.WxUserEntity;
import com.kinstalk.her.qchatmodel.evtbus.TextMsgEvt;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 * Created by wangzhipeng on 2018/4/9.
 */

public class ChatProviderHelper {
    private static final String TAG = "ChatProviderHelper";
    // 1- out; 2-in
    public static final int DIRECTION_IN = 2;
    public static final int DIRECTION_OUT = 1;
    public static final int TYPE_HOMEWORK = 5;
    public static final int TYPE_GETUP = 3;
    public static final int TYPE_SLEEP = 4;

    public static final Uri CONTENT_URI = Uri.parse("content://" + ChatProvider.AUTHORITY);
    public static ConcurrentHashMap<String, WxUserEntity> mWxUserMap = new ConcurrentHashMap<>();
    public static HabitKidsEntity kidsEntity = null;
    public static HabitEntity[] mHabitEntities = new HabitEntity[3];

    public static HabitEntity getWorkHabit() {
        return mHabitEntities[0];
    }

    public static HabitEntity getSleepHabit() {
        return mHabitEntities[1];
    }

    public static HabitEntity getGetupHabit() {
        return mHabitEntities[2];
    }

    public static void updateHabit(HabitEntity entity, int type) {
        if (type == TYPE_HOMEWORK) {
            mHabitEntities[0] = entity;
        } else if (type == TYPE_SLEEP) {
            mHabitEntities[1] = entity;
        } else if (type == TYPE_GETUP) {
            mHabitEntities[2] = entity;
        }
    }

    public static void deleteHabit(HabitEntity entity, int type) {

    }


    /*****************  Chat  ******************/
    public static class Chat implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(ChatProviderHelper.CONTENT_URI,
                "/chat");

        public static final String CONTENT_TYPE = "com.kinstalk.cursor.dir/qchat";

        public static final String COLUMN_SEQ_NO = "seq_no";
        public static final String COLUMN_MSG_ID = "msg_id";
        public static final String COLUMN_MESSAGE = "message";

        public static final String COLUMN_DIRECTION = "direction";
        public static final String COLUMN_MESSAGE_TIME = "message_time";
        // 1-text; 2-voice; 3-emoji; 4-pic
        public static final int MSG_TYPE_TEXT = 1;
        public static final int MSG_TYPE_VOICE = 2;
        public static final int MSG_TYPE_EMOJI = 3;
        public static final int MSG_TYPE_PIC = 4;
        public static final String COLUMN_MESSAGE_TYPE = "message_type";
        public static final String COLUMN_AUDIO_LOCAL_PATH = "audio_local_path";
        public static final String COLUMN_AUDIO_DOWNLOAD_PATH = "audio_download_path";
        public static final String COLUMN_AUDIO_MD5 = "audio_md5";
        public static final String COLUMN_PIC_LOCAL_PATH = "pic_local_path";
        public static final String COLUMN_PIC_URL = "pic_url";
        /*        0-unread; 1-read;
                2-download sucess; 3-download fail;
                4-upload success; 5-upload fail;
                6-send success; 7-send fail;*/
        public static final int STATUS_UNREAD = 0;
        public static final int STATUS_READ = 1;
        public static final int STATUS_DOWNLOAD_SUCCESS = 2;
        public static final int STATUS_DOWNLOAD_FAIL = 3;
        public static final int STATUS_UPLOAD_SUCCESS = 4;
        public static final int STATUS_UPLOAD_FAIL = 5;
        public static final int STATUS_SEND_SUCESS = 6;
        public static final int STATUS_SEND_FAIL = 7;
        public static final int MAX_COLUMN = 1000;
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_DURATION = "duration";

        public static final String DEFAULT_SORT_ORDER = COLUMN_MESSAGE_TIME + " ASC";

        // Define assist directions: 1, SEND; 2, RECIVE
        public static final int DIRECTION_SEND = 1;
        public static final int DIRECTION_RECIVE = 2;

        // 1-image; 2-touch
        public static final int TYPE_IMAGE = 1;
        public static final int TYPE_TOUCH = 2;
        public static final int ITEM_TYPE_NORMAL = 0;
        public static final int ITEM_TYPE_TOUCH = 1;

        // 1-in-progress 2-success 3-failed
        public static final int STATUS_IN_PROGRESS = 1;
        public static final int STATUS_SUCCESS = 2;
        public static final int STATUS_FAILED = 3;

        // 1-received; 0-not received; -1 -timeout
        public static final String RECEIPT_RECEIVED = "1";
        public static final String RECEIPT_NOT_RECEIVED = "0";
        public static final String RECEIPT_TIMEOUT = "-1";

        public static List<QchatMessage> getChatMessages(String selection,
                                                         String[] selectionArgs) {
            QAILog.d(TAG, "getChatMessages");
            final ContentResolver resolver = BaseApplication.mApplication.getContentResolver();
            List<QchatMessage> records = new ArrayList<>();
            Cursor cursor = null;
            int num = 0;
            try {
                cursor = resolver.query(Chat.CONTENT_URI, null, selection, selectionArgs,
                        ChatProviderHelper.Chat.DEFAULT_SORT_ORDER);
                num = cursor.getCount();
                QAILog.d(TAG, "NUM is " + num);
                if (num < MAX_COLUMN) {
                    cursor.moveToFirst();
                } else {
                    cursor.moveToPosition(num - MAX_COLUMN);
                }
                for (; !cursor.isAfterLast(); cursor.moveToNext()) {
                    ChatEntity communication = new ChatEntity();

                    String seq_no = cursor.getString(cursor.getColumnIndex(Chat.COLUMN_SEQ_NO));
                    String openid = cursor.getString(cursor.getColumnIndex(ChatDatabaseHelper.COMMON_COLUMN_OPEN_ID));
                    String message = cursor.getString(cursor.getColumnIndex(Chat.COLUMN_MESSAGE));
                    int direction = cursor.getInt(cursor.getColumnIndex(Chat.COLUMN_DIRECTION));
                    long message_time = cursor.getLong(cursor.getColumnIndex(Chat.COLUMN_MESSAGE_TIME));
                    int message_type = cursor.getInt(cursor.getColumnIndex(Chat.COLUMN_MESSAGE_TYPE));
                    String audio_local_path = cursor.getString(cursor.getColumnIndex(Chat.COLUMN_AUDIO_LOCAL_PATH));
                    String audio_download_path = cursor.getString(cursor.getColumnIndex(Chat.COLUMN_AUDIO_DOWNLOAD_PATH));
                    String audio_md5 = cursor.getString(cursor.getColumnIndex(Chat.COLUMN_AUDIO_MD5));
                    String pic_local_path = cursor.getString(cursor.getColumnIndex(Chat.COLUMN_PIC_LOCAL_PATH));
                    String pic_url = cursor.getString(cursor.getColumnIndex(Chat.COLUMN_PIC_URL));
                    int status = cursor.getInt(cursor.getColumnIndex(Chat.COLUMN_STATUS));
                    int duration = cursor.getInt(cursor.getColumnIndex(Chat.COLUMN_DURATION));

                    QAILog.d(TAG, "value is" + communication.toString());
                    QchatMessage chatMessage = null;
                    WxUserEntity chatUser = null;
                    if (mWxUserMap.containsKey(openid)) {
                        Log.d(TAG, "mWxUserMap containsKey");
                        chatUser = mWxUserMap.get(openid);
                    }
                    String msgID = String.valueOf(seq_no);
                    if (message_type == ChatProviderHelper.Chat.MSG_TYPE_VOICE) {
                        //TODO
                        if (!TextUtils.isEmpty(audio_local_path)/* || !TextUtils.isEmpty(chatEntity.audioDownloadPath)*/) {
                            chatMessage = new QchatMessage(msgID, chatUser, null);
                            VoiceBean voiceBean = new VoiceBean(audio_download_path, audio_local_path, duration);
                            chatMessage.setVoice(voiceBean);
                        }
                    } else if (message_type == ChatProviderHelper.Chat.MSG_TYPE_EMOJI) {
                        if (!TextUtils.isEmpty(message)) {
                            chatMessage = new QchatMessage(msgID, chatUser, message);
                            //          chatMessage.setEmoji(new EmojiBean(new String(message.getBytes())));
                        }
                    } else if (message_type == ChatProviderHelper.Chat.MSG_TYPE_PIC) {

                        if (!TextUtils.isEmpty(pic_local_path)) {
                            chatMessage = new QchatMessage(msgID, chatUser, null);
                            ImageBean imgBean = new ImageBean(pic_url, pic_local_path);
                            Log.d(TAG, "run: picUrl " + pic_url);
                            chatMessage.setImage(imgBean);
                        }
                    } else if (message_type == ChatProviderHelper.Chat.MSG_TYPE_TEXT) {
                        if (!TextUtils.isEmpty(message)) {
                            chatMessage = new QchatMessage(msgID, chatUser, message);
                        }
                    }

                    if (chatMessage != null) {
                        chatMessage.setCreatedAt(new Date(message_time));
                        chatMessage.setDirection(direction);
                        chatMessage.setStatus(status);
                        records.add(chatMessage);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return records;
        }

        public static List<ChatEntity> getChatEntities(String selection,
                                                       String[] selectionArgs) {
            Log.d(TAG, "getChatEntities");
            final ContentResolver resolver = BaseApplication.mApplication.getContentResolver();
            List<ChatEntity> records = new ArrayList<>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(CONTENT_URI, null, selection, selectionArgs,
                        DEFAULT_SORT_ORDER);
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    ChatEntity communication = new ChatEntity();

                    communication.seq_no = cursor.getInt(cursor.getColumnIndex(COLUMN_SEQ_NO));
                    communication.openid = cursor.getString(cursor.getColumnIndex(ChatDatabaseHelper.COMMON_COLUMN_OPEN_ID));
                    communication.message = cursor.getString(cursor.getColumnIndex(COLUMN_MESSAGE));
                    communication.direction = cursor.getInt(cursor.getColumnIndex(COLUMN_DIRECTION));
                    communication.message_time = cursor.getLong(cursor.getColumnIndex(COLUMN_MESSAGE_TIME));
                    communication.message_type = cursor.getInt(cursor.getColumnIndex(COLUMN_MESSAGE_TYPE));
                    communication.audio_local_path = cursor.getString(cursor.getColumnIndex(COLUMN_AUDIO_LOCAL_PATH));
                    communication.audio_download_path = cursor.getString(cursor.getColumnIndex(COLUMN_AUDIO_DOWNLOAD_PATH));
                    communication.audio_md5 = cursor.getString(cursor.getColumnIndex(COLUMN_AUDIO_MD5));
                    communication.pic_local_path = cursor.getString(cursor.getColumnIndex(COLUMN_PIC_LOCAL_PATH));
                    communication.pic_url = cursor.getString(cursor.getColumnIndex(COLUMN_PIC_URL));
                    communication.status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
                    communication.duration = cursor.getInt(cursor.getColumnIndex(COLUMN_DURATION));

                    records.add(communication);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return records;
        }
    }


    /*****************  Homework  ******************/
    public static class Homework implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(ChatProviderHelper.CONTENT_URI,
                "/homework");

        public static final String CONTENT_TYPE = "com.kinstalk.cursor.dir/homework";

        public static final String COLUMN_OPENID = "openid";
        public static final String ID = "id";
        public static final String COLUMN_HOMEWORKID = "homeworkid";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_ASSIGN_TIME = "assign_time";
        public static final String COLUMN_FINISH_TIME = "finish_time";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_URL = "url";

        public static final String DEFAULT_SORT_ORDER = COLUMN_ASSIGN_TIME + " ASC";

        public static List<HomeWorkEntity> getHomeWorkEntities(String selection,
                                                               String[] selectionArgs) {
            Log.d(TAG, "getHomeWorkEntity");
            final ContentResolver resolver = BaseApplication.mApplication.getContentResolver();
            List<HomeWorkEntity> records = new ArrayList<>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(CONTENT_URI, null, selection, selectionArgs,
                        DEFAULT_SORT_ORDER);
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    HomeWorkEntity homework = new HomeWorkEntity();
                    homework.openid = cursor.getString(cursor.getColumnIndex(COLUMN_OPENID));
                    homework.homeworkid = cursor.getString(cursor.getColumnIndex(COLUMN_HOMEWORKID));
                    homework.title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                    homework.content = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT));
                    homework.url = cursor.getString(cursor.getColumnIndex(COLUMN_URL));
                    homework.type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
                    homework.assign_time = cursor.getLong(cursor.getColumnIndex(COLUMN_ASSIGN_TIME));
                    homework.finish_time = cursor.getLong(cursor.getColumnIndex(COLUMN_FINISH_TIME));
                    homework.status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
                    records.add(homework);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return records;
        }

    }

    /******************Reminder*******************/
    public static class Reminder implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ChatProviderHelper.CONTENT_URI, "/reminder");
        public static final String CONTENT_TYPE = "com.kinstalk.cursor.dir/reminder";
        public static final String COLUMN_OPENID = "openid";
        public static final String COLUMN_REMINDER_ID = "remindid";
        public static final String COLUMN_REMINDER_CONTENT = "content";
        public static final String COLUMN_REMINDER_TIME = "reminder_time";
        public static final String TRIGER_TIME = "trigger_time";
        public static final String COLUMN_ASSIGN_TIME = "add_time";
        public static final String COLUMN_REPEAT_TIME = "repeat_time";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_STATUS = "status";
        public static final String DEFAULT_SORT_ORDER = COLUMN_REMINDER_TIME + " ASC";

        public static List<RemindEntity> getReminderEntities(String selection,
                                                             String[] selectionArgs) {
            Log.d(TAG, "getReminderEntity");
            final ContentResolver resolver = BaseApplication.mApplication.getContentResolver();
            List<RemindEntity> records = new ArrayList<>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(CONTENT_URI, null, selection, selectionArgs,
                        DEFAULT_SORT_ORDER);
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    RemindEntity remind = new RemindEntity();
                    remind.openid = cursor.getString(cursor.getColumnIndex(COLUMN_OPENID));
                    remind.remindid = cursor.getString(cursor.getColumnIndex(COLUMN_REMINDER_ID));
                    remind.content = cursor.getString(cursor.getColumnIndex(COLUMN_REMINDER_CONTENT));
                    remind.assign_time = cursor.getLong(cursor.getColumnIndex(COLUMN_ASSIGN_TIME));
                    remind.remind_time = cursor.getLong(cursor.getColumnIndex(COLUMN_REMINDER_TIME));
                    remind.repeat_time = cursor.getString(cursor.getColumnIndex(COLUMN_REPEAT_TIME));
                    remind.type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
                    remind.status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
                    records.add(remind);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return records;
        }

    }

    /******************Reminder*******************/
    public static class Habit implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ChatProviderHelper.CONTENT_URI, "/habit");
        public static final String CONTENT_TYPE = "com.kinstalk.cursor.dir/habit";
        public static final String COLUMN_OPENID = "openid";
        public static final String COLUMN_HABIT_ID = "habitid";
        public static final String COLUMN_REMINDER_CONTENT = "name";
        public static final String COLUMN_REMINDER_TIME = "reminder_time";
        public static final String COLUMN_END_TIME = "end_time";
        public static final String COLUMN_ASSIGN_TIME = "add_time";
        public static final String COLUMN_REPEAT_TIME = "repeat_time";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_STATUS = "status";
        public static final String DEFAULT_SORT_ORDER = COLUMN_REMINDER_TIME + " ASC";

        public static List<HabitEntity> getHabitEntities(String selection,
                                                         String[] selectionArgs) {
            Log.d(TAG, "getHabitEntities");
            final ContentResolver resolver = BaseApplication.mApplication.getContentResolver();
            List<HabitEntity> records = new ArrayList<>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(CONTENT_URI, null, selection, selectionArgs,
                        DEFAULT_SORT_ORDER);
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    HabitEntity habit = new HabitEntity();
                    habit.openid = cursor.getString(cursor.getColumnIndex(COLUMN_OPENID));
                    habit.remindid = cursor.getString(cursor.getColumnIndex(COLUMN_HABIT_ID));
                    habit.name = cursor.getString(cursor.getColumnIndex(COLUMN_REMINDER_CONTENT));
                    habit.assign_time = cursor.getLong(cursor.getColumnIndex(COLUMN_ASSIGN_TIME));
                    habit.remind_time = cursor.getLong(cursor.getColumnIndex(COLUMN_REMINDER_TIME));
                    habit.end_time = cursor.getLong(cursor.getColumnIndex(COLUMN_END_TIME));
                    habit.repeat_time = cursor.getString(cursor.getColumnIndex(COLUMN_REPEAT_TIME));
                    habit.type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
                    habit.status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
                    records.add(habit);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return records;
        }

        public static List<RemindEntity> getHabitReminderEntities(String selection,
                                                                  String[] selectionArgs) {
            Log.d(TAG, "getHabitReminderEntities");
            final ContentResolver resolver = BaseApplication.mApplication.getContentResolver();
            List<RemindEntity> records = new ArrayList<>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(CONTENT_URI, null, selection, selectionArgs,
                        DEFAULT_SORT_ORDER);
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    RemindEntity remind = new RemindEntity();
                    remind.openid = cursor.getString(cursor.getColumnIndex(COLUMN_OPENID));
                    remind.remindid = cursor.getString(cursor.getColumnIndex(COLUMN_HABIT_ID));
                    remind.content = cursor.getString(cursor.getColumnIndex(COLUMN_REMINDER_CONTENT));
                    remind.assign_time = cursor.getLong(cursor.getColumnIndex(COLUMN_ASSIGN_TIME));
                    remind.remind_time = cursor.getLong(cursor.getColumnIndex(COLUMN_REMINDER_TIME));
                    remind.repeat_time = cursor.getString(cursor.getColumnIndex(COLUMN_REPEAT_TIME));
                    remind.type = cursor.getInt(cursor.getColumnIndex(COLUMN_TYPE));
                    remind.status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
                    records.add(remind);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return records;
        }

        public static HabitEntity homeHabitOrNot() {
            try {
                List<HabitEntity> mEntities = Habit.getHabitEntities(null, null);
                if (mEntities != null) {
                    for (int i = 0; i < mEntities.size(); i++) {
                        if (mEntities.get(i).getType() == TYPE_HOMEWORK) {
                            int day = DateUtils.getWeekDay();
                            Long startTime = mEntities.get(i).getAssign_time();
                            Long currentTime = System.currentTimeMillis();
                            String rrule = mEntities.get(i).getRepeat_time();
                            if (rrule.contains(Integer.toString(day)) && (startTime < currentTime))
                                return mEntities.get(i);
                        }
                    }
                }
            } catch (Exception e) {
                QAILog.e(TAG, e);
            }
            return null;
        }

    }

    /******************HabitKidsInfo*******************/
    public static class HabitKidsInfo implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(ChatProviderHelper.CONTENT_URI, "/kidsinfo");
        public static final String CONTENT_TYPE = "com.kinstalk.cursor.dir/kidsinfo";
        public static final String COLUMN_KIDS_NAME = "kidsName";
        public static final String COLUMN_KIDS_GENDER = "gender";
        public static final String COLUMN_KIDS_BIRTHDAY = "birthday";
        public static final String COLUMN_STAR = "star";
        public static final String COLUMN_CREDIT = "credit";
        public static final String COLUMN_GRADE = "grade";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_EXTRA = "extra";

        public static List<HabitKidsEntity> getKidsInfoEntities(String selection,
                                                                String[] selectionArgs) {
            Log.d(TAG, "getKidsInfoEntities");
            final ContentResolver resolver = BaseApplication.mApplication.getContentResolver();
            List<HabitKidsEntity> records = new ArrayList<>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(CONTENT_URI, null, selection, selectionArgs,
                        null);
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    HabitKidsEntity kidsInfo = new HabitKidsEntity();
                    kidsInfo.nick_name = cursor.getString(cursor.getColumnIndex(COLUMN_KIDS_NAME));
                    kidsInfo.gender = cursor.getInt(cursor.getColumnIndex(COLUMN_KIDS_GENDER));
                    kidsInfo.birthday = cursor.getString(cursor.getColumnIndex(COLUMN_KIDS_BIRTHDAY));
                    kidsInfo.grade = cursor.getString(cursor.getColumnIndex(COLUMN_GRADE));
                    kidsInfo.all_star = cursor.getInt(cursor.getColumnIndex(COLUMN_STAR));
                    kidsInfo.all_credit = cursor.getInt(cursor.getColumnIndex(COLUMN_CREDIT));
                    kidsInfo.headimgurl = cursor.getString(cursor.getColumnIndex(COLUMN_URL));
                    records.add(kidsInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return records;
        }

        public static int getStarCount() {
            try {
                if (kidsEntity != null)
                    return kidsEntity.getStar();
                List<HabitKidsEntity> mEntities = getKidsInfoEntities(null, null);
                if (mEntities != null) {
                    kidsEntity = mEntities.get(0);
                    return mEntities.get(0).getStar();
                }
            } catch (Exception e) {
                QAILog.e(TAG, e);
            }
            return 0;
        }

    }

    /*****************  WxUser  ******************/
    public static class WxUser implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(ChatProviderHelper.CONTENT_URI,
                "/wxuser");

        public static final String CONTENT_TYPE = "com.kinstalk.cursor.dir/wxuser";

        public static final String COLUMN_OPENID = "openid";
        public static final String COLUMN_NICKNAME = "nickname";
        public static final String COLUMN_SEX = "sex";
        public static final String COLUMN_COUNTRY = "country";
        public static final String COLUMN_PROVINCE = "province";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_SUBSCRIBE_TIME = "subscribe_time";
        public static final String COLUMN_HEADIMGURL = "headimgurl";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_REMARK = "remark";
        public static final String COLUMN_QR_SCENE = "qr_scene";
        public static final String COLUMN_QR_SCENE_STR = "qr_scene_str";
        public static final String COLUMN_SUBSCRIBE = "subscribe";
        public static final String COLUMN_UNIONID = "unionid";
        public static final String COLUMN_GROUPID = "groupid";
        public static final String COLUMN_SUBSCRIBE_SCENE = "subscribe_scene";
        public static final String COLUMN_STATUS = "status";

        public static final String DEFAULT_SORT_ORDER = COLUMN_SUBSCRIBE_TIME + " ASC";

        public static List<WxUserEntity> getWxUserEntities(String selection,
                                                           String[] selectionArgs) {
            Log.d(TAG, "getWxUserEntity");
            final ContentResolver resolver = BaseApplication.mApplication.getContentResolver();
            List<WxUserEntity> records = new ArrayList<>();
            Cursor cursor = null;
            try {
                cursor = resolver.query(CONTENT_URI, null, selection, selectionArgs,
                        DEFAULT_SORT_ORDER);
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    WxUserEntity user = new WxUserEntity();
                    user.openid = cursor.getString(cursor.getColumnIndex(COLUMN_OPENID));
                    user.nickname = cursor.getString(cursor.getColumnIndex(COLUMN_NICKNAME));
                    user.sex = cursor.getInt(cursor.getColumnIndex(COLUMN_SEX));
                    user.country = cursor.getString(cursor.getColumnIndex(COLUMN_COUNTRY));
                    user.province = cursor.getString(cursor.getColumnIndex(COLUMN_PROVINCE));
                    user.city = cursor.getString(cursor.getColumnIndex(COLUMN_CITY));
                    user.subscribe_time = cursor.getLong(cursor.getColumnIndex(COLUMN_SUBSCRIBE_TIME));
                    user.headimgurl = cursor.getString(cursor.getColumnIndex(COLUMN_HEADIMGURL));
                    user.language = cursor.getString(cursor.getColumnIndex(COLUMN_LANGUAGE));
                    user.remark = cursor.getString(cursor.getColumnIndex(COLUMN_REMARK));
                    user.qr_scene = cursor.getInt(cursor.getColumnIndex(COLUMN_QR_SCENE));
                    user.qr_scene_str = cursor.getString(cursor.getColumnIndex(COLUMN_QR_SCENE_STR));
                    user.subscribe = cursor.getInt(cursor.getColumnIndex(COLUMN_SUBSCRIBE));
                    user.unionid = cursor.getString(cursor.getColumnIndex(COLUMN_UNIONID));
                    user.groupid = cursor.getInt(cursor.getColumnIndex(COLUMN_GROUPID));
                    user.subscribe_scene = cursor.getString(cursor.getColumnIndex(COLUMN_SUBSCRIBE_SCENE));
                    user.status = cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS));
                    records.add(user);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return records;
        }
    }

    public static void saveIncomingMsg(final Context context, final String openid, final String msg, final String res_download_path, final String msg_id, final int message_type, final int duration) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                String audio_download_path = "null";
                String pic_download_path = "null";
                if (message_type == Chat.MSG_TYPE_PIC) {
                    pic_download_path = res_download_path;
                } else if (message_type == Chat.MSG_TYPE_VOICE) {
                    audio_download_path = res_download_path;
                }

                ContentValues values = new ContentValues();
                values.put(ChatDatabaseHelper.COMMON_COLUMN_OPEN_ID, openid);
                values.put(Chat.COLUMN_MSG_ID, msg_id);
                values.put(Chat.COLUMN_MESSAGE, msg);
                values.put(Chat.COLUMN_DIRECTION, DIRECTION_IN);
                values.put(Chat.COLUMN_MESSAGE_TIME, System.currentTimeMillis());
                values.put(Chat.COLUMN_MESSAGE_TYPE, message_type);
                values.put(Chat.COLUMN_AUDIO_LOCAL_PATH, "null");
                values.put(Chat.COLUMN_AUDIO_DOWNLOAD_PATH, audio_download_path);
                values.put(Chat.COLUMN_AUDIO_MD5, "null");
                values.put(Chat.COLUMN_PIC_LOCAL_PATH, "null");
                values.put(Chat.COLUMN_PIC_URL, pic_download_path);
                values.put(Chat.COLUMN_STATUS, 0);
                values.put(Chat.COLUMN_DURATION, duration);
                context.getContentResolver().insert(Chat.CONTENT_URI, values);

                // 文字类型的消息，插入完成后通知界面更新红点
                if (ChatProviderHelper.Chat.MSG_TYPE_TEXT == message_type) {
                    MessageManager.notifyMessageChange(true);
                }

            }
        });
    }

    public static void saveOutcomingMsg(final Context context, final String msg, final String audio_local_path, final String audio_download_path, final int message_type, final int duration) {
        ContentValues values = new ContentValues();
        values.put(ChatDatabaseHelper.COMMON_COLUMN_OPEN_ID, Integer.toString(0));
        values.put(Chat.COLUMN_MESSAGE, msg);
        values.put(Chat.COLUMN_DIRECTION, DIRECTION_OUT);
        values.put(Chat.COLUMN_MESSAGE_TIME, System.currentTimeMillis());
        values.put(Chat.COLUMN_MESSAGE_TYPE, message_type);
        values.put(Chat.COLUMN_AUDIO_LOCAL_PATH, audio_local_path);
        values.put(Chat.COLUMN_AUDIO_DOWNLOAD_PATH, audio_download_path);
        values.put(Chat.COLUMN_AUDIO_MD5, "null");
        values.put(Chat.COLUMN_PIC_LOCAL_PATH, "null");
        values.put(Chat.COLUMN_PIC_URL, "null");
        values.put(Chat.COLUMN_STATUS, 0);
        values.put(Chat.COLUMN_DURATION, duration);
        context.getContentResolver().insert(Chat.CONTENT_URI, values);
    }

    public static void insertOrUpdateWxUser(final List<WxUserEntity> wxUserList) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {

                SQLiteDatabase qchatDB = ChatProvider.mOpenHelper.getWritableDatabase();
                qchatDB.beginTransaction();
                try {
                    for (int i = 0; i < wxUserList.size(); i++) {
                        WxUserEntity wxUserEntity = wxUserList.get(i);
                        ContentValues values = new ContentValues();
                        values.put(WxUser.COLUMN_OPENID, wxUserEntity.getOpenid());
                        values.put(WxUser.COLUMN_NICKNAME, wxUserEntity.getNickname());
                        values.put(WxUser.COLUMN_SEX, wxUserEntity.getSex());
                        values.put(WxUser.COLUMN_COUNTRY, wxUserEntity.getCountry());
                        values.put(WxUser.COLUMN_PROVINCE, wxUserEntity.getProvince());
                        values.put(WxUser.COLUMN_CITY, wxUserEntity.getCity());
                        values.put(WxUser.COLUMN_SUBSCRIBE_TIME, wxUserEntity.getSubscribe_time());
                        values.put(WxUser.COLUMN_HEADIMGURL, wxUserEntity.getHeadimgurl());
                        values.put(WxUser.COLUMN_LANGUAGE, wxUserEntity.getLanguage());
                        values.put(WxUser.COLUMN_REMARK, wxUserEntity.getRemark());
                        values.put(WxUser.COLUMN_QR_SCENE, wxUserEntity.getQr_scene());
                        values.put(WxUser.COLUMN_QR_SCENE_STR, wxUserEntity.getQr_scene_str());
                        values.put(WxUser.COLUMN_SUBSCRIBE, wxUserEntity.getSubscribe());
                        values.put(WxUser.COLUMN_UNIONID, wxUserEntity.getUnionid());
                        values.put(WxUser.COLUMN_GROUPID, wxUserEntity.getGroupid());
                        values.put(WxUser.COLUMN_SUBSCRIBE_SCENE, wxUserEntity.getSubscribe_scene());
                        values.put(WxUser.COLUMN_STATUS, wxUserEntity.getStatus());

                        qchatDB.insertWithOnConflict(ChatDatabaseHelper.TABLE_WXUSER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    }
                    qchatDB.setTransactionSuccessful();
                    BaseApplication.mApplication.getApplicationContext().getContentResolver().notifyChange(WxUser.CONTENT_URI, null);
                } finally {
                    qchatDB.endTransaction();
                }
            }
        });
    }


    public static void insertOrUpdateHomework(final List<HomeWorkEntity> homeworkList) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {

                SQLiteDatabase qchatDB = ChatProvider.mOpenHelper.getWritableDatabase();
                qchatDB.beginTransaction();
                try {
                    for (int i = 0; i < homeworkList.size(); i++) {
                        HomeWorkEntity homeworkEntity = homeworkList.get(i);
                        ContentValues values = new ContentValues();
                        values.put(Homework.COLUMN_OPENID, homeworkEntity.getOpenid());
                        values.put(Homework.COLUMN_HOMEWORKID, homeworkEntity.getHomeworkid());
                        values.put(Homework.COLUMN_TITLE, homeworkEntity.getTitle());
                        values.put(Homework.COLUMN_CONTENT, homeworkEntity.getContent());
                        values.put(Homework.COLUMN_ASSIGN_TIME, homeworkEntity.getAssign_time());
                        values.put(Homework.COLUMN_FINISH_TIME, homeworkEntity.getFinish_time());
                        values.put(Homework.COLUMN_STATUS, homeworkEntity.getStatus());
                        values.put(Homework.COLUMN_TYPE, homeworkEntity.getType());
                        values.put(Homework.COLUMN_URL, homeworkEntity.getUrl());

                        qchatDB.insertWithOnConflict(ChatDatabaseHelper.TABLE_HOMEWORK, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    }
                    qchatDB.setTransactionSuccessful();
                    BaseApplication.mApplication.getApplicationContext().getContentResolver().notifyChange(Homework.CONTENT_URI, null);
                } finally {
                    qchatDB.endTransaction();
                }
            }
        });
    }


    public static void insertOrUpdateRemind(final List<RemindEntity> remindList) {
        SQLiteDatabase qchatDB = ChatProvider.mOpenHelper.getWritableDatabase();
        qchatDB.beginTransaction();
        try {
            QAILog.d(TAG, "insertOrUpdateRemind");
            for (int i = 0; i < remindList.size(); i++) {
                RemindEntity remindEntity = remindList.get(i);
                ContentValues values = new ContentValues();
                values.put(Reminder.COLUMN_OPENID, remindEntity.getOpenid());
                values.put(Reminder.COLUMN_REMINDER_ID, remindEntity.getRemindid());
                values.put(Reminder.COLUMN_REMINDER_CONTENT, remindEntity.getContent());
                values.put(Reminder.COLUMN_ASSIGN_TIME, remindEntity.getAssign_time());
                values.put(Reminder.COLUMN_REPEAT_TIME, remindEntity.getRepeat_time());
                values.put(Reminder.COLUMN_REMINDER_TIME, remindEntity.getRemind_time());
                values.put(Reminder.COLUMN_STATUS, remindEntity.getStatus());

                qchatDB.insertWithOnConflict(ChatDatabaseHelper.TABLE_REMINDER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            qchatDB.setTransactionSuccessful();
            BaseApplication.mApplication.getApplicationContext().getContentResolver().notifyChange(Reminder.CONTENT_URI, null);
        } catch (SQLException e) {
            QAILog.e(TAG, e);
        } finally {
            qchatDB.endTransaction();
        }
    }

    public static int insertOrUpdateHabit(final List<HabitEntity> habitList) {
        int status = 0;
        SQLiteDatabase qchatDB = ChatProvider.mOpenHelper.getWritableDatabase();
        qchatDB.beginTransaction();
        try {
            QAILog.d(TAG, "insertOrUpdateHabit");
            for (int i = 0; i < habitList.size(); i++) {
                HabitEntity habitEntity = habitList.get(i);
                ContentValues values = new ContentValues();
                values.put(Habit.COLUMN_OPENID, habitEntity.getOpenid());
                values.put(Habit.COLUMN_HABIT_ID, habitEntity.getRemindid());
                values.put(Habit.COLUMN_REMINDER_CONTENT, habitEntity.getName());
                values.put(Habit.COLUMN_ASSIGN_TIME, habitEntity.getAssign_time());
                values.put(Habit.COLUMN_REPEAT_TIME, habitEntity.getRepeat_time());
                values.put(Habit.COLUMN_REMINDER_TIME, habitEntity.getRemind_time());
                values.put(Habit.COLUMN_END_TIME, habitEntity.getEnd_time());
                values.put(Habit.COLUMN_TYPE, habitEntity.getType());
                values.put(Habit.COLUMN_STATUS, habitEntity.getStatus());

                qchatDB.insertWithOnConflict(ChatDatabaseHelper.TABLE_HABIT, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            qchatDB.setTransactionSuccessful();
            BaseApplication.mApplication.getApplicationContext().getContentResolver().notifyChange(Habit.CONTENT_URI, null);
        } catch (SQLException e) {
            status = -1;
        } finally {
            qchatDB.endTransaction();
        }
        return status;
    }

    public static void insertOrUpdateKidsInfo(final List<HabitKidsEntity> habitList) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {

                SQLiteDatabase qchatDB = ChatProvider.mOpenHelper.getWritableDatabase();
                qchatDB.beginTransaction();
                try {
                    QAILog.d(TAG, "insertOrUpdateKidsInfo");
                    for (int i = 0; i < habitList.size(); i++) {
                        HabitKidsEntity kidsInfoEntity = habitList.get(i);
                        ContentValues values = new ContentValues();
                        values.put(HabitKidsInfo.COLUMN_GRADE, kidsInfoEntity.getGrade());
                        values.put(HabitKidsInfo.COLUMN_KIDS_BIRTHDAY, kidsInfoEntity.getBirthday());
                        values.put(HabitKidsInfo.COLUMN_KIDS_GENDER, kidsInfoEntity.getGender());
                        values.put(HabitKidsInfo.COLUMN_KIDS_NAME, kidsInfoEntity.getNick_name());
                        values.put(HabitKidsInfo.COLUMN_STAR, kidsInfoEntity.getStar());
                        values.put(HabitKidsInfo.COLUMN_URL, kidsInfoEntity.getHeadimgurl());
                        values.put(HabitKidsInfo.COLUMN_CREDIT, kidsInfoEntity.getCredit());
                        qchatDB.insertWithOnConflict(ChatDatabaseHelper.TABLE_KIDINFO, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    }
                    qchatDB.setTransactionSuccessful();
//                    BaseApplication.mApplication.getApplicationContext().getContentResolver().notifyChange(Habit.CONTENT_URI, null);
                } catch (SQLException e) {
                    QAILog.e(TAG, e);
                } finally {
                    qchatDB.endTransaction();
                }
            }
        });
    }

    public static boolean isBinded() {
        return false;

    }
}
