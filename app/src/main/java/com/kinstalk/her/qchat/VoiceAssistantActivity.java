package com.kinstalk.her.qchat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kinstalk.her.qchat.activityhelper.VoiceAssistantActivityHelper;
import com.kinstalk.her.qchat.messaging.XgPushHelper;
import com.kinstalk.her.qchat.utils.LogUtil;

import ly.count.android.sdk.Countly;

/**
 * 语音助手
 */
public class VoiceAssistantActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * TAG
     */
    private static final String TAG = VoiceAssistantActivity.class.getSimpleName();

    /**
     * 小薇语音助手提示语
     */
    private static final String TIPS = "我是无所不能的小微， 想跟我玩点什么呢？";

    /**
     * context
     */
    private Context mContext;

    private Button bt_close;

    private Button bt_left;

    private Button bt_right;

    private ImageView iv_1;

    private ImageView iv_2;

    private ImageView iv_3;

    private TextView tv_title_1;

    private TextView tv_title_2;

    private TextView tv_title_3;

    private TextView tv_content_1;

    private TextView tv_content_2;

    private TextView tv_content_3;

    private int currentPageIndex = 0;

    private VoiceAssistantActivityHelper mHelper;

    private LinearLayout ll_content_1;

    private LinearLayout ll_content_2;

    private LinearLayout ll_content_3;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {

            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_voice_assistant);

        initView();

        initData();
        initAudioRecord();

    }

    private void initData() {

        mContext = this;

        mHelper = VoiceAssistantActivityHelper.instance;

    }

    private void initView() {

        bt_close = (Button) findViewById(R.id.bt_close);
        bt_close.setOnClickListener(this);

        bt_left = (Button) findViewById(R.id.bt_left);
        bt_left.setOnClickListener(this);

        bt_right = (Button) findViewById(R.id.bt_right);
        bt_right.setOnClickListener(this);

        iv_1 = (ImageView) findViewById(R.id.iv_1);
        iv_2 = (ImageView) findViewById(R.id.iv_2);
        iv_3 = (ImageView) findViewById(R.id.iv_3);

        tv_title_1 = (TextView) findViewById(R.id.tv_title_1);
        tv_title_2 = (TextView) findViewById(R.id.tv_title_2);
        tv_title_3 = (TextView) findViewById(R.id.tv_title_3);

        tv_content_1 = (TextView) findViewById(R.id.tv_content_1);
        tv_content_2 = (TextView) findViewById(R.id.tv_content_2);
        tv_content_3 = (TextView) findViewById(R.id.tv_content_3);

        ll_content_1 = (LinearLayout) findViewById(R.id.ll_content_1);
        ll_content_1.setOnClickListener(this);

        ll_content_2 = (LinearLayout) findViewById(R.id.ll_content_2);
        ll_content_2.setOnClickListener(this);

        ll_content_3 = (LinearLayout) findViewById(R.id.ll_content_3);
        ll_content_3.setOnClickListener(this);

        if (currentPageIndex == 0) {

            LogUtil.d(TAG, "initView -> " + currentPageIndex);

            bt_left.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        LogUtil.d(TAG, "onStart -> ");

        mHelper.playWorkContent(mContext, TIPS);
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
        mHelper.playContent(mContext, 12);
        releaseRecord();
        mHelper = null;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.bt_close:
                finish();
                overridePendingTransition(R.anim.fullscreen_enter, R.anim.fullscreen_exit);
                XgPushHelper.getInstance().startMainActivity();
                break;
            case R.id.bt_left:
                currentPageIndex = currentPageIndex - 1;

//                mHelper.refreshContent(currentPageIndex, handler);

                LogUtil.d(TAG, "bt_left -> " + currentPageIndex);
                refreshContent(currentPageIndex);

                break;
            case R.id.bt_right:
                currentPageIndex = currentPageIndex + 1;

//                mHelper.refreshContent(currentPageIndex, handler);

                LogUtil.d(TAG, "bt_right -> " + currentPageIndex);
                refreshContent(currentPageIndex);

                break;

            case R.id.ll_content_1:

                playContentByText(mContext, currentPageIndex * 3);

                break;
            case R.id.ll_content_2:

                playContentByText(mContext, currentPageIndex * 3 + 1);

                break;
            case R.id.ll_content_3:

                playContentByText(mContext, currentPageIndex * 3 + 2);

                break;
            default:
                break;
        }

    }

    private void playContentByText(final Context mContext, final int i) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                mHelper.playContent(mContext, i);
                try {
                    Thread.sleep(3000);

                    startRecord();
                    Thread.sleep(3000);
                    stopRecord();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private void refreshContent(int currentPageIndex) {

        switch (currentPageIndex) {
            case 0:

                bt_left.setVisibility(View.INVISIBLE);
                bt_right.setVisibility(View.VISIBLE);

                iv_1.setBackgroundResource(R.mipmap.joke);
                iv_2.setBackgroundResource(R.mipmap.universe);
                iv_3.setBackgroundResource(R.mipmap.story);

                tv_title_1.setBackgroundResource(R.mipmap.title_joke);
                tv_title_2.setBackgroundResource(R.mipmap.title_universe);
                tv_title_3.setBackgroundResource(R.mipmap.title_story);

                tv_content_1.setBackgroundResource(R.mipmap.content_joke);
                tv_content_2.setBackgroundResource(R.mipmap.content_universe);
                tv_content_3.setBackgroundResource(R.mipmap.content_story);

                break;
            case 1:

                bt_left.setVisibility(View.VISIBLE);
                bt_right.setVisibility(View.VISIBLE);

                iv_1.setBackgroundResource(R.mipmap.idiom);
                iv_2.setBackgroundResource(R.mipmap.math);
                iv_3.setBackgroundResource(R.mipmap.wikipedia);

                tv_title_1.setBackgroundResource(R.mipmap.title_idiom);
                tv_title_2.setBackgroundResource(R.mipmap.title_math);
                tv_title_3.setBackgroundResource(R.mipmap.title_wikipedia);

                tv_content_1.setBackgroundResource(R.mipmap.content_idiom);
                tv_content_2.setBackgroundResource(R.mipmap.content_math);
                tv_content_3.setBackgroundResource(R.mipmap.content_wikipedia);

                break;
            case 2:

                bt_left.setVisibility(View.VISIBLE);
                bt_right.setVisibility(View.VISIBLE);

                iv_1.setBackgroundResource(R.mipmap.english);
                iv_2.setBackgroundResource(R.mipmap.poetry);
                iv_3.setBackgroundResource(R.mipmap.allegory);

                tv_title_1.setBackgroundResource(R.mipmap.title_english);
                tv_title_2.setBackgroundResource(R.mipmap.title_poetry);
                tv_title_3.setBackgroundResource(R.mipmap.title_allegory);

                tv_content_1.setBackgroundResource(R.mipmap.content_english);
                tv_content_2.setBackgroundResource(R.mipmap.content_poetry);
                tv_content_3.setBackgroundResource(R.mipmap.content_allegory);

                break;
            case 3:

                bt_left.setVisibility(View.VISIBLE);
                bt_right.setVisibility(View.INVISIBLE);

                iv_1.setBackgroundResource(R.mipmap.remind);
                iv_2.setBackgroundResource(R.mipmap.sentence_making);
                iv_3.setBackgroundResource(R.mipmap.conversion);

                tv_title_1.setBackgroundResource(R.mipmap.title_remind);
                tv_title_2.setBackgroundResource(R.mipmap.title_sentence_making);
                tv_title_3.setBackgroundResource(R.mipmap.title_conversion);

                tv_content_1.setBackgroundResource(R.mipmap.content_remind);
                tv_content_2.setBackgroundResource(R.mipmap.content_sentence_making);
                tv_content_3.setBackgroundResource(R.mipmap.content_conversion);

                break;
            default:
                break;
        }
    }

    private void initAudioRecord() {
        int frequency = 16000;
        //格式
        int channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
    }

    private AudioRecord audioRecord = null;


    public void startRecord() {
        if (audioRecord != null) {
            audioRecord.startRecording();
        }
    }

    private void releaseRecord() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    public void stopRecord() {
        if (audioRecord != null) {
            audioRecord.stop();
        }
    }
}
