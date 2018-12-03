package com.kinstalk.her.qchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchatmodel.entity.GiftEntity;

import java.util.List;

/**
 * Created by bean on 2018/5/22.
 */

public class ReceiveAdapter extends BaseAdapter {

    private Context context;
    private List<GiftEntity> receiveGift;
    private LayoutInflater inflater;

    public ReceiveAdapter(Context c, List<GiftEntity> mReceiveGift) {
        context = c;
        receiveGift = mReceiveGift;
        inflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return receiveGift.size();
    }

    @Override
    public Object getItem(int position) {
        return receiveGift.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_receive, null);
            viewHolder = new ViewHolder();
            viewHolder.giftName = (TextView) convertView.findViewById(R.id.gift_name);
            viewHolder.starCount = (Button) convertView.findViewById(R.id.star_count);
            viewHolder.date = (TextView) convertView.findViewById(R.id.date);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.giftName.setText(receiveGift.get(position).getGiftName());
        viewHolder.starCount.setText(receiveGift.get(position).getStarCount()+"");
        viewHolder.date.setText(receiveGift.get(position).getDate());
        return convertView;
    }

    class ViewHolder {
        private TextView giftName;//礼物名称
        private Button starCount;//兑换星星数量
        private TextView date;//已领取的日期
    }

}
