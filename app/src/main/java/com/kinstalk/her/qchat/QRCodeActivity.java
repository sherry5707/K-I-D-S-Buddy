package com.kinstalk.her.qchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.common.UIHelper;
import com.kinstalk.her.qchat.messaging.XgPushHelper;

import java.io.File;

import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;

public class QRCodeActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView mView;
    private ImageView mHTXView;
    private RelativeLayout mBackLayout;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode2);
        mView = (ImageView) findViewById(R.id.imageView);
        mHTXView = (ImageView) findViewById(R.id.htximageView);
        mBackLayout= (RelativeLayout) findViewById(R.id.back_layout);
        mBackLayout.setOnClickListener(this);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        boolean finish = getIntent().getBooleanExtra("finish", false);
        if(UIHelper.wchatBindStatus(this))
            finish = true;
        if(finish)
            finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showQRCode();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean finish = intent.getBooleanExtra("finish", false);
        QAILog.d("QRCodeActivity", "onNewIntent " +intent.toString());
        if(finish) {
            finish();
        }
        else
            showQRCode();
    }

    @Override
    protected  void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QAILog.d("QRCodeActivity", "onDestroy");
    }
    private void showQRCode() {
     //   File imgFile = XgPushHelper.getInstance().getQRImageFile();
     //   if (imgFile.exists()) {
       //     Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
       //     mView.setImageBitmap(myBitmap);
        String qrcodeUrl = QAIConfig.qrcode;
        if(!TextUtils.isEmpty(qrcodeUrl)) {
            Glide.with(QchatApplication.getInstance())
                    .load(qrcodeUrl)
                    .placeholder(R.drawable.loading_qrcode)
                    .fallback(R.drawable.refresh_qrcode)
                    .error(R.drawable.refresh_qrcode)
                    .crossFade()
                    .skipMemoryCache(false)
                    .into(mView);
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    XgPushHelper.getInstance().refreshQRCodeByHand();
                }
            });
            mHTXView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick (View view) {
                    //    通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
                    final AlertDialog.Builder builder = new AlertDialog.Builder(QRCodeActivity.this);
                    //    设置Title的内容
                    builder.setTitle("重启机器");
                    //    设置Content来显示一个信息
                    builder.setMessage("确定要重启么？");
                    //    设置一个PositiveButton
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                             builder.setTitle("正在准备重启...");
                             mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    UIHelper.clearAndReboot(QRCodeActivity.this);
                                }
                            }, 500);

                        }
                    });
                    //    设置一个NegativeButton
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                        }
                    });
                    //    显示出该对话框
                    builder.show();
                    return false;
                }
            });
        } else {
            Glide.with(QchatApplication.getInstance())
                        .load(R.drawable.refresh_qrcode)
                        .fallback(R.drawable.refresh_qrcode)
                        .error(R.drawable.refresh_qrcode)
                        .crossFade()
                        .skipMemoryCache(false)
                        .into(mView);
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    XgPushHelper.getInstance().refreshQRCodeByHand();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_layout:
                XgPushHelper.getInstance().startHerBootGuideActivity();
                finish();
                break;
        }
    }

    /*public void backButtonClicked(View view) {
        XgPushHelper.getInstance().startHerBootGuideActivity();
        finish();
    }*/



}
