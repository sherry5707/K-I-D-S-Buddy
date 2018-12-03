package com.kinstalk.her.qchat.greendao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.gen.DaoMaster;
import com.kinstalk.her.qchat.gen.DaoSession;
import com.kinstalk.her.qchat.translation.TranslationDBHelper;
import com.kinstalk.her.qchatcomm.utils.QAILog;

/**
 * 封装greendao管理类
 *
 * @author ailianpeng
 */
public class GreenDaoManager {

    private static final String TAG = GreenDaoManager.class.getSimpleName();

    private static final String DB_NAME = "content-db";

    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    private static GreenDaoManager mInstance; //单例

    private Context context;

    //创建数据库的工具
    private MySQLiteOpenHelper mHelper;

    private GreenDaoManager() {
        if (mInstance == null) {
            try {
                mHelper = new
                        MySQLiteOpenHelper(QchatApplication.getApplicationInstance(), DB_NAME, null);//此处为自己需要处理的表
                mDaoMaster = new DaoMaster(mHelper.getWritableDatabase());
                mDaoSession = mDaoMaster.newSession();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static GreenDaoManager getInstance() {
        if (mInstance == null) {
            synchronized (GreenDaoManager.class) {//保证异步处理安全操作

                if (mInstance == null) {
                    mInstance = new GreenDaoManager();
                }
            }
        }
        return mInstance;
    }

    public DaoMaster getMaster() {
        return mDaoMaster;
    }

    public DaoSession getSession() {
        return mDaoSession;
    }

    public DaoSession getNewSession() {
        mDaoSession = mDaoMaster.newSession();
        return mDaoSession;
    }

    /**
     * 关闭所有的操作，数据库开启后，使用完毕要关闭
     */
    public void closeConnection() {
        closeHelper();
        closeDaoSession();
    }

    public void closeHelper() {
        if (mHelper != null) {
            mHelper.close();
            mHelper = null;
        }
    }

    public void closeDaoSession() {
        if (mDaoSession != null) {
            mDaoSession.clear();
            mDaoSession = null;
        }
    }

    /**
     * 获取可读数据库
     */
    private SQLiteDatabase getReadableDatabase(Context context) {
        if (mHelper == null) {
            mHelper = new MySQLiteOpenHelper(context, DB_NAME, null);
        }
        SQLiteDatabase db = mHelper.getReadableDatabase();
        return db;
    }

    /**
     * 获取可写数据库
     */
    private SQLiteDatabase getWritableDatabase(Context context) {
        if (mHelper == null) {
            mHelper = new MySQLiteOpenHelper(context, DB_NAME, null);
        }
        SQLiteDatabase db = mHelper.getWritableDatabase();
        return db;
    }

    public void delDBData(Context context) {
        this.context = context;

        new DelDBThread().start();
    }


    private void deleSQL(Context context) {
        SQLiteDatabase db = getWritableDatabase(context);
        DaoMaster daoMaster = new DaoMaster(db);
        DaoMaster.dropAllTables(daoMaster.getDatabase(), true);
        DaoMaster.createAllTables(daoMaster.getDatabase(), true);

    }

    class DelDBThread extends Thread {

        public void run() {
            QAILog.d(TAG, "DelDBThread run");
            try {
                TranslationDBHelper.getInstance(context).delTranslationData();
                deleSQL(context);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

}

