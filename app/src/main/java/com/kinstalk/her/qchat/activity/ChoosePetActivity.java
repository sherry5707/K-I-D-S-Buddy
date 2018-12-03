package com.kinstalk.her.qchat.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.kinstalk.her.qchat.PKBaseActivity;
import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.adapter.PetChooseAdapter;
import com.kinstalk.her.qchat.utils.CountlyUtils;
import com.kinstalk.her.qchat.utils.DepthPageTransformer;
import com.kinstalk.her.qchat.utils.MusicUtil;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bean on 2018/8/21.
 */

public class ChoosePetActivity extends PKBaseActivity implements View.OnClickListener, pkInfoCallback {
    private ImageButton mBack;
    private ViewPager mViewpager;
    private ImageButton mPetUse;
    private PetChooseAdapter mAdapter;
    private List<PKPetInfo> petList = new ArrayList<>(4);
    private PKUserInfo user;
    private int type;//宠物类型,0,正在使用，1未使用，2未拥有
    private int pagerNum = 0;//当前的坐标,默认第一页

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        QchatApplication.getApplicationInstance().addActivity(this);
        setContentView(R.layout.activity_pet_choose);
        initData();
        initViews();
    }

    private void initData() {
        user = (PKUserInfo) getIntent().getSerializableExtra("userInfo");
        petList = (List<PKPetInfo>) getIntent().getSerializableExtra("petList");
        MessageManager.registerPKInfoCallback(this);
        //mAdapter.notifyDataSetChanged(petList, user);
    }

    private void initViews() {
        mBack = (ImageButton) findViewById(R.id.choose_back);
        mBack.setOnClickListener(this);
        mPetUse = (ImageButton) findViewById(R.id.pet_use);
        mPetUse.setOnClickListener(this);
        mViewpager = (ViewPager) findViewById(R.id.pet_viewpager);
        mAdapter = new PetChooseAdapter(this, petList, user);
        mViewpager.setAdapter(mAdapter);
        mViewpager.setOffscreenPageLimit(3);
        mViewpager.setPageTransformer(true, new DepthPageTransformer());

        //判断第一页宠物类型
        if (user.getPetId() == petList.get(pagerNum).getId()) {
            type = 0;
        } else if (petList.get(0).isIfOwn()) {
            type = 1;
        } else {
            type = 2;
        }
        updateBottomBtn(type);
        mViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                MusicUtil.getInstance().playUrl(ChoosePetActivity.this, MUSIC_BUTTON);
                pagerNum = position;
                if (user.getPetId() == petList.get(position).getId()) {
                    type = 0;
                } else if (petList.get(position).isIfOwn()) {
                    type = 1;
                } else {
                    type = 2;
                }
                updateBottomBtn(type);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    //宠物类型,0,正在使用，1未使用，2未拥有
    private void updateBottomBtn(int type) {
        if (0 == type) {
            mPetUse.setBackground(getDrawable(R.drawable.pet_use));
        } else if (1 == type) {
            mPetUse.setBackground(getDrawable(R.drawable.pet_not_use));
        } else if (2 == type) {
            mPetUse.setBackground(getDrawable(R.drawable.pet_not_has));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choose_back:
                MusicUtil.getInstance().playUrl(this, MUSIC_BUTTON);
                finish();
                break;
            case R.id.pet_use:
                if (0 == type) {
                } else if (1 == type) {
                    MusicUtil.getInstance().playUrl(this, MUSIC_BUTTON);
                    //未使用
                    showDialog();
                    QchatThreadManager.getInstance().start(new Runnable() {
                        @Override
                        public void run() {
                            Api.postChoosePet(token, sn, petList.get(pagerNum).getId());
                        }
                    });
                    Map<String, String> segmentation = new HashMap<>();
                    segmentation.put("petid", petList.get(pagerNum).getId() + "");
                    segmentation.put("petname", petList.get(pagerNum).getName());
                    CountlyUtils.countlyRecordEvent("game", "t_game_pet_card", segmentation, 1);
                    //Countly.sharedInstance().recordEvent("game", "t_game_pet_card_" + petList.get(pagerNum).getId());
                } else if (2 == type) {
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                finish();
            }
        });
    }

    @Override
    public void onPKChoosePetError(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                ToastGift.makeText(ChoosePetActivity.this, "选择宠物失败！", Toast.LENGTH_LONG).show();
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
}
