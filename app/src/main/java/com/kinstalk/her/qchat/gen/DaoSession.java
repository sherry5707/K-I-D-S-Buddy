package com.kinstalk.her.qchat.gen;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.kinstalk.her.qchat.translation.TranslationBean;
import com.kinstalk.her.qchat.voiceresponse.TTSContentBean;

import com.kinstalk.her.qchat.gen.TranslationBeanDao;
import com.kinstalk.her.qchat.gen.TTSContentBeanDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig translationBeanDaoConfig;
    private final DaoConfig tTSContentBeanDaoConfig;

    private final TranslationBeanDao translationBeanDao;
    private final TTSContentBeanDao tTSContentBeanDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        translationBeanDaoConfig = daoConfigMap.get(TranslationBeanDao.class).clone();
        translationBeanDaoConfig.initIdentityScope(type);

        tTSContentBeanDaoConfig = daoConfigMap.get(TTSContentBeanDao.class).clone();
        tTSContentBeanDaoConfig.initIdentityScope(type);

        translationBeanDao = new TranslationBeanDao(translationBeanDaoConfig, this);
        tTSContentBeanDao = new TTSContentBeanDao(tTSContentBeanDaoConfig, this);

        registerDao(TranslationBean.class, translationBeanDao);
        registerDao(TTSContentBean.class, tTSContentBeanDao);
    }
    
    public void clear() {
        translationBeanDaoConfig.clearIdentityScope();
        tTSContentBeanDaoConfig.clearIdentityScope();
    }

    public TranslationBeanDao getTranslationBeanDao() {
        return translationBeanDao;
    }

    public TTSContentBeanDao getTTSContentBeanDao() {
        return tTSContentBeanDao;
    }

}