package com.kinstalk.her.qchat.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kinstalk.her.qchat.R;

public class TaskStarDialog extends Dialog {

    private static final String TAG = TaskStarDialog.class.getSimpleName();
    public final static int DISMISS = 9;//退出
    public final static int STATE_FAIL      = 0;
    public final static int STATE_SUCCESS   = 1;
    private OnContentClickListener contentClickListener;
    private ImageView starCountView;
    private LinearLayout successLayout;
    private FrameLayout failLayout;
    private ImageView closeButton;
    private Button retryButton;
    private int showState;

    public TaskStarDialog(@NonNull Context context) {
        super(context, R.style.FullscreenDialog);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TaskStarDialog.DISMISS) {
                Log.d(TAG, "dismiss");
                contentClickListener.onClickTaskStarDialog();
                dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_task_star);

        successLayout = (LinearLayout) findViewById(R.id.task_alert_success);
        starCountView = (ImageView) findViewById(R.id.star_count_image);
        starCountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showState == STATE_SUCCESS && null != contentClickListener) {
                    contentClickListener.onClickTaskStarDialog();
                    dismiss();
                }
            }
        });

        failLayout = (FrameLayout) findViewById(R.id.task_alert_fail);
        closeButton = (ImageView) findViewById(R.id.task_alert_close);
        retryButton = (Button) findViewById(R.id.task_alert_retry);

        findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showState == STATE_SUCCESS && null != contentClickListener) {
                    contentClickListener.onClickTaskStarDialog();
                    dismiss();
                }
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showState == STATE_FAIL && null != contentClickListener) {
                    contentClickListener.onClickTaskStarClose();
                    dismiss();
                }
            }
        });

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showState == STATE_FAIL && null != contentClickListener) {
                    contentClickListener.onClickTaskStarRetry();
                }
            }
        });

        setCanceledOnTouchOutside(true);
        if (showState == STATE_SUCCESS) {
            dismissDelayed();
        }

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        getWindow().setGravity(Gravity.TOP);
    }

    public void showSuccess(int starCount) {
        showState = 1;
        successLayout.setVisibility(View.VISIBLE);
        failLayout.setVisibility(View.GONE);
        switch (starCount) {
            case 1:
                starCountView.setImageResource(R.mipmap.task_star_1);
                break;
            case 2:
                starCountView.setImageResource(R.mipmap.task_star_2);
                break;
            case 3:
                starCountView.setImageResource(R.mipmap.task_star_3);
                break;
            default:
                break;
        }
        dismissDelayed();
    }

    public void showFail() {
        showState = 0;
        successLayout.setVisibility(View.GONE);
        failLayout.setVisibility(View.VISIBLE);
    }

    public void dismissDelayed() {
        mHandler.removeCallbacksAndMessages(null);
        Message msg = new Message();
        msg.what = TaskStarDialog.DISMISS;
        mHandler.sendMessageDelayed(msg, 8000);
    }

    public void dismiss(long millis) {
        mHandler.removeCallbacksAndMessages(null);
        Message msg = new Message();
        msg.what = TaskStarDialog.DISMISS;
        mHandler.sendMessageDelayed(msg, millis);
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    public static class Builder {

        private TaskStarDialog taskStarDialog;

        public Builder(Context context) {
            taskStarDialog = new TaskStarDialog(context);
        }

        public Builder setOnContentClickListener(OnContentClickListener contentClickListener) {
            taskStarDialog.contentClickListener = contentClickListener;
            return this;
        }

        public TaskStarDialog create() {
            Log.d(TAG, "create: TaskStarDialog");
            return taskStarDialog;
        }
    }

    public interface OnContentClickListener {
        void onClickTaskStarDialog();
        void onClickTaskStarClose();
        void onClickTaskStarRetry();
    }
}
