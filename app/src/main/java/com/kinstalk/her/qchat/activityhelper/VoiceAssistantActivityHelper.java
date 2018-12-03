package com.kinstalk.her.qchat.activityhelper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.kinstalk.her.qchat.voiceresponse.TTSContentBean;
import com.kinstalk.her.qchat.voiceresponse.TTSResources;
import com.kinstalk.her.qchat.voiceresponse.VoiceContentDBHelper;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.qloveaicore.AIManager;
import com.kinstalk.qloveaicore.TTSListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 枚举单例
 */
public enum VoiceAssistantActivityHelper {

    //定义一个枚举的元素，它就是Singleton的一个实例
    instance;

    /**
     * TAG
     */
    private static final String TAG = VoiceAssistantActivityHelper.class.getSimpleName();

    private static final String PR_TIPS = "想知道这是什么技能，可以对我说，你好小微，";

//    String[][] resources = {{"0|笑话|我要听笑话", "1|宇宙|太阳系有多大", "2|故事|我要听睡前故事",},
//            {"3|成语|七上八下是什么意思", "4|算数|1＋1=？", "5|百科|太阳离地球有多远",},
//            {"6|英语|苹果的英语怎么说", "7|诗词|静夜思", "8|寓言|给我讲个寓言故事"},
//            {"9|提醒|10分钟后提醒我吃零食", "10|造句|用成语造句", "11|换算|1千克等于多少克"}};

    public void playWorkContent(Context context, String content) {

        QAILog.d(TAG, "playWorkContent -> content -> " + content);

        AIManager.getInstance(context).playTextWithStr(content, null);

    }

    /**
     * 语音播报
     *
     * @param mContext
     * @param i        播报编码
     */
    public void playTTSContent(Context mContext, int i) {

        String content = VoiceContentDBHelper.getInstance(mContext).getContentById(i);
        QAILog.d(TAG, "playTTSContent -> content -> " + content);
        if (TextUtils.isEmpty(content)) {

        } else {

            playWorkContent(mContext, content);
        }

    }

    public void playContent(Context mContext, int i) {

        String content = codeTransformationStr(i);

//        String[][] resources = {{"0|笑话|我要听笑话", "1|翻译|英语翻译官", "2|故事|我要听睡前故事",},
//                {"3|算数|1＋1=？", "4|成语|七上八下是什么", "5|百科|太阳离地球有多远",},
//                {"6|英语|苹果的英语怎么说", "7|造句|用成果造句", "8|寓言|给我讲个寓言故事"},
//                {"9|提醒|10分钟后提醒我吃零食"}};


        if (content == null && content == "") {

        } else {
            playWorkContent(mContext, content);
        }

    }


//    public void refreshContent(int currentPageIndex, Handler handler) {
//    }

    /**
     * 将数据源存入数据库
     */
    public void saveResourceToDB(Context context) {
        TTSResources resources = new TTSResources();
        String[] datas = resources.getResource();

        if (datas != null && datas.length > 0) {
            int size = datas.length;
            List<TTSContentBean> list = new ArrayList<TTSContentBean>();
            for (int i = 0; i < size; i++) {
                TTSContentBean bean = new TTSContentBean();
                QAILog.d(TAG, "saveResourceToDB -> " + datas[i]);
                String[] tmp = datas[i].split("\\|");
                bean.setId(Integer.valueOf(tmp[0]));
                bean.setContent(tmp[1]);

                list.add(bean);
            }

            if (list != null) {
                VoiceContentDBHelper.getInstance(context).insertResoucesContents(list);
            }
        }

    }

    public void sendSkillCommand(Context context, String skill_command) {

        Intent intent = new Intent("VOICE_RESPONSE_SKILL_COMMAND");
        intent.putExtra("SKILL_COMMAND", skill_command);
        context.sendBroadcast(intent);

    }

    public void playContent(Context mContext, int i, TTSListener mCommonTTSCb) {

        String content = codeTransformationStr(i);

        if (content == null && content == "") {

        } else {
            playWorkContent(mContext, content, mCommonTTSCb);
        }

    }

    private void playWorkContent(Context mContext, String content, TTSListener mCommonTTSCb) {

        AIManager.getInstance(mContext).playTextWithStr(content, mCommonTTSCb);

    }

    private String codeTransformationStr(int code) {

        String result = null;

        String[] resources = {"0|图书馆|我要听有声书", "1|翻译|英语翻译官", "2|故事|我要听睡前故事",
                "3|笑话|我要听笑话", "4|英语|苹果的英语怎么说", "5|造句|用成果造句",
                "6|寓言|给我讲个寓言故事", "7|成语|七上八下是什么意思", "8|算数|1+1=?",
                "9|百科|1千克等于多少克", "10|提醒|10分钟后提醒我吃零食"};

        switch (code) {
            case 0:

                result = PR_TIPS + "我要听有声书";

                break;
            case 1:

                result = PR_TIPS + "英语翻译官";

                break;
            case 2:

                result = PR_TIPS + "我要听睡前故事";

                break;
            case 3:

                result = PR_TIPS + "我要听笑话";

                break;
            case 4:

                result = PR_TIPS + "苹果的英语怎么说";

                break;
            case 5:

                result = PR_TIPS + "用成果造句";

                break;
            case 6:

                result = PR_TIPS + "给我讲个寓言故事";

                break;
            case 7:

                result = PR_TIPS + "七上八下是什么意思";

                break;
            case 8:

                result = PR_TIPS + "1+1=?";

                break;
            case 9:

                result = PR_TIPS + "1千克等于多少克";

                break;
            case 10:

                result = PR_TIPS + "10分钟后提醒我吃零食";

                break;
            default:
                result = " ";
                break;

        }

        return result;

    }

}
