package com.kinstalk.her.qchat.activity;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinstalk.her.qchat.GiftActivity;
import com.kinstalk.her.qchat.R;


/**
 * Created by bean on 2018/7/3.
 */

public class DialogTaskActivity extends Activity {
    private String butText;//按钮文字
    private Button button;

    private RelativeLayout successLayout;
    private TextView numText;//获取的学分数
    private TextView successText;//获取成功的内容
    private RelativeLayout failLayout;
    private TextView failText;//获取失败的内容

    private int gotCredit;//获取到的学分
    private boolean isSuccess;//是否成功

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_get_task);
        button = (Button) findViewById(R.id.button);
        numText = (TextView) findViewById(R.id.num);
        successText = (TextView) findViewById(R.id.content_success);
        successLayout = (RelativeLayout) findViewById(R.id.layout_success);
        failLayout = (RelativeLayout) findViewById(R.id.layout_fail);
        failText = (TextView) findViewById(R.id.content_fail);

        gotCredit = getIntent().getIntExtra("credit_got", 0);
        isSuccess = getIntent().getBooleanExtra("is_success", false);

        if (isSuccess) {
            setButText("确定");
            setSuccessContent("领取奖励");
            setNum(gotCredit);
        } else {
            setButText("挣学分>>");
            setFailContent("糟啦！学分不够，一起去挣学分吧");
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSuccess) {
                    Intent i = new Intent(DialogTaskActivity.this, GiftActivity.class);
                    i.putExtra("page_number", 0);
                    startActivity(i);
                }
                finish();
            }
        });
    }

    public void setNum(int num) {
        numText.setText(num + "个");
    }


    public void setSuccessContent(String successContent) {
        successText.setText(successContent);
        failLayout.setVisibility(View.GONE);
        successLayout.setVisibility(View.VISIBLE);
    }

    public void setFailContent(String failContent) {
        failText.setText(failContent);
        failLayout.setVisibility(View.VISIBLE);
        successLayout.setVisibility(View.GONE);
    }

    public void setButText(String butText) {
        button.setText(butText);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //有可能已用户在领取奖励之后，直接唤醒百科知识，activity就不会销毁。这边处理是为了让数据可以及时刷新。
        gotCredit = intent.getIntExtra("credit_got", 0);
        isSuccess = intent.getBooleanExtra("is_success", false);
        if (isSuccess) {
            setButText("确定");
            setSuccessContent("领取奖励");
            setNum(gotCredit);
        } else {
            setButText("挣学分>>");
            setFailContent("遭啦！学分不够，一起去挣学分吧");
        }
    }
}
