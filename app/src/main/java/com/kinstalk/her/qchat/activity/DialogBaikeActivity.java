package com.kinstalk.her.qchat.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinstalk.her.qchat.R;

/**
 * Created by bean on 2018/7/18.
 */

public class DialogBaikeActivity extends Activity {
    private TextView gotCredit;
    private ImageView imageView;//动画图片
    private AnimationSet animationSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);//窗口对齐屏幕宽度
        Window win = this.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;//设置对话框置顶显示
        win.setAttributes(lp);
        setContentView(R.layout.dialog_baike_task);

        gotCredit = (TextView) findViewById(R.id.got_credit);
        imageView = (ImageView) findViewById(R.id.dialog_baike_img);

        animationSet = new AnimationSet(true);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 596, 0, 0);
        translateAnimation.setDuration(1000);
        translateAnimation.setRepeatCount(1);
        animationSet.addAnimation(translateAnimation);
        imageView.startAnimation(animationSet);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        gotCredit.setText("+ " + getIntent().getIntExtra("credit_got", 0));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 3000);
    }
}
