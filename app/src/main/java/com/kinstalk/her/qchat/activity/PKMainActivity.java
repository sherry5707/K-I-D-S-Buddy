package com.kinstalk.her.qchat.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.PKBaseActivity;
import com.kinstalk.her.qchat.PKStartActivity;
import com.kinstalk.her.qchat.QchatApplication;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.utils.CountlyUtils;
import com.kinstalk.her.qchat.utils.CropCircleTransformation;
import com.kinstalk.her.qchat.utils.MusicUtil;
import com.kinstalk.her.qchat.utils.Utils;
import com.kinstalk.her.qchat.view.ToastGift;
import com.kinstalk.her.qchatapi.Api;
import com.kinstalk.her.qchatcomm.thread.QchatThreadManager;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.Manager.model.pkInfoCallback;
import com.kinstalk.her.qchatmodel.entity.CardEntity;
import com.kinstalk.her.qchatmodel.entity.PKPetInfo;
import com.kinstalk.her.qchatmodel.entity.PKPropInfo;
import com.kinstalk.her.qchatmodel.entity.PKQuestionInfo;
import com.kinstalk.her.qchatmodel.entity.PKUserInfo;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Created by bean on 2018/8/23.
 */

public class PKMainActivity extends PKBaseActivity implements View.OnClickListener, pkInfoCallback {
    private PKUserInfo user;
    private List<PKQuestionInfo> questionInfo = new ArrayList<>(10);
    private List<PKPropInfo> propInfo = new ArrayList<>(3);
    private List<PKQuestionInfo.Sections> sections = new ArrayList<>(3);

    private ImageView mAvatar;//人物头像
    private TextView mLastNum;//剩余人数
    private TextView mContentText;//文本题目
    private ImageView mContentImg;//图片题目
    private ImageView mCardOne, mCardTwo, mCardThree;//三张道具卡
    private TextView mCardNumOne, mCardNumTwo, mCardNumThree;//卡片数量
    private List<Integer> errorAnswer = new ArrayList<>();//道具卡获取的错误答案集合
    private RelativeLayout mLayoutDiaoxue;//掉血的边框

