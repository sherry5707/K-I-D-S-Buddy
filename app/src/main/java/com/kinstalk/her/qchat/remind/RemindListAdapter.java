/**
 * created by jiang, 12/3/15
 * Copyright (c) 2015, jyuesong@gmail.com All Rights Reserved.
 * *                #                                                   #
 * #                       _oo0oo_                     #
 * #                      o8888888o                    #
 * #                      88" . "88                    #
 * #                      (| -_- |)                    #
 * #                      0\  =  /0                    #
 * #                    ___/`---'\___                  #
 * #                  .' \\|     |# '.                 #
 * #                 / \\|||  :  |||# \                #
 * #                / _||||| -:- |||||- \              #
 * #               |   | \\\  -  #/ |   |              #
 * #               | \_|  ''\---/''  |_/ |             #
 * #               \  .-\__  '-'  ___/-. /             #
 * #             ___'. .'  /--.--\  `. .'___           #
 * #          ."" '<  `.___\_<|>_/___.' >' "".         #
 * #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 * #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 * #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 * #                       `=---='                     #
 * #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 * #                                                   #
 * #               佛祖保佑         永无BUG              #
 * #                                                   #
 */

package com.kinstalk.her.qchat.remind;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.RemindCustomActivity;
import com.kinstalk.her.qchat.WorkActivity;
import com.kinstalk.her.qchat.activity.SleepActivity;
import com.kinstalk.her.qchat.activity.TaskSleepOrWakeupActivity;
import com.kinstalk.her.qchat.activity.WakeUpActivity;
import com.kinstalk.her.qchat.utils.CountlyConstant;
import com.kinstalk.her.qchat.widget.SwipeItemLayout;
import com.kinstalk.her.qchatcomm.utils.DateUtils;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.util.UIHelp;
import com.kinstalk.her.qchatmodel.entity.HabitKidsEntity;
import com.kinstalk.her.qchatmodel.entity.QReminderMessage;
import com.kinstalk.her.qchatmodel.entity.RemindEntity;
import com.kinstalk.qloveaicore.AIManager;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ly.count.android.sdk.Countly;

