package com.kinstalk.her.qchat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.adapter.ReceiveAdapter;
import com.kinstalk.her.qchatmodel.entity.GiftEntity;

import java.util.ArrayList;
import java.util.List;

import ly.count.android.sdk.Countly;

/**
 * Created by bean on 2018/5/21.
 * 已领取
 */

public class ReceiveFragment extends Fragment {
    private static String TAG = "GiftActivityLog";
    View view;
    private TextView mNoReceiveGift;
    private ListView mListView;
    private List<GiftEntity> mReceiveGift;
    private ReceiveAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_receive, null);
        initViews(view);
        Countly.sharedInstance().recordEvent("gift", "t_gift_store_gift_received");
        return view;
    }

    private void initViews(View view) {
        mNoReceiveGift = (TextView) view.findViewById(R.id.no_receive_gift);
        mListView = (ListView) view.findViewById(R.id.receive_listview);
        mReceiveGift = new ArrayList<GiftEntity>();
        mAdapter = new ReceiveAdapter(getActivity(), mReceiveGift);
        mListView.setAdapter(mAdapter);
    }


    public void updateData(List<GiftEntity> receivedList) {
        Log.i(TAG, "receivedList=" + receivedList.toString() + "   receivedList.size()=" + receivedList.size());
        if (null == mNoReceiveGift) {//防止数据回来，界面还未绘画完成
            return;
        }
        if (receivedList.size() == 0) {//没有领取的礼物
            noDataLayout();
        } else {
            hasDataLayout();
            mReceiveGift.clear();
            mReceiveGift.addAll(receivedList);
            mAdapter.notifyDataSetChanged();
        }
    }

    //没有数据的界面
    private void noDataLayout() {
        mNoReceiveGift.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
    }

    //有数据的界面
    private void hasDataLayout() {
        mNoReceiveGift.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
    }
}
