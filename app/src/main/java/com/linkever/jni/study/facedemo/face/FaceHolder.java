package com.linkever.jni.study.facedemo.face;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.linkever.jni.study.facedemo.R;
import com.linkever.jni.study.facedemo.recycler.RecyclerAdapter;

import butterknife.BindView;

/**
 * Author:      WW
 * Date:        2018/3/9 15:55
 * Description: This is FaceHolder
 */

public class FaceHolder extends RecyclerAdapter.ViewHolder<Face.Bean>{
    @BindView(R.id.im_face)
    ImageView mFace;

    public FaceHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void onBind(Face.Bean bean) {
        //bean只有两种，一种drawable，一种zip文件
         if(bean!=null&&
                 //drawable资源
                 ((bean.preview instanceof Integer)
                         //face.zip包
                         ||(bean.preview instanceof String))){
             Glide.with(mFace.getContext())
                     .load(bean.preview)
                     .asBitmap()
                     .format(DecodeFormat.PREFER_ARGB_8888)//设置解码格式，保证清晰度
                     .into(mFace);
         }
    }
}
