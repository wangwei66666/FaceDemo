package com.linkever.jni.study.facedemo.face;

import android.view.View;

import com.linkever.jni.study.facedemo.R;
import com.linkever.jni.study.facedemo.recycler.RecyclerAdapter;

import java.util.List;

/**
 * Author:      WW
 * Date:        2018/3/9 15:54
 * Description: This is FaceAdapter
 */

public class FaceAdapter extends RecyclerAdapter<Face.Bean>{

    public FaceAdapter(List<Face.Bean> beans, AdapterListener<Face.Bean> listener) {
        super(beans, listener);
    }

    @Override
    protected int getItemViewType(int position, Face.Bean bean) {
        return R.layout.cell_face;
    }

    @Override
    protected ViewHolder<Face.Bean> onCreateViewHolder(View root, int viewType) {
        return new FaceHolder(root);
    }
}
