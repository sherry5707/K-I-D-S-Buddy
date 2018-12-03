package com.kinstalk.her.qchat.utils;

import java.util.Map;

import ly.count.android.sdk.Countly;

/**
 *
 * 统计某个界面触发次数
 * 如 recordView(activity.getClass().getName())就表示追踪 activity 的界面
 *
 * 记录事件: 第一个参数是自定义的事件名称，第二个参数是计数(一般计一次)
 * Countly.sharedInstance().recordEvent("purchase ", 1);表示记录一次购买事件
 *
 * HashMap<String, String> segmentation = new HashMap<String, String>(); segmentation.put("country", "Germany");
 * segmentation.put("app_version", "1.0");
 * Countly.sharedInstance().recordEvent("purchase", segmentation, 1);
 * 这段代码表示记录购买事件和此次购买事件发生的国家和使用的应用版本号
 *
 * 通过定义一个开始时间和结束时间来统计某个事件的耗时，startEvent 和 endEvent 需要成对 匹配使用
 * 通过开始事件和结束事件计时
 * String eventName = "Custom event";
 * Countly.sharedInstance().startEvent(eventName);
 *
 * Countly.sharedInstance().endEvent(eventName);
 *  / OR /
 * segmentation, count 和 sum 三个参数。分别对应于网站上的事件细分，计数和总数。
 * Map<String, String> segmentation = new HashMap<>();
 * segmentation.put("country", "Korea");
 * Countly.sharedInstance().endEvent(eventName, segmentation, 1, 0);
 *
 * 一般记录一次事件，count 传入 1，sum 传入 0
 */

public class CountlyUtils {

    public static String skillName = "KidsBuddy";

    // 数据统计
    public static void countlyRecordEvent(String skillType, String key, int count) {
        Countly.sharedInstance().recordEvent(skillType, key, count);
    }

    public static void countlyRecordEvent(String skillType, String key, Map<String, String> segmentation, int count) {
        Countly.sharedInstance().recordEvent(skillType, key, segmentation, count);
    }

    public static void countlyStartEvent(String skillType, String key) {
        Countly.sharedInstance().startEvent(skillType, key);
    }

    public static void countlyEndEvent(String skillType, String key) {
        Countly.sharedInstance().endEvent(skillType, key);
    }
}
