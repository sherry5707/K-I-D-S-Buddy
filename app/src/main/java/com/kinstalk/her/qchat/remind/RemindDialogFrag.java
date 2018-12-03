/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchat.remind;

import android.app.Dialog;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchatmodel.beans.RemindBean;
import com.kinstalk.her.qchatcomm.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Knight.Xu on 2018/4/7.
 */
public class RemindDialogFrag extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.frag_remind_dialog, null);
        TextView title = (TextView) view.findViewById(R.id.remind_title);
        title.setText(getString(R.string.remind_title, DateUtils.getDate(SystemClock.currentThreadTimeMillis())));

        TextView close = (TextView) view.findViewById(R.id.remind_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissAllowingStateLoss();
            }
        });

        ListView hwList = (ListView) view.findViewById(R.id.remind_list);
        //TODO 动态获取今日作业列表
        // 这里ListView的适配器选用ArrayAdapter，ListView中每一项的布局选用系统的simple_list_item_1。
        List<RemindBean> reminds = new ArrayList<RemindBean>();
        long now = SystemClock.currentThreadTimeMillis();
        reminds.add(new RemindBean("提醒事项A", now + 1 * 3600 * 1000));
        reminds.add(new RemindBean("提醒事项B", now + 2 * 3600 * 1000));
        reminds.add(new RemindBean("提醒事项C", now + 4 * 3600 * 1000));
        ArrayAdapter<RemindBean> adapter = new ArrayAdapter<RemindBean>(getActivity(), android.R.layout.simple_list_item_1, reminds);
        hwList.setAdapter(adapter);

        builder.setView(view);
        return builder.create();
    }
}
