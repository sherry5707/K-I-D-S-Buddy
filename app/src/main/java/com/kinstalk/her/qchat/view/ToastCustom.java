package com.kinstalk.her.qchat.view;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kinstalk.her.qchat.R;

/**
 * Created by bean on 2018/4/25.
 */

public class ToastCustom {
    private Toast toast;
    private Context mContext;
    private TextView toastTextField;

    public ToastCustom(Context context) {
        mContext = context;
        toast = new Toast(mContext);
        toast.setGravity(Gravity.CENTER, 0, 0);
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View toastRoot = inflate.inflate(R.layout.toast_view, null);
        toastTextField = (TextView) toastRoot.findViewById(R.id.toast_text);
        toast.setView(toastRoot);
    }

    public void setDuration(int d) {
        toast.setDuration(d);
    }

    public void setText(String t) {
        toastTextField.setText(t);
    }

    public static ToastCustom makeText(Context context, String text, int duration) {
        ToastCustom toastCustom = new ToastCustom(context);
        toastCustom.setText(text);
        toastCustom.setDuration(duration);
        return toastCustom;
    }

    public void show() {
        toast.show();
    }
    public void cancel() {
        toast.cancel();
    }
}