    private TextView mTime;//倒计时
    private int allTime = 15;//每题的总时间
    private int nowTime;//现在的时间
    private static final int OVER_TIME = 1000;//1000毫秒后结束
    private ProgressBar progressBar;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (Utils.isAtHome(PKMainActivity.this)) {//如果再游戏过程中回到主界面，直接提交成绩并销毁
                Log.i(TAG, "回到主界面，游戏结束");
                QchatThreadManager.getInstance().start(new Runnable() {
                    @Override
                    public void run() {
                        //当前回答错误，position不+1
                        Api.postPKRewrd(token, sn, position, questionInfo.get(position).getLastNum());
                    }
                });
                finish();
            }
            if (nowTime == 0) {
                //时间到了
                Log.i(TAG, "答题时间到了");
                cancelDialog();//防止请求超时大于答题时间
                progressBar.setProgress(0);
                handler.removeCallbacks(runnable);
                showDialog();
                QchatThreadManager.getInstance().start(new Runnable() {
                    @Override
                    public void run() {
                        //当前回答错误，position不+1
                        Api.postPKRewrd(token, sn, position, questionInfo.get(position).getLastNum());
                    }
                });
                return;
            }
            nowTime--;
            if (nowTime < 10) {
                mTime.setText("00:0" + nowTime);
            } else {
                mTime.setText("00:" + nowTime);
            }
            progressBar.setProgress(nowTime * 100 / allTime);
            handler.postDelayed(runnable, 1000);
        }
    };
    private TextView mAnswerTextOne, mAnswerTextTwo, mAnswerTextThree;//答案文字
    private ImageView mAnswerImgOne, mAnswerImgTwo, mAnswerImgThree;//答案图片
    private ImageView mAnswerTrueOne, mAnswerTrueTwo, mAnswerTrueThree;//答案正确图片
    private ImageView mAnswerFalseOne, mAnswerFalseTwo, mAnswerFalseThree;//答案错误图片


    private int position = -1;//当前坐标

    private boolean isChoose;//是否可以选择答案，避免重复点击

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QchatApplication.getApplicationInstance().addActivity(this);
        setContentView(R.layout.activity_pk_main);
        user = (PKUserInfo) getIntent().getSerializableExtra("userInfo");
        questionInfo = (List<PKQuestionInfo>) getIntent().getSerializableExtra("questionInfo");
        propInfo = (List<PKPropInfo>) getIntent().getSerializableExtra("propInfo");
        initViews();
    }

    private void initViews() {
        MessageManager.registerPKInfoCallback(this);

        mLayoutDiaoxue = (RelativeLayout) findViewById(R.id.diaoxue);
        mAvatar = (ImageView) findViewById(R.id.pk_main_avatar);
        mLastNum = (TextView) findViewById(R.id.last_num);
        mContentText = (TextView) findViewById(R.id.content_text);
        mContentImg = (ImageView) findViewById(R.id.content_img);
        mCardOne = (ImageView) findViewById(R.id.card_one);
        mCardOne.setOnClickListener(this);
        mCardTwo = (ImageView) findViewById(R.id.card_two);
        mCardTwo.setOnClickListener(this);
        mCardThree = (ImageView) findViewById(R.id.card_three);
        mCardThree.setOnClickListener(this);
        mCardNumOne = (TextView) findViewById(R.id.card_num_one);
        mCardNumTwo = (TextView) findViewById(R.id.card_num_two);
        mCardNumThree = (TextView) findViewById(R.id.card_num_three);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTime = (TextView) findViewById(R.id.time);
        mAnswerImgOne = (ImageView) findViewById(R.id.answer_img_one);
        mAnswerImgTwo = (ImageView) findViewById(R.id.answer_img_two);
        mAnswerImgThree = (ImageView) findViewById(R.id.answer_img_three);
        mAnswerTextOne = (TextView) findViewById(R.id.answer_text_one);
        mAnswerTextOne.setOnClickListener(this);
        mAnswerTextTwo = (TextView) findViewById(R.id.answer_text_two);
        mAnswerTextTwo.setOnClickListener(this);
        mAnswerTextThree = (TextView) findViewById(R.id.answer_text_three);
        mAnswerTextThree.setOnClickListener(this);
        mAnswerTrueOne = (ImageView) findViewById(R.id.answer_true_one);
        mAnswerTrueTwo = (ImageView) findViewById(R.id.answer_true_two);
        mAnswerTrueThree = (ImageView) findViewById(R.id.answer_true_three);
        mAnswerFalseOne = (ImageView) findViewById(R.id.answer_false_one);
        mAnswerFalseTwo = (ImageView) findViewById(R.id.answer_false_two);
        mAnswerFalseThree = (ImageView) findViewById(R.id.answer_false_three);

        //头像
        Glide.with(this)
                .load(user.getAvatar())
                .asBitmap()
                .transform(new CropCircleTransformation(this))
                .placeholder(R.drawable.avatar_pk_default)
                .into(mAvatar);

        //道具卡
        if (null != propInfo.get(0)) {
            Glide.with(this)
                    .load(propInfo.get(0).getUrl())
                    .asBitmap()
                    .placeholder(R.drawable.prop_card)
                    .into(mCardOne);
        }

        if (null != propInfo.get(1)) {
            Glide.with(this)
                    .load(propInfo.get(1).getUrl())
                    .asBitmap()
                    .placeholder(R.drawable.prop_card)
                    .into(mCardTwo);
        }
        if (null != propInfo.get(2)) {
            Glide.with(this)
                    .load(propInfo.get(2).getUrl())
                    .asBitmap()
                    .placeholder(R.drawable.prop_card)
                    .into(mCardThree);
        }
        resetCardNum();
        resetUi();
    }

    //卡片数量改变的时候调用
    private void resetCardNum() {
        if (null != propInfo.get(0)) {
            mCardNumOne.setText(propInfo.get(0).getNum() + "");
        }
        if (null != propInfo.get(1)) {
            mCardNumTwo.setText(propInfo.get(1).getNum() + "");
        }
        if (null != propInfo.get(2)) {
            mCardNumThree.setText(propInfo.get(2).getNum() + "");
        }
    }

    //每题开始之前重置UI
    private void resetUi() {
        if ((position + 1) == questionInfo.size()) {
            Log.i(TAG, "当前没有题目了");
            handler.removeCallbacks(runnable);
            showDialog();
            QchatThreadManager.getInstance().start(new Runnable() {
                @Override
                public void run() {
                    //当前全部回答完毕，position+1
                    Api.postPKRewrd(token, sn, position + 1, questionInfo.get(position).getLastNum());
                }
            });
            return;
        }
        position++;
        Log.i(TAG, "当前是第" + (position + 1) + "题");

        //前面7题15s，后面3题8s
        if (position < 7) {
            allTime = 15;
            mTime.setText("00:" + allTime);
            mLayoutDiaoxue.setVisibility(View.GONE);
        } else {
            allTime = 8;
            mTime.setText("00:0" + allTime);
            mLayoutDiaoxue.setVisibility(View.VISIBLE);
        }
        mAnswerTrueOne.setVisibility(View.GONE);
        mAnswerTrueTwo.setVisibility(View.GONE);
        mAnswerTrueThree.setVisibility(View.GONE);
        mAnswerFalseOne.setVisibility(View.GONE);
        mAnswerFalseTwo.setVisibility(View.GONE);
        mAnswerFalseThree.setVisibility(View.GONE);
        mAnswerTextOne.setText("");
        mAnswerTextTwo.setText("");
        mAnswerTextThree.setText("");
        mAnswerImgOne.setImageDrawable(null);
        mAnswerImgTwo.setImageDrawable(null);
        mAnswerImgThree.setImageDrawable(null);

        mContentImg.setImageDrawable(null);
        mContentText.setText("");

        isChoose = true;
        nowTime = allTime;
        errorAnswer.clear();
        progressBar.setProgress(100);
        sections = questionInfo.get(position).getSections();
        mLastNum.setText(questionInfo.get(position).getLastNum() + "");

        //图片为空的时候才显示文字
        if (TextUtils.isEmpty(questionInfo.get(position).getTopicUrl())) {
            mContentText.setText(questionInfo.get(position).getTopic());
        }
        if (null == this) {
            //glider加载图片之前判断当前界面是否存在，如果不存在了，下面传参context会为null
            return;
        }
        Glide.with(this)
                .load(questionInfo.get(position).getTopicUrl())
                .asBitmap()
                .into(mContentImg);

        //图片为空的时候才显示文字
        if (TextUtils.isEmpty(sections.get(0).getUrl())) {
            mAnswerTextOne.setText(sections.get(0).getName());
        }

        if (TextUtils.isEmpty(sections.get(1).getUrl())) {
            mAnswerTextTwo.setText(sections.get(1).getName());
        }

        if (TextUtils.isEmpty(sections.get(2).getUrl())) {
            mAnswerTextThree.setText(sections.get(2).getName());
        }

        Glide.with(this)
                .load(sections.get(0).getUrl())
                .asBitmap()
                .into(mAnswerImgOne);
        Glide.with(this)
                .load(sections.get(1).getUrl())
                .asBitmap()
                .into(mAnswerImgTwo);
        Glide.with(this)
                .load(sections.get(2).getUrl())
                .asBitmap()
                .into(mAnswerImgThree);

        //播放语音
        playTTS(questionInfo.get(position).getVoice());
        handler.postDelayed(runnable, 1000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != handler) {
            handler.removeCallbacks(runnable);
            runnable = null;
        }
        MessageManager.unRegisterPKInfoCallback(this);
        QchatApplication.getApplicationInstance().removeActivity(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //选择答案
            case R.id.answer_text_one:
                if (isChoose) {
                    chooseAnswer(0);
                }
                break;
            case R.id.answer_text_two:
                if (isChoose) {
                    chooseAnswer(1);
                }
                break;
            case R.id.answer_text_three:
                if (isChoose) {
                    chooseAnswer(2);
                }
                break;

            //选择道具卡
            case R.id.card_one:
                if (propInfo.get(0).getNum() == 0) {
                    playTTS("没有更多的卡片啦！");
                    return;
                }
                if (propInfo.get(0).getNum() > 0 && isChoose) {
                    MusicUtil.getInstance().playUrl(this, MUSIC_BUTTON);
                    clickProp(propInfo.get(0));
                    Map<String, String> segmentation = new HashMap<>();
                    segmentation.put("propid", propInfo.get(0).getId() + "");
                    segmentation.put("propname", propInfo.get(0).getName());
                    CountlyUtils.countlyRecordEvent("game", "t_game_prop_card", segmentation, 1);
                }
                break;
            case R.id.card_two:
                if (propInfo.get(1).getNum() == 0) {
                    playTTS("没有更多的卡片啦！");
                    return;
                }
                if (propInfo.get(1).getNum() > 0 && isChoose) {
                    if (errorAnswer.size() == (sections.size() - 1)) {//说明只剩下一个正确答案了
                        ToastGift.makeText(PKMainActivity.this, "只剩下一个正确答案啦了！", Toast.LENGTH_LONG).show();
                        playTTS("只剩下一个正确答案啦！");
                        return;
                    }
                    MusicUtil.getInstance().playUrl(this, MUSIC_BUTTON);
                    clickProp(propInfo.get(1));
                    Map<String, String> segmentation = new HashMap<>();
                    segmentation.put("propid", propInfo.get(1).getId() + "");
                    segmentation.put("propname", propInfo.get(1).getName());
                    CountlyUtils.countlyRecordEvent("game", "t_game_prop_card", segmentation, 1);
                }
                break;
            case R.id.card_three:
                if (propInfo.get(2).getNum() == 0) {
                    playTTS("没有更多的卡片啦！");
                    return;
                }
                if (propInfo.get(2).getNum() > 0 && isChoose) {
                    MusicUtil.getInstance().playUrl(this, MUSIC_BUTTON);
                    clickProp(propInfo.get(2));
                    Map<String, String> segmentation = new HashMap<>();
                    segmentation.put("propid", propInfo.get(2).getId() + "");
                    segmentation.put("propname", propInfo.get(2).getName());
                    CountlyUtils.countlyRecordEvent("game", "t_game_prop_card", segmentation, 1);
                }
                break;
        }
    }

    //使用的道具
    private void clickProp(final PKPropInfo pkPropInfo) {
        showDialog();
        QchatThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                Api.postPKPropUse(token, sn, pkPropInfo);
            }
        });
    }

    //使用道具卡
    private void useProp(PKPropInfo pkPropInfo) {
        switch (pkPropInfo.getId()) {
            case 1://(瞒天过海:20%的几率使用户失败，赶出考场)
                Random random = new Random();
                int num = random.nextInt(100);
                if (num < 20) {
                    //赶出考场
                    ToastGift.makeText(PKMainActivity.this, "很遗憾，您被赶出考场！", Toast.LENGTH_LONG).show();
                    isChoose = false;//防止失败之后用户点击
                    handler.removeCallbacks(runnable);
                    QchatThreadManager.getInstance().start(new Runnable() {
                        @Override
                        public void run() {
                            //当前回答错误，position不+1
                            Api.postPKRewrd(token, sn, position, questionInfo.get(position).getLastNum());
                        }
                    });
                } else {
                    //答对
                    for (int i = 0; i < sections.size(); i++) {
                        if (sections.get(i).isAnswer()) {
                            switch (i) {
                                case 0:
                                    onClick(mAnswerTextOne);
                                    break;
                                case 1:
                                    onClick(mAnswerTextTwo);
                                    break;
                                case 2:
                                    onClick(mAnswerTextThree);
                                    break;
                            }
                        }
                    }
                }
                break;
            case 2://(雪中送炭:去掉一个错误答案)
                Random random2 = new Random();
                int num2 = random2.nextInt(sections.size());
                while (sections.get(num2).isAnswer() || errorAnswer.contains(num2)) {
                    num2 = random2.nextInt(sections.size());
                }
                errorAnswer.add(num2);//添加错误答案，1.防止用户重复点击 2.防止用户使用多次道具排重
                switch (num2) {
                    case 0:
                        mAnswerFalseOne.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        mAnswerFalseTwo.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        mAnswerFalseThree.setVisibility(View.VISIBLE);
                        break;
                }
                break;
            case 3://（东张西望:告诉你周围人答案。正确答案有70%的几率超过50%，其他随机）
                int allNum = 100;//总份额100
                int lastNum;//得到正确答案份额之后的剩余份额
                int trueNum;//正确答案的份额
                int oneNum = 0;//第一个错误答案的份额
                int twoNum = 0;//第二个错误答案的份额
                Random random3 = new Random();
                int num3 = random3.nextInt(allNum);//这里随机数是（0-99），所以结果需要+1
                if (num3 > 70) {//份额低于百分50
                    trueNum = random3.nextInt(50) + 1;
                } else {//份额高于百分50
                    trueNum = random3.nextInt(50) + 51;
                }
                lastNum = allNum - trueNum;
                if (0 == lastNum) {//随机数值如果为0，会报错：java.lang.IllegalArgumentException: n must be positive
                    oneNum = 0;
                    twoNum = 0;
                } else {
                    oneNum = random3.nextInt(lastNum) + 1;
                    twoNum = lastNum - oneNum;
                }
                if (sections.get(0).isAnswer()) {
                    sections.get(0).setNum(trueNum);
                    sections.get(1).setNum(oneNum);
                    sections.get(2).setNum(twoNum);
                } else if (sections.get(1).isAnswer()) {
                    sections.get(0).setNum(oneNum);
                    sections.get(1).setNum(trueNum);
                    sections.get(2).setNum(twoNum);
                } else if (sections.get(2).isAnswer()) {
                    sections.get(0).setNum(oneNum);
                    sections.get(1).setNum(twoNum);
                    sections.get(2).setNum(trueNum);
                }
                playTTS("百分之" + sections.get(0).getNum() + "的人选择第一个,"
                        + "百分之" + sections.get(1).getNum() + "的人选择第二个,"
                        + "百分之" + sections.get(2).getNum() + "的人选择第三个");
                Log.i(TAG, "trueNum=" + trueNum + " oneNum=" + oneNum + " twoNum=" + twoNum);
                break;
        }
    }

    //选择第几个答案
    private void chooseAnswer(int id) {
        if (errorAnswer.contains(id)) {
            //当用户使用道具卡之后，不能再次点击错误答案
            return;
        }
        isChoose = false;
        handler.removeCallbacks(runnable);
        switch (id) {
            case 0:
                if (questionInfo.get(position).getSections().get(0).isAnswer()) {
                    mAnswerTrueOne.setVisibility(View.VISIBLE);
                    MusicUtil.getInstance().playUrl(this, MUSIC_ANSWER_TRUE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resetUi();
                        }
                    }, OVER_TIME);
                } else {
                    mAnswerFalseOne.setVisibility(View.VISIBLE);
                    MusicUtil.getInstance().playUrl(this, MUSIC_ANSWER_FALSE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showDialog();
                            QchatThreadManager.getInstance().start(new Runnable() {
                                @Override
                                public void run() {
                                    //当前回答错误，position不+1
                                    Api.postPKRewrd(token, sn, position, questionInfo.get(position).getLastNum());
                                }
                            });
                        }
                    }, OVER_TIME);
                }
                break;

            case 1:
                if (questionInfo.get(position).getSections().get(1).isAnswer()) {
                    mAnswerTrueTwo.setVisibility(View.VISIBLE);
                    MusicUtil.getInstance().playUrl(this, MUSIC_ANSWER_TRUE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resetUi();
                        }
                    }, OVER_TIME);
                } else {
                    mAnswerFalseTwo.setVisibility(View.VISIBLE);
                    MusicUtil.getInstance().playUrl(this, MUSIC_ANSWER_FALSE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showDialog();
                            QchatThreadManager.getInstance().start(new Runnable() {
                                @Override
                                public void run() {
                                    //当前回答错误，position不+1
                                    Api.postPKRewrd(token, sn, position, questionInfo.get(position).getLastNum());
                                }
                            });
                        }
                    }, OVER_TIME);
                }
                break;

            case 2:
                if (questionInfo.get(position).getSections().get(2).isAnswer()) {
                    mAnswerTrueThree.setVisibility(View.VISIBLE);
                    MusicUtil.getInstance().playUrl(this, MUSIC_ANSWER_TRUE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resetUi();
                        }
                    }, OVER_TIME);
                } else {
                    mAnswerFalseThree.setVisibility(View.VISIBLE);
                    MusicUtil.getInstance().playUrl(this, MUSIC_ANSWER_FALSE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showDialog();
                            QchatThreadManager.getInstance().start(new Runnable() {
                                @Override
                                public void run() {
                                    //当前回答错误，position不+1
                                    Api.postPKRewrd(token, sn, position, questionInfo.get(position).getLastNum());
                                }
                            });
                        }
                    }, OVER_TIME);
                }
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        position = -1;
        user = (PKUserInfo) intent.getSerializableExtra("userInfo");
        questionInfo = (List<PKQuestionInfo>) intent.getSerializableExtra("questionInfo");
        propInfo = (List<PKPropInfo>) intent.getSerializableExtra("propInfo");
        initViews();
    }

    @Override
    public void onPKInfoChanged(List<PKPetInfo> petList, PKUserInfo user) {

    }

    @Override
    public void onPKInfoGetError(int code) {

    }

    @Override
    public void onPKChoosePet(int id) {

    }

    @Override
    public void onPKChoosePetError(int code) {

    }

    @Override
    public void onPKQuestionChanged(List<PKQuestionInfo> questionList, List<PKPropInfo> propList) {

    }

    @Override
    public void onPKQuestionGetError(int code) {

    }

    /**
     * 提交成绩成功
     *
     * @param credit
     * @param propInfos
     * @param cardEntities
     * @param ranking
     */
    @Override
    public void onPKRewardChanged(final int credit, final List<PKPropInfo> propInfos, final List<CardEntity> cardEntities, final int ranking) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                Intent intent = new Intent(PKMainActivity.this, PKRewardActivity.class);
                intent.putExtra("credit", credit);
                intent.putExtra("ranking", ranking);
                intent.putExtra("propInfos", (Serializable) propInfos);
                intent.putExtra("cardEntities", (Serializable) cardEntities);
                intent.putExtra("userInfo", user);
                startActivity(intent);
                finish();
                Log.i(TAG, "onPKRewardChanged credit=" + credit
                        + " propInfos=" + propInfos.toString()
                        + " cardEntities=" + cardEntities.toString()
                        + " ranking=" + ranking);
            }
        });
    }

    @Override
    public void onPKRewardGetError(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                AlertDialog.Builder builder = new AlertDialog.Builder(PKMainActivity.this);
                builder.setMessage("啊嗷，网络开小差了，成绩单飞走了，再玩一局吧");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(PKMainActivity.this, PKStartActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                //ToastGift.makeText(PKMainActivity.this, "提交成绩失败！", Toast.LENGTH_LONG).show();
                Log.i(TAG, "onPKRewardGetError  code=" + code);
            }
        });

    }

    /**
     * 使用道具卡成功
     *
     * @param pkPropInfo 道具卡信息
     */
    @Override
    public void onPKPropUse(final PKPropInfo pkPropInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                Log.i(TAG, "onPKPropUse  pkPropInfo=" + pkPropInfo.toString());
                for (int i = 0; i < propInfo.size(); i++) {
                    if (pkPropInfo.getId() == propInfo.get(i).getId()) {
                        propInfo.get(i).setNum(propInfo.get(i).getNum() - 1);
                        resetCardNum();
                        useProp(pkPropInfo);
                        return;
                    }
                }
            }
        });
    }

    /**
     * 使用道具卡失败
     *
     * @param code
     */
    @Override
    public void onPKPropUseError(final int code) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
                ToastGift.makeText(PKMainActivity.this, "使用道具卡片失败！", Toast.LENGTH_LONG).show();
                Log.i(TAG, "onPKPropUseError  code=" + code);
            }
        });
    }
}
