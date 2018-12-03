package com.kinstalk.her.qchat.fragment;

import com.kinstalk.her.qchat.GiftActivity;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.adapter.GiftAdapter;
import com.kinstalk.her.qchatmodel.Manager.model.giftCallback;
import com.kinstalk.her.qchatmodel.entity.GiftEntity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ly.count.android.sdk.Countly;

/**
 * Created by bean on 2018/5/21.
 * 礼物中心
 */

public class GiftFragment extends Fragment {
    private static String TAG = "GiftActivityLog";
    View view;
    private TextView mNoGift;
    private ListView mListView;
    private List<GiftEntity> mGift = new ArrayList<GiftEntity>();
    ;
    private GiftAdapter mAdapter;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_gift, null);
        mContext = getActivity();
        initViews(view);
        Countly.sharedInstance().recordEvent("gift", "t_gift_store_gift_unreceived");
        return view;
    }

    private void initViews(View view) {
        mNoGift = (TextView) view.findViewById(R.id.no_gift);
        mListView = (ListView) view.findViewById(R.id.gift_listview);
        mAdapter = new GiftAdapter(mContext, mGift);
        mListView.setAdapter(mAdapter);
    }

    public void updateData(List<GiftEntity> receiveList) {
        if (null == mNoGift) {//防止数据回来，界面还未绘画完成
            return;
        }
        Log.i(TAG, "receiveList=" + receiveList.toString() + " receiveList.size()+" + receiveList.size());
        if (receiveList.size() == 0) {//没有可领取的礼物
            noDataLayout();
        } else {
            hasDataLayout();
            mGift.clear();
            mGift.addAll(receiveList);
            mAdapter.notifyDataSetChanged();
        }
    }

    //没有数据的界面
    private void noDataLayout() {
        mNoGift.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
    }

    //有数据的界面
    private void hasDataLayout() {
        mNoGift.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.cancelDialog();
    }
}
