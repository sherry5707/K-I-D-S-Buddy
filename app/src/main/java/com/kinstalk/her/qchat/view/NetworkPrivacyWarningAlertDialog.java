package com.kinstalk.her.qchat.view;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;

import com.kinstalk.util.AppUtils;

public class NetworkPrivacyWarningAlertDialog extends AlertDialog {
    private static final String TAG = "NetworkPrivacyWarningAlertDialog";
    private static final int FRAGMENT_NETWORK_ID = 2;
    public static int TYPE_WIFI = 0;
    public static int TYPE_PRIVACY = 1;
    private int type;

    public NetworkPrivacyWarningAlertDialog(Context context, int type) {
        super(context, R.style.NetworkWarningDialogStyle);
        // 拿到Dialog的Window, 修改Window的属性
        Window window = getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        // 获取Window的LayoutParams
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        this.type = type;
        if (type == TYPE_WIFI) {
            attributes.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        } else if (type == TYPE_PRIVACY) {
            attributes.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        }
        // 一定要重新设置, 才能生效
        window.setAttributes(attributes);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        if (type == TYPE_WIFI) {
            setContentView(R.layout.network_warning_dialog);
            findViewById(R.id.set_network).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClassName("com.kinstalk.her.settings",
                            "com.kinstalk.her.settings.view.activity.HerSettingsActivity");
                    intent.putExtra("from_network_fragment_id", FRAGMENT_NETWORK_ID);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        getContext().startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (type == TYPE_PRIVACY) {
            setContentView(R.layout.privacy_warning_dialog);
            findViewById(R.id.privacy_warning_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePrivacy(getContext());
                }
            });
        }
    }

    protected void removePrivacy(Context context) {
        try {
            AppUtils.setPrivacyMode(context, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        ApplicationInfo info = null;
        try {
            info = QchatApplication.getApplicationInstance()
                    .getPackageManager().getApplicationInfo("com.kinstalk.qloveandlinkapp", 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("HerSettingsActivity", "No andlink package found!");
        }

        Log.d("HerSettingsActivity", "andlink package: " + info);
        if (info == null) {
            return false;
        }
        this.dismiss();
        return true;
    }
}
