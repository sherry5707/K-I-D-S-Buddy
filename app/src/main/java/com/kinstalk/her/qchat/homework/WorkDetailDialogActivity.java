package com.kinstalk.her.qchat.homework;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.utils.CountlyConstant;
import com.kinstalk.qloveaicore.AIManager;

import com.kinstalk.util.AppUtils;

import ly.count.android.sdk.Countly;

public class WorkDetailDialogActivity extends Activity {

    private String TAG = WorkDetailDialogActivity.class.getSimpleName();
    private ImageButton closeBtn, noPicClose;
    private ImageButton playBtn;
    private TextView contentView;
    private ImageView imageView;
    private CardView cardView;
    private static final Long SPACE_TIME = 600L;
    private static Long mLastClickTime = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_work_detail);

        initView();
        initData();
        setAutoSwitchLauncher(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        super.onNewIntent(intent);
        initData();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void initView() {

        closeBtn = (ImageButton) findViewById(R.id.work_detail_close_btn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playWorkContent(" ");
                finish();
            }
        });
        noPicClose = (ImageButton) findViewById(R.id.work_detail_close_nopic);
        noPicClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playWorkContent(" ");
                finish();
            }
        });
        cardView = (CardView) findViewById(R.id.cardview);

        playBtn = (ImageButton) findViewById(R.id.work_detail_play_btn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickPlay();
            }
        });
        contentView = (TextView) findViewById(R.id.work_detail_content);
        contentView.setMovementMethod(ScrollingMovementMethod.getInstance());
        contentView.setClickable(false);

        imageView = (ImageView) findViewById(R.id.work_detail_image);
    }

    public void initData() {
        Intent intent = this.getIntent();
        if (null != intent) {
            String content = intent.getStringExtra("content");
            final String imageUrl = intent.getStringExtra("imageUrl");
            contentView.setText(content);

            if (!TextUtils.isEmpty(imageUrl) && !imageUrl.equals("null")) {
                //set image
                Glide.with(this)
                        .load(imageUrl)
                        .skipMemoryCache(false)
                        .thumbnail(0.3f)
                        .into(imageView);

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickImage(imageUrl);
                    }
                });
            } else {
                noPicClose.setVisibility(View.VISIBLE);
                closeBtn.setVisibility(View.GONE);
                cardView.setVisibility(View.GONE);
            }
        }
    }

    public void playWorkContent(String content) {
        try {
            AIManager.getInstance(getApplicationContext()).playTextWithStr(content, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickPlay() {
        if(isFastClick())
            return;
        playWorkContent(contentView.getText().toString());
        Countly.sharedInstance().recordEvent(CountlyConstant.SKILL_TYPE, CountlyConstant.t_homework_audio);
    }

    public void clickImage(String imageUrl) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.putExtra("imageUrl", imageUrl);
        intent.setClass(this, WorkImageDetailDialogActivity.class);
        startActivity(intent);
    }

    /**
     * 取消自动回到首页方法
     *
     * @param auto true 30s自动回到首页 false 取消自动回到首页
     */
    public void setAutoSwitchLauncher(boolean auto) {
        try {
            AppUtils.setAutoActivityTimeout(getWindow(), false);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    private long lastClickTime;

    private boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 600) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /**
     * 判断触摸时间派发间隔
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (isFastDoubleClick()) {
                finish();
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private static boolean isFastClick() {
        long currentTime = System.currentTimeMillis();//当前系统时间
        boolean isFastClick;//是否允许点击
        if (currentTime - mLastClickTime < SPACE_TIME) {
            isFastClick = true;
        } else {
            isFastClick = false;
            mLastClickTime = currentTime;
        }
        return isFastClick;
    }

}
