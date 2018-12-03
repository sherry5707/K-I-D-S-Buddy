package com.kinstalk.her.qchat.homework;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.utils.CountlyConstant;
import com.kinstalk.her.qchat.utils.SharedPreferencesUtils;
import com.kinstalk.her.qchatmodel.entity.QHomeworkMessage;
import com.kinstalk.qloveaicore.AIManager;

import java.util.ArrayList;
import java.util.List;

import ly.count.android.sdk.Countly;


public class HomeWorkVpAdapter extends PagerAdapter {
    private List<QHomeworkMessage> dataList = new ArrayList<>();
    private final String TAG = HomeWorkVpAdapter.class.getSimpleName();
    private Context context;
    private LayoutInflater inflater;
    private int mChildCount=0;
    private static final Long SPACE_TIME = 600L;
    private static Long mLastClickTime = 0L;

    public HomeWorkVpAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    /**
     * s刷新数据
     *
     * @param dataList
     */
    public void refreshData(List<QHomeworkMessage> dataList) {
        Log.d(TAG, "refreshData: " + dataList);
        this.dataList.clear();
        if (null != dataList && dataList.size() > 0) {
            this.dataList.addAll(dataList);
        }
        notifyDataSetChanged();

    }

    @Override
    public void notifyDataSetChanged() {
        mChildCount = getCount();
        super.notifyDataSetChanged();
    }

    @Override

