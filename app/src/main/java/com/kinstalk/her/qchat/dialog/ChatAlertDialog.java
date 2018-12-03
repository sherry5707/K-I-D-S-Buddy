package com.kinstalk.her.qchat.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchatcomm.base.BaseApplication;

public class ChatAlertDialog extends Dialog {

    private static final String TAG = ChatAlertDialog.class.getSimpleName();
    public final static int DISMISS = 9;//退出
    private TextView contentView;
    private String content;
    private OnContentClickListener contentClickListener;
    private static int showCount;

    public ChatAlertDialog(@NonNull Context context) {
        super(context, R.style.FullscreenDialog);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == ChatAlertDialog.DISMISS) {
                Log.d(TAG, "dismiss");
                dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.kids_chat_alert);

        contentView = (TextView) findViewById(R.id.chat_alert_content);

        findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentClickListener.onClickChatAlert();
                dismiss();
            }
        });

        setCanceledOnTouchOutside(true);

//        setOnShowListener(new OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                mHandler.removeCallbacksAndMessages(null);
//                Message msg = new Message();
//                msg.what = ChatAlertDialog.DISMISS;
//                mHandler.sendMessageDelayed(msg, 8000);
//            }
//        });

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        getWindow().setGravity(Gravity.TOP);
    }

    public void setContent(String string) {
        contentView.setText(string);
        Log.d(TAG, "setContent: " + string);
        dismissDelayed();
    }

    public void dismissDelayed() {
        mHandler.removeCallbacksAndMessages(null);
        Message msg = new Message();
        msg.what = ChatAlertDialog.DISMISS;
        mHandler.sendMessageDelayed(msg, 8000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        showCount = 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    public static class Builder {

        private ChatAlertDialog chatAlertDialog;

        public Builder(Context context) {
            chatAlertDialog = new ChatAlertDialog(context);
        }

        public Builder setOnContentClickListener(OnContentClickListener contentClickListener) {
            chatAlertDialog.contentClickListener = contentClickListener;
            return this;
        }

        public ChatAlertDialog create() {
            Log.d(TAG, "create: ChatAlertDialog");
            return chatAlertDialog;
        }
    }

    public interface OnContentClickListener {
        void onClickChatAlert();
    }
}
