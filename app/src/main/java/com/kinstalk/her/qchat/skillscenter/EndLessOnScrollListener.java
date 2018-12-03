package com.kinstalk.her.qchat.skillscenter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kinstalk.her.qchatcomm.utils.QAILog;

/**
 * RecyclerView 上划加载更多
 */
public abstract class EndLessOnScrollListener extends RecyclerView.OnScrollListener {

    private static final String TAG = "EndLessOnScrollListener";

    LinearLayoutManager linearLayoutManager;

    //已经加载出来的item数
    private int totalItemCount = 0;

    //用来存储上一个totalItemCount
    private int previousTotal = 0;

    //屏幕可见的item数量
    private int visibleItemCount;

    //屏幕可见第一个Item的位置
    private int firstVisibleItem;

    //是否上拉数据
    private boolean loading = true;

    public EndLessOnScrollListener() {

    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        previousTotal = 0;

        visibleItemCount = recyclerView.getChildCount();

        LinearLayoutManager l = (LinearLayoutManager) recyclerView.getLayoutManager();

        totalItemCount = l.getItemCount();
        firstVisibleItem = l.findFirstVisibleItemPosition();
//去掉loading也可以，但是性能会下降，在每次滑动时都会判断，所以的加上
        if (loading) {
            QAILog.d(TAG, "firstVisibleItem: " + firstVisibleItem);
            QAILog.d(TAG, "totalItemCount:" + totalItemCount);
            QAILog.d(TAG, "visibleItemCount:" + visibleItemCount);
            QAILog.d(TAG, "previousTotal:" + previousTotal);

            if (totalItemCount > previousTotal) {
                //说明数据项已经加载结束
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        //实际效果是滑动到已加载页最后一项可见的瞬间，添加下一页
        if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem) {

            QAILog.d(TAG, "onScrolled end run: ");

            onLoadMore(totalItemCount);
            loading = true;
        }

    }

    /**
     * 提供一个抽闲方法，在Activity中监听到这个EndLessOnScrollListener
     * 并且实现这个方法
     * 这个方法在可见的页的最后一项，可见时调用
     * currentPage是加载到的页面编号
     */
    public abstract void onLoadMore(int i);

}
