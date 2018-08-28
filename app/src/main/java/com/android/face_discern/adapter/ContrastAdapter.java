package com.android.face_discern.adapter;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.android.face_discern.R;
import com.android.face_discern.model.Contrast;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.text.SimpleDateFormat;
import java.util.List;

public class ContrastAdapter extends BaseQuickAdapter<Contrast,BaseViewHolder> {
    public ContrastAdapter( @Nullable List<Contrast> data) {
        super(R.layout.itme_contrast, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Contrast item) {
        helper.setText(R.id.tv_contrast, "相似度"+item.getContrastPercentage());
        helper.setText(R.id.tv_time, new SimpleDateFormat("yyyy.MM.dd").format(item.getTime()));

        Glide.with(mContext).load(item.getCaptureImg()).into((ImageView) helper.getView(R.id.img_capture));
        Glide.with(mContext).load(item.getRegisterImg()).into((ImageView) helper.getView(R.id.img_register));

        helper.setText(R.id.tv_name, "姓名:"+item.getName());
    }
}
