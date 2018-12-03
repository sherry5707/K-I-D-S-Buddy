package com.kinstalk.her.qchat.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.dialog.LoadingDialog;
import com.kinstalk.her.qchat.utils.GrayscaleTransformation;
import com.kinstalk.her.qchat.view.ToastGift;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.model.cardCallback;
import com.kinstalk.her.qchatmodel.entity.CardEntity;
import com.kinstalk.her.qchatmodel.entity.PKPropInfo;
import com.kinstalk.qloveaicore.AIManager;


import com.kinstalk.util.AppUtils;

import java.util.List;

/**
 * Created by bean on 2018/7/4.
 */

public class GetCardActivity extends Activity implements View.OnClickListener, cardCallback {
    private static String TAG = "GiftActivityLog";
    private CardEntity entity;//卡片数据
    private ImageButton back;
    private TextView mPeopleText;//多少人获得
    private ImageView mAvatar;//三个人的头像
    private TextView mAllCredit;//学分总数
    private TextView mContent;//描述内容
    private RelativeLayout mYuyin;//语音播报内容;
    private Button mYuyinBtn;//语音图标
    private ImageView mCardImg;//卡片图片
    private ImageView mCardRare;//稀有卡片图片
    private String allCredit;//学分总数
    private int hasCardNum;//拥有的普通卡片数量;

    //底部四个botton
    private Button mCardShow, mCardShow2, mCardGet, mCardGet2;
    private LoadingDialog loadingDialog;//加载中布局;
    private String sn = QAIConfig.getMacForSn();
    private String token = StringEncryption.generateToken();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //禁止30秒回首页代码，不用屏蔽
      /*
        try {
            AppUtils.setAutoActivityTimeout(getWindow(), false);
        } catch (Exception e1) {
            e1.printStackTrace();
        }*/

