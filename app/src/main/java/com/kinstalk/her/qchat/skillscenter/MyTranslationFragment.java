package com.kinstalk.her.qchat.skillscenter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.translation.TranslationBean;
import com.kinstalk.her.qchat.translation.TranslationDBHelper;
import com.kinstalk.her.qchatcomm.utils.QAILog;

import java.util.ArrayList;

/**
 * 我的翻译
 */
public class MyTranslationFragment extends Fragment {

    private static final String TAG = MyTranslationFragment.class.getSimpleName();

    private Activity mActivity;

    private RecyclerView rl;

    private TextView tv_empty;

    private ArrayList<TranslationBean> lists;

    private int lastId = 0;

    private ItemAdapter itemAdapter;

    private LinearLayoutManager layoutManager;

//    UpdateBroadcastReceiver ubr;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        QAILog.d(TAG, "onAttach RUN");

//        if (context != null) {
//            mActivity = (Activity) context;
//        } else {
//            mActivity.getApplicationContext();
//        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        QAILog.d(TAG, "onCreateView RUN");

        mActivity = getActivity();

        View view = inflater.inflate(R.layout.fragment_my_tanslation, null);
        //初始化控件

        rl = (RecyclerView) view.findViewById(R.id.rl);

//        layoutManager = (LinearLayoutManager) rl.getLayoutManager();

        rl.addOnScrollListener(new EndLessOnScrollListener() {
            @Override
            public void onLoadMore(int i) {
                getTranslationList(i + 1);

            }
        });

        tv_empty = (TextView) view.findViewById(R.id.tv_empty);

        mActivity.getContentResolver().registerContentObserver(Uri.parse("content://com.kinstalk.her.qchat/TRANSLATION_BEAN"), false,
                new TranslationObserver(new Handler()));

        rl.setLayoutManager(new WrapContentLinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        QAILog.d(TAG, "onActivityCreated RUN");

    }

    public void onStart() {
        super.onStart();

        QAILog.d(TAG, "onStart RUN");
    }


    @Override
    public void onResume() {

        super.onResume();

        QAILog.d(TAG, "onResume RUN -> " + lastId);

        getTranslationList(lastId);

//        ubr = new UpdateBroadcastReceiver();
//        IntentFilter ufilter = new IntentFilter();
//        ufilter.addAction("ACTION_TRANSLATION_UPDATE_DATA");
//        mActivity.registerReceiver(ubr, ufilter);

    }

    @Override
    public void onDetach() {

        super.onDetach();

        QAILog.d(TAG, "onDetach RUN");

//        mActivity.unregisterReceiver(ubr);

        if (lists != null) {
            lists.clear();
            lists = null;
        }

        mActivity = null;

    }

    private void getTranslationList(int lastId) {

        QAILog.d(TAG, "getTranslationList RUN -> " + lastId);

        lists = (ArrayList<TranslationBean>) TranslationDBHelper.getInstance(mActivity).getTranslationList(lastId);

        int size = lists.size();

        if (lists != null && size > 0) {
            if (lastId == 0) {//初始化RecyclerView

                QAILog.d(TAG, "getTranslationList RUN");

                itemAdapter = new ItemAdapter(lists, mActivity);//添加适配器，这里适配器刚刚装入了数据
                rl.setAdapter(itemAdapter);

            } else {//更新RecyclerView
//                itemAdapter.updateList(lists);
                itemAdapter.loadMore(lists);
                rl.post(new Runnable() {
                    public void run() {
                        itemAdapter.notifyItemInserted(itemAdapter.getItemCount() - 1);
                    }
                });

            }

        } else {

            if (lastId == 0) {
                rl.setVisibility(View.GONE);
                tv_empty.setVisibility(View.VISIBLE);
            }

        }

//        lists.clear();

    }

    private class UpdateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            lastId = 0;

            if (lists != null) {
                lists.clear();
            }

            lists = (ArrayList<TranslationBean>) TranslationDBHelper.getInstance(mActivity).getTranslationList(lastId);
            itemAdapter.reload(lists);
        }
    }

    private class TranslationObserver extends ContentObserver {
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public TranslationObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            QAILog.d(TAG, "TranslationObserver -> onChange -> selfChange -> " + selfChange + " | " + lastId);

            if (lists != null) {
                lists.clear();
            }

            lists = (ArrayList<TranslationBean>) TranslationDBHelper.getInstance(mActivity).getTranslationList(lastId);

            if (lists.size() > 0) {
                rl.setVisibility(View.VISIBLE);
                tv_empty.setVisibility(View.GONE);
            }

            if (itemAdapter != null) {

                QAILog.d(TAG, "TranslationObserver -> onChange -> 111111111111111111111 -> ");

                itemAdapter.updateList(lists);

                rl.post(new Runnable() {
                    public void run() {
                        itemAdapter.notifyItemInserted(itemAdapter.getItemCount() - 1);
                    }
                });
            } else {
                QAILog.d(TAG, "TranslationObserver -> onChange -> 222222222222222222222 -> ");

                itemAdapter = new ItemAdapter(lists, mActivity);//添加适配器，这里适配器刚刚装入了数据
                rl.setAdapter(itemAdapter);
            }
        }
    }
}
