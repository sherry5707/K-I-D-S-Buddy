/*
 * Copyright (c) 2018. Beijing Shuzijiayuan, All Rights Reserved.
 * Beijing Shuzijiayuan Confidential and Proprietary
 */

package com.kinstalk.her.qchat.voice;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchatcomm.utils.QAILog;

/**
 * Created by Knight.Xu on 2018/4/8.
 * 录音过程显示界面
 */

public class QRecorderDialog {

    private static final String TAG = QRecorderDialog.class.getSimpleName();


    private Context mContext;
    private Dialog mDialog;

    private ImageView mIcon;
    private ImageView mVoice;

    private TextView mLabel;

    public QRecorderDialog(Context context) {
        mContext = context;
    }

    public void showRecordingDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }
        mDialog = new Dialog(mContext, R.style.Theme_Dialog_Recorder);
        Log.d("MMMM", "showRecordingDialog" + mDialog.toString());
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.recorder_layout_dialog, null);
        mDialog.setContentView(view);
        //清除掉 dialog出入场动画
        mDialog.getWindow().setWindowAnimations(0);

        mIcon = (ImageView) view.findViewById(R.id.recorder_dialog_icon);
        mVoice = (ImageView) view.findViewById(R.id.recorder_dialog_voice);
        mLabel = (TextView) view.findViewById(R.id.recorder_dialog_label);

        mDialog.show();
    }

    public void showRecording() {
        if (mDialog != null && mDialog.isShowing()) {
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.VISIBLE);
            mLabel.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.drawable.voice_recorder);
            mLabel.setText("手指上划，取消发送");
        }
    }

    //想要取消
    public void wantToCancel() {
        if (mDialog != null && mDialog.isShowing()) {
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLabel.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.drawable.voice_cancel);
            mLabel.setText("松开手指，取消发送");
        }
    }

    //录音时间太短
    public void tooShort() {
        if (mDialog != null && mDialog.isShowing()) {
            Log.d("Event", "mDialog");
            mIcon.setVisibility(View.VISIBLE);
            mVoice.setVisibility(View.GONE);
            mLabel.setVisibility(View.VISIBLE);

            mIcon.setImageResource(R.drawable.voice_too_short);
            mLabel.setText("录音时间过短");
        }
    }

    //关闭dialog
    public void dismissDialog() {
        Log.d("MMMM", "diss" + (mDialog == null ? null : mDialog.toString()));
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    /**
     * 通过level更新voice上的图片
     *
     * @param level
     */
    public void updateVoiceLevel(int level) {
        if (mDialog != null && mDialog.isShowing()) {
            QAILog.d(TAG, "updateVoiceLevel level = ", level);
            mVoice.setImageLevel(level);
        }
    }
}
