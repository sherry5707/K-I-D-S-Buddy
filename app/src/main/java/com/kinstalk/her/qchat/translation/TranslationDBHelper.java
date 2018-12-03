package com.kinstalk.her.qchat.translation;

import android.content.Context;

import com.kinstalk.her.qchat.gen.TranslationBeanDao;
import com.kinstalk.her.qchat.greendao.GreenDaoManager;

import org.greenrobot.greendao.query.Query;

import java.util.List;

public class TranslationDBHelper {

    private static final String TAG = TranslationDBHelper.class.getSimpleName();

    private TranslationBeanDao mDao;

    private static TranslationDBHelper helper;

    private Context mContext;

    private TranslationDBHelper(Context context) {
        this.mContext = context;

        if (mDao == null)
            mDao = GreenDaoManager.getInstance().getNewSession().getTranslationBeanDao();
    }

    /**
     * 提供一个静态方法,如果当前类的对象为空,创建一个新的
     */
    public static TranslationDBHelper getInstance(Context context) {
        if (helper == null) {
            helper = new TranslationDBHelper(context);
        }
        return helper;
    }

    /**
     * 插入翻译bean
     *
     * @param bean
     */
    public void insertTranslationBean(TranslationBean bean) {

        if (bean == null) {
            return;
        }

        if (mDao == null)
            mDao = GreenDaoManager.getInstance().getNewSession().getTranslationBeanDao();

        mDao.insert(bean);

    }

    /**
     * 获取全部数据的个数
     *
     * @return
     */
    public int getAllTranslationListSize() {
        return mDao.loadAll().size();
    }

    /**
     * 获取翻译列表，每次取10条
     *
     * @param id
     * @return
     */
    public List<TranslationBean> getTranslationList(int id) {

        Query<TranslationBean> nQuery = mDao.queryBuilder()
                .where(TranslationBeanDao.Properties.Id.ge(id))
                .limit(10)
                .build();

        List<TranslationBean> list = nQuery.list();

        if (list != null) {
            return list;
        } else {
            return null;
        }

    }

    public void delTranslationData() {
        List<TranslationBean> nQueryList = mDao.queryBuilder()
                .where(TranslationBeanDao.Properties.UserId.eq(1001))
                .build().list();

        for (TranslationBean bean : nQueryList) {
            mDao.delete(bean);
        }
    }

}
