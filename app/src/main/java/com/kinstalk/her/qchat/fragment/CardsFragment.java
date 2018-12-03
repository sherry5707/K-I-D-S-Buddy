package com.kinstalk.her.qchat.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kinstalk.her.qchat.GiftActivity;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.view.NoScrollViewPager;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.model.cardCallback;
import com.kinstalk.her.qchatmodel.entity.CardEntity;
import com.kinstalk.her.qchatmodel.entity.PKPropInfo;

import java.util.ArrayList;
import java.util.List;

import ly.count.android.sdk.Countly;

/**
 * Created by bean on 2018/7/2.
 */

public class CardsFragment extends Fragment implements View.OnClickListener, cardCallback {
    private static String TAG = "GiftActivityLog";
    View view;
    private List<Fragment> fragments = new ArrayList<>(2);
    private List<CardEntity> mCardEntity = new ArrayList<>(26);//宠物卡
    private List<CardEntity> mPKPropInfo = new ArrayList<>(3);//道具卡
    private NoScrollViewPager mViewpager;
    private CardPagerAdapter mPagerAdapter;
    private CardFragment mCardFragment;
    private PropFragment mPropFragment;
    private Button mCardBtn;
    private Button mPropBtn;//

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_cards, null);
        initViews(view);
        initData();
        Countly.sharedInstance().recordEvent("gift", "t_gift_store_collection");
        return view;
    }

    private void initData() {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                String sn = QAIConfig.getMacForSn();
                String token = StringEncryption.generateToken();
                Api.postCard(token, sn);
            }
        });
    }

    private void initViews(View view) {
        mViewpager = (NoScrollViewPager) view.findViewById(R.id.card_viewpager);
        mViewpager.setOverScrollMode(mViewpager.OVER_SCROLL_NEVER);
        mCardBtn = (Button) view.findViewById(R.id.card_btn);
        mCardBtn.setOnClickListener(this);
        mPropBtn = (Button) view.findViewById(R.id.prop_btn);
        mPropBtn.setOnClickListener(this);
        mCardFragment = new CardFragment();
        mPropFragment = new PropFragment();
        fragments.add(mCardFragment);
        fragments.add(mPropFragment);
        mPagerAdapter = new CardPagerAdapter(getActivity().getSupportFragmentManager());
        mViewpager.setAdapter(mPagerAdapter);
        mViewpager.setOffscreenPageLimit(2);
        MessageManager.registerCardCallback(this);
    }

    @Override
    public void onCardChanged(final List<CardEntity> receiveList, final List<CardEntity> propList, final int credit, final int hasCardNum) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onCardChanged receiveList=" + receiveList.toString());
                Log.i(TAG, "onCardChanged propList=" + propList.toString());
                Log.i(TAG, "onCardChanged receiveList=" + "credit=" + credit
                        + "  hasCardNum=" + hasCardNum);
                ((GiftActivity) getActivity()).cancelDialog();
                if (receiveList.size() == 0) {
                    return;
                }
                mCardEntity = receiveList;
                mPKPropInfo = propList;
                ((GiftActivity) getActivity()).updateCreditNum(credit);
                ((GiftActivity) getActivity()).setHasCardNum(hasCardNum);
                mCardFragment.updateData(mCardEntity);
                mPropFragment.updateData(mPKPropInfo);
            }
        });
    }

    @Override
    public void onCardGetError(final int code) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((GiftActivity) getActivity()).cancelDialog();
                Log.i(TAG, "onCardGetError  code=" + code);
            }
        });
    }

    /**
     * @param credit
     * @param id
     * @param type   // 1, # 卡类型： 1普通卡 2稀有卡 3道具卡
     */
    @Override
    public void onCardReceive(final int credit, final int id, final int type) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((GiftActivity) getActivity()).cancelDialog();
                ((GiftActivity) getActivity()).updateCreditNum(credit);
                //如果获取的是普通卡片，这里加1
                if (1 == type) {
                    ((GiftActivity) getActivity()).setHasCardNum(((GiftActivity) getActivity()).getHasCardNum() + 1);
                }
                if (1 == type || 2 == type) {
                    for (int i = 0; i < mCardEntity.size(); i++) {
                        if (id == mCardEntity.get(i).getId()) {
                            mCardEntity.get(i).setStatus(1);//状态 (0未拥有，1.已拥有)
                            mCardEntity.get(i).setPerson(mCardEntity.get(i).
                                    getPerson() + 1);
                        }
                    }
                    mCardFragment.updateData(mCardEntity);
                    Log.i(TAG, "宠物卡片兑换成功");
                }

                if (3 == type) {
                    for (int i = 0; i < mPKPropInfo.size(); i++) {
                        if (id == mPKPropInfo.get(i).getId()) {
                            mPKPropInfo.get(i).setCardNums(mPKPropInfo.get(i).getCardNums() + 1);
                            if (1 == mPKPropInfo.get(i).getCardNums()) {//如果数量为1，所以之前没有卡片，人数+1
                                mPKPropInfo.get(i).setPerson(mPKPropInfo.get(i).
                                        getPerson() + 1);
                            }
                        }
                    }
                    mPropFragment.updateData(mPKPropInfo);
                    Log.i(TAG, "道具卡片兑换成功");
                }

            }
        });
    }

    @Override
    public void onCardReceiveError(final int code) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((GiftActivity) getActivity()).cancelDialog();
                Log.i(TAG, "onCardReceiveError code=" + code);
            }
        });

    }

    //卡片分享成功
    @Override
    public void onShareReceive(final int addCredit, final int id, final int type) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //刷新学分数
                int oldCredit = Integer.parseInt(((GiftActivity) getActivity()).getCreditNum());
                ((GiftActivity) getActivity()).updateCreditNum(oldCredit + addCredit);
                for (int i = 0; i < mCardEntity.size(); i++) {
                    if (id == mCardEntity.get(i).getId()) {
                        mCardEntity.get(i).setShareCredit(0);//分享可以得到的学分（已分享过的这里返回0即可）
                    }
                }
                Log.i(TAG, "CardsFragment  onShareReceive mCardEntity=" + mCardEntity.get(0).toString());
                mCardFragment.updateData(mCardEntity);
            }
        });
    }

    //卡片分享失败
    @Override
    public void onShareReceiveError(int code) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.card_btn:
                mViewpager.setCurrentItem(0);
                mCardBtn.setTextColor(Color.parseColor("#D08002"));
                mCardBtn.setBackground(getResources().getDrawable(R.drawable.gift_title_item_bg2));
                mPropBtn.setTextColor(Color.parseColor("#C35527"));
                mPropBtn.setBackground(null);
                break;
            case R.id.prop_btn:
                mViewpager.setCurrentItem(1);
                mPropBtn.setTextColor(Color.parseColor("#D08002"));
                mPropBtn.setBackground(getResources().getDrawable(R.drawable.gift_title_item_bg2));
                mCardBtn.setTextColor(Color.parseColor("#C35527"));
                mCardBtn.setBackground(null);
                break;

        }
    }

    private class CardPagerAdapter extends FragmentPagerAdapter {

        public CardPagerAdapter(android.support.v4.app.FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageManager.unRegisterCardCallback(this);
    }
}
