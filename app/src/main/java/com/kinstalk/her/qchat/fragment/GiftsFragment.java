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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kinstalk.her.qchat.GiftActivity;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.adapter.ReceiveAdapter;
import com.kinstalk.her.qchat.dialog.LoadingDialog;
import com.kinstalk.her.qchat.view.NoScrollViewPager;
import com.kinstalk.her.qchat.view.ToastGift;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatcomm.utils.SystemTool;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.model.giftCallback;
import com.kinstalk.her.qchatmodel.entity.GiftEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ly.count.android.sdk.Countly;

/**
 * 礼物中心一级页面
 * Created by bean on 2018/7/2.
 */

public class GiftsFragment extends Fragment implements View.OnClickListener, giftCallback {
    private static String TAG = "GiftActivityLog";
    View view;
    private NoScrollViewPager viewPager;
    private List<Fragment> fragments = new ArrayList<Fragment>(2);
    private GiftFragment mGiftFaragment;
    private ReceiveFragment mReceiveFragment;
    private GiftPagerAdapter mGiftPagerAdapter;
    private LoadingDialog loadingDialog;//加载中布局;

    private Button mGiftBtn;
    private Button mReceiveBtn;

    private int giftNum;//当前礼物数量


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_gifts, null);
        initViews(view);
        if (SystemTool.isNetworkAvailable(getActivity())) {
            initData();
        }
        return view;
    }

    private void initData() {
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                String sn = QAIConfig.getMacForSn();
                String token = StringEncryption.generateToken();
                Api.postGift(token, sn);
            }
        });
    }

    private void initViews(View view) {
        loadingDialog = new LoadingDialog(getActivity());
        viewPager = (NoScrollViewPager) view.findViewById(R.id.gift_viewpager);
        viewPager.setOverScrollMode(viewPager.OVER_SCROLL_NEVER);
        viewPager = (NoScrollViewPager) view.findViewById(R.id.gift_viewpager);
        mGiftBtn = (Button) view.findViewById(R.id.gift_btn);
        mGiftBtn.setOnClickListener(this);
        mReceiveBtn = (Button) view.findViewById(R.id.receive_btn);
        mReceiveBtn.setOnClickListener(this);


        mGiftFaragment = new GiftFragment();
        mReceiveFragment = new ReceiveFragment();
        fragments.add(mGiftFaragment);
        fragments.add(mReceiveFragment);
        mGiftPagerAdapter = new GiftPagerAdapter(getActivity().getSupportFragmentManager());
        viewPager.setAdapter(mGiftPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        MessageManager.registerGiftCallback(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gift_btn:
                viewPager.setCurrentItem(0);
                mGiftBtn.setTextColor(Color.parseColor("#D08002"));
                mGiftBtn.setBackground(getResources().getDrawable(R.drawable.gift_title_item_bg2));
                mReceiveBtn.setTextColor(Color.parseColor("#C35527"));
                mReceiveBtn.setBackground(null);
                Countly.sharedInstance().recordEvent("gift", "t_gift_store_gift_unreceived");
                break;
            case R.id.receive_btn:
                viewPager.setCurrentItem(1);
                mReceiveBtn.setTextColor(Color.parseColor("#D08002"));
                mReceiveBtn.setBackground(getResources().getDrawable(R.drawable.gift_title_item_bg2));
                mGiftBtn.setTextColor(Color.parseColor("#C35527"));
                mGiftBtn.setBackground(null);
                Countly.sharedInstance().recordEvent("gift", "t_gift_store_gift_received");
                break;

        }
    }

    //请求礼物列表成功
    @Override
    public void onGiftChanged(final List<GiftEntity> receiveList, final List<GiftEntity> receivedList, final int star) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setGiftNum(receiveList.size());
                notifyAllData(star, receiveList, receivedList);
                //没有播放过语音，并且是语音唤醒的
               /* if ((!((GiftActivity) getContext()).isPlayGift()) && ((GiftActivity) getContext()).getPageNumber() == 1) {
                    ((GiftActivity) getContext()).playGiftTTS();
                }*/
                Log.i(TAG, "onGiftChanged receiveList=" + receiveList.toString() + "receivedList=" + receivedList + "star=" + star);
            }
        });

    }

    //请求礼物列表失败
    @Override
    public void onGiftGetError(final int code) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastGift.makeText(getActivity(), "礼物获取失败！", Toast.LENGTH_LONG).show();
                Log.i(TAG, "onGiftGetError  code=" + code);
            }
        });
    }

    private void notifyAllData(int star, List<GiftEntity> receiveList, List<GiftEntity> receivedList) {
        updateStarNum(star);
        mGiftFaragment.updateData(receiveList);//刷新可领取礼物列表
        mReceiveFragment.updateData(receivedList);//刷新已领取
    }

    /**
     * @param action "got"领取中，点击红星的操作    "finished"点击已领取礼物   "cancel"取消领取礼物
     */
    @Override
    public void onGiftReceive(final List<GiftEntity> receiveList, final List<GiftEntity> receivedList, final int star, final String action) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                if ("finished".equals(action)) {
                    ((GiftActivity) getActivity()).playGiftContent("你真厉害，靠自己的努力，又完成一个小目标");
                }
                notifyAllData(star, receiveList, receivedList);
            }
        });
        Log.i(TAG, "onGiftReceive receiveList=" + receiveList.toString() + "receivedList=" + receivedList + "star=" + star);

    }

    //礼物领取失败
    @Override
    public void onGiftReceiveError(final int code) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastGift.makeText(getActivity(), "操作失败！", Toast.LENGTH_LONG).show();
                cancelDialog();
                Log.i(TAG, "onGiftReceiveError  code=" + code);
            }
        });

    }

    private class GiftPagerAdapter extends FragmentPagerAdapter {

        public GiftPagerAdapter(android.support.v4.app.FragmentManager supportFragmentManager) {
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
        MessageManager.unRegisterGiftCallback();
    }


    public void cancelDialog() {
        ((GiftActivity) getActivity()).cancelDialog();
    }

    //修改星星数量
    public void updateStarNum(int num) {
        ((GiftActivity) getActivity()).updateStarNum(num);
    }

    public int getGiftNum() {
        return giftNum;
    }

    public void setGiftNum(int giftNum) {
        this.giftNum = giftNum;
    }
}
