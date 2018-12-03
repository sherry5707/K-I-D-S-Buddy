package com.kinstalk.her.qchat.gen;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.kinstalk.her.qchat.translation.TranslationBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "TRANSLATION_BEAN".
*/
public class TranslationBeanDao extends AbstractDao<TranslationBean, Long> {

    public static final String TABLENAME = "TRANSLATION_BEAN";

    /**
     * Properties of entity TranslationBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "id");
        public final static Property UserId = new Property(1, int.class, "userId", false, "USER_ID");
        public final static Property Input = new Property(2, String.class, "input", false, "INPUT");
        public final static Property Translation = new Property(3, String.class, "translation", false, "TRANSLATION");
    }


    public TranslationBeanDao(DaoConfig config) {
        super(config);
    }
    
    public TranslationBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"TRANSLATION_BEAN\" (" + //
                "\"id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"USER_ID\" INTEGER NOT NULL ," + // 1: userId
                "\"INPUT\" TEXT," + // 2: input
                "\"TRANSLATION\" TEXT);"); // 3: translation
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"TRANSLATION_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, TranslationBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getUserId());
 
        String input = entity.getInput();
        if (input != null) {
            stmt.bindString(3, input);
        }
 
        String translation = entity.getTranslation();
        if (translation != null) {
            stmt.bindString(4, translation);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, TranslationBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getUserId());
 
        String input = entity.getInput();
        if (input != null) {
            stmt.bindString(3, input);
        }
 
        String translation = entity.getTranslation();
        if (translation != null) {
            stmt.bindString(4, translation);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public TranslationBean readEntity(Cursor cursor, int offset) {
        TranslationBean entity = new TranslationBean( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // userId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // input
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3) // translation
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, TranslationBean entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUserId(cursor.getInt(offset + 1));
        entity.setInput(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setTranslation(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(TranslationBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(TranslationBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(TranslationBean entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
