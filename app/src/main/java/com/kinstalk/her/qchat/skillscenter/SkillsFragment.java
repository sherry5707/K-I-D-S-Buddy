package com.kinstalk.her.qchat.skillscenter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.activityhelper.VoiceAssistantActivityHelper;
import com.kinstalk.her.qchat.dialog.LoadingDialog;
import com.kinstalk.her.qchat.library.VoiceBookActivity;
import com.kinstalk.her.qchat.messaging.PrivacyManager;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.qloveaicore.TTSListener;

/**
 * 技能中心 fragment
 */
public class SkillsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SkillsFragment.class.getSimpleName();

//    private static final int ACTIVITY_RESULT_CODE = 10001;

    private Activity mActivity;

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

    private LinearLayout ll_content_1;

    private LinearLayout ll_content_2;

    private LinearLayout ll_content_3;

    private AudioRecord audioRecord = null;

    String[] resources = {"0|图书馆|我要听有声书", "1|翻译|英语翻译官", "2|故事|我要听睡前故事",
            "3|笑话|我要听笑话", "4|英语|苹果的英语怎么说", "5|造句|用成果造句",
            "6|寓言|给我讲个寓言故事", "7|成语|七上八下是什么意思", "8|算数|1+1=?",
            "9|百科|1千克等于多少克", "10|提醒|10分钟后提醒我吃零食"};

    // 2.1 定义用来与外部activity交互，获取到宿主activity
    private FragmentInteraction listterner;

    private VoiceAssistantActivityHelper helper;
    public LoadingDialog loadingDialog;

    private boolean isQuickClick = false;

    private static final Long SPACE_TIME = 800L;
    private static Long lastClickTime = 0L;

    // 1 定义了所有activity必须实现的接口方法
    public interface FragmentInteraction {
        void process(int index, boolean b);

        void sendSkillCommand(String command);

//        void playContent(int i);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        QAILog.d(TAG, "onAttach RUN");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        QAILog.d(TAG, "onCreateView RUN");

        mActivity = getActivity();

        helper = VoiceAssistantActivityHelper.instance;

        if (mActivity instanceof FragmentInteraction) {
            listterner = (FragmentInteraction) mActivity; // 2.2 获取到宿主activity并赋值
        } else {
            throw new IllegalArgumentException("activity must implements FragmentInteraction");
        }

        View view = inflater.inflate(R.layout.fragment_skills_center, null);
        //初始化控件

        bt_left = (Button) view.findViewById(R.id.bt_left);
        bt_left.setOnClickListener(this);

        bt_right = (Button) view.findViewById(R.id.bt_right);
        bt_right.setOnClickListener(this);

        iv_1 = (ImageView) view.findViewById(R.id.iv_1);
        iv_1.setOnClickListener(this);

        iv_2 = (ImageView) view.findViewById(R.id.iv_2);
        iv_2.setOnClickListener(this);

        iv_3 = (ImageView) view.findViewById(R.id.iv_3);
        iv_3.setOnClickListener(this);

        tv_title_1 = (TextView) view.findViewById(R.id.tv_title_1);
        tv_title_1.setOnClickListener(this);

        tv_title_2 = (TextView) view.findViewById(R.id.tv_title_2);
        tv_title_2.setOnClickListener(this);

        tv_title_3 = (TextView) view.findViewById(R.id.tv_title_3);
        tv_title_3.setOnClickListener(this);

        tv_content_1 = (TextView) view.findViewById(R.id.tv_content_1);
        tv_content_1.setOnClickListener(this);

        tv_content_2 = (TextView) view.findViewById(R.id.tv_content_2);
        tv_content_2.setOnClickListener(this);

        tv_content_3 = (TextView) view.findViewById(R.id.tv_content_3);
        tv_content_3.setOnClickListener(this);

        ll_content_1 = (LinearLayout) view.findViewById(R.id.ll_content_1);
//        ll_content_1.setOnClickListener(this);

        ll_content_2 = (LinearLayout) view.findViewById(R.id.ll_content_2);
//        ll_content_2.setOnClickListener(this);

        ll_content_3 = (LinearLayout) view.findViewById(R.id.ll_content_3);
//        ll_content_3.setOnClickListener(this);

        if (currentPageIndex == 0) {

            QAILog.d(TAG, "onCreateView -> " + currentPageIndex);

            bt_left.setVisibility(View.GONE);

            refreshContent(currentPageIndex);

        }

        loadingDialog = new LoadingDialog(mActivity);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        QAILog.d(TAG, "onActivityCreated RUN");

    }

    @SuppressLint("NewApi")
    @Override
    public void onResume() {

        super.onResume();

        initAudioRecord();
        QAILog.d(TAG, "onResume RUN");
    }

    @Override
    public void onDetach() {

        super.onDetach();

        QAILog.d(TAG, "onDetach RUN");

        //  mHelper.playContent(mActivity, 12);
        releaseRecord();
//        mHelper = null;

        mActivity = null;

        listterner = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelDialog();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_left:
                currentPageIndex = currentPageIndex - 1;

//                mHelper.refreshContent(currentPageIndex, handler);

                QAILog.d(TAG, "bt_left -> " + currentPageIndex);
                refreshContent(currentPageIndex);

                break;
            case R.id.bt_right:
                currentPageIndex = currentPageIndex + 1;

//                mHelper.refreshContent(currentPageIndex, handler);

                QAILog.d(TAG, "bt_right -> " + currentPageIndex);
                refreshContent(currentPageIndex);

                break;
            case R.id.iv_1:

                isQuickClick = isFastClick();
                QAILog.d(TAG, "iv_1 CLICK -> " + isQuickClick);

//                if (!isPlayingTTS)
                if (isQuickClick) {
                    return;
                }
                playContentByText(mActivity, currentPageIndex * 3);
                break;

            case R.id.iv_2:

                isQuickClick = isFastClick();
                QAILog.d(TAG, "iv_2 CLICK -> " + isQuickClick);

//                if (!isPlayingTTS)
                if (isQuickClick) {
                    return;
                }
                playContentByText(mActivity, currentPageIndex * 3 + 1);
                break;

            case R.id.iv_3:

                isQuickClick = isFastClick();

                QAILog.d(TAG, "iv_3 CLICK -> " + isQuickClick);

//                if (!isPlayingTTS)
                if (isQuickClick) {
                    return;
                }
                playContentByText(mActivity, currentPageIndex * 3 + 2);
                break;

            case R.id.tv_title_1:
            case R.id.tv_content_1:

                int index1 = currentPageIndex * 3;

                listterner.process(index1, false);

                if (currentPageIndex == 0) {
                    Intent i = new Intent(mActivity, VoiceBookActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                } else {
                    String skill_command_1 = getCommandStr(index1);

                    if (index1 != 7 && index1 != 8) {
                        showDialog();
                    }
                    listterner.sendSkillCommand(skill_command_1);
                }

                break;

            case R.id.tv_title_2:
            case R.id.tv_content_2:

                int index2 = currentPageIndex * 3 + 1;

                listterner.process(index2, false);

                if (currentPageIndex == 0) {
                    if (PrivacyManager.getInstance(mActivity).isPrivacy()) {
                        Toast.makeText(mActivity, "使用我的翻译，需要关闭隐私模式", Toast.LENGTH_LONG).show();
                        return;
                    }
                    String mPackageName = "kinstalk.com.wateranimapp";
                    String mActivityName = "kinstalk.com.translation.TranslationActivity";

                    Intent i = new Intent();
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setPackage(mPackageName);
                    i.setClassName(mPackageName, mActivityName);
                    startActivity(i);


                } else {
                    String skill_command_2 = getCommandStr(currentPageIndex * 3 + 1);
//                    mHelper.sendSkillCommand(mActivity, skill_command_2);

                    if (index2 != 7 && index2 != 8) {
                        showDialog();
                    }
                    listterner.sendSkillCommand(skill_command_2);
                }

                break;

            case R.id.tv_title_3:
            case R.id.tv_content_3:

                int index3 = currentPageIndex * 3 + 2;

                listterner.process(index3, false);

                String skill_command_3 = getCommandStr(index3);
//                mHelper.sendSkillCommand(mActivity, skill_command_3);

                if (index3 != 7 && index3 != 8) {
                    showDialog();
                }
                listterner.sendSkillCommand(skill_command_3);

                break;

            default:
                break;
        }
    }

    private String getCommandStr(int i) {

        String[] tmp = resources[i].split("\\|");
        QAILog.d(TAG, "getCommandStr -> " + tmp[2]);

        return tmp[2];

    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void initAudioRecord() {
        int frequency = 16000;
        //格式
        int channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
    }

    public void startRecord() {
        if (audioRecord != null && (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED)) {
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
        if (audioRecord != null && (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)) {
            audioRecord.stop();
        }
    }

    private void playContentByText(final Context mContext, final int i) {

        QAILog.d(TAG, "playContentByText run");

        new Thread(new Runnable() {
            @Override
            public void run() {

//                listterner.playContent(i);

                helper.playContent(mContext, i, mCommonTTSCb);

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

    /**
     * 刷新左右按钮切换显示技能内容
     *
     * @param currentPageIndex
     */
    private void refreshContent(int currentPageIndex) {

        switch (currentPageIndex) {
            case 0:

                bt_left.setVisibility(View.INVISIBLE);
                bt_right.setVisibility(View.VISIBLE);


                iv_2.setVisibility(View.VISIBLE);
                iv_3.setVisibility(View.VISIBLE);
                tv_title_2.setVisibility(View.VISIBLE);
                tv_title_3.setVisibility(View.VISIBLE);
                tv_content_2.setVisibility(View.VISIBLE);
                tv_content_3.setVisibility(View.VISIBLE);


                iv_1.setBackgroundResource(R.mipmap.skill_center_voice_book_icon);
                iv_2.setBackgroundResource(R.mipmap.translation);
                iv_3.setBackgroundResource(R.mipmap.story);
//
                tv_title_1.setText("图书馆");
                tv_title_2.setText("翻译");
                tv_title_3.setText("故事");

                tv_content_1.setText("我要听有声书");
                tv_content_2.setText("英语翻译官");
                tv_content_3.setText("我要听睡前故事");

                break;
            case 1:

                bt_left.setVisibility(View.VISIBLE);
                bt_right.setVisibility(View.VISIBLE);

                iv_2.setVisibility(View.VISIBLE);
                iv_3.setVisibility(View.VISIBLE);
                tv_title_2.setVisibility(View.VISIBLE);
                tv_title_3.setVisibility(View.VISIBLE);
                tv_content_2.setVisibility(View.VISIBLE);
                tv_content_3.setVisibility(View.VISIBLE);

                iv_1.setBackgroundResource(R.mipmap.joke);
                iv_2.setBackgroundResource(R.mipmap.english);
                iv_3.setBackgroundResource(R.mipmap.sentence_making);

                tv_title_1.setText("笑话");
                tv_title_2.setText("英语");
                tv_title_3.setText("造句");

                tv_content_1.setText("我要听笑话");
                tv_content_2.setText("苹果的英语怎么说");
                tv_content_3.setText("用成果造句");

                break;
            case 2:

                bt_left.setVisibility(View.VISIBLE);
                bt_right.setVisibility(View.VISIBLE);

                iv_2.setVisibility(View.VISIBLE);
                iv_3.setVisibility(View.VISIBLE);
                tv_title_2.setVisibility(View.VISIBLE);
                tv_title_3.setVisibility(View.VISIBLE);
                tv_content_2.setVisibility(View.VISIBLE);
                tv_content_3.setVisibility(View.VISIBLE);

                iv_1.setBackgroundResource(R.mipmap.allegory);
                iv_2.setBackgroundResource(R.mipmap.idiom);
                iv_3.setBackgroundResource(R.mipmap.math);

                tv_title_1.setText("寓言");
                tv_title_2.setText("成语");
                tv_title_3.setText("算数");

                tv_content_1.setText("给我讲个寓言故事");
                tv_content_2.setText("七上八下是什么意思");
                tv_content_3.setText("1+1=?");

                break;
            case 3:

                bt_left.setVisibility(View.VISIBLE);
                bt_right.setVisibility(View.INVISIBLE);

                iv_1.setBackgroundResource(R.mipmap.wikipedia);
                iv_2.setBackgroundResource(R.mipmap.remind);
                iv_3.setVisibility(View.INVISIBLE);

                tv_title_1.setText("百科");
                tv_title_2.setText("提醒");
                tv_title_3.setVisibility(View.INVISIBLE);

                tv_content_1.setText("1千克等于多少克");
                tv_content_2.setText("10分钟后提醒我吃零食");
                tv_content_3.setVisibility(View.INVISIBLE);

                break;
            default:
                break;
        }
    }

    private TTSListener mCommonTTSCb = new TTSListener() {

        @Override
        public void onTTSPlayBegin(String s) {
            QAILog.d(TAG, "onTTSPlayBegin");
        }

        @Override
        public void onTTSPlayEnd(String s) {
            QAILog.d(TAG, "onTTSPlayEnd");
        }

        @Override
        public void onTTSPlayProgress(String s, int i) {
            QAILog.d(TAG, "onTTSPlayProgress");
        }

        @Override
        public void onTTSPlayError(String s, int i, String s1) {
            QAILog.d(TAG, "onTTSPlayError");
        }
    };

    private static boolean isFastClick() {
        long currentTime = System.currentTimeMillis();//当前系统时间
        boolean isFastClick;//是否允许点击
        if (currentTime - lastClickTime < SPACE_TIME) {
            isFastClick = true;
        } else {
            isFastClick = false;
            lastClickTime = currentTime;
        }
        return isFastClick;
    }

    private void showDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getActivity());
            loadingDialog.show();
        } else {
            loadingDialog.show();
        }
    }

    private void cancelDialog() {
        if (loadingDialog != null) {
            loadingDialog.cancel();
            loadingDialog = null;
        }
    }

}
