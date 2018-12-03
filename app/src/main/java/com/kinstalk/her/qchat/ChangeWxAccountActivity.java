package com.kinstalk.her.qchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.messaging.XgPushHelper;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.QAILog;

import java.io.File;

import ly.count.android.sdk.Countly;

public class ChangeWxAccountActivity extends Activity {
    private ImageView mView;
    private RelativeLayout mBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_wx_account);
        mView = (ImageView) findViewById(R.id.imageView);
        mBackLayout = (RelativeLayout) findViewById(R.id.back_layout);
        mBackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        boolean finish = getIntent().getBooleanExtra("finish", false);
        if(finish)
            finish();
        showQRCode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Countly.sharedInstance().recordEvent("KidsBuddy","t_setting_all_wechat_changeid");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean finish = intent.getBooleanExtra("finish", false);
        QAILog.d("ChangeWxAccountActivity", "onNewIntent " +intent.toString());
        if(finish) {
            finish();
        } else
            showQRCode();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.unbind_qrconde_enter, R.anim.unbind_qrconde_exit);
    }

    private void showQRCode() {
/*       File imgFile = XgPushHelper.getInstance().getQRImageFile();
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            if (myBitmap != null) {
                mView.setImageBitmap(myBitmap);
            }
        }*/
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
        } else {
            Glide.with(QchatApplication.getInstance())
                    .load(R.drawable.refresh_qrcode)
                    .fallback(R.drawable.refresh_qrcode)
                    .error(R.drawable.refresh_qrcode)
                    .crossFade()
                    .skipMemoryCache(false)
                    .into(mView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
