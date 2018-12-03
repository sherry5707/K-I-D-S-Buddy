package com.kinstalk.her.qchat.remind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.fragment.NewMessageCallback;
import com.kinstalk.her.qchat.frags.QBaseFragment;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.model.reminderCallback;
import com.kinstalk.her.qchatmodel.entity.QReminderMessage;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.List;

public class RemindFragment extends QBaseFragment implements MessagesListAdapter.OnLoadMoreListener,
        reminderCallback {

    private String TAG = RemindFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private RemindListAdapter adapter;
    private NewMessageCallback newMessageCallback;
    private TextView noTextView;

    private BroadcastReceiver UpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_TIME_CHANGED)) {
                getRemindList(getContext());
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_remind, container, false);
        initView(rootView);
        initData();
        return rootView;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        MessageManager.unRegisterReminderCallback(this);
        getContext().unregisterReceiver(UpdateReceiver);
    }

    public void initData() {
        Log.d(TAG, "initData: ");
        MessageManager.registerReminderCallback(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        getContext().registerReceiver(UpdateReceiver,filter);
        getRemindList(this.getContext());
    }

    public void initView(View rootView) {
        Log.d("RemindListAdapter", "initView: ");
        recyclerView = (RecyclerView) rootView.findViewById(R.id.remind_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new RemindListAdapter(recyclerView.getContext(),recyclerView);
        recyclerView.setAdapter(adapter);

        noTextView = (TextView) rootView.findViewById(R.id.remind_no_view);
    }

    public void getRemindList(Context context) {

        final List<QReminderMessage> reminderMessageList = MessageManager.getTodayReminderMessage(context);
        Log.d(TAG, "getRemindList: " + reminderMessageList);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (null != reminderMessageList) {
                    noTextView.setVisibility(reminderMessageList.size() > 0 ? View.GONE : View.VISIBLE);
                } else {
                    noTextView.setVisibility(View.VISIBLE);
                }

                adapter.refreshData(reminderMessageList, null);
            }
        });
    }

    @Override
    public void onReminderChanged(Boolean status) {
        Log.d(TAG, "onReminderChanged: ");
        getRemindList(getContext());
//        if (status) {
//            RingPlayer.getInstance().startRing();
//        }
//        newMessageCallback.receiverNewMessage(RemindFragment.class.getSimpleName(), null);
    }

    public void setNewMessageCallback(NewMessageCallback newMessageCallback) {
        this.newMessageCallback = newMessageCallback;
    }

    public void unSetMessageCallback() {
        this.newMessageCallback = null;
    }

    @Override
    public void onLoadMore(int page, int totalItemsCount) {

    }
}
