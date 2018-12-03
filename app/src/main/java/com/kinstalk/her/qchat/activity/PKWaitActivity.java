package com.kinstalk.her.qchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.PKBaseActivity;
import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.utils.CropCircleTransformation;
import com.kinstalk.her.qchat.utils.Utils;
import com.kinstalk.her.qchat.view.ToastGift;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.model.pkInfoCallback;
import com.kinstalk.her.qchatmodel.entity.CardEntity;
import com.kinstalk.her.qchatmodel.entity.PKPetInfo;
import com.kinstalk.her.qchatmodel.entity.PKPropInfo;
import com.kinstalk.her.qchatmodel.entity.PKQuestionInfo;
import com.kinstalk.her.qchatmodel.entity.PKUserInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by bean on 2018/8/22.
 */

public class PKWaitActivity extends PKBaseActivity implements pkInfoCallback {
    private PKUserInfo user;
    private ImageView mPetImg;//宠物头像
    private ImageView mAvatarImg;//人物头像
    private TextView mName;//名称
    private TextView mTime;//倒计时
    private int time = 0;
    private int allTime;//总时间，随机数
    private TextView mRemind;//提醒消息
    private Handler handler = new Handler();
    private List<PKQuestionInfo> questionInfo = new ArrayList<>(10);
    private List<PKPropInfo> propInfo = new ArrayList<>(4);
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (Utils.isAtHome(PKWaitActivity.this)) {//如果再游戏过程中回到主界面，直接销毁
                Log.i(TAG, "回到主界面，结束游戏等待");
                finish();
            }
            if (allTime == time) {
                if (questionInfo.size() > 0) {
                    Intent intent = new Intent();
                    intent.putExtra("questionInfo", (Serializable) questionInfo);
                    intent.putExtra("propInfo", (Serializable) propInfo);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    ToastGift.makeText(PKWaitActivity.this, "进入游戏失败，请重试！", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
            time++;
            if (time < 10) {
                mTime.setText("00:0" + time);
            } else {
                mTime.setText("00:" + time);
            }
            handler.postDelayed(runnable, 1000);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QchatApplication.getApplicationInstance().addActivity(this);
        setContentView(R.layout.activity_pk_wait);
        user = (PKUserInfo) getIntent().getSerializableExtra("userInfo");
        Random random3 = new Random();
        allTime = random3.nextInt(6) + 5;
        initViews();
        initData();
    }

    private void initData() {
        handler.postDelayed(runnable, 1000);
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                Api.postPKQuestion(token, sn);
            }
        });
    }

    private void initViews() {
        mPetImg = (ImageView) findViewById(R.id.img_pet);
        mAvatarImg = (ImageView) findViewById(R.id.img_avatar);
        mName = (TextView) findViewById(R.id.pk_name);
        Glide.with(this)
                .load(user.getPetUrl())
                .asBitmap()
                .placeholder(R.drawable.img_pet_default)
                .into(mPetImg);
        Glide.with(this)
                .load(user.getAvatar())
                .asBitmap()
                .transform(new CropCircleTransformation(this))
                .placeholder(R.drawable.avatar_pk_default)
                .into(mAvatarImg);
        mName.setText(user.getNickName());
        mTime = (TextView) findViewById(R.id.time);
        mRemind = (TextView) findViewById(R.id.remind);
        switch (new Random().nextInt(5) + 1) {
            case 1:
                mRemind.setText("小微提示：答对题数越多，获得稀有卡的机会就越大！");
                break;
            case 2:
                mRemind.setText("小微提示：答题时，左侧的道具卡也许可以帮助到你呢");
                break;
            case 3:
                mRemind.setText("小微提示：最后三道题，答题时间会缩减一半，注意时间哦！");
                break;
            case 4:
                mRemind.setText("小微提示：在报名页携带宠物卡，更容易获得稀有卡片奖励！");
                break;
            case 5:
                mRemind.setText("小微提示：10道题目都答对者，答快者胜~");
                break;
        }
        MessageManager.registerPKInfoCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != handler) {
            handler.removeCallbacks(runnable);
            runnable = null;
        }
        MessageManager.unRegisterPKInfoCallback(this);
        QchatApplication.getApplicationInstance().removeActivity(this);
    }

    @Override
    public void onPKInfoChanged(List<PKPetInfo> petList, PKUserInfo user) {

    }

    @Override
    public void onPKInfoGetError(int code) {

    }

    @Override
    public void onPKChoosePet(int id) {

    }

    @Override
    public void onPKChoosePetError(int code) {

    }

    @Override
    public void onPKQuestionChanged(final List<PKQuestionInfo> questionList, final List<PKPropInfo> propList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                questionInfo = questionList;
                propInfo = propList;
                Log.i(TAG, "onPKQuestionChanged questionList=" + questionList.toString() + " propList=" + propList.toString());
            }
        });
    }

    @Override
    public void onPKQuestionGetError(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onPKQuestionGetError  code=" + code);
            }
        });
    }

    @Override
    public void onPKRewardChanged(int credit, List<PKPropInfo> propInfos, List<CardEntity> cardEntities, int ranking) {

    }

    @Override
    public void onPKRewardGetError(int code) {

    }

    @Override
    public void onPKPropUse(PKPropInfo pkPropInfo) {

    }

    @Override
    public void onPKPropUseError(int code) {

    }
}
