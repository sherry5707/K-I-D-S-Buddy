package com.kinstalk.her.qchatmodel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.kinstalk.her.qchatcomm.utils.QAILog;

/**
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 * Created by wangzhipeng on 2018/4/8.
 */

public class ChatDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "ChatDatabaseHelper";
    private static final String DATABASE_NAME = "qchat.db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_CHAT = "chat";
    public static final String TABLE_HOMEWORK = "homework";
    public static final String TABLE_REMINDER = "reminder";
    public static final String TABLE_WXUSER = "wxuser";
    public static final String TABLE_HABIT = "habit";
    public static final String TABLE_KIDINFO = "kidsinfo";

    public static final String COMMON_COLUMN_OPEN_ID = "openid";


    public ChatDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static ChatDatabaseHelper mChatDatabaseHelper;

    public static ChatDatabaseHelper getInstance(final Context context) {
        if (mChatDatabaseHelper == null) {
            synchronized (ChatDatabaseHelper.class) {
                if (mChatDatabaseHelper == null) {
                    mChatDatabaseHelper = new ChatDatabaseHelper(context);
                }
            }
        }

        return mChatDatabaseHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        db.execSQL("drop table if exists chat");
        db.execSQL("drop table if exists homework");
        db.execSQL("drop table if exists reminder");
        db.execSQL("drop table if exists habit");
        db.execSQL("drop table if exists kidsinfo");
        createTables(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        // TODO do nothing
        if(currentVersion == 2 && oldVersion > 2) {
            db.execSQL("drop table if exists chat");
            db.execSQL("drop table if exists homework");
            db.execSQL("drop table if exists reminder");
            db.execSQL("drop table if exists habit");
            db.execSQL("drop table if exists kidsinfo");
            createTables(db);
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldV, final int newV) {
        for (int version = oldV + 1; version <= newV; version++) {
            upgradeTo(db, version);
        }
    }

    /**
     * Upgrade database from (version - 1) to version.
     */
    private void upgradeTo(SQLiteDatabase db, int version) {
        switch (version) {
            case 1:
                db.execSQL("drop table if exists chat");
                db.execSQL("drop table if exists homework");
                db.execSQL("drop table if exists reminder");
                createTables(db);
                break;
            case 2:
                db.execSQL("drop table if exists chat");
                db.execSQL("drop table if exists homework");
                db.execSQL("drop table if exists reminder");
                db.execSQL("drop table if exists habit");
                db.execSQL("drop table if exists kidsinfo");
                createTables(db);
          //      upgradeToVersionTwo(db);
                break;
            case 3:
                upgradeToVersionThree(db);
                break;
            case 4:
                upgradeToVersionFour(db);
                break;
            default:
                Log.d(TAG, "Don't know how to upgrade to " + version);
        }
    }

    private  void upgradeToVersionTwo(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + TABLE_HOMEWORK + " ADD COLUMN " + ChatProviderHelper.HabitKidsInfo.COLUMN_URL + " TEXT ");
            db.execSQL("ALTER TABLE " + TABLE_HOMEWORK + " ADD COLUMN " + ChatProviderHelper.HabitKidsInfo.COLUMN_EXTRA + " TEXT ");
            db.execSQL("ALTER TABLE " + TABLE_REMINDER + " ADD COLUMN " + ChatProviderHelper.Homework.COLUMN_TYPE + " TEXT ");
        } catch (Exception e) {
            QAILog.e(TAG, e);
        }
    }

    private void upgradeToVersionThree(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + TABLE_KIDINFO + " ADD COLUMN " + ChatProviderHelper.Homework.COLUMN_TYPE + " TEXT ");
            db.execSQL("ALTER TABLE " + TABLE_KIDINFO + " ADD COLUMN " + ChatProviderHelper.Homework.COLUMN_URL + " TEXT ");
            db.execSQL("ALTER TABLE " + TABLE_KIDINFO + " ADD COLUMN " + ChatProviderHelper.HabitKidsInfo.COLUMN_CREDIT + " INTEGER ");
        } catch (Exception e) {
            QAILog.e(TAG, e);
        }
    }

    private void upgradeToVersionFour(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + TABLE_CHAT + " ADD COLUMN " + ChatProviderHelper.Chat.COLUMN_MSG_ID + " TEXT ");
        } catch (Exception e) {
            QAILog.e(TAG, e);
        }
    }

    private void createTables(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CHAT + " (" + ChatProviderHelper.Chat.COLUMN_SEQ_NO + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COMMON_COLUMN_OPEN_ID + " TEXT, "
                    + ChatProviderHelper.Chat.COLUMN_MESSAGE + " TEXT, "
                    + ChatProviderHelper.Chat.COLUMN_DIRECTION + " INTEGER, "
                    + ChatProviderHelper.Chat.COLUMN_MESSAGE_TIME + " TEXT , "
                    + ChatProviderHelper.Chat.COLUMN_MESSAGE_TYPE + " INTEGER, "
                    + ChatProviderHelper.Chat.COLUMN_AUDIO_LOCAL_PATH + " TEXT DEFAULT NULL, "
                    + ChatProviderHelper.Chat.COLUMN_AUDIO_DOWNLOAD_PATH + " TEXT DEFAULT NULL, "
                    + ChatProviderHelper.Chat.COLUMN_AUDIO_MD5 + " TEXT DEFAULT NULL, "
                    + ChatProviderHelper.Chat.COLUMN_PIC_LOCAL_PATH + " TEXT DEFAULT NULL, "
                    + ChatProviderHelper.Chat.COLUMN_PIC_URL + " TEXT DEFAULT NULL, "
                    + ChatProviderHelper.Chat.COLUMN_STATUS + " INTEGER, "
                    + ChatProviderHelper.Chat.COLUMN_DURATION + " INTEGER, "
                    + ChatProviderHelper.Chat.COLUMN_MSG_ID + " TEXT DEFAULT NULL);");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_WXUSER + " (" + ChatProviderHelper.WxUser.COLUMN_OPENID + " TEXT UNIQUE, "
                    + ChatProviderHelper.WxUser.COLUMN_NICKNAME + " TEXT, "
                    + ChatProviderHelper.WxUser.COLUMN_SEX + " INTEGER, "
                    + ChatProviderHelper.WxUser.COLUMN_COUNTRY + " TEXT DEFAULT NULL, "
                    + ChatProviderHelper.WxUser.COLUMN_PROVINCE + " TEXT DEFAULT NULL, "
                    + ChatProviderHelper.WxUser.COLUMN_CITY + " TEXT DEFAULT NULL, "
                    + ChatProviderHelper.WxUser.COLUMN_SUBSCRIBE_TIME + " TEXT, "
                    + ChatProviderHelper.WxUser.COLUMN_HEADIMGURL + " TEXT DEFAULT NULL, "
                    + ChatProviderHelper.WxUser.COLUMN_LANGUAGE + " TEXT DEFAULT NULL, "
                    + ChatProviderHelper.WxUser.COLUMN_REMARK + " TEXT DEFAULT NULL, "
                    + ChatProviderHelper.WxUser.COLUMN_QR_SCENE + " INTEGER, "
                    + ChatProviderHelper.WxUser.COLUMN_QR_SCENE_STR + " TEXT DEFAULT NULL, "
                    + ChatProviderHelper.WxUser.COLUMN_SUBSCRIBE + " INTEGER, "
                    + ChatProviderHelper.WxUser.COLUMN_UNIONID + " TEXT DEFAULT NULL, "
                    + ChatProviderHelper.WxUser.COLUMN_GROUPID + " INTEGER, "
                    + ChatProviderHelper.WxUser.COLUMN_SUBSCRIBE_SCENE + " TEXT DEFAULT NULL, "
                    + ChatProviderHelper.WxUser.COLUMN_STATUS + " INTEGER);");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HOMEWORK + " (" + ChatProviderHelper.WxUser.COLUMN_OPENID + " TEXT , "
                    + ChatProviderHelper.Homework.COLUMN_HOMEWORKID + " TEXT UNIQUE, "
                    + ChatProviderHelper.Homework.COLUMN_TITLE + " TEXT, "
                    + ChatProviderHelper.Homework.COLUMN_CONTENT + " TEXT, "
                    + ChatProviderHelper.Homework.COLUMN_ASSIGN_TIME + " TEXT, "
                    + ChatProviderHelper.Homework.COLUMN_FINISH_TIME + " TEXT, "
                    + ChatProviderHelper.Homework.COLUMN_TYPE + " TEXT, "
                    + ChatProviderHelper.Homework.COLUMN_URL + " TEXT, "
                    + ChatProviderHelper.WxUser.COLUMN_STATUS + " INTEGER);");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_REMINDER + " (" + ChatProviderHelper.WxUser.COLUMN_OPENID + " TEXT, "
                    + ChatProviderHelper.Reminder.COLUMN_REMINDER_ID + " TEXT UNIQUE, "
                    + ChatProviderHelper.Reminder.COLUMN_REMINDER_CONTENT + " TEXT, "
                    + ChatProviderHelper.Reminder.COLUMN_ASSIGN_TIME + " TEXT, "
                    + ChatProviderHelper.Reminder.COLUMN_REMINDER_TIME + " TEXT, "
                    + ChatProviderHelper.Reminder.COLUMN_REPEAT_TIME + " TEXT, "
                    + ChatProviderHelper.Reminder.COLUMN_TYPE + " INTEGER, "
                    + ChatProviderHelper.Reminder.COLUMN_STATUS + " INTEGER)");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HABIT + " (" + ChatProviderHelper.Chat.COLUMN_SEQ_NO + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ChatProviderHelper.WxUser.COLUMN_OPENID + " TEXT, "
                    + ChatProviderHelper.Habit.COLUMN_HABIT_ID + " TEXT, "
                    + ChatProviderHelper.Habit.COLUMN_REMINDER_CONTENT + " TEXT, "
                    + ChatProviderHelper.Habit.COLUMN_ASSIGN_TIME + " TEXT, "
                    + ChatProviderHelper.Habit.COLUMN_REMINDER_TIME + " TEXT, "
                    + ChatProviderHelper.Habit.COLUMN_REPEAT_TIME + " TEXT, "
                    + ChatProviderHelper.Habit.COLUMN_END_TIME + " TEXT, "
                    + ChatProviderHelper.Habit.COLUMN_TYPE + " INTEGER, "
                    + ChatProviderHelper.Habit.COLUMN_STATUS + " INTEGER)");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_KIDINFO + " (" + ChatProviderHelper.Chat.COLUMN_SEQ_NO + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ChatProviderHelper.HabitKidsInfo.COLUMN_GRADE + " TEXT, "
                    + ChatProviderHelper.HabitKidsInfo.COLUMN_KIDS_BIRTHDAY + " TEXT, "
                    + ChatProviderHelper.HabitKidsInfo.COLUMN_KIDS_GENDER + " INTEGER, "
                    + ChatProviderHelper.HabitKidsInfo.COLUMN_KIDS_NAME + " TEXT, "
                    + ChatProviderHelper.HabitKidsInfo.COLUMN_STAR + " INTEGER, "
                    + ChatProviderHelper.HabitKidsInfo.COLUMN_URL + " TEXT, "
                    + ChatProviderHelper.HabitKidsInfo.COLUMN_EXTRA + " TEXT, "
                    + ChatProviderHelper.HabitKidsInfo.COLUMN_CREDIT + " INTEGER) ");
        } catch (Exception e) {
            QAILog.e(TAG, e);
        }
    }
}
