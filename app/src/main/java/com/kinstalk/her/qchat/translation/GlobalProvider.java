package com.kinstalk.her.qchat.translation;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.kinstalk.her.qchat.greendao.GreenDaoManager;
import com.kinstalk.her.qchat.greendao.MySQLiteOpenHelper;

import java.util.Iterator;

/**
 * content providess
 */

public class GlobalProvider extends ContentProvider {

    //    public static final Uri AUTHORITY_URI = Uri.parse("content://com.kinstalk.her.qchat");
//    public static final Uri CONTENT_URI = AUTHORITY_URI;
    private final String DB_NAME = "content-db";

    /**
     * 设置ContentProvider的唯一标识
     */
    public static final String AUTOHORITY = "com.kinstalk.her.qchat";

    private Context mContext;
    private MySQLiteOpenHelper helper = null;
    SQLiteDatabase db = null;

    public static final int VOICE_RESPONSE_Code = 1;
    public static final int TANSLATION_Code = 2;

    private static final UriMatcher mMatcher;

    static {
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 初始化
        mMatcher.addURI(AUTOHORITY, "TTSCONTENT_BEAN", VOICE_RESPONSE_Code);
        mMatcher.addURI(AUTOHORITY, "TRANSLATION_BEAN", TANSLATION_Code);
    }

    @Override
    public boolean onCreate() {

        mContext = getContext();

        helper = new MySQLiteOpenHelper(mContext, DB_NAME, null);

        db = helper.getWritableDatabase();

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // 根据URI匹配 URI_CODE，从而匹配ContentProvider中相应的表名
        // 该方法在最下面
        String table = getTableName(uri);

//        // 通过ContentUris类从URL中获取ID
//        long personid = ContentUris.parseId(uri);
//        System.out.println(personid);

        // 查询数据
        return db.query(table, projection, selection, selectionArgs, null, null, sortOrder, null);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        // 根据URI匹配 URI_CODE，从而匹配ContentProvider中相应的表名
        // 该方法在最下面
        String table = getTableName(uri);

        // 向该表添加数据
        db.insert(table, null, values);

        // 当该URI的ContentProvider数据发生变化时，通知外界（即访问该ContentProvider数据的访问者）
        mContext.getContentResolver().notifyChange(uri, null);

        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    /**
     * 根据URI匹配 URI_CODE，从而匹配ContentProvider中相应的表名
     */
    private String getTableName(Uri uri) {
        String tableName = null;
        switch (mMatcher.match(uri)) {
            case VOICE_RESPONSE_Code:
                tableName = "TTSCONTENT_BEAN";
                break;
            case TANSLATION_Code:
                tableName = "TRANSLATION_BEAN";
                break;
        }
        return tableName;
    }
}