        setContentView(R.layout.activity_card_get);
        entity = (CardEntity) getIntent().getSerializableExtra("CardEntity");
        allCredit = getIntent().getStringExtra("AllCredit");
        hasCardNum = getIntent().getIntExtra("HasCardNum", 0);
        initView();
        initData();
    }

    private void initData() {
        // 卡片:状态 (0未拥有，1.已拥有)
        if (0 == entity.getStatus()) {
            Glide.with(this).load(entity.getUrl()).asBitmap().transform(new GrayscaleTransformation(this)).placeholder(R.drawable.card_img_default).into(mCardImg);
        } else if (1 == entity.getStatus()) {
            Glide.with(this).load(entity.getUrl()).placeholder(R.drawable.card_img_default).into(mCardImg);
        }

        //头像
        Glide.with(this).load(entity.getPersonUrl()).placeholder(R.drawable.card_avatar).into(mAvatar);

        mPeopleText.setText(entity.getPerson() + "人已获得");
        mContent.setText(entity.getCardDesp());
        mAllCredit.setText(allCredit);
        updateBottom();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        entity = (CardEntity) intent.getSerializableExtra("CardEntity");
        allCredit = intent.getStringExtra("AllCredit");
        hasCardNum = intent.getIntExtra("HasCardNum", 0);
        initData();
    }

    private void updateBottom() {
        //# 卡类型： 1普通卡 2高级卡 3道具卡
        if (1 == entity.getCardType()) {
            mCardRare.setVisibility(View.GONE);
            if (0 == entity.getStatus()) {//状态 (0未拥有，1.已拥有)
                cardGet2(entity.getGetCredit());
            } else if (1 == entity.getStatus()) {//分享可以得到的学分（已分享过的这里返回0即可）
                if (entity.getShareCredit() > 0) {
                    cardShow2(entity.getShareCredit());
                } else {
                    cardShow();
                }
            }
        } else if (2 == entity.getCardType()) {
            mCardRare.setVisibility(View.VISIBLE);
            if (0 == entity.getStatus()) {//状态 (0未拥有，1.已拥有)
                cardGet(entity.getCardNum());
            } else if (1 == entity.getStatus()) {//分享可以得到的学分（已分享过的这里返回0即可）
                if (entity.getShareCredit() > 0) {
                    cardShow2(entity.getShareCredit());
                } else {
                    cardShow();
                }
            }
        } else if (3 == entity.getCardType()) {
            //TODO
        }
    }

    private void initView() {
        loadingDialog = new LoadingDialog(this);
        back = (ImageButton) findViewById(R.id.card_back);
        back.setOnClickListener(this);
        mPeopleText = (TextView) findViewById(R.id.people);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mAllCredit = (TextView) findViewById(R.id.credit_num);
        mContent = (TextView) findViewById(R.id.content);
        mContent.setOnClickListener(this);
        mYuyin = (RelativeLayout) findViewById(R.id.yuyin);
        mYuyin.setOnClickListener(this);
        mYuyinBtn = (Button) findViewById(R.id.yuyin2);
        mYuyinBtn.setOnClickListener(this);
        mCardShow = (Button) findViewById(R.id.card_show);
        mCardShow2 = (Button) findViewById(R.id.card_show2);
        mCardGet = (Button) findViewById(R.id.card_get);
        mCardGet2 = (Button) findViewById(R.id.card_get2);
        mCardShow.setOnClickListener(this);
        mCardShow2.setOnClickListener(this);
        mCardGet.setOnClickListener(this);
        mCardGet2.setOnClickListener(this);
        mCardImg = (ImageView) findViewById(R.id.card_img);
        mCardRare = (ImageView) findViewById(R.id.card_rare);
        MessageManager.registerCardCallback(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.card_back:
                finish();
                break;
            case R.id.yuyin:
            case R.id.yuyin2:
            case R.id.content:
                // 播放内容
                playGiftContent(entity.getCardDesp());
                break;
            case R.id.card_show:  //炫耀按钮不加学分
            case R.id.card_show2://炫耀按钮加学分
                showDialog();
                QchatThreadManager.getInstance().start(new Runnable() {
                    @Override
                    public void run() {
                        Api.postShareCard(token, sn, entity.getId(), entity.getCardType());
                    }
                });
                break;
            case R.id.card_get://卡片购买
            case R.id.card_get2:
                //普通卡片先判断学分是否够,不够就去挣学分
                if (1 == entity.getCardType() && entity.getGetCredit() > Integer.parseInt(allCredit)) {
                    playGiftContent("学分不够，一起去挣学分吧");
                    Intent i = new Intent(this, DialogTaskActivity.class);
                    i.putExtra("is_success", false);
                    startActivity(i);
                    return;
                }

                //稀有卡片先判断次数是否够，不够无法点击
                if (2 == entity.getCardType() && entity.getCardNum() > hasCardNum) {//卡片数量不足
                    return;
                }
                showDialog();
                QchatThreadManager.getInstance().start(new Runnable() {
                    @Override
                    public void run() {
                        Api.postReceiveCard(token, sn, entity.getId(), entity.getCardType());
                    }
                });
                break;
        }
    }

    /**
     * 炫耀按钮不加学分
     */
    private void cardShow() {
        mCardShow.setVisibility(View.VISIBLE);
        mCardShow2.setVisibility(View.GONE);
        mCardGet.setVisibility(View.GONE);
        mCardGet2.setVisibility(View.GONE);
    }

    /**
     * 炫耀按钮加学分
     */
    private void cardShow2(int credit) {
        mCardShow.setVisibility(View.GONE);
        mCardShow2.setVisibility(View.VISIBLE);
        mCardShow2.setText("炫耀一下        +" + credit);
        mCardGet.setVisibility(View.GONE);
        mCardGet2.setVisibility(View.GONE);
    }

    /**
     * 稀有卡片购买按钮
     */
    private void cardGet(int cardNum) {
        mCardShow.setVisibility(View.GONE);
        mCardShow2.setVisibility(View.GONE);
        mCardGet.setVisibility(View.VISIBLE);
        if (1 == (cardNum + "").length()) {
            mCardGet.setText("普通卡" + cardNum + "张         购买");
        } else if (2 == (cardNum + "").length()) {
            mCardGet.setText("普通卡" + cardNum + "张       购买");
        } else if (3 == (cardNum + "").length()) {
            mCardGet.setText("普通卡" + cardNum + "张      购买");
        }
        mCardGet2.setVisibility(View.GONE);

        if (entity.getCardNum() > hasCardNum) {//卡片数量不足
            mCardGet.setTextColor(Color.parseColor("#848484"));
            mCardGet.setBackground(getResources().getDrawable(R.drawable.card_cannot_get));
            playGiftContent("这是一张稀有卡片，需要累计兑换过" + entity.getCardNum() + "张卡片才能获得");
        } else {//卡片数量足够
            mCardGet.setTextColor(Color.parseColor("#FFFFFF"));
            mCardGet.setBackground(getResources().getDrawable(R.drawable.card_get));
            playGiftContent("你太棒了，现在可以领取这张稀有卡片啦");
        }
    }

    /**
     * 普通卡片购买按钮
     */
    private void cardGet2(int credit) {
        mCardShow.setVisibility(View.GONE);
        mCardShow2.setVisibility(View.GONE);
        mCardGet.setVisibility(View.GONE);
        mCardGet2.setVisibility(View.VISIBLE);
        if (1 == (credit + "").length()) {
            mCardGet2.setText(credit + "           购买");
        } else if (2 == (credit + "").length()) {
            mCardGet2.setText(credit + "         购买");
        } else if (3 == (credit + "").length()) {
            mCardGet2.setText(credit + "        购买");
        }
    }

    public void playGiftContent(String content) {
        try {
            AIManager.getInstance(getApplicationContext()).playTextWithStr(content, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCardChanged(List<CardEntity> receiveList, List<CardEntity> propList, int credit, int hasCardNum) {

    }

    @Override
    public void onCardGetError(int code) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * @param credit
     * @param id     // 1, # 卡类型： 1普通卡 2稀有卡 3道具卡
     * @param type
     */
    @Override
    public void onCardReceive(final int credit, final int id, final int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                playGiftContent("恭喜你，兑换成功");
                allCredit = credit + "";
                mAllCredit.setText(allCredit);
                entity.setPerson(entity.getPerson() + 1);
                mPeopleText.setText(entity.getPerson() + "人已获得");
                entity.setStatus(1);//状态 (0未拥有，1.已拥有)
                Glide.with(GetCardActivity.this).load(entity.getUrl()).placeholder(R.drawable.card_img_default).into(mCardImg);
                if (1 == type) {
                    hasCardNum++;
                }
                //兑换成功了，肯定是第一次，所以必定进入炫耀加学分界面
                cardShow2(entity.getShareCredit());
            }
        });
    }

    @Override
    public void onCardReceiveError(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                ToastGift.makeText(GetCardActivity.this, "购买失败，请重新尝试", Toast.LENGTH_LONG).show();
                Log.i(TAG, "onCardReceiveError code=" + code);
            }
        });

    }

    @Override
    public void onShareReceive(final int addCredit, final int id, final int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                playGiftContent("你真棒，已经发送给家长啦");
                allCredit = String.valueOf(Integer.parseInt(allCredit) + addCredit);
                mAllCredit.setText(allCredit);
                entity.setShareCredit(0);//分享可以得到的学分（已分享过的这里返回0即可）
                cardShow();
            }
        });
    }

    @Override
    public void onShareReceiveError(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                ToastGift.makeText(GetCardActivity.this, "卡片分享失败！", Toast.LENGTH_LONG).show();
                Log.i(TAG, "onCardReceiveError code=" + code);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageManager.unRegisterCardCallback(this);
    }

    public void showDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.show();
        } else {
            loadingDialog.show();
        }
    }

    public void cancelDialog() {
        if (loadingDialog != null) {
            loadingDialog.cancel();
            loadingDialog = null;
        }
    }
}
