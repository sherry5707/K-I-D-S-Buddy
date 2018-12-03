package com.kinstalk.her.qchat.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
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
import com.kinstalk.qloveaicore.AIManager;

import java.util.List;

/**
 * Created by bean on 2018/9/20.
 */

public class GetPropCardActivity extends Activity implements View.OnClickListener, cardCallback {
    private static String TAG = "GiftActivityLog";
    private CardEntity entity;//卡片数据
    private ImageButton back;
    private TextView mPeopleText;//多少人获得
    private ImageView mAvatar;//三个人的头像
    private TextView mAllCredit;//学分总数
    private TextView mContent;//描述内容
    private RelativeLayout mYuyin;//语音播报内容;
    private Button mYuyinBtn;//语音图标
    private String allCredit;//学分总数
    private Button mCardGet2;
    private ImageView mCardImg;//卡片图片
    private LoadingDialog loadingDialog;//加载中布局;
    private String sn = QAIConfig.getMacForSn();
    private String token = StringEncryption.generateToken();
    private static final int MAX_PROP_NUM = 30;//最多30张道具卡

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prop_card_get);
        entity = (CardEntity) getIntent().getSerializableExtra("CardEntity");
        allCredit = getIntent().getStringExtra("AllCredit");
        Log.i(TAG, "onCreate entity=" + entity.toString());
        initView();
        initData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        entity = (CardEntity) intent.getSerializableExtra("CardEntity");
        allCredit = intent.getStringExtra("AllCredit");
        initData();
    }

    private void initData() {
        if (entity.getCardNums() > 0) {
            Glide.with(this).load(entity.getUrl()).placeholder(R.drawable.card_img_default).into(mCardImg);
        } else {
            Glide.with(this).load(entity.getUrl()).asBitmap().transform(new GrayscaleTransformation(this)).placeholder(R.drawable.card_img_default).into(mCardImg);
        }
        //头像
        Glide.with(this).load(entity.getPersonUrl()).placeholder(R.drawable.card_avatar).into(mAvatar);
        mPeopleText.setText(entity.getPerson() + "人已获得");
        mContent.setText(entity.getCardDesp());
        mAllCredit.setText(allCredit);
        if (1 == (entity.getGetCredit() + "").length()) {
            mCardGet2.setText(entity.getGetCredit() + "           购买");
        } else if (2 == (entity.getGetCredit() + "").length()) {
            mCardGet2.setText(entity.getGetCredit() + "         购买");
        } else if (3 == (entity.getGetCredit() + "").length()) {
            mCardGet2.setText(entity.getGetCredit() + "        购买");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageManager.unRegisterCardCallback(this);
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
        mCardGet2 = (Button) findViewById(R.id.card_get2);
        mCardGet2.setOnClickListener(this);
        mCardImg = (ImageView) findViewById(R.id.card_img);
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
            case R.id.card_get2://卡片购买
                if (entity.getCardNums() >= MAX_PROP_NUM) {
                    ToastGift.makeText(GetPropCardActivity.this, "最多可以购买30张该道具卡哦！", Toast.LENGTH_LONG).show();
                    return;
                }
                //普通卡片先判断学分是否够,不够就去挣学分
                if (entity.getGetCredit() > Integer.parseInt(allCredit)) {
                    playGiftContent("学分不够，一起去挣学分吧");
                    Intent i = new Intent(this, DialogTaskActivity.class);
                    i.putExtra("is_success", false);
                    startActivity(i);
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

    @Override
    public void onCardChanged(List<CardEntity> receiveList, List<CardEntity> propList, int credit, int hasCardNum) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
            }
        });
    }

    @Override
    public void onCardGetError(int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
            }
        });
    }

    @Override
    public void onCardReceive(final int credit, int id, final int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                if (3 == type) {
                    allCredit = credit + "";
                    mAllCredit.setText(allCredit);
                    entity.setCardNums(entity.getCardNums() + 1);
                    if (1 == entity.getCardNums()) {//如果获取道具卡成功后且数量为1张时候，人数对应+1,图片变色
                        Glide.with(GetPropCardActivity.this).load(entity.getUrl()).placeholder(R.drawable.card_img_default).into(mCardImg);
                        entity.setPerson(entity.getPerson() + 1);
                        mPeopleText.setText(entity.getPerson() + "人已获得");
                    }
                }
            }
        });
    }

    @Override
    public void onCardReceiveError(int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
            }
        });
    }

    @Override
    public void onShareReceive(int credit, int id, int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
            }
        });
    }

    @Override
    public void onShareReceiveError(int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
            }
        });
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

    public void playGiftContent(String content) {
        try {
            AIManager.getInstance(getApplicationContext()).playTextWithStr(content, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
