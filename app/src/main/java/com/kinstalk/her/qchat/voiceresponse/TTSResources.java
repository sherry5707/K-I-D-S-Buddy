package com.kinstalk.her.qchat.voiceresponse;

import com.kinstalk.her.qchatmodel.Manager.MessageManager;
import com.kinstalk.her.qchatmodel.entity.HabitKidsEntity;

public class TTSResources {


    private String baby_name = getNickName();

    private String[] resource = new String[]{
            //开机绑定
            "101|请链接WIFI",
            "102|WIFI链接成功，下载腾讯云小微，解锁更多技能",
            "103|绑定好童学公众号，为宝宝规划生活习惯",
            "104|任何时候只要叫“小微，小微”我都在这里等你",
            //开机引导
            "201|嗨，我是无所不能的小微，我会告诉你很多好玩的事情呦",
            "202|你的小星星和礼物都在这里啦",
            "203|时钟里隐藏着获取小星星的神秘任务，一定要经常来看看哦",
            "204|每天的作业我都帮你整理到这里啦",
            "205|这里可以聊天哦，猜猜给你聊天的是谁",
            //早起
            "301|轩轩，起床啦，新的一天开始了，快来看太阳公公升起来了",
            "302|轩轩，起床啦，太阳都晒屁股了",
            "303|有一只小鸟停在窗前，正在对你眨眼睛哦",
            "304|轩轩快起床哦，鸟儿在树上唱歌了",
            "305|轩轩，现在为你播放每日小故事了，你可以边听故事边起床哦，如果能自己穿好衣服，就更棒啦！",
            "307|轩轩，每日小故事已经播放完毕，赶快点击屏幕确认起床吧",
            "309|轩轩，现在打卡可以获得2颗星",
            "310|轩轩，快打卡，星星已经消失了一颗了",
            "311|轩轩，现在打卡可以获得1颗星",
            "312|轩轩，小星星快跑光了，快来打卡吧",
            "313|今天只得了1颗星，明天要更早一点哦",
            //早睡
            "401|到睡觉的时间了，作业本里的作业好像还没有写完啊，快去检查一下",
            "402|准备睡觉啦，准备好了就跟我说：你好小微，我开始睡觉了，或者也可以，点击屏幕确认开始睡觉",
            "404|已经很晚了，睡前要记得刷牙啊",
            "405|快睡觉了，不能再吃东西了哦",
            "406|快睡觉了，记得把书包收拾好啊",
            "407|轩轩，今天的故事很有趣哦，快来听我讲故事吧",
            "408|轩轩，现在打卡可以获得2颗星",
            "409|轩轩，快打卡，星星已经消失了一颗了",
            "410|轩轩，现在打卡可以获得1颗星",
            "411|轩轩，小星星快跑光了，快来打卡吧",
            "412|轩轩晚安",
            "413|轩轩，做个好梦",
            "414|今天的轩轩棒棒的，晚安喽",
            "403|轩轩真棒把今天的任务都完成了，快准备睡觉吧",
            //作业
            "501|轩轩，你有新的作业，快来看看吧",
            "502|轩轩，有作业喽",
            "504|还没有新作业，你可以先休息一下",
            "505|作业还没有写完，要加油哦",
            "506|作业已经都写完了，你真棒",
            "507|作业都写完了，你可以休息一下",
            "508|轩轩真棒",
            "509|轩轩真棒，作业写完了，可以休息一下了",
            //消息
            "601|轩轩，你有新的消息，快来看看吧",
            "602|轩轩，有消息喽",
            "603|轩轩，快来看看妈妈说了什么",
            "604|好的",
            "605|好的",
            "606|没有新的消息",
            "607|没有新的消息，要不要和妈妈说点什么？",
            "608|好的",
            "609|好的",
            "610|好的",
            "611|好的",
            "612|好的",
            "613|好的",
            //习惯（提醒）
            "701|好的，以为你设置10分钟后提醒喝水",
            "702|请问明天几点",
            "703|是早上7点还是晚上7点",
            "704|好的，以为你设置明天早上7点提醒带语文书",
            "705|轩轩这棒，今天的事情都做完了",
            "706|轩轩，今天下午没有安排，你可以看看书，听听故事",
            "707|好的",
            "708|好的",
            //礼物
            "801|这里有好多的礼物",
            "802|快来看看有都有什么礼物",
            "803|礼物箱空空的",
            "804|箱子里什么都没有，去告诉妈妈你想要什么礼物",
            "805|我已帮你把愿望转告了妈妈",
            "806|星星不足，你还要继续努力哦！做任务可以获得跟多星星",
            "807|星星不足，你还要继续努力哦！做任务可以获得跟多星星",
            "808|祝贺你轩轩",
            "809|祝贺你轩轩",
            "810|祝贺你轩轩，但这个礼物箱里没有自行车，去问问妈妈吧",
            //触屏反馈
            "901|在",
            "902|好的",
            "903|我在",
            //习惯养成
            "1001|轩轩是个好孩子不能挑食哦",
            "1002|好好吃饭，不要变吃边玩",
            "1003|吃饭时不要看电视哦",
            "1004|吃饭时一件专心的事情，吃完饭再玩",
            "1005|吃饭时不可以大声说话哦",
            "1006|吃饭前记得洗手",
            "1007|吃饭时不能边吃边玩",
            "1008|粮食是很宝贵的，不能浪费啊",
            "1009|饭后不能做剧烈的运动",
            "1010|饭后要记得洗手",
            "1011|要记得多喝水",
            "1012|马上就要吃饭了",
            "1013|可以多吃一点水果",
            "1014|糖不能吃的太多",
            "1015|吃零食不好，要少吃",
            "1016|零食要少吃",
            "1017|在学校要和同学玩的开心啊",
            "1018|路上小心，祝你今天在学校里开心",
            "1019|要和同学好好玩的开心啊",
            "1020|记得吧开心的事情分享给周围的小伙伴",
            "1021|玩的开心",
            "1022|去吧，注意安全",
            "1023|去吧，路上小心记得不能跑的太远",
            "1024|出门前一定要告诉爸爸妈妈",
            "1025|公告场合不要大声喧哗",
            "1026|你要做一个懂礼貌的好孩子",
            "1027|画画草草也是有生命的，出门时要小心照顾他们",
            "1028|在外面要听爸爸妈妈的话",
            "1029|很高兴又见到你",
            "1030|欢迎回家",
            "1031|休息一下，可以开始写作业了",
            "1032|写完作业才可以玩哦",
            "1033|写作业要专心，我先不和你说了",
            "1034|要认真写作业哦，写完了我给你讲个故事",
            "1035|作业写完了要检查一下",
            "1036|写完作业记得把书包整理好，不要忘记带东西哦",
            "1037|要记得早晚刷牙",
            "1038|你真是一个爱劳动的好孩子",
            "1039|写完作业才能玩啊",
            "1040|玩完的玩具记得要自己收拾好啊"
    };

    public String[] getResource() {
        return resource;
    }

    private String getNickName() {

        HabitKidsEntity kidsEntity = MessageManager.getHabitKidsInfo();
        return (kidsEntity != null) ? kidsEntity.nick_name : "宝宝";
    }
}
