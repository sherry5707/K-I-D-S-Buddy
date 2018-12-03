package com.kinstalk.her.qchat.library;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.dialog.LoadingDialog;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.model.voiceBookCallback;
import com.kinstalk.her.qchatmodel.entity.VoiceBookBean;

import java.util.ArrayList;
import java.util.List;

public class VoiceBookActivity extends AppCompatActivity implements View.OnClickListener, voiceBookCallback, ParentTabChangeListener, AutoRollControllListener {

    private static final String TAG = VoiceBookActivity.class.getSimpleName();

    private Context mContext;

    private ImageView iv_back;

    private RecyclerView rv_parent_name;

    private AutoRollRecyclerView recycler;

    private AutoRollAdapter adapter;

    private ItemAdapter itemAdapter;

    public LoadingDialog loadingDialog;//加载中布局;

    private ArrayList<VoiceBookBean> beans;

//    private List<VoiceBookBean> empty = new ArrayList<>();

    private String currentTabName;

    public String sn;

    public String token;

    private ArrayList<String> str_titles = new ArrayList<>();

    private ArrayList<TitlesBean> list_titles = new ArrayList<>();

    private void insertDatas(List<VoiceBookBean> beans) {

        int size = beans.size();

        QAILog.d(TAG, "insertDatas size 1-> " + size);
        if (size > 0) {
//            if (titles != null) {
//                titles.clear();
//            }

            for (int i = 0; i < size; i++) {

                QAILog.d(TAG, "insertDatas size 2-> " + size);
                if (i == 0) {
                    currentTabName = beans.get(i).getParentName();
//                    tb.setTitle(currentTabName);
//                    tb.setSelected(true);
//                    QAILog.d(TAG, "0000000000000 -> " + currentTabName);
//                    titles.add(tb);

                    str_titles.add(currentTabName);
                } else {

                    QAILog.d(TAG, "i -> " + i);

                    if (!str_titles.contains(beans.get(i).getParentName())) {
                        str_titles.add(beans.get(i).getParentName());
                    }

//                    for (int j = 0; j < titles.size(); j++) {
//                        if (!beans.get(i).getParentName().equals(titles.get(j).getTitle())) {
//                            tb.setTitle(beans.get(i).getParentName());
//                            tb.setSelected(false);
//                            titles.add(tb);
//                        }
//                    }

                }

            }

            if (itemAdapter != null) {

//                QAILog.d(TAG, "titles size -> " + titles.size());

//                itemAdapter.updateDatas(titles);
            } else {

                for (int i = 0; i < str_titles.size(); i++) {

                    QAILog.d(TAG, "111111111111111111111 -> " + str_titles.get(i));

                    TitlesBean tb = new TitlesBean();

                    tb.setTitle(str_titles.get(i));

                    if (i == 0) {
                        tb.setSelected(true);
                    } else {
                        tb.setSelected(false);
                    }

                    list_titles.add(tb);

                }

                itemAdapter = new ItemAdapter(list_titles, this);//添加适配器，这里适配器刚刚装入了数据}
                rv_parent_name.setAdapter(itemAdapter);
            }

            List<VoiceBookBean> tmp = getDatasByParentName(currentTabName, beans);
            if (adapter != null) {
                adapter.changeData(tmp);
            } else {
                adapter = new AutoRollAdapter(this, tmp);
                recycler.setAdapter(adapter);
            }
            recycler.start();

        }
    }

    private List<VoiceBookBean> getDatasByParentName(String currentTabName, List<VoiceBookBean> beans) {

        QAILog.d(TAG, "getDatasByParentName currentTabName -> " + currentTabName);

        if (TextUtils.isEmpty(currentTabName))
            return null;

        List<VoiceBookBean> result = new ArrayList<>();

        if (beans != null && beans.size() > 0) {
            int size = this.beans.size();

            for (int i = 0; i < size; i++) {
                if (currentTabName.equals(beans.get(i).getParentName())) {
                    result.add(beans.get(i));
                }
            }
        }
        QAILog.d(TAG, "getDatasByParentName -> " + result.size());
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_book);

        initView();

        initData();

        initListener();
    }

    private void initListener() {

        MessageManager.registerVoiceBookCallback(this);

    }

    private void changeData(String currentTabName) {

        QAILog.d(TAG, "changeData -> currentTabName -> " + currentTabName);

        if (TextUtils.isEmpty(currentTabName) || beans == null) {
            return;
        }

        int size = beans.size();

        if (size > 0) {

            List<VoiceBookBean> list = new ArrayList<VoiceBookBean>();

            for (int i = 0; i < size; i++) {

                if (currentTabName.equals(beans.get(i).getParentName())) {
                    VoiceBookBean tmp = new VoiceBookBean();
                    tmp.setParentName(currentTabName);
                    tmp.setBookName(beans.get(i).getBookName());
                    tmp.setIconUrl(beans.get(i).getIconUrl());
                    tmp.setCommand(beans.get(i).getCommand());
                    list.add(tmp);
                }

            }

            recycler.stop();
            adapter.changeData(list);
//            recycler.post(new Runnable() {
//                public void run() {
//                    adapter.notifyItemInserted(adapter.getItemCount() - 1);
//                }
//            });
            recycler.start();
        }

    }

    private void initData() {

        mContext = this;

        sn = QAIConfig.getMacForSn();

        token = StringEncryption.generateToken();

        getDatasFromServer();
    }

    private void initView() {

        loadingDialog = new LoadingDialog(this);

        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);

        rv_parent_name = (RecyclerView) findViewById(R.id.rv_parent_name);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(mContext);
        layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        rv_parent_name.setLayoutManager(layoutManager1);

        recycler = (AutoRollRecyclerView) findViewById(R.id.autopollrecyclerview);
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        recycler.addItemDecoration(new SpaceItemDecoration(30, 0));
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycler.setLayoutManager(layoutManager2);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!recycler.running) {
            recycler.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageManager.unRegisterVoiceBookCallback(this);

//        adapter = null;
//        itemAdapter = null;
//        beans.clear();
//        beans = null;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
        }

    }

    private void showDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.show();
        } else {
            loadingDialog.show();
        }
    }

    private void cancelDialog() {
        if (loadingDialog != null) {
            loadingDialog.cancel();
            loadingDialog = null;
        }
    }

    public void getDatasFromServer() {
        showDialog();

        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                Api.getVoiceBooks(token, sn);
            }
        });
    }

    @Override
    public void getVoiceBookListSuccess(final List<VoiceBookBean> list) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();

                if (list != null && list.size() > 0) {

                    for (int i = 0; i < list.size(); i++) {
                        QAILog.d(TAG, "111:" + list.get(i).toString());
                    }

//            beans.addAll(list);


                    beans = (ArrayList<VoiceBookBean>) list;

                    for (int j = 0; j < beans.size(); j++) {
                        QAILog.d(TAG, "222:" + beans.get(j).toString());
                    }

                    insertDatas(beans);
                }
            }
        });

    }

    @Override
    public void getVoiceBookListFail(final int errorCode, final String errorMsg) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                cancelDialog();

                if (errorCode == -1000) {
                    Toast.makeText(mContext, "小微出现异常了", Toast.LENGTH_SHORT);
                } else {
                    Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT);
                }

            }
        });


    }

    @Override
    public void changeTab(String name) {
        changeData(name);
    }

    @Override
    public void stopRoll() {
        recycler.stop();
    }

    @Override
    public void startRoll() {

    }
}
