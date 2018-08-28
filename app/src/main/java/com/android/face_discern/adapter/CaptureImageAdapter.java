package com.android.face_discern.adapter;

import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.android.face_discern.R;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.io.File;
import java.util.List;

public class CaptureImageAdapter extends BaseQuickAdapter<File,BaseViewHolder> {
    public CaptureImageAdapter( @Nullable List<File> data) {
        super(R.layout.itme_capture_image, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, File item) {
        Log.e("CaptureImageAdapter",""+item);
        Glide.with(mContext).load(item).into((ImageView) helper.getView(R.id.img_capture));
//        helper.setText(R.id.tv_test, item.getPath());

    }
}
