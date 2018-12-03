package com.kinstalk.her.qchat;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.activity.ChoosePetActivity;
import com.kinstalk.her.qchat.activity.DialogTaskActivity;
import com.kinstalk.her.qchat.activity.PKMainActivity;
import com.kinstalk.her.qchat.activity.PKWaitActivity;
import com.kinstalk.her.qchat.utils.CountlyUtils;
import com.kinstalk.her.qchat.utils.CropCircleTransformation;
import com.kinstalk.her.qchat.utils.MusicUtil;
import com.kinstalk.her.qchat.view.ToastGift;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.SystemTool;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.model.pkInfoCallback;
import com.kinstalk.her.qchatmodel.entity.CardEntity;
import com.kinstalk.her.qchatmodel.entity.PKPetInfo;
import com.kinstalk.her.qchatmodel.entity.PKPropInfo;
import com.kinstalk.her.qchatmodel.entity.PKQuestionInfo;
import com.kinstalk.her.qchatmodel.entity.PKUserInfo;


import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bean on 2018/8/21.
 */

public class PKStartActivity extends PKBaseActivity implements View.OnClickListener, pkInfoCallback {
    private ImageButton mBack;
    private ImageView mStart;
    private TextView mCount;//剩余次数
    private ImageView mPetImg;//宠物头像
    private ImageView mAvatarImg;//人物头像
    private TextView mName;//名称
    private List<PKPetInfo> petList;
    private PKUserInfo user;
    private AnimationSet animationSet;
    private static final int PLAY_NEED_CREDIT = 100;//每次需要100学分
    private TextView mCredit;//总学分
    private RelativeLayout mAddCreditLayout;//添加的学分布局
    private TextView mAddCredit;//添加的学分
    private LinearLayout mUseGreditLayout;
    private TextView mUseGreditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        QchatApplication.getApplicationInstance().addActivity(this);
        setContentView(R.layout.activity_pk_start);
        initViews();
        isShowAddAnimation(getIntent().getIntExtra("add_credit", 0));
        initData();
    }

    private void initData() {
        showDialog();
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                Api.postPKInfo(token, sn);
            }
        });
    }

    private void initViews() {
        mBack = (ImageButton) findViewById(R.id.back_pk);
        mBack.setOnClickListener(this);
        mStart = (ImageView) findViewById(R.id.btn_pk_start);
        mStart.setOnClickListener(this);
        Glide.with(this).
                load(R.drawable.game_start).
                asGif().//注意:这里显示的指明了要加载的是gif图片,当然即使不指明,glide也会自己判断.
                into(mStart);
        mCount = (TextView) findViewById(R.id.pk_count);
        mPetImg = (ImageView) findViewById(R.id.img_pet);
        mPetImg.setOnClickListener(this);
        mAvatarImg = (ImageView) findViewById(R.id.img_avatar);
        mName = (TextView) findViewById(R.id.pk_name);
        mCredit = (TextView) findViewById(R.id.credit_num);
        mAddCreditLayout = (RelativeLayout) findViewById(R.id.add_credit_layout);
        mAddCredit = (TextView) findViewById(R.id.add_credit_num);

        mUseGreditLayout = (LinearLayout) findViewById(R.id.pk_use_credit_layout);
        mUseGreditText = (TextView) findViewById(R.id.pk_use_credit);

        animationSet = new AnimationSet(true);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 120, 46);
        translateAnimation.setDuration(2000);
        translateAnimation.setRepeatCount(0);
        animationSet.addAnimation(translateAnimation);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAddCreditLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        MessageManager.registerPKInfoCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageManager.unRegisterPKInfoCallback(this);
        MusicUtil.getInstance().stop();
        QchatApplication.getApplicationInstance().removeActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MusicUtil.getInstance().playUrl(this, MUSIC_ENTER_BACKGROUND);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_pk:
                finish();
                break;
            case R.id.btn_pk_start:
                if (SystemTool.isNetworkAvailable(this)) {
                    //开始游戏,请求到数据之后跳转
                    if (null != user) {//请求失败，use为null
                        if (user.getNowTimes() >= user.getAllTimes() && user.getAllCredit() < PLAY_NEED_CREDIT) {
                            playTTS("学分不足，你可以通过做任务获取学分！");
                            Intent i = new Intent(this, DialogTaskActivity.class);
                            i.putExtra("is_success", false);
                            startActivity(i);
                            return;
                        }
                        Intent waitIntent = new Intent(this, PKWaitActivity.class);
                        waitIntent.putExtra("userInfo", user);
                        startActivityForResult(waitIntent, 0);
                    }
                } else {
                    //playTTS("网络断了，先连网再玩游戏吧。");
                    ToastGift.makeText(PKStartActivity.this, "网络断了，先连网再玩游戏吧!", Toast.LENGTH_LONG).show();

                }
                break;
            case R.id.img_pet:
                //选择宠物
                if (null != petList && petList.size() > 0) {
                    Intent intent = new Intent(this, ChoosePetActivity.class);
                    intent.putExtra("userInfo", user);
                    intent.putExtra("petList", (Serializable) petList);
                    startActivity(intent);
                }
                break;
        }
    }

    //获取用户信息和宠物信息成功
    @Override
    public void onPKInfoChanged(final List<PKPetInfo> petList, final PKUserInfo user) {
        this.user = user;
        this.petList = petList;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                updateUI();
                Log.i(TAG, "onPKInfoChanged petList=" + petList.toString() + "user=" + user.toString());
            }
        });
    }

    private void updateUI() {
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
        mCredit.setText(user.getAllCredit() + "");
        if (user.getNowTimes() < user.getAllTimes()) {
            mCount.setVisibility(View.VISIBLE);
            mUseGreditLayout.setVisibility(View.GONE);
            mCount.setText("今日免费考试" + user.getNowTimes() + "/" + user.getAllTimes() + "次");
        } else {
            mCount.setVisibility(View.GONE);
            mUseGreditLayout.setVisibility(View.VISIBLE);
            mUseGreditText.setText(PLAY_NEED_CREDIT + "");
        }
    }

    private void isShowAddAnimation(int addCredit) {
        if (addCredit > 0) {
            mAddCreditLayout.setVisibility(View.VISIBLE);
            mAddCredit.setText("+" + addCredit);
            mAddCreditLayout.startAnimation(animationSet);
        } else {
            mAddCreditLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPKInfoGetError(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                Log.i(TAG, "onPKInfoGetError  code=" + code);
            }
        });
    }

    @Override
    public void onPKChoosePet(final int id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                //选择宠物成功之后，修改数据源和UI
                updateList(id);
                Log.i(TAG, "onPKChoosePet id=" + id);
            }
        });
    }

    private void updateList(int id) {
        user.setPetId(id);
        for (int i = 0; i < petList.size(); i++) {
            if (id == petList.get(i).getId()) {
                user.setPetUrl(petList.get(i).getImgUrl());
                updateUI();
                return;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int addCredit = intent.getIntExtra("add_credit", 0);
        if (null != user) {//如果第一次进入没有网络，再次调用这个方法use会为空
            user.setAllCredit(user.getAllCredit() + addCredit);
            mCredit.setText(user.getAllCredit() + "");
        }
        isShowAddAnimation(addCredit);
        initData();
    }

    @Override
    public void onPKChoosePetError(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                Log.i(TAG, "onPKChoosePetError  code=" + code);
            }
        });
    }

    @Override
    public void onPKQuestionChanged(List<PKQuestionInfo> questionList, List<PKPropInfo> propList) {

    }

    @Override
    public void onPKQuestionGetError(int code) {

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult requestCode=" + requestCode);
            if (0 == requestCode) {
                //进入游戏成功
                if (user.getNowTimes() < user.getAllTimes()) {//次数没满，加次数
                    user.setNowTimes(user.getNowTimes() + 1);
                    updateUI();
                } else {//次数满了减学分
                    user.setAllCredit(user.getAllCredit() - PLAY_NEED_CREDIT);
                    Map<String, String> segmentation = new HashMap<>();
                    segmentation.put("type", "paid");
                    CountlyUtils.countlyRecordEvent("game", "t_game_start_game", segmentation, 1);
                    //Countly.sharedInstance().recordEvent("game", "t_game_use_credit");
                }

                Intent mainIntent = new Intent(PKStartActivity.this, PKMainActivity.class);
                mainIntent.putExtra("userInfo", user);
                List<PKQuestionInfo> questionInfo = new ArrayList<>(10);
                List<PKPropInfo> propInfo = new ArrayList<>(3);
                questionInfo = (List<PKQuestionInfo>) data.getExtras().getSerializable("questionInfo");
                propInfo = (List<PKPropInfo>) data.getExtras().getSerializable("propInfo");
                mainIntent.putExtra("questionInfo", (Serializable) questionInfo);
                mainIntent.putExtra("propInfo", (Serializable) propInfo);
                startActivity(mainIntent);
                MusicUtil.getInstance().stop();
                Map<String, String> segmentation = new HashMap<>();
                segmentation.put("type", "number");
                CountlyUtils.countlyRecordEvent("game", "t_game_start_game", segmentation, 1);
                //Countly.sharedInstance().recordEvent("game", "t_game_start_game");
            }
        }
    }
}