    public int getItemPosition(Object object)   {

        if ( mChildCount >= 0) {

            mChildCount --;

            return POSITION_NONE;

        }

        return super.getItemPosition(object);

    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = inflater.inflate(R.layout.kids_work_item, null);
        ViewHolder holder = new ViewHolder(itemView);
        final QHomeworkMessage workMessage = dataList.get(position);
        final String workId = workMessage.getHomeWorkEntity().getHomeworkid();


        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentView.getLayoutParams();
        if (TextUtils.isEmpty(workMessage.getHomeWorkEntity().getUrl()) || workMessage.getHomeWorkEntity().getUrl().equals("null")) {
            //文字不为空 且 没有图
            layoutParams.width = 541;
        } else {
            layoutParams.width = 320;
        }
        holder.contentView.setLayoutParams(layoutParams);
        holder.contentView.postInvalidate();

        if (!TextUtils.isEmpty(workMessage.getHomeWorkEntity().getContent())) {
            //文字不为空
            String text = workMessage.getHomeWorkEntity().getContent();
            holder.contentView.setText(text);
            holder.playButton.setVisibility(View.VISIBLE);
            holder.playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickPlay(workMessage.getHomeWorkEntity().getContent());
                }
            });
        } else {
            holder.playButton.setVisibility(View.GONE);
            holder.contentView.setVisibility(View.GONE);
            LinearLayout.LayoutParams imgLayoutParams = (LinearLayout.LayoutParams) holder.cardview.getLayoutParams();
            imgLayoutParams.width = 568;
            imgLayoutParams.height=228;
            imgLayoutParams.leftMargin=16;
            imgLayoutParams.topMargin=-4;
            holder.cardview.setLayoutParams(imgLayoutParams);

        }


        if (!TextUtils.isEmpty(workMessage.getHomeWorkEntity().getUrl()) && !workMessage.getHomeWorkEntity().getUrl().equals("null")) {
            //图片不为空
            Glide.with(QchatApplication.getInstance())
                    .load(workMessage.getHomeWorkEntity().getUrl())
                    .skipMemoryCache(false)
                    .thumbnail(0.1f)
                    .into(holder.imageWorkView);

            holder.cardview.setVisibility(View.VISIBLE);
            holder.imageWorkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickImage(workMessage.getHomeWorkEntity().getUrl());
                }
            });

            holder.setImageUrl(workMessage.getHomeWorkEntity().getUrl());
        } else {
            //无图
            holder.cardview.setVisibility(View.GONE);
            holder.setImageUrl("");
        }
        final ViewHolder holder1 = holder;
        setFinishState(holder.finishButton, workMessage.getHomeWorkEntity().getHomeworkid(), holder.finishView);
        holder.finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setViewFinishState(v, workId, holder1.finishView);
            }
        });



        container.addView(itemView);
        return itemView;
    }

    @Override
    public int getCount() {
        return dataList.size() > 0 ? dataList.size() : 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public class ViewHolder {
        public FrameLayout rootLayout;
        public ViewGroup viewLayout;
        public ImageView imageWorkView;
        public TextView contentView;
        public ImageButton playButton;
        public ImageButton finishButton;
        public TextView finishView;
        public String imageUrl;
        public CardView cardview;

        public ViewHolder(View itemView) {
            rootLayout = (FrameLayout) itemView.findViewById(R.id.work_item_root);
            viewLayout = (LinearLayout) itemView.findViewById(R.id.work_item);
            contentView = (TextView) itemView.findViewById(R.id.work_item_content);
            imageWorkView = (ImageView) itemView.findViewById(R.id.work_item_image);
            playButton = (ImageButton) itemView.findViewById(R.id.work_item_play_btn);
            finishButton = (ImageButton) itemView.findViewById(R.id.work_item_finish_btn);
            finishView = (TextView) itemView.findViewById(R.id.work_item_finish_text);
            cardview = (CardView)itemView.findViewById(R.id.cardview);


            viewLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onDoubleClick: ");
                    Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_homework_detail);

                    if (!TextUtils.isEmpty(imageUrl) && !imageUrl.equals("null") && TextUtils.isEmpty(getContent())) {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        intent.putExtra("imageUrl", imageUrl);
                        intent.setClass(context, WorkImageDetailDialogActivity.class);
                        context.startActivity(intent);
                    } else if (!TextUtils.isEmpty(getContent())) {
                        Intent intent = new Intent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        intent.putExtra("content", getContent());
                        intent.putExtra("imageUrl", imageUrl);
                        intent.setClass(context, WorkDetailDialogActivity.class);
                        context.startActivity(intent);
                    }
                }
            });

        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getContent() {
            return String.valueOf(contentView.getText());
        }
    }

    /**
     * 发送广播给语音精灵 ---播放tts
     *
     * @param content
     */
    public void playWorkContent(String content) {
        try {
            AIManager.getInstance(this.context).playTextWithStr(content, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 点击图片 --》显示全屏
     *
     * @param imageUrl
     */
    public void clickImage(String imageUrl) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.putExtra("imageUrl", imageUrl);
        intent.setClass(context, WorkImageDetailDialogActivity.class);
        context.startActivity(intent);
    }


    public void setFinishState(View v, String workId, View textView) {
        boolean isFinish = SharedPreferencesUtils.getSharedPreferencesWithBool(context, SharedPreferencesUtils.WORK_FINISH, workId, false);
        setButtonState(v, isFinish, textView);
    }

    public void setButtonState(View v, boolean isFinish, View textView) {
        if (isFinish) {
            v.setBackgroundResource(R.mipmap.work_finish_press);
            textView.setVisibility(View.VISIBLE);
            Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_homework_done);
        } else {
            v.setBackgroundResource(R.mipmap.work_finish_normal);
            textView.setVisibility(View.INVISIBLE);
            Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_homework_todo);
        }
    }

    public void setViewFinishState(View v, String workId, View textView) {
        boolean isFinish = SharedPreferencesUtils.getSharedPreferencesWithBool(context, SharedPreferencesUtils.WORK_FINISH, workId, false);
        isFinish = !isFinish;
        setButtonState(v, isFinish, textView);
        SharedPreferencesUtils.setSharedPreferencesWithBool(context, SharedPreferencesUtils.WORK_FINISH, workId, isFinish);
    }

    public void onClickPlay(String content) {
        if(isFastClick())
            return;
        playWorkContent(content);
        Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_homework_audio);
    }


    private static boolean isFastClick() {
        long currentTime = System.currentTimeMillis();//当前系统时间
        boolean isFastClick;//是否允许点击
        if (currentTime - mLastClickTime < SPACE_TIME) {
            isFastClick = true;
        } else {
            isFastClick = false;
            mLastClickTime = currentTime;
        }
        return isFastClick;
    }

}
