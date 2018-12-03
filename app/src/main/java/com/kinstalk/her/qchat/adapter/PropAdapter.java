package com.kinstalk.her.qchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.GiftActivity;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.activity.GetCardActivity;
import com.kinstalk.her.qchat.activity.GetPropCardActivity;
import com.kinstalk.her.qchat.utils.GrayscaleTransformation;
import com.kinstalk.her.qchatmodel.entity.CardEntity;
import com.kinstalk.her.qchatmodel.entity.PKPropInfo;

import java.util.List;

/**
 * Created by bean on 2018/9/18.
 */

public class PropAdapter extends RecyclerView.Adapter<PropAdapter.ViewHolder> {
    private Context mConext;
    private List<CardEntity> mPKPropInfo;
    private LayoutInflater inflater;
    private static String TAG = "GiftActivityLog";

    public PropAdapter(Context mContext, List<CardEntity> mPKPropInfo) {
        this.mConext = mContext;
        this.mPKPropInfo = mPKPropInfo;
        inflater = LayoutInflater.from(mConext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_prop, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mPropImg.setOnClickListener(new ImgClick(mPKPropInfo.get(position)));
        if (mPKPropInfo.get(position).getCardNums() > 0) {
            Glide.with(mConext)
                    .load(mPKPropInfo.get(position)
                            .getUrl())
                    .placeholder(R.drawable.card_img_default)
                    .into(holder.mPropImg);
            holder.mPropNum.setVisibility(View.VISIBLE);
            holder.mPropNum.setText(mPKPropInfo.get(position).getCardNums() + "");
        } else {
            Glide.with(mConext)
                    .load(mPKPropInfo.get(position)
                            .getUrl())
                    .asBitmap()
                    .transform(new GrayscaleTransformation(mConext))
                    .placeholder(R.drawable.card_img_default)
                    .into(holder.mPropImg);
            holder.mPropNum.setVisibility(View.GONE);
            holder.mPropNum.setText("");
        }
    }

    class ImgClick implements View.OnClickListener {
        private CardEntity cardEntity;

        public ImgClick(CardEntity cardEntity) {
            this.cardEntity = cardEntity;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mConext, GetPropCardActivity.class);
            intent.putExtra("CardEntity", cardEntity);
            intent.putExtra("AllCredit", ((GiftActivity) mConext).getCreditNum());
            mConext.startActivity(intent);
        }
    }

    @Override
    public int getItemCount() {
        return mPKPropInfo.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mPropImg;
        TextView mPropNum;

        public ViewHolder(View itemView) {
            super(itemView);
            mPropImg = (ImageView) itemView.findViewById(R.id.prop_img);
            mPropNum = (TextView) itemView.findViewById(R.id.prop_num);
        }
    }
}
