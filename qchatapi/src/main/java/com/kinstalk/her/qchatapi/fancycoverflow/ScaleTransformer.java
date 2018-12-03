package com.kinstalk.her.qchatapi.fancycoverflow;

import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

/**
 * Created by 王松 on 2016/8/28.
 */
public class ScaleTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.70f;

    @Override
    public void transformPage(View page, float position) {
        if (position < -1 || position > 1) {
            page.setScaleX(MIN_SCALE);
            page.setScaleY(MIN_SCALE);
        } else if (position <= 1) { // [-1,1]
            if (position < 0) {
                float scaleX = 1 + 0.05f * position;
                Log.d("google_lenve_fb", "transformPage: scaleX:" + scaleX);
                page.setScaleX(scaleX);
                page.setScaleY(scaleX);
            } else {
                float scaleX = 1 - 0.05f * position;
                page.setScaleX(scaleX);
                page.setScaleY(scaleX);
            }
        }
    }}
