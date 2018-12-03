package com.kinstalk.her.qchat.homework;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.adapter.KidsBaseAdapter;
import com.kinstalk.her.qchatmodel.Manager.util.WorkAlertInfo;

import java.util.ArrayList;
import java.util.List;

public class WorkAlertAdapter extends KidsBaseAdapter {

    private String TAG = WorkAlertAdapter.class.getSimpleName();
    private Context context;
    private RecyclerView mRecyclerView;
    private List<WorkAlertInfo> dataList = new ArrayList<>();

    public WorkAlertAdapter(Context context, RecyclerView recyclerView, List<WorkAlertInfo> infoList) {
        super(context, recyclerView);
        Log.d(TAG, "WorkListAdapter: new");
        this.context = context;
        this.mRecyclerView = recyclerView;
        this.dataList = infoList;
        mBottomCount = 1;
    }

    public void refreshData(List<WorkAlertInfo> dataList) {
        this.dataList.clear();
        if (null != dataList && dataList.size() > 0) {
            this.dataList.addAll(dataList);
        }
        Log.d(TAG, "refreshData: " + dataList);
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
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.kids_work_alert_item, parent, false);
            WorkAlertAdapter.WorkViewHolder workViewHolder = new WorkAlertAdapter.WorkViewHolder(v);
            return workViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: " + position);

        if (holder instanceof WorkAlertAdapter.WorkViewHolder && null != dataList && dataList.size() >= position) {
            WorkAlertInfo workAlertInfo = dataList.get(position - mHeaderCount);
            WorkAlertAdapter.WorkViewHolder itemHolder = (WorkAlertAdapter.WorkViewHolder) holder;
            if (workAlertInfo.getType() == 1) {
                if (!TextUtils.isEmpty(workAlertInfo.getWorkString())) {
                    itemHolder.contentView.setText(workAlertInfo.getWorkString());
                    itemHolder.imageView.setVisibility(View.GONE);
                } else {
                    itemHolder.contentView.setText("收到一条作业");
                    itemHolder.imageView.setVisibility(View.VISIBLE);
                }
            }
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
        public TextView contentView;
        public ImageView imageView;

        public WorkViewHolder(View itemView) {
            super(itemView);

            rootLayout = (ViewGroup) itemView.findViewById(R.id.work_alert_root);
            viewLayout = (ViewGroup) itemView.findViewById(R.id.work_alert_item);
            contentView = (TextView) itemView.findViewById(R.id.work_alert_item_content);
            imageView = (ImageView) itemView.findViewById(R.id.work_alert_item_image);
        }
    }
}
