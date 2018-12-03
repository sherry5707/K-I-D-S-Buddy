package com.kinstalk.her.qchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinstalk.her.qchat.GiftActivity;
import com.kinstalk.her.qchat.PKStartActivity;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.RemindActivity;
import com.kinstalk.her.qchat.activity.WikipediaActivity;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatmodel.entity.TaskEntity;

import java.util.List;

import ly.count.android.sdk.Countly;

/**
 * Created by bean on 2018/7/3.
 */

public class TaskAdapter extends BaseAdapter {
    private Context mContext;
    private List<TaskEntity> mTask;
    private static String TAG = "GiftActivityLog";
    private String sn = QAIConfig.getMacForSn();
    private String token = StringEncryption.generateToken();
    private LayoutInflater inflater;
    private int gotCredit;///获取到的学分

    public TaskAdapter(Context context, List<TaskEntity> task) {
        mContext = context;
        mTask = task;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mTask.size();
    }

    @Override
    public Object getItem(int position) {
        return mTask.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_task, null);
            viewHolder = new ViewHolder();
            viewHolder.mAvatar = (ImageView) convertView.findViewById(R.id.avatar);
            viewHolder.mName = (TextView) convertView.findViewById(R.id.name);
            viewHolder.mNum = (TextView) convertView.findViewById(R.id.num);
            viewHolder.mCredit = (TextView) convertView.findViewById(R.id.credit_text);
            viewHolder.mEach = (TextView) convertView.findViewById(R.id.each);
            viewHolder.mStatus = (Button) convertView.findViewById(R.id.status_img);
            viewHolder.mLayout = (RelativeLayout) convertView.findViewById(R.id.task_layout);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((GiftActivity) mContext).playGiftContent("完成" + mTask.get(position).getName() + "任务,可以获取" + mTask.get(position).getCredit() + "学分");

