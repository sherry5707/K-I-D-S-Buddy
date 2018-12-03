package com.kinstalk.her.qchat.voiceresponse;

import android.content.Context;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.gen.TTSContentBeanDao;
import com.kinstalk.her.qchat.greendao.GreenDaoManager;
import com.kinstalk.her.qchat.utils.SharedPreferencesUtils;
import com.kinstalk.her.qchatcomm.utils.QAILog;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * 播报语音内容 DB helper
 */
public class VoiceContentDBHelper {

    private static final String TAG = VoiceContentDBHelper.class.getSimpleName();

    private TTSContentBeanDao mDao;

    private static VoiceContentDBHelper helper;

    private Context mContext;

    private VoiceContentDBHelper(Context context) {
        this.mContext = context;

        if (mDao == null)
            mDao = GreenDaoManager.getInstance().getNewSession().getTTSContentBeanDao();
    }

    /**
     * 提供一个静态方法,如果当前类的对象为空,创建一个新的
     */
    public static VoiceContentDBHelper getInstance(Context context) {
        if (helper == null) {
            helper = new VoiceContentDBHelper(context);
        }
        return helper;
    }

    /**
     * 根据ID获取语音播报内容
     *
     * @param id
     * @return
     */
    public String getContentById(int id) {

        QAILog.d(TAG, "getContentById id is " + id);

        String result = null;

        if (mDao == null) {
            QAILog.d(TAG, "getContentById mDao is null");
        } else {

            QueryBuilder qb = mDao.queryBuilder();

            qb.where(TTSContentBeanDao.Properties.Id.eq(id));

            List<TTSContentBean> resultQuery = qb.list();

            if (resultQuery != null && resultQuery.size() > 0) {


                TTSContentBean bean = resultQuery.get(0);

                if (bean != null) {
                    result = bean.getContent();
                } else {
                    QAILog.d(TAG, "getContentById TTSContentBean is null");
                }
            }

        }


        return result;
    }

    /**
     * 批量插入数据
     *
     * @param list
     */
    public void insertResoucesContents(List<TTSContentBean> list) {
        if (list != null && list.size() > 0) {
            int size = list.size();

            for (int i = 0; i < size; i++) {
                mDao.insert(list.get(i));
            }

            SharedPreferencesUtils.setSharedPreferencesWithBool(mContext, SharedPreferencesUtils.TTS_CONTENT, SharedPreferencesUtils.HAS_INSERT, true);
        }

    }
}
