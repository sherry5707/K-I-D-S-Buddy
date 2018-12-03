package com.kinstalk.her.qchat.utils;

import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;

public class MessageUtils {
    public static boolean bMultiLineMsg(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }

        if (hasHuiche(text)) {
            return true;
        }

        if (getTextWidth(text) > 350) {
            return true;
        }

        return false;
    }

    private static boolean hasHuiche(String text) {
        int index = -1;
        if (!TextUtils.isEmpty(text)) {
            index = text.indexOf("\n");
        }
        return index >= 0;
    }

    public static float getTextWidth(String text) {
        if (TextUtils.isEmpty(text)) {
            return 0;
        }

        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(24);
        float textWidth = mTextPaint.measureText(text);
        if (textWidth == 0) {
            return 1;
        }

        return textWidth;
    }
}
