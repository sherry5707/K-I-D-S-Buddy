package com.kinstalk.her.qchat.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.adapter.PropAdapter;
import com.kinstalk.her.qchatmodel.entity.CardEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bean on 2018/9/18.
 */

public class PropFragment extends Fragment {
    private static String TAG = "GiftActivityLog";
    View view;
    private List<CardEntity> mPKPropInfo = new ArrayList<>(3);
    private PropAdapter mPropAdapter;
    private Context mContext;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_prop, null);
        mContext = getActivity();
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mPropAdapter = new PropAdapter(mContext, mPKPropInfo);
        recyclerView = (RecyclerView) view.findViewById(R.id.prop_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayout.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mPropAdapter);
    }

    public void updateData(List<CardEntity> PKPropInfo) {
        if (null != mPKPropInfo && mPKPropInfo.size() > 0) {
            mPKPropInfo.clear();
        }
        mPKPropInfo.addAll(PKPropInfo);
        if (null != mPropAdapter) {
            mPropAdapter.notifyDataSetChanged();
        }
    }
}
