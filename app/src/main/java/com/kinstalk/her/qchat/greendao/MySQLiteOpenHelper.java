package com.kinstalk.her.qchat.greendao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.kinstalk.her.qchat.gen.DaoMaster;
import com.kinstalk.her.qchat.gen.TTSContentBeanDao;
import com.kinstalk.her.qchat.gen.TranslationBeanDao;
import com.kinstalk.her.qchatcomm.utils.QAILog;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.StandardDatabase;

/**
 * Created by Growth on 2016/3/3.
 */
public class MySQLiteOpenHelper extends DaoMaster.OpenHelper {

    private static final String TAG = MySQLiteOpenHelper.class.getSimpleName();

    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);

        QAILog.d(TAG, "MySQLiteOpenHelper RUN");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {
            @Override
            public void onCreateAllTables(Database db, boolean ifNotExists) {
                DaoMaster.createAllTables(db, ifNotExists);

                QAILog.d(TAG, "onUpgrade onCreateAllTables RUN");
            }

            @Override
            public void onDropAllTables(Database db, boolean ifExists) {
                DaoMaster.dropAllTables(db, ifExists);

                QAILog.d(TAG, "onUpgrade onDropAllTables RUN");
            }
        }, TTSContentBeanDao.class, TranslationBeanDao.class);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /**
         * 执行数据库的降级操作
         * 1、只有新版本比旧版本低的时候才会执行
         * 2、如果不执行降级操作，会抛出异常
         */
        QAILog.i(TAG, "#############数据库降级了##############：" + oldVersion + " | " + newVersion);
        super.onDowngrade(db, oldVersion, newVersion);
        Database database = new StandardDatabase(db);
        TranslationBeanDao.dropTable(database, true);
        TTSContentBeanDao.dropTable(database, true);

        TranslationBeanDao.createTable(database, true);
        TTSContentBeanDao.createTable(database, true);
    }

}
