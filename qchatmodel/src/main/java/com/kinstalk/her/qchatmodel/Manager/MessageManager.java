package com.kinstalk.her.qchatmodel.Manager;

import android.content.Context;
import android.database.SQLException;
import android.net.Uri;
import android.text.TextUtils;

import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.DateUtils;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.CalenderProviderHelper;
import com.kinstalk.her.qchatmodel.ChatDatabaseHelper;
import com.kinstalk.her.qchatmodel.ChatProvider;
import com.kinstalk.her.qchatmodel.ChatProviderHelper;
import com.kinstalk.her.qchatmodel.Manager.model.cardCallback;
import com.kinstalk.her.qchatmodel.Manager.model.giftCallback;
import com.kinstalk.her.qchatmodel.Manager.model.pkInfoCallback;
import com.kinstalk.her.qchatmodel.Manager.model.taskCallback;
import com.kinstalk.her.qchatmodel.Manager.model.voiceBookCallback;
import com.kinstalk.her.qchatmodel.Manager.util.UIHelp;
import com.kinstalk.her.qchatmodel.entity.CalendarEvent;
import com.kinstalk.her.qchatmodel.entity.CardEntity;
import com.kinstalk.her.qchatmodel.entity.GiftEntity;
import com.kinstalk.her.qchatmodel.entity.HabitEntity;
import com.kinstalk.her.qchatmodel.entity.HabitKidsEntity;
import com.kinstalk.her.qchatmodel.entity.HomeWorkEntity;
import com.kinstalk.her.qchatmodel.entity.PKPetInfo;
import com.kinstalk.her.qchatmodel.entity.PKPropInfo;
import com.kinstalk.her.qchatmodel.entity.PKQuestionInfo;
import com.kinstalk.her.qchatmodel.entity.PKUserInfo;
import com.kinstalk.her.qchatmodel.entity.QHomeworkMessage;
import com.kinstalk.her.qchatmodel.entity.QReminderMessage;
import com.kinstalk.her.qchatmodel.entity.QchatMessage;
import com.kinstalk.her.qchatmodel.Manager.model.homeCallback;
import com.kinstalk.her.qchatmodel.Manager.model.messageCallback;
import com.kinstalk.her.qchatmodel.Manager.model.reminderCallback;
import com.kinstalk.her.qchatmodel.Manager.model.starCallback;
import com.kinstalk.her.qchatmodel.entity.RemindEntity;
import com.kinstalk.her.qchatmodel.entity.TaskEntity;
import com.kinstalk.her.qchatmodel.entity.VoiceBookBean;
import com.kinstalk.her.qchatmodel.entity.WxUserEntity;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Created by tracy on 2018/4/19.
 */

public class MessageManager {

    private static final String TAG = "MessageManager";
    private static List<QHomeworkMessage> listHomework = new ArrayList<>();
    private static List<QchatMessage> listMessage = new ArrayList<>();
    private static List<QHomeworkMessage> prevListHomework = new ArrayList<>();
    private static List<QchatMessage> prevListMessage = new ArrayList<>();
    private static List<QHomeworkMessage> unReadListHomework = new ArrayList<>();
    private static List<homeCallback> mHomeCallbackList = new ArrayList<>();
    private static List<messageCallback> mMessageCallbackList = new ArrayList<>();
    private static List<reminderCallback> mReminderCallbackList = new ArrayList<>();
    private static List<starCallback> mStarCallbackList = new ArrayList<>();
    private static List<String> mChatMsgID = new ArrayList<>();
    //礼物接口
    private static giftCallback mGiftCallback;
    //任务接口
    private static taskCallback mTaskCallback;
    //收藏册接口
    private static List<cardCallback> mCardCallbackList = new ArrayList<>();

    //PK详情接口
    private static List<pkInfoCallback> mPKInfoCallbackList = new ArrayList<>();

    /**
     * 有声书
     */
    private static List<voiceBookCallback> vbCallbackList = new ArrayList<>();

    private static Context mContext;
    private static SecureRandom rnd = new SecureRandom();
    private final static Object mHomeworkLock = new Object();
    private static MessageManager mMessageManager;
    private static final int CHECK_SIZE = 50;

    private MessageManager(Context context) {
        mContext = context;
    }

    public static MessageManager getInstance(Context context) {
        synchronized (MessageManager.class) {
            if (mMessageManager == null) {
                mMessageManager = new MessageManager(context);
            } else {
                QAILog.d(TAG, "all ready created");
            }
            return mMessageManager;
        }
    }

    public static List<QHomeworkMessage> getUnReadHomework() {
        return unReadListHomework;
    }

    public static List<QHomeworkMessage> getListHomework() {
        return listHomework;
    }

    public static boolean chatMsgCheck(String id) {
        boolean mSingle = false;
        if (mChatMsgID.isEmpty() || !mChatMsgID.contains(id)) {
            mChatMsgID.add(id);
            if (mChatMsgID.size() > CHECK_SIZE)
                mChatMsgID.remove(0);
            mSingle = true;
        }
        return mSingle;
    }

    public static void clearAllHomework() {
        synchronized (mHomeworkLock) {
            if (listHomework != null) {
                for (int i = 0; i < listHomework.size(); i++)
                    listHomework.remove(i);
            }
        }

        localDeleteAllHomeworkInDB();
        asyncNotifyHomeworkChange(false);
    }

    public static void updateHabitKidsInfo(HabitKidsEntity entity) {
        ChatProviderHelper.kidsEntity = entity;
    }

    public static HabitKidsEntity getHabitKidsInfo() {
        if (ChatProviderHelper.kidsEntity != null)
            return ChatProviderHelper.kidsEntity;
        else {
            readOutKidsInfoSync();
            return ChatProviderHelper.kidsEntity;
        }
    }

