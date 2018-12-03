package com.kinstalk.her.qchat;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kinstalk.her.qchat.dialog.LoadingDialog;
import com.kinstalk.her.qchat.utils.MusicUtil;
import com.kinstalk.her.qchatcomm.utils.QAIConfig;
import com.kinstalk.her.qchatcomm.utils.StringEncryption;
import com.kinstalk.qloveaicore.AIManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bean on 2018/8/22.
 */

public class PKBaseActivity extends Activity {
    public static String TAG = "PKActivityLog";
    public LoadingDialog loadingDialog;//加载中布局;
    public String sn;
    public String token;

    //音频地址
    public final static int MUSIC_BUTTON = R.raw.button;
    public final static int MUSIC_ENTER_BACKGROUND = R.raw.enter_background;
    public final static int MUSIC_RECEIVE_AWARD = R.raw.receive_award;
    public final static int MUSIC_RESULT = R.raw.result;
    public final static int MUSIC_ANSWER_TRUE = R.raw.answer_true;
    public final static int MUSIC_ANSWER_FALSE = R.raw.answer_false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingDialog(this);
        sn = QAIConfig.getMacForSn();
        token = StringEncryption.generateToken();
    }

    public void showDialog() {
        if (!isFinishing()) {//bug 17089 【用户数据】【>1/台】android.view.WindowManager$BadTokenException: Unable to add window
            if (loadingDialog == null) {
                loadingDialog = new LoadingDialog(this);
                loadingDialog.show();
            } else {
                loadingDialog.show();
            }
        }
    }

    public void cancelDialog() {
        if (loadingDialog != null) {
            loadingDialog.cancel();
            loadingDialog = null;
        }
    }

    public void playTTS(String content) {
        try {
            AIManager.getInstance(this).playTextWithStr(content, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //数字转文字
    public String numToString(int ranking) {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "一");
        map.put(2, "二");
        map.put(3, "三");
        map.put(4, "四");
        map.put(5, "五");
        map.put(6, "六");
        map.put(7, "七");
        map.put(8, "八");
        map.put(9, "九");
        map.put(10, "十");
        map.put(11, "十一");
        map.put(12, "十二");
        map.put(13, "十三");
        map.put(14, "十四");
        map.put(15, "十五");
        map.put(16, "十六");
        map.put(17, "十七");
        map.put(18, "十八");
        map.put(19, "十九");
        map.put(20, "二十");
        map.put(21, "二十一");
        map.put(22, "二十二");
        map.put(23, "二十三");
        map.put(24, "二十四");
        map.put(25, "二十五");
        map.put(26, "二十六");
        map.put(27, "二十七");
        map.put(28, "二十八");
        map.put(29, "二十九");
        map.put(30, "三十");
        return map.get(ranking);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicUtil.getInstance().stop();
    }
}