public class RemindListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int REMIND_TYPE_NORMAL = 0;
    public static final int REMIND_TYPE_XIAOWEI = 2;
    public static final int REMIND_TYPE_GETUP = 3;
    public static final int REMIND_TYPE_SLEEP = 4;
    public static final int REMIND_TYPE_WORK = 5;
    protected int mHeaderCount;// 头部View个数
    protected int mBottomCount;// 底部View个数

    private String TAG = RemindListAdapter.class.getSimpleName();
    private Context context;
    private RecyclerView recyclerView;
    private LayoutInflater inflater;
    private List<QReminderMessage> dataList = new ArrayList<>();
    private List<QReminderMessage> todoDataList = new ArrayList<>();
    private List<QReminderMessage> doneDataList = new ArrayList<>();
    private List<SwipeItemLayout> mOpenedSil = new ArrayList<>();
    private int mChildCount = 0;

    public RemindListAdapter(Context context, RecyclerView recyclerView) {
        Log.d(TAG, "RemindListAdapter: ");
        this.context = context;
        this.recyclerView = recyclerView;
        mHeaderCount = 0;
        mBottomCount = 0;
    }

    /**
     * s刷新数据
     *
     * @param dataList
     */
    public void refreshData(List<QReminderMessage> dataList) {
        Log.d(TAG, "refreshData: " + dataList.size());
        this.dataList.clear();
        if (null != dataList && dataList.size() > 0) {
            this.dataList.addAll(dataList);
        }
        notifyDataSetChanged();

    }

    public int getContentItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemCount() {
        return dataList.size() + mHeaderCount + mBottomCount;
    }

    public boolean isHeaderView(int position) {
        return mHeaderCount != 0 && position < mHeaderCount;
    }

    public boolean isBottomView(int position) {
        return mBottomCount != 0 && position >= (mHeaderCount + getContentItemCount());
    }

    public void refreshData(List<QReminderMessage> dataList, List<QReminderMessage> doneDataList) {
        Log.d(TAG, "refreshData: ");
        setDataList(dataList, doneDataList);
        notifyDataSetChanged();
    }

    public void setDataList(List<QReminderMessage> dataList, List<QReminderMessage> overDateList) {
        this.dataList.clear();
        if (null != dataList && dataList.size() > 0) {
            Log.d(TAG, "datalist size" + dataList.size());
            this.dataList.addAll(dataList);
        }
        if (null != overDateList && overDateList.size() > 0) {
            this.dataList.addAll(overDateList);
            Log.d(TAG, "dataList.size " +this.dataList.size());
        }
        todoDataList = dataList;
        doneDataList = overDateList;
    }

    public enum ITEM_TYPE {
        ITEM_TYPE_HEADER, ITEM_TYPE_CONTENT, ITEM_TYPE_BOTTOM
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: " + viewType);
        if (viewType == ITEM_TYPE.ITEM_TYPE_HEADER.ordinal()) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.remind_item, parent, false);
            RemindViewHolder remindViewHolder = new RemindViewHolder(v);
            return remindViewHolder;
        } else if (viewType == ITEM_TYPE.ITEM_TYPE_BOTTOM.ordinal()) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.remind_item, parent, false);
            RemindViewHolder remindViewHolder = new RemindViewHolder(v);
            return remindViewHolder;
        } else if (viewType == ITEM_TYPE.ITEM_TYPE_CONTENT.ordinal()) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.remind_item, parent, false);
            RemindViewHolder remindViewHolder = new RemindViewHolder(v);
            return remindViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + position);

        if (holder instanceof RemindListAdapter.RemindViewHolder && null != dataList && dataList.size() > position - mHeaderCount) {
            RemindViewHolder itemHolder = (RemindViewHolder) holder;
            final QReminderMessage reminderMessage = dataList.get(position);
            Long time = reminderMessage.getRemindEntity().getRemind_time();
            if (time > 0) {
                String hhMMSS = DateUtils.getHHmmssTime(time);
                String hourMTime = hhMMSS.substring(0, 5);
                itemHolder.timeView.setText(hourMTime);
            }
            final RemindEntity remindEntity = reminderMessage.getRemindEntity();
            String contentItem = remindEntity.getContent();
            Log.i(TAG, "RemindContent:" + contentItem + position);
            if((!TextUtils.isEmpty(contentItem) && contentItem.length() <= 8) || TextUtils.isEmpty(contentItem)  ) {
                itemHolder.contentView.setText(reminderMessage.getRemindEntity().getContent());
                itemHolder.contentView1.setText(contentItem);
                itemHolder.contentView.setVisibility(View.VISIBLE);
                itemHolder.contentView1.setVisibility(View.GONE);
            } else {
                itemHolder.contentView1.setText(reminderMessage.getRemindEntity().getContent());
                itemHolder.contentView.setText(contentItem);
                itemHolder.contentView.setVisibility(View.GONE);
                itemHolder.contentView1.setVisibility(View.VISIBLE);
            }


            //set UI
            if (remindEntity.getType() > 5) {
                itemHolder.remind_bg.setImageResource(R.drawable.remind_habit);
                isShowStarView(itemHolder, true);
                setStarCount(itemHolder.starViewList, reminderMessage.getRemindEntity().getStatus(), UIHelp.checkHabitSignOrNot(context, remindEntity.getType()));
            } else {
                switch (remindEntity.getType()) {
                    case REMIND_TYPE_NORMAL:
                        itemHolder.remind_bg.setImageResource(R.drawable.remind_normal);
                        isShowStarView(itemHolder, false);
                        break;
                    case REMIND_TYPE_XIAOWEI:
                        itemHolder.remind_bg.setImageResource(R.drawable.xw_reminder);
                        isShowStarView(itemHolder, false);
                        break;
                    case REMIND_TYPE_GETUP:
                        itemHolder.remind_bg.setImageResource(R.drawable.remind_getup);
                        isShowStarView(itemHolder, true);
                        setStarCount(itemHolder.starViewList, reminderMessage.getRemindEntity().getStatus(), UIHelp.checkGetupSignOrNot(context));
                        break;
                    case REMIND_TYPE_SLEEP:
                        itemHolder.remind_bg.setImageResource(R.drawable.remind_sleep);
                        isShowStarView(itemHolder, true);
                        setStarCount(itemHolder.starViewList, reminderMessage.getRemindEntity().getStatus(), UIHelp.checkSleepSignOrNot(context));
                        break;
                    case REMIND_TYPE_WORK:
                        itemHolder.remind_bg.setImageResource(R.drawable.remind_work);
                        isShowStarView(itemHolder, true);
                        setStarCount(itemHolder.starViewList, reminderMessage.getRemindEntity().getStatus(), UIHelp.checkWorkSignOrNot(context));
                        break;
                    default:
                        break;
                }
            }

            //set click
            if ((null != todoDataList && (position > (todoDataList.size() - 1))) || (null == todoDataList)) {
                switch (remindEntity.getType()) {
                    case REMIND_TYPE_NORMAL:
                        itemHolder.remind_bg.setImageResource(R.drawable.remind_gray);
                        break;
                    case REMIND_TYPE_XIAOWEI:
                        itemHolder.remind_bg.setImageResource(R.drawable.xw_reminder_gray);
                        break;
                    case REMIND_TYPE_GETUP:
                        itemHolder.remind_bg.setImageResource(R.drawable.getup_gray);
                        break;
                    case REMIND_TYPE_SLEEP:
                        itemHolder.remind_bg.setImageResource(R.drawable.sleep_gray);
                        break;
                    case REMIND_TYPE_WORK:
                        itemHolder.remind_bg.setImageResource(R.drawable.homework_gray);
                        break;
                    default:
                        itemHolder.remind_bg.setImageResource(R.drawable.habit_gray);
                }
                Log.d(TAG, "setGrayBackground");
                itemHolder.rootLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "置灰不可点");
                    }
                });
            } else {
                if (remindEntity.getType() == REMIND_TYPE_GETUP || remindEntity.getType() == REMIND_TYPE_SLEEP) {
                    itemHolder.rootLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //跳转
                            mStartActivity(remindEntity);
                        }
                    });
                }
            }

            if (remindEntity.getType() == REMIND_TYPE_WORK || remindEntity.getType() > 5) {
                itemHolder.rootLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //跳转
                        mStartActivity(remindEntity);
                    }
                });
            } else if (remindEntity.getType() == REMIND_TYPE_NORMAL || remindEntity.getType() == REMIND_TYPE_XIAOWEI) {
                itemHolder.rootLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //跳转
                        playReminder(remindEntity);
                    }
                });
            }

        }
    }


    public void setStarCount(LinkedList<ImageView> starViewList, int status, int starCount) {

        if (starCount < 0) {
            starCount = 0;
        }
        for (int i = 0; i < 3; i++) {
            ImageView starView = starViewList.get(i);
            starView.setVisibility(View.VISIBLE);
            if (starCount > i) {
                starView.setImageResource(R.drawable.star_post);
            } else if (status > i) {
                starView.setImageResource(R.drawable.star_unposted);
            } else {
                starView.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void isShowStarView(RemindViewHolder itemHolder, boolean isShowStarView) {
        if (isShowStarView) {
 //           itemHolder.contentView.setVisibility(View.VISIBLE);
//            itemHolder.starLayout.setVisibility(View.VISIBLE);
            itemHolder.starImage_1.setVisibility(View.VISIBLE);
            itemHolder.starImage_2.setVisibility(View.VISIBLE);
            itemHolder.starImage_3.setVisibility(View.VISIBLE);
        } else {
//            itemHolder.contentView.setVisibility(View.VISIBLE);
//            itemHolder.starLayout.setVisibility(View.GONE);
            itemHolder.starImage_1.setVisibility(View.GONE);
            itemHolder.starImage_2.setVisibility(View.GONE);
            itemHolder.starImage_3.setVisibility(View.GONE);
        }
    }

    public Long diffTime(Long time) {
        if (time > 0) {
            String hhMMSS = DateUtils.getHHmmssTime(time);
            String yearMD = DateUtils.getYearMonthDay();
            Long time1 = DateUtils.strDateToLong(yearMD + " " + hhMMSS);
            Long curTime = System.currentTimeMillis();
            long diff = time1 - curTime;
            Long min = diff / (60 * 1000); //以分钟为单位取整
            return min;
        }
        return Long.valueOf(0);
    }

    public void mStartActivity(RemindEntity remindEntity) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        int starCount = 0;
        HabitKidsEntity habitKidsEntity = MessageManager.getHabitKidsInfo();
        String name = "宝宝";
        if (habitKidsEntity != null) {
            if (!TextUtils.isEmpty(habitKidsEntity.getNick_name())) {
                name = habitKidsEntity.getNick_name();
            }
        }

        String hhMMSS = DateUtils.getHHmmssTime(remindEntity.getRemind_time());
        String yearMD = DateUtils.getYearMonthDay();
        Long time1 = DateUtils.strDateToLong(yearMD + " " + hhMMSS);

        if (remindEntity.getType() > 5) {
            intent.setClass(context, RemindCustomActivity.class);
            intent.putExtra("habitType", remindEntity.getType());
            intent.putExtra("nick_name", name);
            intent.putExtra("alarm_time", remindEntity.getRemind_time());
            intent.putExtra("content", remindEntity.getContent());
            int localStar = UIHelp.checkHabitSignOrNot(context, remindEntity.getType());
            intent.putExtra("star", localStar > 0 ? localStar: remindEntity.getStatus());
        } else {
            switch (remindEntity.getType()) {
                case REMIND_TYPE_NORMAL:
                    intent = null;
                    break;
                case REMIND_TYPE_XIAOWEI:
                    intent = null;
                    break;
                case REMIND_TYPE_GETUP:
                    starCount = UIHelp.checkGetupSignOrNot(context);
                    if (QchatApplication.isWakeupActive()) {
                        QAILog.d(TAG, "Wakeup active");
                        intent.setClass(context, WakeUpActivity.class);

                    } else if (starCount >= 0) {
                        //已打卡 语音播报
                        intent = null;
                        playTTS("起床已打卡");
                    } else {
                        //未打卡
                        Long diffTime = diffTime(remindEntity.getRemind_time());
                        if ((diffTime > 0) || (diffTime == 0 && !QchatApplication.isSleepingActive())) {
                            //时间未到，进倒计时
                            intent.setClass(context, TaskSleepOrWakeupActivity.class);
                            //起床习惯未打卡
                            Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_calendar_clockin);
                        } else {
                            intent = null;
                            playTTS("起床已打卡");
                        }
                        if (intent != null) {
                            intent.putExtra("sleep", false);
                            intent.putExtra("featureTimeMillis", time1);
                            intent.putExtra("star_1", remindEntity.getStatus());
                            intent.putExtra("nickName", name);
                        }
                    }
                    break;
                case REMIND_TYPE_WORK:
                    intent.setClass(context, WorkActivity.class);
                    intent.putExtra("goHome", false);
                    break;
                case REMIND_TYPE_SLEEP:
                    starCount = UIHelp.checkSleepSignOrNot(context);
                    if (starCount >= 0 && !QchatApplication.isSleepingActive()) {
                        //已打卡 语音播报
                        intent = null;
                        playTTS("睡觉已打卡");
                    } else {
                        //未打卡
                        Long diffTime = diffTime(remindEntity.getRemind_time());
                        if ((diffTime > 0 && (starCount < 0)) || (starCount < 0 && diffTime == 0 && !QchatApplication.isSleepingActive())) {
                            //时间未到，进倒计时
                            intent.setClass(context, TaskSleepOrWakeupActivity.class);
                            Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_calendar_clockin);
                        } else {
                            //时间已到，进详情
                            if (QchatApplication.isSleepingActive()) {
                                Log.d(TAG, "time reached");
                                intent.setClass(context, SleepActivity.class);
                            } else {
                                intent = null;
                                Log.d(TAG, "other scene");
                                playTTS("睡觉已打卡");
                            }
                        }
                        if (intent != null) {
                            intent.putExtra("sleep", true);
                            intent.putExtra("featureTimeMillis", time1);
                            intent.putExtra("star_1", remindEntity.getStatus());
                            intent.putExtra("nickName", name);
                        }
                    }
                    break;
                default:
                    intent = null;
                    break;

            }
        }

        if (null != intent) {
            context.startActivity(intent);
        }
    }

    private void playReminder(RemindEntity remindEntity) {
        String content = "这条提醒的内容是: ";
        if (null == remindEntity) {
            content = content + "空";
        } else {
            String hhMMSS = DateUtils.getHHmmssTime(remindEntity.getRemind_time());
            String hourMTime = hhMMSS.substring(0, 2);
            String minuTime = hhMMSS.substring(3, 5);
            content = content + hourMTime +"点"+ minuTime+"分";
            if (!TextUtils.isEmpty(remindEntity.getContent())) {
                content = content + remindEntity.getContent();
            } else {
                content = content + "提醒我";
            }
        }
        playTTS(content);
    }

    public void playTTS(String content) {
        try {
            AIManager.getInstance(context).playTextWithStr(content, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteItem(RemindViewHolder holder, final QReminderMessage reminderMessage, final int position) {
        SwipeItemLayout swipeRoot = (SwipeItemLayout) holder.rootLayout;
        swipeRoot.setSwipeAble(true);
        swipeRoot.setDelegate(new SwipeItemLayout.SwipeItemLayoutDelegate() {
            @Override
            public void onSwipeItemLayoutOpened(SwipeItemLayout swipeItemLayout) {
                mOpenedSil.add(swipeItemLayout);
            }

            @Override
            public void onSwipeItemLayoutClosed(SwipeItemLayout swipeItemLayout) {
                mOpenedSil.remove(swipeItemLayout);
            }

            @Override
            public void onSwipeItemLayoutStartOpen(SwipeItemLayout swipeItemLayout) {
                closeOpenedSwipeItemLayoutWithAnim();
            }
        });

    }

    public void closeOpenedSwipeItemLayoutWithAnim() {
        for (SwipeItemLayout sil : mOpenedSil) {
            sil.closeWithAnim();
        }
        mOpenedSil.clear();
    }

    public interface OnDeleteListener {
        void onDelete(int index);
    }

    public class RemindViewHolder extends RecyclerView.ViewHolder {
        public ViewGroup rootLayout;
        public ViewGroup bgLayout;
        public TextView timeView;
        public TextView contentView;
        public TextView contentView1;
        public ViewGroup starLayout;
        public ImageView starImage_1;
        public ImageView starImage_2;
        public ImageView starImage_3;
        public ImageView remind_bg;
        public LinkedList<ImageView> starViewList = new LinkedList<>();

        public RemindViewHolder(View itemView) {
            super(itemView);
            rootLayout = (ViewGroup) itemView.findViewById(R.id.remind_item_root);
            bgLayout = (ViewGroup) itemView.findViewById(R.id.remind_item_bg_group);
            timeView = (TextView) itemView.findViewById(R.id.remind_item_time);
            contentView = (TextView) itemView.findViewById(R.id.remind_item_content);
            contentView1 = (TextView) itemView.findViewById(R.id.remind_item_content_line1);
            remind_bg = (ImageView) itemView.findViewById(R.id.remind_bg);
            starLayout = (ViewGroup) itemView.findViewById(R.id.remind_item_star_layout);
            starImage_1 = (ImageView) itemView.findViewById(R.id.remind_item_star_1);
            starImage_2 = (ImageView) itemView.findViewById(R.id.remind_item_star_2);
            starImage_3 = (ImageView) itemView.findViewById(R.id.remind_item_star_3);
            starViewList.add(starImage_1);
            starViewList.add(starImage_2);
            starViewList.add(starImage_3);
        }
    }
}
