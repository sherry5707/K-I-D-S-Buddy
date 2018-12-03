package com.kinstalk.her.qchat.homework;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.adapter.KidsBaseAdapter;
import com.kinstalk.her.qchat.utils.CountlyConstant;
import com.kinstalk.her.qchat.utils.SharedPreferencesUtils;
import com.kinstalk.her.qchatmodel.entity.QHomeworkMessage;
import com.kinstalk.qloveaicore.AIManager;

import java.util.ArrayList;
import java.util.List;

import ly.count.android.sdk.Countly;

public class WorkListAdapter extends KidsBaseAdapter {

    private String TAG = WorkListAdapter.class.getSimpleName();
    private Context context;
    private RecyclerView mRecyclerView;
    private List<QHomeworkMessage> dataList = new ArrayList<>();

    public WorkListAdapter(Context context, RecyclerView recyclerView) {
        super(context, recyclerView);
        Log.d(TAG, "WorkListAdapter: new");
        this.context = context;
        this.mRecyclerView = recyclerView;

        mHeaderCount = 1;
        mBottomCount = 1;
    }

    public void refreshData(List<QHomeworkMessage> dataList) {
        Log.d(TAG, "refreshData: " + dataList);
        this.dataList.clear();
        if (null != dataList && dataList.size() > 0) {
            this.dataList.addAll(dataList);
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == ITEM_TYPE.ITEM_TYPE_HEADER.ordinal()) {
            return onCreateHeaderView(parent);
        } else if (viewType == ITEM_TYPE.ITEM_TYPE_BOTTOM.ordinal()) {
            return onCreateBottomView(parent);
        } else if (viewType == ITEM_TYPE.ITEM_TYPE_CONTENT.ordinal()) {
            Log.d(TAG, "onCreateViewHolder: " + viewType);
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.kids_work_item, parent, false);
            WorkListAdapter.WorkViewHolder workViewHolder = new WorkListAdapter.WorkViewHolder(v);
            return workViewHolder;
        }
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateBottomView(ViewGroup parent) {
        return bottomView(parent);
    }

    private RecyclerView.ViewHolder bottomView(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kids_work_header, parent, false);

