package com.kinstalk.her.qchat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kinstalk.her.qchat.R;

import java.util.ArrayList;
import java.util.List;

public class KidsBaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected int mHeaderCount;// 头部View个数
    protected int mBottomCount;// 底部View个数
    private Context mContext;
    private RecyclerView mRecyclerView;
    private List<Object> mItems = new ArrayList<Object>();


    public KidsBaseAdapter(Context context, RecyclerView mRecyclerView) {
        this.mContext = context;
        this.mRecyclerView = mRecyclerView;

        mHeaderCount = 0;
        mBottomCount = 0;
    }

    public RecyclerView.ViewHolder onCreateHeaderView(ViewGroup parent) {
        return onCreateView(parent);
    }

    public RecyclerView.ViewHolder onCreateBottomView(ViewGroup parent) {
        return onCreateView(parent);
    }

    private RecyclerView.ViewHolder onCreateView(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kids_list_header, parent, false);
        HeaderViewHolder headerViewHolder = new HeaderViewHolder(view);
        return headerViewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

    }

    public int getContentItemCount() {
        return mItems.size();
    }

    public boolean isHeaderView(int position) {
        return mHeaderCount != 0 && position < mHeaderCount;
    }

    public boolean isBottomView(int position) {
        return mBottomCount != 0 && position >= (mHeaderCount + getContentItemCount());
    }

    @Override
    public int getItemViewType(int position) {
        int dataItemCount = getContentItemCount();
        if (mHeaderCount != 0 && position < mHeaderCount) {// 头部View
            return ITEM_TYPE.ITEM_TYPE_HEADER.ordinal();
        } else if (mBottomCount != 0 && position >= (mHeaderCount + dataItemCount)) {// 底部View
            return ITEM_TYPE.ITEM_TYPE_BOTTOM.ordinal();
        } else {
            return ITEM_TYPE.ITEM_TYPE_CONTENT.ordinal();
        }
    }

    @Override
    public int getItemCount() {
        return mHeaderCount + getContentItemCount() + mBottomCount;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE.ITEM_TYPE_HEADER.ordinal()) {
            return onCreateHeaderView(parent);
        } else  if (viewType == ITEM_TYPE.ITEM_TYPE_BOTTOM.ordinal()) {
            return onCreateBottomView(parent);
        }
        return null;
    }

    public enum ITEM_TYPE {
        ITEM_TYPE_HEADER, ITEM_TYPE_CONTENT, ITEM_TYPE_BOTTOM
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(View view) {
            super(view);
        }
    }

    public static class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

}
