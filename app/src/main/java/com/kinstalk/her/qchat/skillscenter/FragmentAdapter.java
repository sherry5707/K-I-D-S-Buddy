package com.kinstalk.her.qchat.skillscenter;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class FragmentAdapter extends FragmentPagerAdapter {

    private static final String TAG = FragmentAdapter.class.getSimpleName();

    private List<Fragment> fragmentList;


    public FragmentAdapter(FragmentManager supportFragmentManager, List<Fragment> fragments) {
        super(supportFragmentManager);

        this.fragmentList = fragments;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

//    @Override
//    public Object instantiateItem(ViewGroup container, int position) {
//        return null;
//
//    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }
}
