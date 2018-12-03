package com.kinstalk.her.qchat.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kinstalk.her.qchat.R;
import com.kinstalk.her.qchat.utils.GrayscaleTransformation;
import com.kinstalk.her.qchatmodel.entity.PKPetInfo;
import com.kinstalk.her.qchatmodel.entity.PKUserInfo;

import java.util.List;

/**
 * Created by bean on 2018/8/21.
 */

public class PetChooseAdapter extends PagerAdapter {
    private static String TAG = "PKActivityLog";

    private Context context;
    private List<PKPetInfo> list;
    private PKUserInfo user;

    public PetChooseAdapter(Context context, List<PKPetInfo> list, PKUserInfo user) {
        this.context = context;
        this.list = list;
        this.user = user;
    }


    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.pet_choose_item, null);

        TextView content = (TextView) view.findViewById(R.id.content);
        ImageView petImg = (ImageView) view.findViewById(R.id.img_pet);
        content.setText(list.get(position).getCaption());

        if (user.getPetId() == list.get(position).getId()) {
            Glide.with(context)
                    .load(list.get(position).getImgUrl())
                    .asBitmap()
                    .into(petImg);
        } else if (list.get(position).isIfOwn()) {
            Glide.with(context)
                    .load(list.get(position).getImgUrl())
                    .asBitmap()
                    .into(petImg);
        } else {
            Glide.with(context)
                    .load(list.get(position).getImgUrl())
                    .asBitmap()
                    .transform(new GrayscaleTransformation(context))
                    .into(petImg);
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // super.destroyItem(container,position,object); 这一句要删除，否则报错
        container.removeView((View) object);
    }

   /* public void notifyDataSetChanged(List<PKPetInfo> petList, PKUserInfo user) {
        this.list = petList;
        this.user = user;
        notifyDataSetChanged();
    }*/
}
