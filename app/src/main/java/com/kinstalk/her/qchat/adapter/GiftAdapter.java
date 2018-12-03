package com.kinstalk.her.qchat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kinstalk.her.qchat.ChatMainActivity;
import com.kinstalk.her.qchat.GiftActivity;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.dialog.GiftDialog;
import com.kinstalk.her.qchat.view.ToastCustom;
import com.kinstalk.her.qchat.view.ToastGift;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatcomm.utils.SystemTool;
import com.kinstalk.her.qchatmodel.entity.GiftEntity;

import java.util.List;

import ly.count.android.sdk.Countly;

/**
 * Created by bean on 2018/5/22.
 */

public class GiftAdapter extends BaseAdapter {
    private Context context;
    private List<GiftEntity> receiveGift;
    private LayoutInflater inflater;
    private GiftDialog dialog;
    private static String TAG = "GiftActivityLog";
    private String sn = QAIConfig.getMacForSn();
    private String token = StringEncryption.generateToken();

    public GiftAdapter(Context c, List<GiftEntity> mReceiveGift) {
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
            convertView = inflater.inflate(R.layout.item_gift, null);
            viewHolder = new ViewHolder();
            viewHolder.giftName = (TextView) convertView.findViewById(R.id.gift_name);
            viewHolder.starCount = (Button) convertView.findViewById(R.id.star_count);
            viewHolder.receiveImg = (Button) convertView.findViewById(R.id.image_receive);
            viewHolder.receiveText = (TextView) convertView.findViewById(R.id.text_receive);
            viewHolder.gettingLayout = (RelativeLayout) convertView.findViewById(R.id.getting_layout);
            viewHolder.cancelImg = (Button) convertView.findViewById(R.id.image_cancel);
            viewHolder.getImg = (Button) convertView.findViewById(R.id.image_get);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.giftName.setText(receiveGift.get(position).getGiftName());
        viewHolder.starCount.setText(receiveGift.get(position).getStarCount() + "");
        int status = receiveGift.get(position).getStatus();
        if (0 == status) {
            viewHolder.receiveImg.setVisibility(View.VISIBLE);
            viewHolder.receiveText.setVisibility(View.VISIBLE);
            viewHolder.gettingLayout.setVisibility(View.GONE);
            viewHolder.receiveImg.setBackground(context.getResources().getDrawable(R.drawable.gift_selector));
            viewHolder.receiveImg.setTextColor(Color.parseColor("#FFFFFF"));
        } else if (1 == status) {
            viewHolder.receiveImg.setVisibility(View.VISIBLE);
            viewHolder.receiveText.setVisibility(View.VISIBLE);
            viewHolder.gettingLayout.setVisibility(View.GONE);
            viewHolder.receiveImg.setBackground(context.getResources().getDrawable(R.drawable.gift_item_cannot_receive));
            viewHolder.receiveImg.setTextColor(Color.parseColor("#8F8F8F"));
        } else if (3 == status) {
            viewHolder.receiveImg.setVisibility(View.GONE);
            viewHolder.receiveText.setVisibility(View.GONE);
            viewHolder.gettingLayout.setVisibility(View.VISIBLE);
        }
        viewHolder.receiveImg.setOnClickListener(new GettingImgOnclickListener(receiveGift.get(position)));//点击红星
        viewHolder.getImg.setOnClickListener(new FinishOnclickListener(receiveGift.get(position)));//确认收到礼物
        viewHolder.cancelImg.setOnClickListener(new CancelOnclickListener(receiveGift.get(position)));//取消收到礼物
        return convertView;
    }

    class GettingImgOnclickListener implements View.OnClickListener {
        GiftEntity giftEntity;

        public GettingImgOnclickListener(GiftEntity mEntity) {
            giftEntity = mEntity;
        }

