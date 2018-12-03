package com.kinstalk.her.qchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.PKBaseActivity;
import com.kinstalk.her.qchat.PKStartActivity;
import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.utils.CropCircleTransformation;
import com.kinstalk.her.qchat.utils.MusicUtil;
import com.kinstalk.her.qchatmodel.entity.CardEntity;
import com.kinstalk.her.qchatmodel.entity.PKPropInfo;
import com.kinstalk.her.qchatmodel.entity.PKUserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bean on 2018/8/24.
 */

public class PKRewardActivity extends PKBaseActivity implements View.OnClickListener {
    private PKUserInfo user;
    private int credit;//赠送的学分
    private List<PKPropInfo> propInfos = new ArrayList<>();//道具卡
    private List<CardEntity> cardEntities = new ArrayList<>();//宠物卡
    private int ranking;//排名
    private TextView mRankingText;
    private ImageButton mSure;//确认按钮
    private ImageView mGetReward;//获取奖励按钮
    private ImageView mRankingBg;
    private ImageView mAvatar;
    private RelativeLayout aboveLayout;//上面的布局
    private TextView mRankingNum;//名次
    //学分奖励
    private TextView mCreditNum;
    //道具奖励
    private RelativeLayout propLayout;
    private ImageView propImg;
    private TextView propNum;
    //宠物卡奖励
    private RelativeLayout cardLayout;
    private ImageView cardImg;

    private ImageView mGameRankingBg;//游戏排名动效
    private ImageView mGameRewardBg;//游戏奖励动效

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playTTS(" ");//bug 16801 [100%][FT][PK游戏设备端]给出考试成绩时，设备还在继续播报题目内容的TTS
        QchatApplication.getApplicationInstance().addActivity(this);
        setContentView(R.layout.activity_pk_reward);
        MusicUtil.getInstance().playUrl(this, MUSIC_RESULT);
        credit = getIntent().getIntExtra("credit", 0);
        ranking = getIntent().getIntExtra("ranking", 0);
        propInfos = (List<PKPropInfo>) getIntent().getSerializableExtra("propInfos");
        cardEntities = (List<CardEntity>) getIntent().getSerializableExtra("cardEntities");
        user = (PKUserInfo) getIntent().getSerializableExtra("userInfo");
        Log.i(TAG, "PKRewardActivity user=" + user.toString());
        initViews();
        updateUi();
    }

    private void updateUi() {
        aboveLayout.setVisibility(View.GONE);
        mGameRankingBg.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(user.getAvatar())
                .asBitmap()
                .transform(new CropCircleTransformation(this))
                .placeholder(R.drawable.avatar_pk_default)
                .into(mAvatar);
        mRankingText.setText("恭喜你！你获得了第" + numToString(ranking) + "名");
        //playTTS(mRankingText.getText().toString());
        if (1 == ranking) {
            mRankingBg.setImageDrawable(getDrawable(R.drawable.pk_ranking_one));
            mRankingNum.setVisibility(View.GONE);
        } else if (2 == ranking) {
            mRankingBg.setImageDrawable(getDrawable(R.drawable.pk_ranking_two));
            mRankingNum.setVisibility(View.GONE);
        } else if (3 == ranking) {
            mRankingBg.setImageDrawable(getDrawable(R.drawable.pk_ranking_three));
            mRankingNum.setVisibility(View.GONE);
        } else {
            mRankingBg.setImageDrawable(getDrawable(R.drawable.pk_ranking_other));
            mRankingNum.setVisibility(View.VISIBLE);
            mRankingNum.setText(ranking + "");
        }
        //上层布局数据
        mCreditNum.setText(credit + "");
        if (propInfos.size() > 0) {
            if (propInfos.get(0).getId() != 0) {//Id为0是没奖励
                propLayout.setVisibility(View.VISIBLE);
                propNum.setText(propInfos.size() + "");
                Glide.with(this)
                        .load(propInfos.get(0).getUrl())
                        .asBitmap()
                        .placeholder(R.drawable.prop_card)
                        .into(propImg);
            } else {
                propLayout.setVisibility(View.GONE);
            }
        } else {
            propLayout.setVisibility(View.GONE);
        }
        if (cardEntities.size() > 0) {
            if (cardEntities.get(0).getId() != 0) {//Id为0是没奖励
                cardLayout.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(cardEntities.get(0).getUrl())
                        .asBitmap()
                        .into(cardImg);
            } else {
                cardLayout.setVisibility(View.GONE);
            }
        } else {
            cardLayout.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        mRankingText = (TextView) findViewById(R.id.ranking_text);
        mSure = (ImageButton) findViewById(R.id.sure);
        mSure.setOnClickListener(this);
        mRankingBg = (ImageView) findViewById(R.id.pk_ranking_bg);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        aboveLayout = (RelativeLayout) findViewById(R.id.above_layout);
        aboveLayout.setVisibility(View.GONE);
        mRankingNum = (TextView) findViewById(R.id.ranking_num);
        mGetReward = (ImageView) findViewById(R.id.get_reward);
        Glide.with(this).
                load(R.drawable.game_reward).
                asGif().//注意:这里显示的指明了要加载的是gif图片,当然即使不指明,glide也会自己判断.
                into(mGetReward);
        mGetReward.setOnClickListener(this);
        mCreditNum = (TextView) findViewById(R.id.credit_text);
        propLayout = (RelativeLayout) findViewById(R.id.prop_layout);
        propImg = (ImageView) findViewById(R.id.prop_img);
        propNum = (TextView) findViewById(R.id.prop_text);
        cardLayout = (RelativeLayout) findViewById(R.id.card_layout);
        cardImg = (ImageView) findViewById(R.id.card_img);
        mGameRankingBg = (ImageView) findViewById(R.id.game_ranking_bg);
        Glide.with(this).
                load(R.drawable.game_ranking_bg).
                asGif().//注意:这里显示的指明了要加载的是gif图片,当然即使不指明,glide也会自己判断.
                into(mGameRankingBg);
        mGameRewardBg = (ImageView) findViewById(R.id.game_reward_bg);
        Glide.with(this).
                load(R.drawable.game_reward_bg).
                asGif().//注意:这里显示的指明了要加载的是gif图片,当然即使不指明,glide也会自己判断.
                into(mGameRewardBg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QchatApplication.getApplicationInstance().removeActivity(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        playTTS(" ");//bug 16801 [100%][FT][PK游戏设备端]给出考试成绩时，设备还在继续播报题目内容的TTS
        credit = intent.getIntExtra("credit", 0);
        ranking = intent.getIntExtra("ranking", 0);
        propInfos = (List<PKPropInfo>) intent.getSerializableExtra("propInfos");
        cardEntities = (List<CardEntity>) intent.getSerializableExtra("cardEntities");
        user = (PKUserInfo) intent.getSerializableExtra("userInfo");
        playTTS(" ");//bug 16801 [100%][FT][PK游戏设备端]给出考试成绩时，设备还在继续播报题目内容的TTS
        MusicUtil.getInstance().playUrl(this, MUSIC_RESULT);
        updateUi();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sure:
                MusicUtil.getInstance().playUrl(this, MUSIC_BUTTON);
                Intent intent = new Intent(PKRewardActivity.this, PKStartActivity.class);
                intent.putExtra("add_credit", credit);
                startActivity(intent);
                finish();
                break;
            case R.id.get_reward:
                aboveLayout.setVisibility(View.VISIBLE);
                mGameRankingBg.setVisibility(View.GONE);
                MusicUtil.getInstance().playUrl(this, MUSIC_RECEIVE_AWARD);
                break;
        }
    }
}