                //任务类型（0表示习惯打卡任务，1表示小微百科任务，2游戏）
               /* if (0 == mTask.get(position).getType()) {
                    ((GiftActivity) mContext).playGiftContent("完成" + mTask.get(position).getName() + "任务,可以获取" + mTask.get(position).getCredit() + "学分");
                } else if (1 == mTask.get(position).getType()) {
                    ((GiftActivity) mContext).playGiftContent("完成" + mTask.get(position).getName() + "任务,每次可以获取" + mTask.get(position).getCredit() + "学分");
                } else if (2 == mTask.get(position).getType()) {
                    ((GiftActivity) mContext).playGiftContent("完成" + mTask.get(position).getName() + "任务,可以获取" + mTask.get(position).getCredit() + "学分");
                }*/
            }
        });
        viewHolder.mName.setText(mTask.get(position).getName());
        int now = mTask.get(position).getNowNum();
        int all = mTask.get(position).getAllNum();
        now = now >= all ? all : now;//如果当前次数大于总次数，按照总次数算
        viewHolder.mNum.setText(now + "/" + all);
        viewHolder.mCredit.setText("+" + mTask.get(position).getCredit());

        /*if (1 == mTask.get(position).getType()) {//x小微百度每次获取
            viewHolder.mEach.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mEach.setVisibility(View.GONE);
        }*/
        if (0 == mTask.get(position).getType()) {//打卡习惯
            viewHolder.mAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.task_remind));//设置头像
            if (now >= all) {
                if (0 == mTask.get(position).getStatus()) {//可领取
                    viewHolder.mStatus.setBackground(mContext.getResources().getDrawable(R.drawable.task_get));
                } else {//已领取过
                    viewHolder.mStatus.setBackground(mContext.getResources().getDrawable(R.drawable.task_finish));
                }
            } else {
                viewHolder.mStatus.setBackground(mContext.getResources().getDrawable(R.drawable.task_go));
            }
        } else if (1 == mTask.get(position).getType()) {//小微百科
            viewHolder.mAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.task_wikipedi));//设置头像
            if (now >= all) {
                viewHolder.mStatus.setBackground(mContext.getResources().getDrawable(R.drawable.task_finish));
            } else {
                viewHolder.mStatus.setBackground(mContext.getResources().getDrawable(R.drawable.task_go));
            }
        } else if (2 == mTask.get(position).getType()) {
            viewHolder.mAvatar.setImageDrawable(mContext.getResources().getDrawable(R.drawable.task_game));//设置头像
            if (now >= all) {
                if (0 == mTask.get(position).getStatus()) {//可领取
                    viewHolder.mStatus.setBackground(mContext.getResources().getDrawable(R.drawable.task_get));
                } else {//已领取过
                    viewHolder.mStatus.setBackground(mContext.getResources().getDrawable(R.drawable.task_finish));
                }
            } else {
                viewHolder.mStatus.setBackground(mContext.getResources().getDrawable(R.drawable.task_go));
            }
        }
        viewHolder.mStatus.setOnClickListener(new StatusOnclickListener(mTask.get(position).getId(), mTask.get(position).getType(), mTask.get(position).getStatus(), mTask.get(position).getCredit(), now, all));
        return convertView;
    }

    class StatusOnclickListener implements View.OnClickListener {
        private int type;//任务类型（0表示习惯打卡任务，1表示小微百科任务，2游戏）
        private int now;//当前次数
        private int all;//总次数
        private int status;//0可领取奖励，1其他
        private int credit;//学分
        private int id;

        public StatusOnclickListener(int id, int type, int status, int credit, int now, int all) {
            this.type = type;
            this.now = now;
            this.all = all;
            this.status = status;
            this.credit = credit;
            this.id = id;
        }

        @Override
        public void onClick(View v) {
            if (1 == type) {//小微没有可领取状态，这里直接跳转
                if (now < all) {
                    ((GiftActivity) mContext).playGiftContent("你可以尝试问我这些百科知识");
                    mContext.startActivity(new Intent(mContext, WikipediaActivity.class));
                    Countly.sharedInstance().recordEvent("gift", "t_gift_store_task_goto");
                } else {
                    ((GiftActivity) mContext).playGiftContent("已经领取过啦");
                }

            } else if (0 == type) {
                if (now < all) {//跳转到打卡界面（r日程表）
                    mContext.startActivity(new Intent(mContext, RemindActivity.class));
                    Countly.sharedInstance().recordEvent("gift", "t_gift_store_task_goto");
                } else {
                    if (0 == status) {//可领取状态，弹框
                        //TODO
                        setGotCredit(all * credit);
                        ((GiftActivity) mContext).showDialog();
                        QchatThreadManager.getInstance().start(new Runnable() {
                            @Override
                            public void run() {
                                Api.postReceiveTask(mContext, token, sn, id, credit, type, "got");
                            }
                        });
                    } else {
                        ((GiftActivity) mContext).playGiftContent("已经领取过啦");
                    }
                }
            } else if (2 == type) {
                if (now < all) {//跳转游戏界面
                    mContext.startActivity(new Intent(mContext, PKStartActivity.class));
                    Countly.sharedInstance().recordEvent("gift", "t_gift_store_task_goto");
                } else {
                    if (0 == status) {//可领取状态，弹框
                        //TODO
                        setGotCredit(all * credit);
                        ((GiftActivity) mContext).showDialog();
                        QchatThreadManager.getInstance().start(new Runnable() {
                            @Override
                            public void run() {
                                Api.postReceiveTask(mContext, token, sn, id, credit, type, "got");
                            }
                        });
                    } else {
                        ((GiftActivity) mContext).playGiftContent("已经领取过啦");
                    }
                }
            }
        }
    }

    class ViewHolder {
        private RelativeLayout mLayout;//点击这个区域，发送语音
        private ImageView mAvatar;//头像
        private TextView mName;//任务名称
        private TextView mNum;//任务完成次数：当前次数/总次数
        private TextView mCredit;//返回的学分
        private TextView mEach;//“每日”字样
        private Button mStatus;//已完成：task_finish  去完成：task_go  领奖励:task_get
    }

    public int getGotCredit() {
        return gotCredit;
    }

    public void setGotCredit(int gotCredit) {
        this.gotCredit = gotCredit;
    }
}
