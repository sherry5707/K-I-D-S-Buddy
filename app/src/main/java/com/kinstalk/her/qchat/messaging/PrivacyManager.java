package com.kinstalk.her.qchat.messaging;


import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.StyleRes;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.kinstalk.util.AppUtils;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.view.NetworkPrivacyWarningAlertDialog;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.qloveaicore.AIManager;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.kinstalk.her.qchat.view.NetworkPrivacyWarningAlertDialog.TYPE_PRIVACY;

/**
 * Created by Tracy on 2018/4/24.
 */

public class PrivacyManager {

    private static String TAG = "PrivacyManager";
    private static PrivacyManager _instance;
    private Context context;
    private boolean isPrivacy = false;
    private Builder builder;
    private static NetworkPrivacyWarningAlertDialog mPrivacyDialog;
    private static final String PRIVACY_ACTION = "kingstalk.action.privacymode";

    /**
     * 隐私模式弹框的显示规则：在没有绑定小微/微信,引导页没有结束的时候不显示弹框(
     * 因为这个流程需要开启隐私模式，但是不想要用户关闭隐私模式)
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PRIVACY_ACTION.equals(intent.getAction())) {
                isPrivacy = intent.getBooleanExtra("enable", false);
                QAILog.d(TAG, "privacy:"+isPrivacy);
                if (!isPrivacy) {
                    //hidePrivacyDialog();
                    if (mPrivacyDialog != null) {
                        mPrivacyDialog.dismiss();
                        mPrivacyDialog = null;
                        playTTSWithContent(context, context.getString(R.string.privacy_close_tips));
                    }
                } else {
                    getWarningDlg(context);
                    if (!mPrivacyDialog.isShowing() && isXiaoweiBind(context) && isWchatBind(context) && !isGuidePage(context)) {
                        mPrivacyDialog.show();
                    } else {
                        QAILog.d(TAG, "onReceive: dialog dissmiss");
                        mPrivacyDialog.dismiss();
                        mPrivacyDialog = null;
                    }
                }
            }
        }
    };

    public static boolean isXiaoweiBind(Context context) {
        QAILog.d(TAG, "isXiaoweiBind: "+Settings.Secure.getInt(context.getContentResolver(), "xiaowei_bind_status", 0));
        return (1 == Settings.Secure.getInt(context.getContentResolver(), "xiaowei_bind_status", 0));
    }

    public static boolean isWchatBind(Context context) {
        QAILog.d(TAG, "isWchatBind: "+Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0));
        return (1 == Settings.Secure.getInt(context.getContentResolver(), "wechat_bind_status", 0));
    }

    public static boolean isGuidePage(Context context) {
        QAILog.d(TAG, "isGuidePage: "+Settings.System.getInt(context.getContentResolver(), "show_guidepagedlg", 0));
        return (1 == Settings.System.getInt(context.getContentResolver(), "show_guidepagedlg", 0));
    }

    private PrivacyManager(Context context) {
        this.context = context.getApplicationContext();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PRIVACY_ACTION);
        this.context.registerReceiver(broadcastReceiver, intentFilter);

    }

    public static PrivacyManager getInstance(Context context) {
        if (_instance == null) {
            synchronized (PrivacyManager.class) {
                if (_instance == null) {
                    _instance = new PrivacyManager(context);
                }
            }
        }
        return _instance;
    }

    public void destory(){
        context.unregisterReceiver(broadcastReceiver);
    }

    public boolean isPrivacy() {
        return isPrivacy;
    }

    /**
     * 关闭隐私模式
     */
    public void closePrivacy() {
        try {
            QAILog.d(TAG, "closePrivacy");
            AppUtils.turnOffPrivacyMode(context);
        } catch (Exception e) {
           QAILog.w(TAG, e);
        }
    }

    public void showPrivacyDialog(Context context, String msg, String leftStr, DialogInterface.OnClickListener leftAction, String rightStr, DialogInterface.OnClickListener rightAction ) {
        builder = new Builder(context)
                .setMessage(msg)
                .setLeftAction(leftStr, leftAction)
                .setRightAction(rightStr, rightAction)
                .build();
        builder.show();
    }

    /**
     * Voip 展示隐私模式对话框
     * @param context
     * @param leftAction
     * @param rightAction
     */
    public void showPrivacyDialog(Context context, DialogInterface.OnClickListener leftAction, DialogInterface.OnClickListener rightAction){
        showPrivacyDialog(context, "关闭隐私模式后才能正常使用", "取消", leftAction, "关闭", rightAction);
    }

    public void hidePrivacyDialog() {
        if(null != builder&& builder.getDialog().isShowing()){
            builder.getDialog().dismiss();
        }
    }

    public class PrivacyDialog extends Dialog {
        private TextView titleText, subtitleText;
        private Button cancelBtn, confirmBtn;

        public PrivacyDialog(Context context) {
            this(context, 0);
        }

        public PrivacyDialog(Context context, @StyleRes int theme) {
            super(context, theme);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.dialog_privacy);
            titleText = (TextView) findViewById(R.id.title_text);
            subtitleText = (TextView) findViewById(R.id.subtitle_text);
            cancelBtn = (Button) findViewById(R.id.cancel_btn);
            confirmBtn = (Button) findViewById(R.id.confirm_btn);
            titleText.setVisibility(View.GONE);
            WindowManager.LayoutParams lps = getWindow().getAttributes();
            lps.gravity = Gravity.BOTTOM;
            lps.width = WindowManager.LayoutParams.MATCH_PARENT;
            lps.height = WindowManager.LayoutParams.MATCH_PARENT;
            lps.dimAmount = 0;
            getWindow().setBackgroundDrawable(null);
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            getWindow().setAttributes(lps);
            getWindow().getDecorView().setPadding(0, 0, 0, 0);
            Display d = getWindow().getWindowManager().getDefaultDisplay();
            getWindow().setLayout(d.getWidth(), d.getHeight());
        }

        @Override
        public void show() {
            super.show();
        }

        public void setTitle(String title) {
            titleText.setText(title);
            titleText.setVisibility(View.VISIBLE);
        }

        public void setMessage(String message) {
            subtitleText.setText(message);
        }

        public void setCancel(String text, final OnClickListener listener) {
            cancelBtn.setText(text);
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(PrivacyDialog.this, 0);
                }
            });
        }

        public void setConfirm(String text, final OnClickListener listener) {
            confirmBtn.setText(text);
            confirmBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(PrivacyDialog.this, 1);
                }
            });
        }
    }

    public class Builder {
        PrivacyDialog dialog;
        public Builder(Context context) {
            dialog = new PrivacyDialog(context);
        }

            public Builder build() {
                return this;
            }

            public Builder setTitle(String title) {
                dialog.setTitle(title);
                return this;
            }

            public Builder setMessage(String message) {
                dialog.setMessage(message);
                return this;
            }

            public Builder setLeftAction(String text, DialogInterface.OnClickListener listener) {
                dialog.setCancel(text, listener);
                return this;
            }

            public Builder setRightAction(String text, DialogInterface.OnClickListener listener) {
                dialog.setConfirm(text, listener);
                return this;
            }

            public void show() {
                dialog.show();
            }

            public PrivacyDialog getDialog() {
                return dialog;
            }
        }

    private void getWarningDlg(Context context) {
        if (mPrivacyDialog == null) {
            mPrivacyDialog = new NetworkPrivacyWarningAlertDialog(context, TYPE_PRIVACY);
            mPrivacyDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
            mPrivacyDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
    }

    public void playTTSWithContent(Context context,String content) {
        try {
            AIManager.getInstance(context).playTextWithStr(content, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
