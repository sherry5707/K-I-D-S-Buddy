package com.kinstalk.her.qchatmodel;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.text.TextUtils;
import android.util.Log;

import com.kinstalk.her.qchatcomm.base.BaseApplication;
import com.kinstalk.her.qchatcomm.utils.QAILog;

import java.util.ArrayList;
import java.util.List;


/**
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 * Created by wangzhipeng on 2018/4/8.
 */

public class ChatProvider extends ContentProvider {
    private static final String TAG = "AI-QchatApplication";
    public static final String AUTHORITY = "com.qchat.provider";
    private static final int CHAT = 1;
    private static final int HOMEWORK = 2;
    private static final int REMINDER = 3;
    private static final int WX_USER = 4;
    private static final int HABIT = 5;
    private static final int KIDSINFO = 6;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, "chat", CHAT);
        sURIMatcher.addURI(AUTHORITY, "homework", HOMEWORK);
        sURIMatcher.addURI(AUTHORITY, "reminder", REMINDER);
        sURIMatcher.addURI(AUTHORITY, "wxuser", WX_USER);
        sURIMatcher.addURI(AUTHORITY, "habit", HABIT);
        sURIMatcher.addURI(AUTHORITY, "kidsinfo", KIDSINFO);
    }

    public static ChatDatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        Log.d(TAG,"onCreate");
        mOpenHelper = new ChatDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sort) {
        Log.d(TAG,"query");

        if (!hasReadPermission()) {
            return null;
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int match = sURIMatcher.match(uri);
        Cursor ret = null;
        SqlSelection formatSelection = getWhereClause(uri, selection, selectionArgs, match);
        switch (match) {
            case CHAT: {
                ret = db.query(ChatDatabaseHelper.TABLE_CHAT, projection,
                        formatSelection.getSelection(), formatSelection.getParameters(), null,
                        null, sort);
                break;
            }
            case HOMEWORK: {
                ret = db.query(ChatDatabaseHelper.TABLE_HOMEWORK, projection,
                        formatSelection.getSelection(), formatSelection.getParameters(), null,
                        null, sort);
                break;
            }
            case REMINDER: {
                ret = db.query(ChatDatabaseHelper.TABLE_REMINDER, projection,
                        formatSelection.getSelection(), formatSelection.getParameters(), null,
                        null, sort);
                break;
            }
            case WX_USER: {
                ret = db.query(ChatDatabaseHelper.TABLE_WXUSER, projection,
                        formatSelection.getSelection(), formatSelection.getParameters(), null,
                        null, sort);
                break;
            }
            case HABIT: {
                ret = db.query(ChatDatabaseHelper.TABLE_HABIT, projection,
                        formatSelection.getSelection(), formatSelection.getParameters(), null,
                        null, sort);
                break;
            }
            case KIDSINFO: {
                ret = db.query(ChatDatabaseHelper.TABLE_KIDINFO, projection,
                        formatSelection.getSelection(), formatSelection.getParameters(), null,
                        null, sort);
                break;
            }
            default:
                Log.d(TAG,"querying unknown URI: " + uri);
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (ret != null) {
            ret.setNotificationUri(getContext().getContentResolver(), uri);
            Log.d(TAG,"created cursor " + ret + " on behalf of " + Binder.getCallingPid());
        } else {
            Log.d(TAG,"query failed in qchat_provider database");
        }

        return ret;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(TAG,"getType");
        int match = sURIMatcher.match(uri);
        switch (match) {
            case CHAT:
                return ChatProviderHelper.Chat.CONTENT_TYPE;
            case HOMEWORK:
                return ChatProviderHelper.Homework.CONTENT_TYPE;
            case REMINDER:
                return ChatProviderHelper.Reminder.CONTENT_TYPE;
            case HABIT:
                return ChatProviderHelper.Habit.CONTENT_TYPE;
            case KIDSINFO:
                return ChatProviderHelper.HabitKidsInfo.CONTENT_TYPE;
            default:
                Log.d(TAG,"calling getType on an unknown URI: " + uri);
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        Log.d(TAG,"update");

        if (!hasWritePermission() || values.size() == 0) {
            return -1;
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = -1;
        final int match = sURIMatcher.match(uri);
        SqlSelection selection = getWhereClause(uri, where, whereArgs, match);
        switch (match) {
            case CHAT: {
                count = db.update(ChatDatabaseHelper.TABLE_CHAT, values, selection.getSelection(),
                        selection.getParameters());
                break;
            }
            case HOMEWORK: {
                count = db.update(ChatDatabaseHelper.TABLE_HOMEWORK, values,
                        selection.getSelection(), selection.getParameters());
                break;
            }
            case REMINDER: {
                count = db.update(ChatDatabaseHelper.TABLE_REMINDER, values,
                        selection.getSelection(), selection.getParameters());
                break;
            }
            case HABIT: {
                count = db.update(ChatDatabaseHelper.TABLE_HABIT, values,
                        selection.getSelection(), selection.getParameters());
                break;
            }
            case KIDSINFO: {
                count = db.update(ChatDatabaseHelper.TABLE_KIDINFO, values,
                        selection.getSelection(), selection.getParameters());
                break;
            }
            default:
                Log.d(TAG,"updating unknown/invalid URI: " + uri);
                throw new UnsupportedOperationException("Cannot update URI: " + uri);
        }

        if (count > 0) {
            notifyContentChanged(uri, match);
        }
        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG,"insert");

        if (!hasWritePermission() || values.size() == 0) {
            return null;
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sURIMatcher.match(uri);
        long id = 0;

        switch (match) {
            case CHAT: {
                id = db.insert(ChatDatabaseHelper.TABLE_CHAT, null, values);
                break;
            }
            case HOMEWORK: {
                id = db.insert(ChatDatabaseHelper.TABLE_HOMEWORK, null, values);
                break;
            }
            case REMINDER: {
                id = db.insert(ChatDatabaseHelper.TABLE_REMINDER, null, values);
                break;
            }
            case HABIT: {
                id = db.insert(ChatDatabaseHelper.TABLE_HABIT, null, values);
                break;
            }
            case WX_USER: {
                id = db.insert(ChatDatabaseHelper.TABLE_WXUSER, null, values);
                break;
            }
            case KIDSINFO: {
                id = db.insert(ChatDatabaseHelper.TABLE_KIDINFO, null, values);
                break;
            }
            default:
                Log.d(TAG,"inserting unknown/invalid URI: " + uri);
                throw new UnsupportedOperationException("Cannot insert URI: " + uri);
        }

        if (id < 0) {
            Log.d(TAG,"couldn't insert into qchat_provider database");
            return null;
        } else {
            notifyContentChanged(uri, match);
        }
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        Log.d(TAG,"delete");

        if (!hasWritePermission()) {
            return -1;
        }
        int count = -1;
        try {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = sURIMatcher.match(uri);
            SqlSelection selection = getWhereClause(uri, where, whereArgs, match);
            switch (match) {
                case CHAT: {
                    count = db.delete(ChatDatabaseHelper.TABLE_CHAT, selection.getSelection(),
                            selection.getParameters());
                    break;
                }
                case HOMEWORK: {
                    count = db.delete(ChatDatabaseHelper.TABLE_HOMEWORK, selection.getSelection(),
                            selection.getParameters());
                    break;
                }
                case REMINDER: {
                    count = db.delete(ChatDatabaseHelper.TABLE_REMINDER, selection.getSelection(),
                            selection.getParameters());
                    break;
                }
                case HABIT: {
                    count = db.delete(ChatDatabaseHelper.TABLE_HABIT, selection.getSelection(),
                            selection.getParameters());
                    break;
                }
                case WX_USER: {
                    count = db.delete(ChatDatabaseHelper.TABLE_WXUSER, selection.getSelection(),
                            selection.getParameters());
                    break;
                }
                case KIDSINFO: {
                    count = db.delete(ChatDatabaseHelper.TABLE_KIDINFO, selection.getSelection(),
                            selection.getParameters());
                    break;
                }
                default:
                    Log.d(TAG, "deleting unknown/invalid URI: " + uri);
                    throw new UnsupportedOperationException("Cannot delete URI: " + uri);
            }

            if (count > 0) { // && ((where != null)|| (whereArgs!=null))) {
                notifyContentChanged(uri, match);
                Log.i(TAG, "delete count" + count);
            }
        }catch(Exception e) {
            QAILog.e(TAG, e);
        }
        return count;
    }

    /**
     * Notify of a change through URIs
     *
     * @param uri URI for the changed record(s)
     * @param uriMatch the match ID from {@link #sURIMatcher}
     */
    private void notifyContentChanged(final Uri uri, int uriMatch) {
        Long recordId = null;
        Uri uriToNotify = null;
        switch (uriMatch) {
            case CHAT: {
                uriToNotify = ChatProviderHelper.Chat.CONTENT_URI;
                break;
            }
            case HOMEWORK: {
                uriToNotify = ChatProviderHelper.Homework.CONTENT_URI;
                break;
            }
            case REMINDER: {
                uriToNotify = ChatProviderHelper.Reminder.CONTENT_URI;
                break;
            }
            case HABIT: {
                uriToNotify = ChatProviderHelper.Habit.CONTENT_URI;
                break;
            }
            case WX_USER: {
                uriToNotify = ChatProviderHelper.WxUser.CONTENT_URI;
                break;
            }
            case KIDSINFO: {
                uriToNotify = ChatProviderHelper.HabitKidsInfo.CONTENT_URI;
                break;
            }
            default:
                Log.d(TAG,"updating unknown/invalid URI: " + uri);
                throw new UnsupportedOperationException("Cannot update URI: " + uri);
        }

//        if (recordId != null) {
//            uriToNotify = ContentUris.withAppendedId(uriToNotify, recordId);
//        }
        BaseApplication.mApplication.getContentResolver().notifyChange(uriToNotify, null);
    }

    private static SqlSelection getWhereClause(final Uri uri, final String where,
                                               final String[] whereArgs, int uriMatch) {
        SqlSelection selection = new SqlSelection();
        selection.appendClause(where, whereArgs);
//        switch (uriMatch) {
//            case FAMILIES_ID:
//            case ASSIST_RECORDS_ID:
//            case COMMUNICATIONS_ID:
//            case TOUCH_DETAILS_ID: {
//                selection.appendClause(BaseColumns._ID + " = ?", getRecordIdFromUri(uri));
//                break;
//            }
//            default:
//        }

        return selection;
    }

    private static String getRecordIdFromUri(final Uri uri) {
        return uri.getPathSegments().get(1);
    }

    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

    private static class SqlSelection {
        public StringBuilder mWhereClause = new StringBuilder();
        public List<String> mParameters = new ArrayList<String>();

        public <T> void appendClause(String newClause, final T... parameters) {
            if (newClause == null || newClause.isEmpty()) {
                return;
            }
            if (mWhereClause.length() != 0) {
                mWhereClause.append(" AND ");
            }
            mWhereClause.append("(");
            mWhereClause.append(newClause);
            mWhereClause.append(")");
            if (parameters != null) {
                for (Object parameter : parameters) {
                    mParameters.add(parameter.toString());
                }
            }
        }

        public String getSelection() {
            return mWhereClause.toString();
        }

        public String[] getParameters() {
            String[] array = new String[mParameters.size()];
            return mParameters.toArray(array);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        if (!hasWritePermission()) {
            return -1;
        }

        int numInserted = 0;

        SQLiteDatabase snsDB = mOpenHelper.getWritableDatabase();
        snsDB.beginTransaction();
        try {
            final int match = sURIMatcher.match(uri);
            String table = null;
            switch (match) {
                case CHAT: {
                    table = ChatDatabaseHelper.TABLE_CHAT;
                    break;
                }
                case HOMEWORK: {
                    table = ChatDatabaseHelper.TABLE_HOMEWORK;
                    break;
                }
                case REMINDER: {
                    table = ChatDatabaseHelper.TABLE_REMINDER;
                    break;
                }
                case HABIT: {
                    table = ChatDatabaseHelper.TABLE_HABIT;
                    break;
                }
                case KIDSINFO: {
                    table = ChatDatabaseHelper.TABLE_KIDINFO;
                    break;
                }
                default:
            }

            for (ContentValues cv : values) {
                long newID = snsDB.insertOrThrow(table, null, cv);
                if (newID <= 0) {
                    throw new SQLException("Failed to insert row into " + uri);
                }
            }
            snsDB.setTransactionSuccessful();
            getContext().getContentResolver().notifyChange(uri, null);
            numInserted = values.length;
        } finally {
            snsDB.endTransaction();
        }
        return numInserted;
    }

    private boolean hasWritePermission() {
        //return getContext().checkCallingOrSelfPermission(PERMISSION_WRITE) == PackageManager.PERMISSION_GRANTED;
        return true;

    }

    private boolean hasReadPermission() {
        //return getContext().checkCallingOrSelfPermission(PERMISSION_READ) == PackageManager.PERMISSION_GRANTED;
        return true;
    }
}
