package com.kinstalk.her.qchat.homework;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinstalk.her.qchat.R;

public class GuidePageDialog extends Dialog {

    private ImageView mView;
    private TextView toastText;
    private AnimationDrawable mAnimationDrawable;
    private Handler mHandler = new Handler();

    public GuidePageDialog(@NonNull Context context) {
        super(context, R.style.FullscreenDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        mView = (ImageView) findViewById(R.id.guide_vp);
        toastText = (TextView)findViewById(R.id.guide_tips);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        getWindow().setGravity(Gravity.CENTER);

        initView();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopAnimation();
                dismiss();
            }
        }, 6000);
    }

    public void initView() {
        startAnimation();
    }

    public void startAnimation() {

        mView.setBackgroundResource(R.drawable.homework_guide);
        mAnimationDrawable = (AnimationDrawable) mView.getBackground();
        mAnimationDrawable.start();
    }

    public void stopAnimation() {
        if ((mAnimationDrawable != null) && mAnimationDrawable.isRunning()) {
            mAnimationDrawable.stop();
        }
    }



}
