package com.kinstalk.her.qchat.homework;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.fragment.NewMessageCallback;
import com.kinstalk.her.qchat.frags.QBaseFragment;
import com.kinstalk.her.qchat.messaging.XgPushHelper;
import com.kinstalk.her.qchat.view.TouchableRecyclerView;
import com.kinstalk.her.qchat.voice.RingPlayer;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.model.homeCallback;
import com.kinstalk.her.qchatmodel.entity.QHomeworkMessage;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.List;

public class WorkFragment extends QBaseFragment implements MessagesListAdapter.OnLoadMoreListener,
        homeCallback {

    private String TAG = WorkFragment.class.getSimpleName();
    private TouchableRecyclerView recyclerView;
    private WorkListAdapter adapter;
    private NewMessageCallback newMessageCallback;
    private List<QHomeworkMessage> workMessageList;
    private TextView noTextView;
    private BroadcastReceiver UpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_TIME_CHANGED)) {
                getWorkList();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View rootView = inflater.inflate(R.layout.activity_work, container, false);
        initView(rootView);
        initData();
        return rootView;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        getContext().unregisterReceiver(UpdateReceiver);
        MessageManager.unRegisterHomeworkCallback(this);
    }

    public void initData() {
        Log.d(TAG, "initData: ");
        MessageManager.registerHomeworkCallback(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        getContext().registerReceiver(UpdateReceiver, filter);
        getWorkList();
    }

    public void initView(View rootView) {
        Log.d(TAG, "initView: ");
        Log.d("WorkListAdapter", "initView: ");
//        recyclerView = (TouchableRecyclerView) rootView.findViewById(R.id.work_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new WorkListAdapter(recyclerView.getContext(), recyclerView);
        recyclerView.setAdapter(adapter);

        noTextView = (TextView) rootView.findViewById(R.id.work_no_view);
    }

    public void scrollToTop() {
        recyclerView.smoothScrollToPosition(0);
    }

    public void getWorkList() {

        workMessageList = MessageManager.getAllHomeworkMessage();
        Log.d(TAG, "getWorkList: " + workMessageList);

        getActivity().runOnUiThread(new Runnable() { //imitation of internet connection
            @Override
            public void run() {

                if (null != workMessageList) {
                    noTextView.setVisibility(workMessageList.size() > 0 ? View.GONE : View.VISIBLE);
                } else {
                    noTextView.setVisibility(View.VISIBLE);
                }

                adapter.refreshData(workMessageList);
            }
        });
    }

    @Override
    public void onHomeworkChanged(Boolean status,List<QHomeworkMessage> workMsg) {
        Log.d(TAG, "onHomeworkChanged: ");
        getWorkList();

        if (status && null != workMessageList && workMessageList.size() > 0) {
            if (!QchatApplication.isWakeupActive() || !QchatApplication.isSleepingActive()) {
                //非起床睡觉播放故事
                RingPlayer.getInstance().startRing();
            }
            newMessageCallback.receiverNewMessage(WorkFragment.class.getSimpleName(), workMessageList.get(0).getHomeWorkEntity().getContent());
        }
    }

    public void setNewMessageCallback(NewMessageCallback newMessageCallback) {
        this.newMessageCallback = newMessageCallback;
    }

    public void unSetNewMessageCallback() {
        this.newMessageCallback = null;
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {

    }

    public void notifyRead() {
        if (null != workMessageList && workMessageList.size() > 0) {
            XgPushHelper.getInstance().homeworkACK(workMessageList.get(0).getHomeWorkEntity().getHomeworkid());
        }
    }
}