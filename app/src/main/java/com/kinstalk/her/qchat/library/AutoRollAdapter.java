package com.kinstalk.her.qchat.library;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatmodel.entity.VoiceBookBean;
import com.kinstalk.qloveaicore.AIManager;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 滚动RecyclerView adapter
 */
public class AutoRollAdapter extends RecyclerView.Adapter<AutoRollAdapter.BaseViewHolder> {
    private final Context mContext;
    private List<VoiceBookBean> mData;
    private static final String PREFIX_STR = "我要听";

    public AutoRollControllListener controllListener;

    public AutoRollAdapter(Context context, List<VoiceBookBean> list) {

        QAILog.d("AutoPollAdapter", "AutoPollAdapter run -> " + list.size());

        this.mContext = context;
        this.mData = list;

        if (this.mContext instanceof AutoRollControllListener) {
            controllListener = (AutoRollControllListener) this.mContext; // 2.2 获取到宿主activity并赋值
        } else {
            throw new IllegalArgumentException("activity must implements ParentTabChangeListener");
        }

    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_auto_poll, parent, false);
        BaseViewHolder holder = new BaseViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, final int position) {

        //      QAILog.d("AutoPollAdapter", "onBindViewHolder -> " + mData.get(position));

        final String name = mData.get(position % mData.size()).getBookName();
        holder.textView.setText(name);

        String url = mData.get(position % mData.size()).getIconUrl();
        Glide.with(mContext).load(url).into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controllListener.stopRoll();
                AIManager.getInstance(mContext).textRequest(mData.get(position % mData.size()).getCommand());
            }
        });

    }

    @Override
    public int getItemCount() {

        QAILog.d("AutoPollAdapter", "getItemCount -> " + Integer.MAX_VALUE + " | " + this.mData.size());

        return Integer.MAX_VALUE;

        //   return this.mData.size();
    }


    public void changeData(List<VoiceBookBean> list) {

        if (list != null && mData != null)
            mData.clear();

        mData = list;

        notifyDataSetChanged();

    }

    class BaseViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageView;

        private TextView textView;

        public BaseViewHolder(View itemView) {
            super(itemView);

            imageView = (CircleImageView) itemView.findViewById(R.id.iv_icon);
            textView = (TextView) itemView.findViewById(R.id.tv_name);
        }
    }
}
