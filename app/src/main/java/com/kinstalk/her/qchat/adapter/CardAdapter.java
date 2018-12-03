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
import com.kinstalk.her.qchat.PKStartActivity;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.activity.GetCardActivity;
import com.kinstalk.her.qchat.utils.GrayscaleTransformation;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatmodel.entity.CardEntity;

import java.util.List;


/**
 * Created by bean on 2018/7/4.
 */

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {
    private Context mConext;
    private List<CardEntity> mCardEntity;
    private LayoutInflater inflater;
    private static String TAG = "GiftActivityLog";

    public CardAdapter(Context activity, List<CardEntity> cardEntity) {
        mConext = activity;
        mCardEntity = cardEntity;
        inflater = LayoutInflater.from(mConext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mCardImg.setOnClickListener(new ImgClick(mCardEntity.get(position)));
        //状态 (0未拥有，1.已拥有)
        if (0 == mCardEntity.get(position).getStatus()) {
            Glide.with(mConext)
                    .load(mCardEntity.get(position)
                            .getUrl())
                    .asBitmap()
                    .transform(new GrayscaleTransformation(mConext))
                    .placeholder(R.drawable.card_img_default)
                    .into(holder.mCardImg);
            holder.mCardNum.setVisibility(View.GONE);
        } else if (1 == mCardEntity.get(position).getStatus()) {
            Glide.with(mConext)
                    .load(mCardEntity.get(position)
                            .getUrl())
                    .placeholder(R.drawable.card_img_default)
                    .into(holder.mCardImg);
            holder.mCardNum.setVisibility(View.VISIBLE);
            holder.mCardNum.setText(mCardEntity.get(position).getCardNums() + "");
        }

        //# 卡类型： 1普通卡 2高级卡
        if (1 == mCardEntity.get(position).getCardType()) {
            holder.mCardRare.setVisibility(View.GONE);
        } else if (2 == mCardEntity.get(position).getCardType()) {
            holder.mCardRare.setVisibility(View.VISIBLE);
        }
    }

    class ImgClick implements View.OnClickListener {
        private CardEntity cardEntity;

        public ImgClick(CardEntity cardEntity) {
            this.cardEntity = cardEntity;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mConext, GetCardActivity.class);
            intent.putExtra("CardEntity", cardEntity);
            intent.putExtra("AllCredit", ((GiftActivity) mConext).getCreditNum());
            intent.putExtra("HasCardNum", ((GiftActivity) mConext).getHasCardNum());
            mConext.startActivity(intent);
        }
    }

    @Override
    public int getItemCount() {
        return mCardEntity.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mCardRare;//稀有卡片图标
        ImageView mCardImg;//图片
        TextView mCardNum;//图片数量

        public ViewHolder(View itemView) {
            super(itemView);
            mCardRare = (ImageView) itemView.findViewById(R.id.card_rare);
            mCardImg = (ImageView) itemView.findViewById(R.id.card_img);
            mCardNum = (TextView) itemView.findViewById(R.id.card_num);
        }
    }
}