    public static void readOutKidsInfo() {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                List<HabitKidsEntity> records = ChatProviderHelper.HabitKidsInfo.getKidsInfoEntities(null, null);
                if (records != null && records.size() > 0) {
                    for (HabitKidsEntity entity : records) {
                        if (entity != null) {
                            updateHabitKidsInfo(entity);
                        }
                    }
                }
            }
        });
    }

    public static void readOutKidsInfoSync() {
        List<HabitKidsEntity> records = ChatProviderHelper.HabitKidsInfo.getKidsInfoEntities(null, null);
        if (records != null && records.size() > 0) {
            for (HabitKidsEntity entity : records) {
                if (entity != null) {
                    updateHabitKidsInfo(entity);
                }
            }
        }
    }

    public static void getWxUserInfo() {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                QAILog.d("USER", "info1");
                List<WxUserEntity> records = ChatProviderHelper.WxUser.getWxUserEntities(null, null);
                if (records != null && records.size() > 0) {
                    for (WxUserEntity entity : records) {
                        if (entity != null && !TextUtils.isEmpty(entity.openid)) {
                            ChatProviderHelper.mWxUserMap.put(entity.openid, entity);
                        }
                    }
                }
            }
        });
    }

    public static void getWxUserInfoSync() {
        if (ChatProviderHelper.mWxUserMap != null && ChatProviderHelper.mWxUserMap.isEmpty()) {
            QAILog.d("USER", "info");
            List<WxUserEntity> records = ChatProviderHelper.WxUser.getWxUserEntities(null, null);
            if (records != null && records.size() > 0) {
                for (WxUserEntity entity : records) {
                    if (entity != null && !TextUtils.isEmpty(entity.openid)) {
                        ChatProviderHelper.mWxUserMap.put(entity.openid, entity);
                    }
                }
            }
        }
    }

    public static void saveKidsInfoToDB(HabitKidsEntity entity) {
        try {
            deleteItemFromLocalDB(ChatDatabaseHelper.TABLE_KIDINFO, null);
            ArrayList<HabitKidsEntity> kidsListEntity = new ArrayList<>();
            kidsListEntity.add(entity);
            ChatProviderHelper.insertOrUpdateKidsInfo(kidsListEntity);
        } catch (Exception e) {
            QAILog.e(TAG, e);
        }
    }

    public static int getHabitStar(Context context, int type) {
        String selection = "repeat_time NOT NULL AND " + "type =" + type;
        try {
            List<HabitEntity> records = ChatProviderHelper.Habit.getHabitEntities(selection, null);
            if (records != null && records.size() > 0) {
                QAILog.d(TAG, "getStar");
                return records.get(0).getStatus();
            }
        } catch (Exception e) {
            QAILog.d(TAG, e);
        }
        return 3;
    }

    public static List<QchatMessage> getAllChatMessage() {
        List<QchatMessage> records = ChatProviderHelper.Chat.getChatMessages(null, null);
        return records;
    }

    public static void addNewHomeworkList(List<HomeWorkEntity> workList) {
        if (workList != null) {
            for (HomeWorkEntity entity : workList) {
                addNewHomework(entity);
            }
        }
    }

    public static void addNewHomework(HomeWorkEntity work) {
        QHomeworkMessage homeworkMessage = new QHomeworkMessage(getRandomId(), getUser(), work);
        addNewHomework(homeworkMessage);
    }

    public static void addNewHomework(QHomeworkMessage message) {
        int i = searchExistHomework(message);
        synchronized (mHomeworkLock) {
            if (i == -1) {
                listHomework.add(message);
                unReadListHomework.add(message);
            } else {
                listHomework.set(i, message);
            }
        }
    }


    public static int searchExistHomework(QHomeworkMessage msg) {
        for (int i = 0; i < listHomework.size(); i++) {
            if (msg.getHomeWorkEntity().getHomeworkid() == listHomework.get(i).getHomeWorkEntity().getHomeworkid())
                return i;
        }
        return -1;
    }

    public static void wakeup(Context context) {
        UIHelp.wakeUp(context);
    }

    public static void showWorkAlert(Context context, String content, String id) {
        if (!UIHelp.isTopActivityWorkActivity()) {
            UIHelp.sendNewHomeworkBroadcast(context);
            UIHelp.showWorkAlert(context, content, id);
        }
    }

    public static void asyncNotifyHomeworkChange(final boolean status) {
        asyncNotifyHomeworkChange(status, null);
    }

    public static void asyncNotifyHomeworkChange(final boolean status, List<HomeWorkEntity> homeEntity) {
        QAILog.d(TAG, "asyncNotifyHomeworkChange");
        final List<QHomeworkMessage> workMsg = entityToMessage(homeEntity, true);
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                notifyHomeworkChange(status, workMsg);
            }
        });
    }

    public static void asyncNotifyMessageChange(final boolean status) {
        QAILog.d(TAG, "asyncNotifyMessageChange");
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                notifyMessageChange(status);
            }
        });
    }

    public static void asyncNotifyStarChange(final int star) {
        notifyStarChange(star);
    }

    public static void notifyLauncher(Context context) {
        UIHelp.sendNewMessageBroadcast(context);
    }

    public static void notifyStarToLauncher(Context context, int star) {
        UIHelp.sendStarUpdateBroadcast(context, star);
    }

    public static HabitEntity homeHabitOrNot() {
        return ChatProviderHelper.Habit.homeHabitOrNot();
    }

    public static void asyncNotifyReminderChange(final boolean status) {
        QAILog.d(TAG, "asyncNotifyReminderChange");
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                notifyReminderChange(status);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static void sortIntMethod(List list) {
        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                QReminderMessage stu1 = (QReminderMessage) o1;
                QReminderMessage stu2 = (QReminderMessage) o2;
                if (DateUtils.getSortTime(stu1.getRemindEntity().getRemind_time()) > DateUtils.getSortTime(stu2.getRemindEntity().getRemind_time())) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    //Notify reminder change to Launcher
    public static void NotifyReminderToLauncher(final Context context) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                RemindEntity entity = getLauncherNeededReminder(context);
                if (entity != null) {
                    UIHelp.sendReminderUpdateBroadcast(context, entity.getType(), entity.getRemind_time());
                } else {
                    UIHelp.sendReminderUpdateBroadcast(context, -1, 0L);
                }
            }
        });
    }

    public static RemindEntity getLauncherNeededReminder(final Context context) {
        try {
            List<QReminderMessage> msg = getTodayReminderMessage(context);
            ArrayList<Integer> typeArray = new ArrayList<>();
            if (UIHelp.checkSleepSignOrNot(context) != -1) {
                typeArray.add(CalenderProviderHelper.TYPE_SLEEP);
            }
            if (UIHelp.checkWorkSignOrNot(context) != -1) {
                typeArray.add(CalenderProviderHelper.TYPE_WORK);
            }
            if (UIHelp.checkGetupSignOrNot(context) != -1) {
                typeArray.add(CalenderProviderHelper.TYPE_GETUP);
            }
            if (msg != null) {
                Long curHHss = DateUtils.getSortTime(System.currentTimeMillis());
                for (int i = 0; i < msg.size(); i++) {
                    if ((curHHss < DateUtils.getSortTime(msg.get(i).getRemindEntity().getRemind_time())) && (typeArray.isEmpty() || !typeArray.contains(msg.get(i).getRemindEntity().getType()))) {
                        QAILog.d(TAG, "getLauncherNeededReminder " + curHHss + " " + DateUtils.getSortTime(msg.get(i).getRemindEntity().getRemind_time()));
                        if (msg.get(i).getRemindEntity().getType() > 5 && UIHelp.checkHabitSignOrNot(context, msg.get(i).getRemindEntity().getType()) != -1)
                            continue;
                        return msg.get(i).getRemindEntity();
                    }
                }
            }
        } catch (Exception e) {
            QAILog.e(TAG, e);
        }
        return null;
    }

    public static List<QReminderMessage> getTodayReminderMessage(Context context) {
        return getTodayReminderMessage(context, 0);
    }

    //type = 0 獲得當天的提醒/习惯 //type == 1 获得当天仍然有效的提醒习惯 //type == 2 获得当天已过时间的提醒以及不能再打卡的那些习惯。
    public static List<QReminderMessage> getTodayReminderMessage(Context context, int type) {
        List<QReminderMessage> remindList = new ArrayList<>();
        List<QReminderMessage> onetimeRemindList = new ArrayList<>();
        List<QReminderMessage> LocalRemindList = new ArrayList<>();
        List<QReminderMessage> habitRemindList = new ArrayList<>();
        remindList = getRepeatReminderMessage(type);
        onetimeRemindList = getOneTimeReminderMessage(type);
        LocalRemindList = getLocalReminderMessage(context, type);
        habitRemindList = getHabitReminderMessage(context, type);
        try {
            if (remindList != null) {
                if (onetimeRemindList != null)
                    remindList.addAll(onetimeRemindList);
                if (LocalRemindList != null)
                    remindList.addAll(LocalRemindList);
                if (habitRemindList != null)
                    remindList.addAll(habitRemindList);
                sortIntMethod(remindList);
                return remindList;
            } else if (onetimeRemindList != null) {
                if (remindList != null)
                    onetimeRemindList.addAll(remindList);
                if (LocalRemindList != null)
                    onetimeRemindList.addAll(LocalRemindList);
                if (habitRemindList != null)
                    onetimeRemindList.addAll(habitRemindList);
                sortIntMethod(onetimeRemindList);
                return onetimeRemindList;
            } else if (LocalRemindList != null) {
                if (onetimeRemindList != null)
                    LocalRemindList.addAll(onetimeRemindList);
                if (remindList != null)
                    LocalRemindList.addAll(remindList);
                if (habitRemindList != null)
                    LocalRemindList.addAll(habitRemindList);
                sortIntMethod(LocalRemindList);
                return LocalRemindList;
            } else if (habitRemindList != null) {
                if (onetimeRemindList != null)
                    habitRemindList.addAll(onetimeRemindList);
                if (remindList != null)
                    habitRemindList.addAll(remindList);
                if (LocalRemindList != null)
                    habitRemindList.addAll(LocalRemindList);
                sortIntMethod(habitRemindList);
                return habitRemindList;
            } else {
                return null;
            }
        } catch (Exception e) {
            QAILog.e(TAG, e);
            return null;
        }
    }

    //type = 0 獲得當天的小微提醒 //type == 1 获得当天仍然没有到时间的小微提醒 //type == 2 获得当天已过时间的小微提醒。
    public static List<QReminderMessage> getLocalReminderMessage(Context context, int type) {
        List<RemindEntity> records = CalenderProviderHelper.getReminderEvents(context);
        if (records != null) {
            final List<QReminderMessage> msgList = new ArrayList<>();
            long curTime = System.currentTimeMillis();
            long curSortTime = DateUtils.getHHmmSSTime(curTime);
            for (RemindEntity remindEntity : records) {
                long remindTime = remindEntity.getRemind_time();
                long remindSortTime = DateUtils.getHHmmSSTime(remindTime);
                if ((type == 0) || (type == 1 && remindSortTime >= curSortTime) || (type == 2 && remindSortTime < curSortTime)) {
                    QReminderMessage remindMsg = null;
                    if (TextUtils.isEmpty(remindEntity.getContent())) {
                        remindEntity.setContent("提醒");
                    }
                    remindMsg = new QReminderMessage(getUser(), remindEntity);
                    msgList.add(remindMsg);
                }
            }
            return msgList;
        } else
            return null;
    }

    public static List<QReminderMessage> getRepeatReminderMessage() {
        int weekDay = DateUtils.getWeekDay();
        String day = Integer.toString(weekDay);
        QAILog.i(TAG, "week of day" + weekDay);
        try {
            List<RemindEntity> records = ChatProviderHelper.Reminder.getReminderEntities("repeat_time NOT NULL", null);
            if (records != null && records.size() > 0) {
                QAILog.d(TAG, "getRecords" + records.size());
                final List<QReminderMessage> msgList = new ArrayList<>();
                for (RemindEntity remindEntity : records) {
                    QReminderMessage remindMsg = null;
                    if (!TextUtils.isEmpty(remindEntity.content) && !TextUtils.isEmpty(remindEntity.getRepeat_time()) && remindEntity.getRepeat_time().contains(day)) {
                        remindMsg = new QReminderMessage(getUser(), remindEntity);
                        msgList.add(remindMsg);
                    }
                }
                return msgList;
            }
        } catch (SQLException e) {
            QAILog.w(TAG, e);
        }
        return null;
    }

    //type = 0 獲得當天的重複提醒 //type == 1 获得当天仍然没有到时间的重复提醒 //type == 2 获得当天已过时间的重复提醒。
    public static List<QReminderMessage> getRepeatReminderMessage(int type) {
        int weekDay = DateUtils.getWeekDay();
        String day = Integer.toString(weekDay);
        QAILog.i(TAG, "week of day" + weekDay);
        try {
            List<RemindEntity> records = ChatProviderHelper.Reminder.getReminderEntities("repeat_time NOT NULL", null);
            if (records != null && records.size() > 0) {
                QAILog.d(TAG, "getRecords" + records.size());
                final List<QReminderMessage> msgList = new ArrayList<>();
                long curTime = System.currentTimeMillis();
                long curSortTime = DateUtils.getHHmmSSTime(curTime);
                for (RemindEntity remindEntity : records) {
                    QReminderMessage remindMsg = null;
                    long remindTime = remindEntity.getRemind_time();
                    long remindSortTime = DateUtils.getHHmmSSTime(remindTime);
                    if ((type == 1) && (remindSortTime >= curSortTime) && !TextUtils.isEmpty(remindEntity.content) && !TextUtils.isEmpty(remindEntity.getRepeat_time()) && remindEntity.getRepeat_time().contains(day)) {
                        remindMsg = new QReminderMessage(getUser(), remindEntity);
                        msgList.add(remindMsg);
                    }
                    if ((type == 2) && (curSortTime > remindSortTime) && !TextUtils.isEmpty(remindEntity.content) && !TextUtils.isEmpty(remindEntity.getRepeat_time()) && remindEntity.getRepeat_time().contains(day)) {
                        remindMsg = new QReminderMessage(getUser(), remindEntity);
                        msgList.add(remindMsg);
                    }
                    if ((type == 0) && !TextUtils.isEmpty(remindEntity.content) && !TextUtils.isEmpty(remindEntity.getRepeat_time()) && remindEntity.getRepeat_time().contains(day)) {
                        remindMsg = new QReminderMessage(getUser(), remindEntity);
                        msgList.add(remindMsg);
                    }
                }
                return msgList;
            }
        } catch (SQLException e) {
            QAILog.w(TAG, e);
        }
        return null;
    }

    //get All Reminder List
    //type = 0 獲得當天的单次提醒 //type == 1 获得当天仍然没有到时间的单次提醒 //type == 2 获得当天已过时间的单次提醒。
    public static List<QReminderMessage> getOneTimeReminderMessage(int type) {
        Long beginTime = DateUtils.getToadyBeginTime();
        Long endTime = DateUtils.getTodayEndTime();
        Long curTime = System.currentTimeMillis();
        if (type == 1)
            beginTime = curTime;
        else if (type == 2)
            endTime = curTime;
        String selection = "repeat_time IS NULL and " + "reminder_time <" + endTime + " and " + "reminder_time >" + beginTime;
        QAILog.d(TAG, "begintime" + beginTime + "endtime" + endTime);
        try {
            List<RemindEntity> records = ChatProviderHelper.Reminder.getReminderEntities(selection, null);
            if (records != null && records.size() > 0) {
                QAILog.d(TAG, "getRecords" + records.size());
                final List<QReminderMessage> msgList = new ArrayList<>();
                for (RemindEntity remindEntity : records) {
                    QReminderMessage remindMsg = null;
                    if (!TextUtils.isEmpty(remindEntity.content)) {
                        remindMsg = new QReminderMessage(getUser(), remindEntity);
                        msgList.add(remindMsg);
                    }
                }
                return msgList;
            }
        } catch (SQLException e) {
            QAILog.w(TAG, e);
        }
        return null;
    }

    //get All Reminder List
    //type = 0 獲得當天的习惯 //type == 1 获得当天未打卡的自定义习惯以及 未到时间的系统习惯 //type == 2 获得当天已打卡的习惯或者已过时间的起床睡觉
    //getAllHabit
    public static List<QReminderMessage> getHabitReminderMessage(Context context, int type) {
        int weekDay = DateUtils.getWeekDay();
        Long millis = System.currentTimeMillis();
        String day = Integer.toString(weekDay);
        String selection = "repeat_time NOT NULL AND " + "end_time >" + millis + " AND add_time < " + millis;
        QAILog.i(TAG, "week of day" + weekDay + "current " + millis);
        try {
            List<RemindEntity> records = ChatProviderHelper.Habit.getHabitReminderEntities(selection, null);
            if (records != null && records.size() > 0) {
                QAILog.d(TAG, "getRecords" + records.size());
                final List<QReminderMessage> msgList = new ArrayList<>();
                for (RemindEntity remindEntity : records) {
                    QReminderMessage remindMsg = null;
                    QAILog.d(TAG, "repeat: " + remindEntity.getRepeat_time());
                    if ((type == 0) && !TextUtils.isEmpty(remindEntity.getRepeat_time()) && remindEntity.getRepeat_time().contains(day)) {
                        remindMsg = new QReminderMessage(getUser(), remindEntity);
                        msgList.add(remindMsg);
                    } else if ((type == 1) && !TextUtils.isEmpty(remindEntity.getRepeat_time()) && remindEntity.getRepeat_time().contains(day)) {
                        int habitType = remindEntity.getType();
                        long curSortTime = DateUtils.getHHmmSSTime(millis);
                        long remindSortTime = DateUtils.getHHmmSSTime(remindEntity.getRemind_time());
                        if ((habitType > CalenderProviderHelper.TYPE_WORK && (UIHelp.checkHabitSignOrNot(context, habitType) < 0)) ||
                                ((habitType == CalenderProviderHelper.TYPE_GETUP) && ((UIHelp.isWakeupActive() && UIHelp.checkHabitSignOrNot(context, habitType) == -1) ||
                                        ((curSortTime <= remindSortTime) && (UIHelp.checkHabitSignOrNot(context, habitType) < 0)))) ||
                                ((habitType == CalenderProviderHelper.TYPE_WORK) && (UIHelp.checkHabitSignOrNot(context, habitType) < 0)) ||
                                (habitType == CalenderProviderHelper.TYPE_SLEEP && (UIHelp.isSleepingActive() || (curSortTime <= remindSortTime && UIHelp.checkHabitSignOrNot(context, habitType) < 0)))) {
                            QAILog.d(TAG, "active habit " + habitType);
                            if (UIHelp.isSleepingActive())
                                QAILog.d(TAG, "sleep active");
                            remindMsg = new QReminderMessage(getUser(), remindEntity);
                            msgList.add(remindMsg);
                        }
                    } else if ((type == 2) && !TextUtils.isEmpty(remindEntity.getRepeat_time()) && remindEntity.getRepeat_time().contains(day)) {
                        int habitType = remindEntity.getType();
                        long curSortTime = DateUtils.getHHmmSSTime(millis);
                        long remindSortTime = DateUtils.getHHmmSSTime(remindEntity.getRemind_time());
                        if (((habitType > CalenderProviderHelper.TYPE_WORK) && (UIHelp.checkHabitSignOrNot(context, habitType) >= 0)) ||
                                ((habitType == CalenderProviderHelper.TYPE_WORK) && (UIHelp.checkHabitSignOrNot(context, habitType) >= 0)) ||
                                ((habitType == CalenderProviderHelper.TYPE_GETUP) &&
                                        ((!UIHelp.isWakeupActive() && (curSortTime > remindSortTime)) || (UIHelp.checkHabitSignOrNot(context, habitType) >= 0))) ||
                                ((habitType == CalenderProviderHelper.TYPE_SLEEP) && (!UIHelp.isSleepingActive()) && ((curSortTime > remindSortTime || (UIHelp.checkHabitSignOrNot(context, habitType) >= 0))))) {
                            QAILog.d(TAG, "already signed or posted " + habitType);
                            remindMsg = new QReminderMessage(getUser(), remindEntity);
                            msgList.add(remindMsg);
                        }
                    }
                }
                return msgList;
            }
        } catch (SQLException e) {
            QAILog.w(TAG, e);
        }
        return null;
    }

    public static int getHabitDurationTillNow(int type) {
        int duration = 0;
        String selection = "type is  " + type;
        QAILog.i(TAG, "type is" + type);
        try {
            List<HabitEntity> records = ChatProviderHelper.Habit.getHabitEntities(selection, null);
            if (records != null && records.size() > 0) {
                QAILog.d(TAG, "getRecords" + records.size());
                for (HabitEntity habitEntity : records) {
                    duration += getDuration(habitEntity);
                }
            }
        } catch (SQLException e) {
            QAILog.w(TAG, e);
        }
        return duration;
    }

    private static int getDuration(HabitEntity habitEntity) {
        int duration = 0;
        try {
            Long start_time = habitEntity.getAssign_time();
            int startWeekDay = DateUtils.getWeekOfDate(start_time);
            Long end_time = DateUtils.getTodayEndTime();
            int endWeekDay = DateUtils.getWeekOfDate(end_time);
            String repeat = habitEntity.getRepeat_time();
            String[] repeatDay = repeat.split(",");
            int repeatDuration = repeatDay.length;
            int wholeDuraiton = (int) (end_time - start_time) / (24 * 3600 * 1000);
            QAILog.d(TAG, "wholeDuration is" + wholeDuraiton + "startWeekDay " + startWeekDay + " endWeekDay " + endWeekDay);

            if (wholeDuraiton >= 7) {
                int week = wholeDuraiton / 7;
                duration += repeatDuration * week;
            }
            if (startWeekDay == endWeekDay && repeat.contains(Integer.toString(startWeekDay))) {
                duration += 1;
            } else if (startWeekDay < endWeekDay) {
                for (int i = startWeekDay; i <= endWeekDay; i++) {
                    if (repeat.contains(Integer.toString(i)))
                        duration += 1;
                }
                QAILog.d(TAG, "Duration is now" + duration);
            } else if (endWeekDay < startWeekDay) {
                for (int i = startWeekDay; i <= 7; i++) {
                    if (repeat.contains(Integer.toString(i)))
                        duration += 1;
                }
                for (int i = 1; i <= endWeekDay; i++) {
                    if (repeat.contains(Integer.toString(i)))
                        duration += 1;
                }
                QAILog.d(TAG, "Duration is" + duration);
            }
        } catch (Exception e) {
            QAILog.e(TAG, e);
        }
        QAILog.d(TAG, "duration " + duration);
        return duration;
    }

    public static void clearExpireHabit(Context context) {
        Long millis = System.currentTimeMillis();
        try {
            List<HabitEntity> records = ChatProviderHelper.Habit.getHabitEntities(null, null);
            if (records != null) {
                int size = records.size();
                for (int i = 0; i < size; i++) {
                    if (records.get(i).end_time < millis) {
                        int type = records.get(i).getType();
                        deleteHabit(context, type);
                    }
                }
            }
        } catch (Exception e) {
            QAILog.e(TAG, e);
        }
    }

    public static int getHabitType(Long beginTime) {
        try {
            List<HabitEntity> records = ChatProviderHelper.Habit.getHabitEntities(null, null);
            if (records != null) {
                int size = records.size();
                for (int i = 0; i < size; i++) {
                    if (DateUtils.getSortTime(records.get(i).getRemind_time().longValue()) == DateUtils.getSortTime(beginTime).longValue()) {
                        int type = records.get(i).getType();
                        return type;
                    }
                }
            }
        } catch (Exception e) {
            QAILog.e(TAG, e);
        }
        return -1;
    }

    public static List<CalendarEvent> getXiaoWeiReminder(Context context) {
        return CalenderProviderHelper.getEvents(context);
    }

    public static List<QHomeworkMessage> getActiveHomeworkMessage() {
        final List<QHomeworkMessage> msgList = new ArrayList<>();
        msgList.addAll(listHomework);
        return msgList;
    }

    private static List<QHomeworkMessage> entityToMessage(List<HomeWorkEntity> records, boolean unRead) {
        final List<QHomeworkMessage> msgList = new ArrayList<>();
        if (records == null)
            return null;
        try {
            for (HomeWorkEntity homeworkEntity : records) {
                QAILog.d(TAG, "value is" + homeworkEntity.toString());
                QHomeworkMessage homeworkMessage = null;
                if (!TextUtils.isEmpty(homeworkEntity.content) || (!TextUtils.isEmpty(homeworkEntity.getUrl()) && (unRead || homeworkEntity.getUrl().contains("sdcard")))) {
                    homeworkMessage = new QHomeworkMessage(getRandomId(), getUser(), homeworkEntity);
                }

                if (homeworkMessage != null) {
                    homeworkMessage.setCreatedAt(new Date(homeworkEntity.assign_time));
                    homeworkMessage.setDirection(ChatProviderHelper.DIRECTION_IN);
                    homeworkMessage.setText(homeworkEntity.content);
                    msgList.add(homeworkMessage);
                }
            }
            if (msgList != null) {
                Collections.reverse(msgList);
            }
        } catch (Exception e) {
            QAILog.e(TAG, e);
        }
        return msgList;
    }

    public static void addNewMessage(QchatMessage message) {
        if (message != null)
            listMessage.add(message);
        if (listMessage.size() > 200) {
            listMessage.remove(0);
        }
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                notifyMessageChange(true);
            }
        });
    }

    // getUnreadHomework
    public static List<QHomeworkMessage> getHomeworkMessage(Long time) {
        if (time == 0) {
            return null;
        }
        String selection = "assign_time >" + time;
        List<HomeWorkEntity> records = ChatProviderHelper.Homework.getHomeWorkEntities(selection, null);
        if (records != null && records.size() > 0) {
            return entityToMessage(records, true);
        }
        return null;
    }

    //getAllHomeworkList
    public static List<QHomeworkMessage> getAllHomeworkMessage() {
        Long beginTime = DateUtils.getToadyBeginTime();
        Long endTime = DateUtils.getTodayEndTime();
        String selection = "assign_time <" + endTime + " and " + "assign_time >" + beginTime;
        List<HomeWorkEntity> records = ChatProviderHelper.Homework.getHomeWorkEntities(selection, null);
        if (records != null && records.size() > 0) {
            return entityToMessage(records, false);
        }
        return null;
    }

    public static void registerHomeworkCallback(homeCallback mCallback) {
        if (mCallback != null) {
            if (mHomeCallbackList != null) {
                int size = mHomeCallbackList.size();
                for (int i = 0; i < size; i++) {
                    if (mHomeCallbackList.get(i).equals(mCallback)) {
                        mHomeCallbackList.remove(i);
                    }
                }
            }
            mHomeCallbackList.add(mCallback);
        }
    }

    public static void registerMessageCallback(messageCallback mCallback) {
        if (mCallback != null) {
            if (mMessageCallbackList != null) {
                int size = mMessageCallbackList.size();
                for (int i = 0; i < size; i++) {
                    if (mMessageCallbackList.get(i).equals(mCallback)) {
                        mMessageCallbackList.remove(i);
                    }
                }
            }
            mMessageCallbackList.add(mCallback);
        }
    }

    public static void registerReminderCallback(reminderCallback mCallback) {
        if (mCallback != null) {
            mReminderCallbackList.add(mCallback);
        }
    }

    public static void registerStarCallback(starCallback mCallback) {
        if (mCallback != null) {
            mStarCallbackList.add(mCallback);
        }
    }

    public static void unRegisterHomeworkCallback(homeCallback mCallback) {
        if (!mHomeCallbackList.isEmpty() && mCallback != null) {
            if (mHomeCallbackList.contains(mCallback)) {
                mHomeCallbackList.remove(mCallback);
            }
        }
    }

    public static void unRegisterMessageCallback(messageCallback mCallback) {
        if (!mMessageCallbackList.isEmpty() && mCallback != null) {
            if (mMessageCallbackList.contains(mCallback)) {
                mMessageCallbackList.remove(mCallback);
            }
        }
    }

    public static void unRegisterReminderCallback(reminderCallback mCallback) {
        if (!mReminderCallbackList.isEmpty() && mCallback != null) {
            if (mReminderCallbackList.contains(mCallback)) {
                mReminderCallbackList.remove(mCallback);
            }
        }
    }

    public static void unRegisterStarCallback(starCallback mCallback) {
        if (!mStarCallbackList.isEmpty() && mCallback != null) {
            if (mStarCallbackList.contains(mCallback)) {
                mStarCallbackList.remove(mCallback);
            }
        }
    }

    public static void unRegisterAllCallback() {
        if (!mHomeCallbackList.isEmpty()) {
            for (homeCallback mCallback : mHomeCallbackList) {
                mHomeCallbackList.remove(mCallback);
            }
        }
        if (!mMessageCallbackList.isEmpty()) {
            for (messageCallback mCallback : mMessageCallbackList) {
                mMessageCallbackList.remove(mCallback);
            }
        }
        if (!mReminderCallbackList.isEmpty()) {
            for (reminderCallback mCallback : mReminderCallbackList) {
                mReminderCallbackList.remove(mCallback);
            }
        }
        if (!mStarCallbackList.isEmpty()) {
            for (starCallback mCallback : mStarCallbackList) {
                mStarCallbackList.remove(mCallback);
            }
        }
    }

    private static void localDeleteAllHomeworkInDB() {
        QAILog.d(TAG, "localDeleteAllHomeworkInDB");
        deleteItemFromLocalDB(ChatDatabaseHelper.TABLE_HOMEWORK, null);
        asyncNotifyHomeworkChange(false);
    }

    public static void deleteLocalReminder(Context context, String msg_id) {
        QAILog.d(TAG, "deleteLocalReminder");
        CalenderProviderHelper.deleteEvent(context, msg_id, CalenderProviderHelper.TYPE_XIAOWEI);
        asyncNotifyReminderChange(false);
    }

    public static void deleteReminder(Context context, String msg_id) {
        QAILog.d(TAG, "deleteReminder");
        deleteItemFromLocalDB(ChatDatabaseHelper.TABLE_REMINDER, msg_id);
        CalenderProviderHelper.deleteEvent(context, msg_id, CalenderProviderHelper.TYPE_NORMAL);
        asyncNotifyReminderChange(false);
    }

    public static void deleteHomework(Context context, String id) {
        QAILog.d(TAG, "del homework");
        deleteItemFromLocalDB(ChatDatabaseHelper.TABLE_HOMEWORK, id);
        deleteHomeworkFromList(id);
        asyncNotifyHomeworkChange(false);
    }

    public static void deleteHabit(Context context, int type) {
        QAILog.d(TAG, "del habit");
        deleteItemFromLocalDB(ChatDatabaseHelper.TABLE_HABIT, type);
        if (type == CalenderProviderHelper.TYPE_SLEEP || type == CalenderProviderHelper.TYPE_GETUP) {
            CalenderProviderHelper.deleteAllEvents(context, type);
        } else if (type > CalenderProviderHelper.TYPE_WORK) {
            CalenderProviderHelper.deleteEvent(context, Integer.toString(type), CalenderProviderHelper.TYPE_SELFHABIT);
            CalenderProviderHelper.deleteEvent(context, Integer.toString(type + CalenderProviderHelper.TYPE_INTERVAL), CalenderProviderHelper.TYPE_SELFHABIT);
        }
    }

    public static void deleteHabitFromLocalDB(int type) {
        QAILog.d(TAG, "del habit");
        deleteItemFromLocalDB(ChatDatabaseHelper.TABLE_HABIT, type);
    }

    public static void deleteHomeworkFromList(String id) {
        int index = -1;
        for (int i = 0; i < listHomework.size(); i++) {
            if (!TextUtils.isEmpty(id) && id.equals(listHomework.get(i).getHomeWorkEntity().getHomeworkid())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            synchronized (mHomeworkLock) {
                listHomework.remove(index);
            }
        }
    }

    public static void eraseAllDB(final Context context) {
        try {
            ChatProvider mChatProvider = new ChatProvider();
            mChatProvider.delete(Uri.withAppendedPath(ChatProviderHelper.CONTENT_URI,
                    "/homework"), null, null);
            mChatProvider.delete(Uri.withAppendedPath(ChatProviderHelper.CONTENT_URI,
                    "/reminder"), null, null);
            mChatProvider.delete(Uri.withAppendedPath(ChatProviderHelper.CONTENT_URI,
                    "/chat"), null, null);
            mChatProvider.delete(Uri.withAppendedPath(ChatProviderHelper.CONTENT_URI,
                    "/habit"), null, null);
            mChatProvider.delete(Uri.withAppendedPath(ChatProviderHelper.CONTENT_URI,
                    "/wxuser"), null, null);
            mChatProvider.delete(Uri.withAppendedPath(ChatProviderHelper.CONTENT_URI,
                    "/kidsinfo"), null, null);
            CalenderProviderHelper.deleteAllEvents(context);
            asyncNotifyHomeworkChange(false, null);
            asyncNotifyReminderChange(false);
            asyncNotifyMessageChange(false);
        } catch (Exception e) {
            QAILog.e(TAG, e);
        }
    }

    public static int deleteItemFromLocalDB(String type, int id) {
        String path_setment = "/" + type;
        Uri uri = Uri.withAppendedPath(ChatProviderHelper.CONTENT_URI,
                path_setment);
        String selection = "";
        try {
            if (type != null) {
                switch (type) {
                    case ChatDatabaseHelper.TABLE_HOMEWORK:
                        selection = ChatProviderHelper.Homework.COLUMN_HOMEWORKID + " IS " + id;
                        break;
                    case ChatDatabaseHelper.TABLE_REMINDER:
                        selection = ChatProviderHelper.Reminder.COLUMN_REMINDER_ID + " IS " + id;
                        break;
                    case ChatDatabaseHelper.TABLE_HABIT:
                        selection = ChatProviderHelper.Habit.COLUMN_TYPE + " IS " + id;
                        break;
                    default:
                        break;

                }

            } else if (type == null) {
                selection = null;
            }
            return accessChatProvider(uri, selection);
        } catch (Exception e) {
            QAILog.e(TAG, e);
        }
        return -1;
    }

    public static int deleteItemFromLocalDB(String type, String id) {
        String path_setment = "/" + type;
        Uri uri = Uri.withAppendedPath(ChatProviderHelper.CONTENT_URI,
                path_setment);
        String selection = "";
        try {
            if (type != null && id != null) {
                switch (type) {
                    case ChatDatabaseHelper.TABLE_HOMEWORK:
                        selection = ChatProviderHelper.Homework.COLUMN_HOMEWORKID + " IS " + id;
                        break;
                    case ChatDatabaseHelper.TABLE_REMINDER:
                        selection = ChatProviderHelper.Reminder.COLUMN_REMINDER_ID + " IS " + id;
                        break;
                    case ChatDatabaseHelper.TABLE_HABIT:
                        selection = ChatProviderHelper.Habit.COLUMN_HABIT_ID + " IS " + id;
                        break;
                    default:
                        break;

                }
            } else if (type != null) {
                selection = null;
            }
            return accessChatProvider(uri, selection);
        } catch (Exception e) {
            QAILog.d(TAG, e);
        }
        return -1;
    }

    private static int accessChatProvider(Uri uri, String selection) {
        int num = 0;
        QAILog.d(TAG, uri);
        ChatProvider mChatProvider = new ChatProvider();
        try {
            num = mChatProvider.delete(uri, selection, null);
        } catch (SQLException e) {
            QAILog.d(TAG, "AccessChatProvide" + uri + e);
        }
        return num;
    }

    private static String getRandomId() {
        return Long.toString(UUID.randomUUID().getLeastSignificantBits());
    }

    public static WxUserEntity getUser() {
        List<WxUserEntity> records = ChatProviderHelper.WxUser.getWxUserEntities(null, null);
        if (records != null && records.size() > 0) {
            return records.get(0);
        }
        return null;
    }

    public static void saveIncomingMsg(final Context context, final String openid, final String msg, final String res_download_path, final String msg_id, final int message_type, final int duration) {
        ChatProviderHelper.saveIncomingMsg(context, openid, msg, res_download_path, msg_id, message_type, duration);
    }

    public static void saveOutcomingMsg(final Context context, final String msg, final String audio_local_path, final String audio_download_path, final int message_type, final int duration) {
        ChatProviderHelper.saveOutcomingMsg(context, msg, audio_local_path, audio_download_path, message_type, duration);
        notifyMessageChange(true);
    }

    public static void notifyHomeworkChange(boolean status, List<QHomeworkMessage> records) {
        for (homeCallback mCallback : mHomeCallbackList) {
            mCallback.onHomeworkChanged(status, records);
        }
    }

    public static void notifyMessageChange(boolean status) {
        for (messageCallback mCallback : mMessageCallbackList) {
            mCallback.onMessageChanged(status);
        }
    }

    public static void notifyReminderChange(boolean status) {
        for (reminderCallback mCallback : mReminderCallbackList) {
            mCallback.onReminderChanged(status);
        }
    }

    public static void notifyStarChange(final int star) {
        for (starCallback mCallback : mStarCallbackList) {
            mCallback.onStarChanged(star);
        }
    }

    //礼物
    public static void registerGiftCallback(giftCallback call) {
        mGiftCallback = call;
    }

    public static void unRegisterGiftCallback() {
        mGiftCallback = null;
    }

    public static void addGiftList(final List<GiftEntity> receiveList, final List<GiftEntity> receivedList, final int star) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                mGiftCallback.onGiftChanged(receiveList, receivedList, star);
            }
        });
    }

    public static void addGiftListError(final int code) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                mGiftCallback.onGiftGetError(code);
            }
        });
    }

    public static void receiveGift(final List<GiftEntity> receiveList, final List<GiftEntity> receivedList, final int star, final String action) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                mGiftCallback.onGiftReceive(receiveList, receivedList, star, action);
            }
        });
    }

    public static void receiveGiftError(final int code) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                mGiftCallback.onGiftReceiveError(code);
            }
        });
    }

    //任务
    public static void registerTaskCallback(taskCallback call) {
        mTaskCallback = call;
    }

    public static void unRegisterTaskCallback() {
        mTaskCallback = null;
    }

    public static void addTaskList(final List<TaskEntity> receiveList, final int credit) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                mTaskCallback.onTaskChanged(receiveList, credit);
            }
        });
    }

    public static void addTaskListError(final int code) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                mTaskCallback.onTaskGetError(code);
            }
        });
    }

    public static void receiveTask(final List<TaskEntity> receiveList, final int credit, final String action) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                mTaskCallback.onTaskReceive(receiveList, credit, action);
            }
        });
    }

    public static void receiveTaskError(final int code) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                mTaskCallback.onTaskReceiveError(code);
            }
        });
    }

    //PK详情回调
    public static void registerPKInfoCallback(pkInfoCallback callBack) {
        if (null != callBack) {
            if (mPKInfoCallbackList.size() > 0) {
                for (int i = 0; i < mPKInfoCallbackList.size(); i++) {
                    if (mPKInfoCallbackList.get(i).equals(callBack)) {
                        mPKInfoCallbackList.remove(i);
                    }
                }
            }
            mPKInfoCallbackList.add(callBack);
        }
    }

    public static void unRegisterPKInfoCallback(pkInfoCallback callBack) {
        if (mPKInfoCallbackList.contains(callBack)) {
            mPKInfoCallbackList.remove(callBack);
        }
    }

    public static void addPKInfoList(final List<PKPetInfo> petList, final PKUserInfo user) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (pkInfoCallback mPKInfoCallback : mPKInfoCallbackList) {
                    mPKInfoCallback.onPKInfoChanged(petList, user);
                }
            }
        });
    }

    public static void addPKInfoListError(final int code) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (pkInfoCallback mPKInfoCallback : mPKInfoCallbackList) {
                    mPKInfoCallback.onPKInfoGetError(code);
                }
            }
        });
    }

    public static void addPKChoosePet(final int id) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (pkInfoCallback mPKInfoCallback : mPKInfoCallbackList) {
                    mPKInfoCallback.onPKChoosePet(id);
                }
            }
        });
    }

    public static void addPKChoosePetError(final int code) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (pkInfoCallback mPKInfoCallback : mPKInfoCallbackList) {
                    mPKInfoCallback.onPKChoosePetError(code);
                }
            }
        });
    }

    public static void addPKReward(final int credit, final List<PKPropInfo> propInfos, final List<CardEntity> cardEntities, final int ranking) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (pkInfoCallback mPKInfoCallback : mPKInfoCallbackList) {
                    mPKInfoCallback.onPKRewardChanged(credit, propInfos, cardEntities, ranking);
                }
            }
        });
    }

    public static void addPKRewardError(final int code) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (pkInfoCallback mPKInfoCallback : mPKInfoCallbackList) {
                    mPKInfoCallback.onPKRewardGetError(code);
                }
            }
        });
    }

    public static void addPKQuestion(final List<PKQuestionInfo> questionList, final List<PKPropInfo> propList) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (pkInfoCallback mPKInfoCallback : mPKInfoCallbackList) {
                    mPKInfoCallback.onPKQuestionChanged(questionList, propList);
                }
            }
        });
    }

    public static void addPKQuestionError(final int code) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (pkInfoCallback mPKInfoCallback : mPKInfoCallbackList) {
                    mPKInfoCallback.onPKQuestionGetError(code);
                }
            }
        });
    }

    public static void usePropSuccess(final PKPropInfo pkPropInfo) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (pkInfoCallback mPKInfoCallback : mPKInfoCallbackList) {
                    mPKInfoCallback.onPKPropUse(pkPropInfo);
                }
            }
        });
    }

    public static void usePropError(final int code) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (pkInfoCallback mPKInfoCallback : mPKInfoCallbackList) {
                    mPKInfoCallback.onPKPropUseError(code);
                }
            }
        });
    }

    //卡片回调
    public static void registerCardCallback(cardCallback callBack) {
        if (null != callBack) {
            if (mCardCallbackList.size() > 0) {
                for (int i = 0; i < mCardCallbackList.size(); i++) {
                    if (mCardCallbackList.get(i).equals(callBack)) {
                        mCardCallbackList.remove(i);
                    }
                }
            }
            mCardCallbackList.add(callBack);
        }
    }

    public static void unRegisterCardCallback(cardCallback callBack) {
        if (mCardCallbackList.contains(callBack)) {
            mCardCallbackList.remove(callBack);
        }
    }

    public static void addCardList(final List<CardEntity> receiveList, final List<CardEntity> propList, final int credit, final int hasCardNum) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (cardCallback mCardCallback : mCardCallbackList) {
                    mCardCallback.onCardChanged(receiveList, propList, credit, hasCardNum);
                }
            }
        });
    }

    public static void addCardListError(final int code) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (cardCallback mCardCallback : mCardCallbackList) {
                    mCardCallback.onCardGetError(code);
                }
            }
        });
    }

    public static void receiveCard(final int credit, final int id, final int type) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (cardCallback mCardCallback : mCardCallbackList) {
                    mCardCallback.onCardReceive(credit, id, type);
                }
            }
        });
    }

    public static void receiveCardError(final int code) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (cardCallback mCardCallback : mCardCallbackList) {
                    mCardCallback.onCardReceiveError(code);
                }
            }
        });
    }

    public static void shareCard(final int credit, final int id, final int type) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (cardCallback mCardCallback : mCardCallbackList) {
                    mCardCallback.onShareReceive(credit, id, type);
                }
            }
        });
    }

    public static void shareCardError(final int code) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (cardCallback mCardCallback : mCardCallbackList) {
                    mCardCallback.onShareReceiveError(code);
                }
            }
        });
    }

    public static void registerVoiceBookCallback(voiceBookCallback callback) {

        if (null != callback) {
            if (vbCallbackList.size() > 0) {
                for (int i = 0; i < vbCallbackList.size(); i++) {
                    if (vbCallbackList.get(i).equals(callback)) {
                        vbCallbackList.remove(i);
                    }
                }
            }
            vbCallbackList.add(callback);
        }

    }

    public static void unRegisterVoiceBookCallback(voiceBookCallback callback) {
        if (vbCallbackList.contains(callback)) {
            vbCallbackList.remove(callback);
        }
    }

    public static void getVoiceBookListSuccess(final List<VoiceBookBean> list) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (voiceBookCallback callback : vbCallbackList) {

                    QAILog.d(TAG,"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

                    callback.getVoiceBookListSuccess(list);
                }
            }
        });
    }

    public static void getVoiceBookListFail(final int errorCode, final String errorMsg) {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                for (voiceBookCallback callback : vbCallbackList) {
                    callback.getVoiceBookListFail(errorCode, errorMsg);
                }
            }
        });
    }
}
