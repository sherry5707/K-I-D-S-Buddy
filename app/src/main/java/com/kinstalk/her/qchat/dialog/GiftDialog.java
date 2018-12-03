package com.kinstalk.her.qchat.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.kinstalk.her.qchat.R;

/**
 * Created by bean on 2018/5/23.
 */

public class GiftDialog extends Dialog {

    private Button mBtnCancel, mBtnSure;
    private giftDialogCallback callback;

    public GiftDialog(@NonNull Context context) {
        super(context, R.style.Theme_Dialog_Gift);
        // callback = mGiftDialogCallback;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_get_gift);
        setCanceledOnTouchOutside(false);
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        mBtnSure = (Button) findViewById(R.id.btn_sure);
        mBtnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onSure();
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onCancel();
            }
        });
    }

    public void setonSureListener(giftDialogCallback l) {
        callback = l;
    }

    public interface giftDialogCallback {
        void onSure();

        void onCancel();
    }
}
