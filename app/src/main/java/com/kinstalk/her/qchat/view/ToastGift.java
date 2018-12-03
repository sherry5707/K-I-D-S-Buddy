package com.kinstalk.her.qchat.view;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.kinstalk.her.qchat.R;

import javax.security.auth.login.LoginException;

/**
 * Created by bean on 2018/5/23.
 */

public class ToastGift {
    private Toast toast;
    private Context mContext;
    private TextView toastTextField;
    private int TIME = 2;//第三秒的时候消失
    private Handler mHandler = new Handler();
    private int mDuration = 0;


    public ToastGift(Context context) {
        mContext = context;
        toast = new Toast(mContext);
        toast.setGravity(Gravity.CENTER, 0, 0);
        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View toastRoot = inflate.inflate(R.layout.toast_gift, null);
        toastTextField = (TextView) toastRoot.findViewById(R.id.toast_text);
        toast.setView(toastRoot);
    }

    public void setDuration(int d) {
        toast.setDuration(d);
    }

    public void setText(String t) {
        toastTextField.setText(t);
    }

    public static ToastGift makeText(Context context, String text, int duration) {
        ToastGift toastGift = new ToastGift(context);
        toastGift.setText(text);
        toastGift.setDuration(duration);
        return toastGift;
    }

    public void show() {
        //toast.show();
        mHandler.post(showRunnable);
    }

    public void hide() {
        mDuration = 0;
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
    }

    private Runnable showRunnable = new Runnable() {
        @Override
        public void run() {
            if (mDuration <= TIME) {
                toast.show();
                mDuration++;
                mHandler.postDelayed(showRunnable, 1000);
            } else {
                mDuration = 0;
                hide();
            }
        }
    };
}
