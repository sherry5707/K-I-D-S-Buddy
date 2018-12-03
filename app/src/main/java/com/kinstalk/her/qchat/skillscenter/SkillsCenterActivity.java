package com.kinstalk.her.qchat.skillscenter;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.activity.NewAIManager;
import com.kinstalk.her.qchat.activityhelper.VoiceAssistantActivityHelper;
import com.kinstalk.her.qchat.messaging.XgPushHelper;
import com.kinstalk.her.qchat.translation.GlobalProvider;
import com.kinstalk.her.qchat.translation.TranslationBean;
import com.kinstalk.her.qchat.translation.TranslationDBHelper;
import com.kinstalk.her.qchat.utils.Utils;
import com.kinstalk.her.qchat.voiceresponse.VoiceResponseService;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.qloveaicore.AIManager;
import com.kinstalk.qloveaicore.TTSListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 技能中心
 */
public class SkillsCenterActivity extends AppCompatActivity implements View.OnClickListener, SkillsFragment.FragmentInteraction {

    /**
     * TAG
     */
    private static final String TAG = SkillsCenterActivity.class.getSimpleName();

    /**
     * 小薇语音助手提示语
     */
    private static final String TIPS = "我是无所不能的小微， 想跟我玩点什么呢？";

    private ImageView iv_back;

    private TabLayout tl_title;

    private ViewPager viewpager;

    private FragmentAdapter adapter;

    private Context mContext;

    private ArrayList<Fragment> fragments = new ArrayList<Fragment>();

    private String[] titles = {"技能中心", "我的翻译"};

    private int tabFlag = 0;

//    private TlBroadcastReceiver tlbr;

    private SkillsFragment slfragment;

    private MyTranslationFragment mtfragment;

    private boolean isClose = true;

    private boolean mXiaoWei = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skills_center);

        initView();

        initData();

        changeVpTab();

        mXiaoWei = getIntent().getBooleanExtra("xiaowei", false);
        if (!mXiaoWei)
            playTips();

    }

    private void playTips() {

        if (Utils.checkNetworkAvailable()) {

            QAILog.d(TAG, "playTips run");

            AIManager.getInstance(mContext).playTextWithStr(TIPS, mCommonTTSCb);
        }

    }

    private void initView() {

        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);

        tl_title = (TabLayout) findViewById(R.id.tl_title);

        viewpager = (ViewPager) findViewById(R.id.viewpager);

    }

    private void initData() {

        QAILog.d(TAG, "initData RUN");

        mContext = this;

        slfragment = new SkillsFragment();
        mtfragment = new MyTranslationFragment();

        fragments.add(slfragment);
        fragments.add(mtfragment);

        adapter = new FragmentAdapter(getSupportFragmentManager(), fragments);
        viewpager.setAdapter(adapter);
        tl_title.setupWithViewPager(viewpager);


        for (int i = 0; i < adapter.getCount(); i++) {

            TabLayout.Tab tab = tl_title.getTabAt(i);
            tab.setCustomView(R.layout.skill_center_title_tab_item);

            TextView textView = (TextView) tab.getCustomView().findViewById(R.id.tab_text);
            textView.setText(titles[i]);//设置tab上的文字

            if (i == 0) {
                textView.setTextColor(getResources().getColor(R.color.a98));
                textView.setBackgroundResource(R.mipmap.tab_current_text_bg);
                textView.setSelected(true);//第一个tab被选中
            } else {
                textView.setTextColor(getResources().getColor(R.color.white));
                textView.setSelected(false);//第一个tab被选中
            }

        }

        tl_title.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                QAILog.d(TAG, "onTabSelected RUN -> " + tab.getPosition());

                tabFlag = tab.getPosition();

                TextView tv = (TextView) tab.getCustomView().findViewById(R.id.tab_text);

                tv.setSelected(true);
                tv.setBackgroundResource(R.mipmap.tab_current_text_bg);
                viewpager.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                TextView tv = (TextView) tab.getCustomView().findViewById(R.id.tab_text);

                tv.setSelected(false);
                tv.setBackgroundResource(R.color.transparent);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

//    /**
//     * 跨app获取sharedpreference数据
//     */
//    private void getDatasFromOtherApp() {
//
//
//        for (int i = 0; i < 15; i++) {
//
//            TranslationBean bean = new TranslationBean();
//
//            bean.setId(i);
//            bean.setUserId(i);
//            bean.setInput("test");
//            bean.setTranslation("test");
//
//            if (bean != null) {
//                TranslationDBHelper.getInstance(mContext).insertTranslationBean(bean);
//            }
//
//        }
//
//
//    }

    @Override
    protected void onStart() {
        super.onStart();

        QAILog.d(TAG, "onStart -> ");

        Intent intent = new Intent(mContext, VoiceResponseService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    protected void onResume() {
        super.onResume();
        QAILog.d(TAG, "onResume run");
//        tabFlag = getIntent().getIntExtra("current_tab", 0);
        if (tabFlag == 0) {
//            playTips();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        mXiaoWei = getIntent().getBooleanExtra("xiaowei", false);
        //      getRemindList(this);
        if (!mXiaoWei)
            playTips();

        changeVpTab();
    }

    void changeVpTab() {

        QAILog.d(TAG, "changeVpTab -> tabFlag -> " + tabFlag);

        if (getIntent().getExtras() != null) {
            tabFlag = getIntent().getExtras().getInt("current_tab");
        } else {
            tabFlag = 0;
//            playTips();
        }

        viewpager.setCurrentItem(tabFlag);
    }

    protected void onStop() {
        super.onStop();

        QAILog.d(TAG, "onStop -> run -> " + isClose + " | " + tabFlag);

        tabFlag = 0;

        if (isClose) {

            getSupportFragmentManager().beginTransaction().remove(slfragment).commitAllowingStateLoss();
            getSupportFragmentManager().beginTransaction().remove(mtfragment).commitAllowingStateLoss();
            fragments.clear();

            finish();
        }
    }

    protected void onDestroy() {
        super.onDestroy();

        QAILog.d(TAG, "onDestroy -> run -> ");
        unbindService(conn);
//        unregisterReceiver(tlbr);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.iv_back:

                overridePendingTransition(R.anim.fullscreen_enter, R.anim.fullscreen_exit);
//                XgPushHelper.getInstance().startMainActivity();

                finish();
                break;
            default:
                break;

        }
    }

    @Override
    public void process(int index, boolean b) {

        QAILog.d(TAG, "process -> " + b + " | " + index);

        isClose = b;

        if (index == 1) {
            tabFlag = index;
        }

    }

    @Override
    public void sendSkillCommand(String command) {
        AIManager.getInstance(mContext).textRequest(command);
    }

//    @Override
//    public void playContent(int i) {
//        mHelper.playContent(mContext, i);
//    }

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

            AIManager.getInstance(mContext).playTextWithStr(TIPS, mCommonTTSCb);
        }
    };

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            QAILog.d(TAG, "onServiceConnected");

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            QAILog.d(TAG, "onServiceDisconnected");
        }
    };

}
