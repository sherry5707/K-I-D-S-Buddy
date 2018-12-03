package com.kinstalk.her.qchat.library;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchatcomm.utils.QAILog;

import java.util.ArrayList;

/**
 * 图书类别RecyclerView Adapter
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> {
    private static final String TAG = ItemAdapter.class.getSimpleName();

    private ArrayList<TitlesBean> titles;//存放数据
    private Context context;

    public ParentTabChangeListener listener;

    public ItemAdapter(ArrayList<TitlesBean> list, Context context) {

        QAILog.d(TAG, "voice book ItemAdapter -> " + list.size());

        this.titles = list;
        this.context = context;

        if (this.context instanceof ParentTabChangeListener) {
            listener = (ParentTabChangeListener) this.context; // 2.2 获取到宿主activity并赋值
        } else {
            throw new IllegalArgumentException("activity must implements ParentTabChangeListener");
        }

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.voice_book_title_tab_item, parent, false));
        return holder;
    }

    //在这里可以获得每个子项里面的控件的实例，比如这里的TextView,子项本身的实例是itemView，
    // 在这里对获取对象进行操作
    //holder.itemView是子项视图的实例，holder.textView是子项内控件的实例
    //position是点击位置
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        //设置textView显示内容为list里的对应项
        holder.textView.setText(this.titles.get(position).getTitle());

        if (this.titles.get(position).isSelected()) {
            holder.textView.setTextColor(context.getResources().getColor(R.color.white));
//            holder.textView.setTextSize(31);
        }else {
            holder.textView.setTextColor(context.getResources().getColor(R.color.color_26a6ff));
        }
        //子项的点击事件监听
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(context, "点击子项" + position, Toast.LENGTH_SHORT).show();

//                holder.textView.setTextColor(context.getResources().getColor(R.color.white));
//                holder.textView.setTextSize(31);

                if (titles != null) {
                    for (int i = 0; i < titles.size(); i++) {
                        if (i == position) {
                            titles.get(i).setSelected(true);
                        } else {
                            titles.get(i).setSelected(false);
                        }
                    }
                }

                notifyDataSetChanged();

                listener.changeTab(titles.get(position).getTitle());
            }
        });
    }

    //要显示的子项数量
    @Override
    public int getItemCount() {

        QAILog.d(TAG, "voice book getItemCount -> " + this.titles.size());
        return this.titles.size();
    }

    public void updateDatas(ArrayList<String> titles) {

        if (titles.size() > 0) {

            QAILog.d(TAG, "updateDatas -> " + titles.size());

            if (this.titles != null && this.titles.size() > 0)
                this.titles.clear();

//            for (int i = 0; i < titles.size(); i++) {
//                if (this.list != null) {
//                    this.list.add(titles.get(i));
//                }
//                QAILog.d(TAG, "updateDatas -> " + titles.get(i));
//            }

//            this.titles = titles;
            notifyDataSetChanged();
        }
    }

    //这里定义的是子项的类，不要在这里直接对获取对象进行操作
    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tab_parentg_name);
        }
    }

    /*之下的方法都是为了方便操作，并不是必须的*/

    //在指定位置插入，原位置的向后移动一格
    public boolean addItem(int position, TitlesBean msg) {
        if (position < titles.size() && position >= 0) {
            titles.add(position, msg);
            notifyItemInserted(position);
            return true;
        }
        return false;
    }

    //去除指定位置的子项
    public boolean removeItem(int position) {
        if (position < titles.size() && position >= 0) {
            titles.remove(position);
            notifyItemRemoved(position);
            return true;
        }
        return false;
    }

    //清空显示数据
    public void clearAll() {
        titles.clear();
        notifyDataSetChanged();
    }
}
