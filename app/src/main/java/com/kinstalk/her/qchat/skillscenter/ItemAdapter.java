package com.kinstalk.her.qchat.skillscenter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.activity.NewAIManager;
import com.kinstalk.her.qchat.translation.TranslationBean;
import com.kinstalk.her.qchatcomm.utils.QAILog;
import com.kinstalk.qloveaicore.AIManager;

import java.util.ArrayList;

/**
 * 我的翻译列表 adapter
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.MyViewHolder> {

    private static final String TAG = ItemAdapter.class.getSimpleName();

    ArrayList<TranslationBean> list;//存放数据
    Context context;

    public ItemAdapter(ArrayList<TranslationBean> list, Context context) {

        QAILog.d(TAG, "ItemAdapter RUN -》 " + list.size());

        this.list = list;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        QAILog.d(TAG, "ItemAdapter RUN");

        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.my_translation_list_item, parent, false));
        return holder;
    }

    //在这里可以获得每个子项里面的控件的实例，比如这里的TextView,子项本身的实例是itemView，
    // 在这里对获取对象进行操作
    //holder.itemView是子项视图的实例，holder.textView是子项内控件的实例
    //position是点击位置
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        //设置textView显示内容为list里的对应项

        QAILog.d(TAG, "onBindViewHolder RUN -> " + list.get(position).getId());
        long _no = list.get(position).getId();
        holder.tv_number.setText("" + _no);
        holder.tv_input.setText(list.get(position).getInput());

        final String str = list.get(position).getTranslation();
        holder.tv_translation.setText(str);

        holder.bt_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AIManager.getInstance(context).playTextWithStr(list.get(position).getInput() + str, null);
            }
        });

        //子项的点击事件监听
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(context, "点击子项"+position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //要显示的子项数量
    @Override
    public int getItemCount() {

        QAILog.d(TAG, "getItemCount RUN -> " + this.list.size());

        return this.list.size();
    }

    public void updateList(ArrayList<TranslationBean> lists) {

        if (list != null) {
            list.clear();
        }

        if (lists != null) {
            if (lists.size() > 0) {
                int size = lists.size();

                for (int i = 0; i < size; i++) {

                    QAILog.d(TAG, "updateList RUN -> " + lists.get(i).toString());

                    list.add(lists.get(i));
                }

            }
        }

    }

    public void reload(ArrayList<TranslationBean> lists) {

        if (lists != null) {
            this.list.clear();
            this.list.addAll(lists);
        }

    }

    public void loadMore(ArrayList<TranslationBean> lists) {

        if (lists != null && list != null) {
            QAILog.d(TAG, "loadMore RUN");
            list.addAll(lists);
        }

    }

    //这里定义的是子项的类，不要在这里直接对获取对象进行操作
    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView tv_number;

        public TextView tv_input;

        public TextView tv_translation;

        public Button bt_play;

        public MyViewHolder(View itemView) {
            super(itemView);

            QAILog.d(TAG, "MyViewHolder RUN");
            tv_number = (TextView) itemView.findViewById(R.id.tv_no);
            tv_input = (TextView) itemView.findViewById(R.id.tv_input_str);
            tv_translation = (TextView) itemView.findViewById(R.id.tv_translation_str);

            bt_play = (Button) itemView.findViewById(R.id.bt_play_translation);
        }
    }
}
