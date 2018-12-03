package com.kinstalk.her.qchat.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.adapter.CardAdapter;
import com.kinstalk.her.qchatmodel.entity.CardEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bean on 2018/7/4.
 */

public class CardFragment extends Fragment {
    private static String TAG = "GiftActivityLog";
    View view;
    private RecyclerView recyclerView;
    private CardAdapter cardAdapter;
    private List<CardEntity> mCardEntity = new ArrayList<>(26);

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_card, null);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        cardAdapter = new CardAdapter(getActivity(), mCardEntity);
        recyclerView = (RecyclerView) view.findViewById(R.id.card_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayout.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(cardAdapter);
    }

    public void updateData(List<CardEntity> cardEntities) {
        if (null != mCardEntity && mCardEntity.size() > 0) {
            mCardEntity.clear();
        }
        mCardEntity.addAll(cardEntities);
        if (null != cardAdapter) {
            cardAdapter.notifyDataSetChanged();

        }
    }
}
