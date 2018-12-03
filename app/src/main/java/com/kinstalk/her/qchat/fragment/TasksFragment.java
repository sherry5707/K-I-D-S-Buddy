package com.kinstalk.her.qchat.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.kinstalk.her.qchat.GiftActivity;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.adapter.TaskAdapter;
import com.kinstalk.her.qchat.view.ToastGift;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatcomm.utils.SystemTool;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.model.taskCallback;
import com.kinstalk.her.qchatmodel.entity.TaskEntity;

import java.util.ArrayList;
import java.util.List;

import ly.count.android.sdk.Countly;

/**
 * Created by bean on 2018/7/2.
 */

public class TasksFragment extends Fragment implements taskCallback {
    private static String TAG = "GiftActivityLog";
    View view;
    private ListView mListView;
    private List<TaskEntity> mTask = new ArrayList<>();
    private Context mContext;
    private TaskAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_tasks, null);
        mContext = getActivity();
        initViews(view);
        if (SystemTool.isNetworkAvailable(getActivity())) {
            initData();
        } else {
            ToastGift.makeText(getActivity(), this.getResources().getString(R.string.no_network_gift), Toast.LENGTH_SHORT).show();
        }
        Countly.sharedInstance().recordEvent("gift", "t_gift_store_task");

        return view;
    }

    private void initData() {
        ((GiftActivity) getActivity()).showDialog();
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                String sn = QAIConfig.getMacForSn();
                String token = StringEncryption.generateToken();
                Api.postTask(token, sn);
            }
        });
        //假数据测试
      /*  mTask.add(new TaskEntity(1, "当日累计习惯打卡1次", 1, 1, 5, 0, 0));
        mTask.add(new TaskEntity(2, "问小微 学习百科知识5次", 5, 1, 20, 1, 1));
        mTask.add(new TaskEntity(3, "当日累计习惯打卡3次", 3, 1, 20, 0, 1));
        mTask.add(new TaskEntity(4, "问小微，学习百科知识", 5, 5, 20, 1, 1));*/
    }

    private void initViews(View view) {
        mListView = (ListView) view.findViewById(R.id.task_listview);
        mAdapter = new TaskAdapter(mContext, mTask);
        mListView.setAdapter(mAdapter);
        MessageManager.registerTaskCallback(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageManager.unRegisterTaskCallback();
    }

    //获取任务成功
    @Override
    public void onTaskChanged(final List<TaskEntity> taskList, final int credit) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((GiftActivity) getActivity()).cancelDialog();
                if (mTask.size() > 0) {
                    mTask.clear();
                }
                mTask.addAll(taskList);
                mAdapter.notifyDataSetChanged();
                ((GiftActivity) getActivity()).updateCreditNum(credit);
            }
        });
    }

    //获取任务失败
    @Override
    public void onTaskGetError(final int code) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((GiftActivity) getActivity()).cancelDialog();
                ToastGift.makeText(getActivity(), "任务获取失败！", Toast.LENGTH_LONG).show();
                Log.i(TAG, "onTaskGetError  code=" + code);
            }
        });
    }

    //领取任务成功
    @Override
    public void onTaskReceive(final List<TaskEntity> taskList, final int credit, String action) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((GiftActivity) getActivity()).cancelDialog();
                if (mTask.size() > 0) {
                    mTask.clear();
                }
                mTask.addAll(taskList);
                mAdapter.notifyDataSetChanged();
                ((GiftActivity) getActivity()).updateCreditNum(credit);
                Log.i(TAG, "onTaskReceive领取任务成功");
            }
        });
    }

    //领取任务失败
    @Override
    public void onTaskReceiveError(final int code) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((GiftActivity) getActivity()).cancelDialog();
                ToastGift.makeText(getActivity(), "积分领取失败！", Toast.LENGTH_LONG).show();
                Log.i(TAG,"onTaskReceiveError code="+code);
            }
        });
    }
}
