package com.kinstalk.her.qchat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kinstalk.her.qchat.dialog.LoadingDialog;
import com.kinstalk.her.qchat.fragment.CardsFragment;
import com.kinstalk.her.qchat.fragment.GiftsFragment;
import com.kinstalk.her.qchat.fragment.TasksFragment;
import com.kinstalk.her.qchat.messaging.XgPushHelper;
import com.kinstalk.her.qchat.view.NoScrollViewPager;
import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.qloveaicore.AIManager;


import java.util.ArrayList;
import java.util.List;


import ly.count.android.sdk.Countly;

public class GiftActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = "GiftActivityLog";
    private NoScrollViewPager viewPager;

    private List<Fragment> fragments = new ArrayList<Fragment>(3);
    private TasksFragment mTasksFragment;
    private GiftsFragment mGiftsFaragment;
    private CardsFragment mCardsFragment;
    private MyPagerAdapter mPagerAdapter;

    private ImageButton mBack;//返回按钮

    //标题栏三个按钮
    private Button mTaskBtn;
    private Button mGiftBtn;
    private Button mCardBtn;

    //右边星星、积分的布局和数量
    private RelativeLayout mStarLayout;
    private TextView mStarText;
    private RelativeLayout mCreditLayout;
    private TextView mCreditText;

    private LoadingDialog loadingDialog;//加载中布局;
    private int hasCardNum;//拥有的普通卡片数量;
    private int pageNumber;//通过其他方式唤醒进入对应界面（0，1,2）
    private boolean isPlayGift = false;//判断是否播放过礼物语音了(分为手动进入和语音进入)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        QchatApplication.getApplicationInstance().addActivity(this);
        //禁止30秒回首页代码，不用屏蔽
        /*
        try {
            AppUtils.setAutoActivityTimeout(getWindow(), false);
        } catch (Exception e1) {
            e1.printStackTrace();
        }*/


        setContentView(R.layout.activity_gift);
        initViews();
        initData();
    }

    //设置星星积分
    private void initData() {
        updateStarNum(getIntent().getIntExtra("all_stars", 0));
    }

    private void initViews() {
        loadingDialog = new LoadingDialog(this);
        viewPager = (NoScrollViewPager) findViewById(R.id.viewpager);
        viewPager.setOverScrollMode(viewPager.OVER_SCROLL_NEVER);

        mGiftsFaragment = new GiftsFragment();
        mTasksFragment = new TasksFragment();
        mCardsFragment = new CardsFragment();
        fragments.add(mTasksFragment);
        fragments.add(mGiftsFaragment);
        fragments.add(mCardsFragment);

        mBack = (ImageButton) findViewById(R.id.gift_back);
        mBack.setOnClickListener(this);
        mTaskBtn = (Button) findViewById(R.id.task_btn);
        mTaskBtn.setOnClickListener(this);
        mGiftBtn = (Button) findViewById(R.id.gift_btn);
        mGiftBtn.setOnClickListener(this);
        mCardBtn = (Button) findViewById(R.id.card_btn);
        mCardBtn.setOnClickListener(this);

        mStarLayout = (RelativeLayout) findViewById(R.id.star_layout);
        mStarText = (TextView) findViewById(R.id.star_num);
        mStarLayout.setOnClickListener(this);
        mCreditLayout = (RelativeLayout) findViewById(R.id.credit_layout);
        mCreditText = (TextView) findViewById(R.id.credit_num);
        mCreditLayout.setOnClickListener(this);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setOffscreenPageLimit(3);

        pageNumber = getIntent().getIntExtra("page_number", 0);
        if (0 == pageNumber) {//第一页
            viewPager.setCurrentItem(0);
            Countly.sharedInstance().recordEvent("gift", "t_gift_store_task");
            clickTaskBtn();
        } else if (1 == pageNumber) {//第二页
            viewPager.setCurrentItem(1);
            clickGiftBtn();
            Countly.sharedInstance().recordEvent("gift", "t_gift_store_gift_unreceived");
        } else if (2 == pageNumber) {//第三页
            viewPager.setCurrentItem(2);
            clickCardBtn();
            Countly.sharedInstance().recordEvent("gift", "t_gift_store_collection");
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (0 == position) {//第一页
                    clickTaskBtn();
                } else if (1 == position) {//第二页
                    clickGiftBtn();
                    //playGiftTTS();
                } else if (2 == position) {//第三页
                    clickCardBtn();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    //播放礼物语音
    public void playGiftTTS() {
        if (!isPlayGift) {
            if (mGiftsFaragment.getGiftNum() > 0) {
                //随机数
                if (0 == (Math.random() > 0.5 ? 1 : 0)) {
                    playGiftContent("这里有好多的礼物");
                } else {
                    playGiftContent("快来看看都有什么礼物");
                }
            } else {
                playGiftContent("礼物箱空空的");
            }
            isPlayGift = true;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        pageNumber = intent.getIntExtra("page_number", 0);
        if (0 == pageNumber) {//第一页
            viewPager.setCurrentItem(0);
            clickTaskBtn();
            Countly.sharedInstance().recordEvent("gift", "t_gift_store_task");
        } else if (1 == pageNumber) {//第二页
            viewPager.setCurrentItem(1);
            clickGiftBtn();
            Countly.sharedInstance().recordEvent("gift", "t_gift_store_gift_unreceived");
        } else if (2 == pageNumber) {//第三页
            viewPager.setCurrentItem(2);
            clickCardBtn();
            Countly.sharedInstance().recordEvent("gift", "t_gift_store_collection");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //点击右上角关闭图标
            case R.id.gift_back:
                Countly.sharedInstance().recordEvent("gift", "t_gift_store_turnoff");
                finish();
                overridePendingTransition(R.anim.fullscreen_enter, R.anim.fullscreen_exit);
                XgPushHelper.getInstance().startMainActivity();
                break;
            case R.id.star_layout:
                playGiftContent("这是你获得的小星星，你可以用它换取爸爸妈妈为你准备的各种礼物哦");
                break;
            case R.id.credit_layout:
                playGiftContent("这是你获得的学分，你可以用它兑换收藏册中的各种卡片哦");

                break;
            case R.id.task_btn:
                viewPager.setCurrentItem(0);
                playGiftContent("完成每天的任务，可以获得更多学分哦");
                break;
            case R.id.gift_btn:
                viewPager.setCurrentItem(1);
                playGiftContent("快用获得的小星星换取爸爸妈妈为你准备的礼物吧");
                break;
            case R.id.card_btn:
                viewPager.setCurrentItem(2);
                playGiftContent("赶快用学分换取这些精美的卡片吧");
                break;
        }
    }

    private void clickCardBtn() {
        mCardBtn.setTextColor(Color.parseColor("#FFFFFF"));
        mCardBtn.setBackground(getResources().getDrawable(R.drawable.gift_title_item_bg));
        mGiftBtn.setTextColor(Color.parseColor("#005A98"));
        mGiftBtn.setBackground(null);
        mTaskBtn.setTextColor(Color.parseColor("#005A98"));
        mTaskBtn.setBackground(null);
        mStarLayout.setVisibility(View.GONE);
        mCreditLayout.setVisibility(View.VISIBLE);
    }

    private void clickGiftBtn() {
        mGiftBtn.setTextColor(Color.parseColor("#FFFFFF"));
        mGiftBtn.setBackground(getResources().getDrawable(R.drawable.gift_title_item_bg));
        mTaskBtn.setTextColor(Color.parseColor("#005A98"));
        mTaskBtn.setBackground(null);
        mCardBtn.setTextColor(Color.parseColor("#005A98"));
        mCardBtn.setBackground(null);
        mStarLayout.setVisibility(View.VISIBLE);
        mCreditLayout.setVisibility(View.GONE);
    }

    private void clickTaskBtn() {
        mTaskBtn.setTextColor(Color.parseColor("#FFFFFF"));
        mTaskBtn.setBackground(getResources().getDrawable(R.drawable.gift_title_item_bg));
        mGiftBtn.setTextColor(Color.parseColor("#005A98"));
        mGiftBtn.setBackground(null);
        mCardBtn.setTextColor(Color.parseColor("#005A98"));
        mCardBtn.setBackground(null);
        mStarLayout.setVisibility(View.GONE);
        mCreditLayout.setVisibility(View.VISIBLE);
    }


    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(android.support.v4.app.FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessageManager.unRegisterGiftCallback();
        QchatApplication.getApplicationInstance().removeActivity(this);
    }

    public void showDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.show();
        } else {
            loadingDialog.show();
        }
    }

    public void cancelDialog() {
        if (loadingDialog != null) {
            loadingDialog.cancel();
            loadingDialog = null;
        }
    }

    public void updateStarNum(int num) {
        mStarText.setText(num + "");
    }

    public void updateCreditNum(int num) {
        mCreditText.setText(num + "");
    }

    public String getCreditNum() {
        return mCreditText.getText().toString();
    }

    public int getHasCardNum() {
        return hasCardNum;
    }

    public void setHasCardNum(int hasCardNum) {
        this.hasCardNum = hasCardNum;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public boolean isPlayGift() {
        return isPlayGift;
    }

    public void setPlayGift(boolean playGift) {
        isPlayGift = playGift;
    }

    public void playGiftContent(String content) {
        try {
            AIManager.getInstance(getApplicationContext()).playTextWithStr(content, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
