package com.kinstalk.her.qchat.homework;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.utils.SharedPreferencesUtils;
import com.kinstalk.her.qchat.view.TouchableRecyclerView;
import com.kinstalk.her.qchatcomm.utils.DateUtils;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.util.WorkAlertInfo;
import com.kinstalk.her.qchatmodel.entity.QHomeworkMessage;

import com.kinstalk.util.AppUtils;

import java.util.ArrayList;
import java.util.List;

import ly.count.android.sdk.Countly;

public class WorkAlertActivity extends Activity {

    public static final int WORK_TYPE_DELETE = 0;
    public static final int WORK_TYPE_ADD = 1;

    private String TAG = WorkAlertActivity.class.getSimpleName();
    private TouchableRecyclerView recyclerView;
    private WorkAlertAdapter adapter;
    private List<WorkAlertInfo> workInfoList = new ArrayList<>();
    private TextView finishView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        boolean clear = getIntent().getBooleanExtra("clear", false);
        if(clear)
            finish();
        setContentView(R.layout.activity_work_alert);

        initView();
        initData();
        setAutoSwitchLauncher(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        boolean clear = intent.getBooleanExtra("clear", false);
        if(clear)
            finish();
        super.onNewIntent(intent);
        reloadView();
    }

    public void reloadView() {
        WorkAlertInfo workAlertInfo = getIntent().getParcelableExtra("WorkContent");
        if (null != workAlertInfo) {
            if (workAlertInfo.getType() == WORK_TYPE_DELETE) {
                for (int i = 0; i < workInfoList.size(); i++) {
                    WorkAlertInfo fWorkAlertInfo = workInfoList.get(i);
                    if (TextUtils.equals(fWorkAlertInfo.getId(), workAlertInfo.getId())) {
                        workInfoList.remove(i);
                        adapter.notifyItemRemoved(i);
                        adapter.notifyItemRangeChanged(0, workInfoList.size());
                        if (workInfoList.size() == 0) {
                            finish();
                        }
                        break;
                    }
                }
            } else if (workAlertInfo.getType() == WORK_TYPE_ADD) {
                boolean isExist = false;
                for (int i = 0; i < workInfoList.size(); i++) {
                    WorkAlertInfo fWorkAlertInfo = workInfoList.get(i);
                    if (TextUtils.equals(fWorkAlertInfo.getId(), workAlertInfo.getId())) {
                        isExist = true;

                       workInfoList.remove(i);
                       workInfoList.add(i, workAlertInfo);
                       adapter.notifyItemRangeChanged(i, workInfoList.size());
                       break;
                    }
                }
                if (isExist == false) {
                    workInfoList.add(0, workAlertInfo);
                    adapter.notifyItemInserted(0);
                    adapter.notifyItemRangeChanged(0, workInfoList.size());
                    recyclerView.smoothScrollToPosition(0);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void initView() {
        Log.d(TAG, "initView: ");
        recyclerView = (TouchableRecyclerView) findViewById(R.id.work_alert_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkAlertAdapter(recyclerView.getContext(), recyclerView, workInfoList);
        recyclerView.setAdapter(adapter);

        finishView = (TextView) findViewById(R.id.work_alert_finish_btn);
        finishView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: finish");
                Countly.sharedInstance().recordEvent("KidsBuddy", "t_push_homework_close");
                finish();
            }
        });
    }

    public void initData() {
        getSPData();
  //      reloadView();
    }

    public void getSPData() {
        if (SharedPreferencesUtils.containsSharedPreferences(this, SharedPreferencesUtils.WORK_TIME, SharedPreferencesUtils.WORK_ALERT_TIME)) {
            long spWorkTime = SharedPreferencesUtils.getSharedPreferencesWithLong(this, SharedPreferencesUtils.WORK_TIME, SharedPreferencesUtils.WORK_ALERT_TIME, DateUtils.getToadyBeginTime());
            if (spWorkTime > 0) {
                List<WorkAlertInfo> spWorkInfoList = new ArrayList<>();
                List<QHomeworkMessage> messageList = MessageManager.getHomeworkMessage(spWorkTime);
                for (int i = 0; (messageList != null) && (i < messageList.size()); i++) {
                    QHomeworkMessage qHomeworkMessage = messageList.get(i);
                    WorkAlertInfo workAlertInfo = new WorkAlertInfo(qHomeworkMessage.getHomeWorkEntity());
                    spWorkInfoList.add(workAlertInfo);
                }
                adapter.refreshData(spWorkInfoList);
            }
        } else {
            List<WorkAlertInfo> spWorkInfoList = new ArrayList<>();
            List<QHomeworkMessage> messageList = MessageManager.getHomeworkMessage(DateUtils.getToadyBeginTime());
            for (int i = 0; (messageList != null) && (i < messageList.size()); i++) {
                QHomeworkMessage qHomeworkMessage = messageList.get(i);
                WorkAlertInfo workAlertInfo = new WorkAlertInfo(qHomeworkMessage.getHomeWorkEntity());
                spWorkInfoList.add(workAlertInfo);
            }
            adapter.refreshData(spWorkInfoList);
        }
    }

    /**
     * 取消自动回到首页方法
     *
     * @param auto true 30s自动回到首页 false 取消自动回到首页
     */
    public void setAutoSwitchLauncher(boolean auto) {
        try {
            AppUtils.setAutoActivityTimeout(getWindow(), auto);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