        @Override
        public void onClick(View v) {
            //点击红星，要判断列表里面是否存在正在的礼物
            if (hasGettingGift() && 0 == giftEntity.getStatus()) {
                ToastGift.makeText(context, context.getResources().getString(R.string.toast_one_exchange), Toast.LENGTH_LONG).show();
                return;
            }
            if (1 == giftEntity.getStatus()) {
                ToastGift.makeText(context, context.getResources().getString(R.string.toast_no_exchange), Toast.LENGTH_LONG).show();
                return;
            }
            if (0 == giftEntity.getStatus()) {
                if (SystemTool.isNetworkAvailable(context)) {
                    //TODO 发送消息给服务器，通知领取该礼物
                    ((GiftActivity) context).showDialog();
                    QchatThreadManager.getInstance().start(new Runnable() {
                        @Override
                        public void run() {
                            Api.postReceiveGift(token, sn, giftEntity.getId(), giftEntity.getStarCount(), "got");
                        }
                    });
                    Countly.sharedInstance().recordEvent("gift", "t_gift_store_gift_unreceived_wish");
                } else {
                    ToastGift.makeText(context, context.getResources().getString(R.string.no_network_gift), Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    class FinishOnclickListener implements View.OnClickListener {
        GiftEntity giftEntity;

        public FinishOnclickListener(GiftEntity entity) {
            giftEntity = entity;
        }

        @Override
        public void onClick(View v) {
            ((GiftActivity) context).playGiftContent("确定收到这个礼物了吗");
            dialog = new GiftDialog(context);
            dialog.show();
            dialog.setonSureListener(new GiftDialog.giftDialogCallback() {
                @Override
                public void onSure() {
                    //TODO 发送消息给服务器，通知已领取该礼物
                    if (SystemTool.isNetworkAvailable(context)) {
                        ((GiftActivity) context).showDialog();
                        QchatThreadManager.getInstance().start(new Runnable() {
                            @Override
                            public void run() {
                                Api.postReceiveGift(token, sn, giftEntity.getId(), giftEntity.getStarCount(), "finished");
                            }
                        });
                        Countly.sharedInstance().recordEvent("gift", "t_gift_store_gift_unreceived_received");
                    } else {
                        ToastGift.makeText(context, context.getResources().getString(R.string.no_network_gift), Toast.LENGTH_SHORT).show();
                    }
                    cancelDialog();
                }

                @Override
                public void onCancel() {
                    cancelDialog();
                }
            });
        }
    }


    class CancelOnclickListener implements View.OnClickListener {
        GiftEntity giftEntity;

        public CancelOnclickListener(GiftEntity entity) {
            giftEntity = entity;
        }

        @Override
        public void onClick(View v) {
            //TODO 发送消息给服务器，取消正在领取的礼物
            if (SystemTool.isNetworkAvailable(context)) {
                ((GiftActivity) context).showDialog();
                QchatThreadManager.getInstance().start(new Runnable() {
                    @Override
                    public void run() {
                        Api.postReceiveGift(token, sn, giftEntity.getId(), giftEntity.getStarCount(), "cancel");
                    }
                });
                Countly.sharedInstance().recordEvent("gift", "t_gift_store_gift_unreceived_unwish");
            } else {
                ToastGift.makeText(context, context.getResources().getString(R.string.no_network_gift), Toast.LENGTH_SHORT).show();
            }
            Log.i(TAG, "R.id.image_cancel onClick");
        }
    }


    private boolean hasGettingGift() {
        for (int i = 0; i < receiveGift.size(); i++) {
            if (3 == receiveGift.get(i).getStatus()) {
                return true;
            }
        }
        return false;
    }

    class ViewHolder {
        private TextView giftName;//礼物名称
        private Button starCount;//兑换星星数量
        private Button receiveImg;//是否可领取?
        private TextView receiveText;//是否可领取提示语
        private RelativeLayout gettingLayout;//待领取礼物的布局，只有一个
        private Button cancelImg, getImg;//确认是否收到礼物
    }

    public void cancelDialog() {
        if (dialog != null) {
            dialog.cancel();
            dialog = null;
        }

    }
}