        HeaderViewHolder headerViewHolder = new HeaderViewHolder(view);
        return headerViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + position);

        if (holder instanceof WorkListAdapter.HeaderViewHolder) {
            WorkListAdapter.HeaderViewHolder itemHolder = (WorkListAdapter.HeaderViewHolder) holder;

            if (dataList.size() > 0) {
                itemHolder.rootLayout.setVisibility(View.VISIBLE);
            } else {
                itemHolder.rootLayout.setVisibility(View.GONE);
            }
        } else if (holder instanceof WorkListAdapter.WorkViewHolder && null != dataList && dataList.size() >= position) {
            final QHomeworkMessage workMessage = dataList.get(position - mHeaderCount);
            final String workId = workMessage.getHomeWorkEntity().getHomeworkid();

            final WorkListAdapter.WorkViewHolder itemHolder = (WorkListAdapter.WorkViewHolder) holder;

            ViewGroup.LayoutParams layoutParams = itemHolder.contentView.getLayoutParams();
            if (TextUtils.isEmpty(workMessage.getHomeWorkEntity().getUrl()) || workMessage.getHomeWorkEntity().getUrl().equals("null")) {
                //文字不为空 且 没有图
                layoutParams.width = 571;
            } else {
                layoutParams.width = 379;
            }
            itemHolder.contentView.setLayoutParams(layoutParams);
            itemHolder.contentView.postInvalidate();

            if (!TextUtils.isEmpty(workMessage.getHomeWorkEntity().getContent())) {
                //文字不为空
                itemHolder.contentView.setText(workMessage.getHomeWorkEntity().getContent());

                itemHolder.playButton.setVisibility(View.VISIBLE);
                itemHolder.playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playWorkContent(workMessage.getHomeWorkEntity().getContent());
                        Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE,CountlyConstant.t_homework_audio);
                    }
                });
            } else {
                itemHolder.playButton.setVisibility(View.GONE);
                itemHolder.contentView.setText("");
            }

            if (!TextUtils.isEmpty(workMessage.getHomeWorkEntity().getUrl()) && !workMessage.getHomeWorkEntity().getUrl().equals("null")) {
                //图片不为空
                Glide.with(QchatApplication.getInstance())
                        .load(workMessage.getHomeWorkEntity().getUrl())
                        .skipMemoryCache(false)
                        .into(itemHolder.imageWorkView);

                itemHolder.imageWorkView.setVisibility(View.VISIBLE);
                itemHolder.imageWorkView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickImage(workMessage.getHomeWorkEntity().getUrl());
                    }
                });

                itemHolder.setImageUrl(workMessage.getHomeWorkEntity().getUrl());
            } else {
                //无图
                itemHolder.imageWorkView.setVisibility(View.GONE);
                itemHolder.setImageUrl("");
            }

            setFinishState(itemHolder.finishButton, workMessage.getHomeWorkEntity().getHomeworkid(), itemHolder.finishView);
            itemHolder.finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setViewFinishState(v, workId, itemHolder.finishView);
                }
            });
        }
    }

    public void setViewFinishState(View v, String workId, View textView) {
        boolean isFinish = SharedPreferencesUtils.getSharedPreferencesWithBool(context, SharedPreferencesUtils.WORK_FINISH, workId, false);
        isFinish = !isFinish;
        setButtonState(v, isFinish, textView);
        SharedPreferencesUtils.setSharedPreferencesWithBool(context, SharedPreferencesUtils.WORK_FINISH, workId, isFinish);
    }

    public void setFinishState(View v, String workId, View textView) {
        boolean isFinish = SharedPreferencesUtils.getSharedPreferencesWithBool(context, SharedPreferencesUtils.WORK_FINISH, workId, false);
        setButtonState(v, isFinish, textView);
    }

    public void setButtonState(View v, boolean isFinish, View textView) {
        if (isFinish) {
            v.setBackgroundResource(R.mipmap.work_finish_press);
            textView.setVisibility(View.VISIBLE);
            Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE,CountlyConstant.t_homework_done);
        } else {
            v.setBackgroundResource(R.mipmap.work_finish_normal);
            textView.setVisibility(View.INVISIBLE);
            Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE,CountlyConstant.t_homework_todo);
        }
    }

    public void clickImage(String imageUrl) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.putExtra("imageUrl", imageUrl);
        intent.setClass(context, WorkImageDetailDialogActivity.class);
        context.startActivity(intent);
    }

    public void playWorkContent(String content) {
        try {
            AIManager.getInstance(context).playTextWithStr(content, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getContentItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemCount() {
        return dataList.size() + mHeaderCount + mBottomCount;
    }

    public class WorkViewHolder extends RecyclerView.ViewHolder {
        public ViewGroup rootLayout;
        public ViewGroup viewLayout;
        public ImageView imageWorkView;
        public TextView contentView;
        public ImageButton playButton;
        public ImageButton finishButton;
        public TextView finishView;

        private String imageUrl;

        public WorkViewHolder(View itemView) {
            super(itemView);

            rootLayout = (ViewGroup) itemView.findViewById(R.id.work_item_root);
            viewLayout = (LinearLayout) itemView.findViewById(R.id.work_item);

            contentView = (TextView) itemView.findViewById(R.id.work_item_content);
            imageWorkView = (ImageView) itemView.findViewById(R.id.work_item_image);

            playButton = (ImageButton) itemView.findViewById(R.id.work_item_play_btn);
            finishButton = (ImageButton) itemView.findViewById(R.id.work_item_finish_btn);
            finishView = (TextView) itemView.findViewById(R.id.work_item_finish_text);

            viewLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onDoubleClick: ");
                    Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE,CountlyConstant.t_homework_detail);
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

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public ViewGroup rootLayout;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            rootLayout = (ViewGroup) itemView.findViewById(R.id.work_content_root);
        }
    }
}
