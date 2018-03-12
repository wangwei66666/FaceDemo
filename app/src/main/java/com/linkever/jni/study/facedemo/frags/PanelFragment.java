package com.linkever.jni.study.facedemo.frags;


import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.TabLayout;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;

import com.linkever.jni.study.facedemo.Fragment;
import com.linkever.jni.study.facedemo.R;
import com.linkever.jni.study.facedemo.face.Face;
import com.linkever.jni.study.facedemo.face.FaceAdapter;
import com.linkever.jni.study.facedemo.recycler.RecyclerAdapter;
import com.linkever.jni.study.facedemo.utils.UiTool;

import net.qiujuer.genius.ui.Ui;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PanelFragment extends Fragment {

    private PanelCallback mPanelCallback;

    public PanelFragment() {
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_panel;
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
        initFace(root);
        initRecord(root);
        initGallery(root);
    }

    /**
     * 初始化接口
     * @param mPanelCallback
     */
    public void setup(PanelCallback mPanelCallback){
        this.mPanelCallback = mPanelCallback;
    }

    private void initFace(final View root) {
        View facePanel = root.findViewById(R.id.lay_panel_face);
        TabLayout tabLayout = (TabLayout)facePanel.findViewById(R.id.tab);
        View backspace = facePanel.findViewById(R.id.im_backsapce);
        //删除逻辑
        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PanelCallback callback = mPanelCallback;
                if(callback==null){
                    return;
                }
                //模拟键盘点击
                KeyEvent event = new KeyEvent(0,0,0,KeyEvent.KEYCODE_DEL,
                        0,0,0,0,KeyEvent.KEYCODE_ENDCALL);
                callback.getInputEditText().dispatchKeyEvent(event);
            }
        });
        ViewPager viewPager = facePanel.findViewById(R.id.pager);
        tabLayout.setupWithViewPager(viewPager);
        //每一个表情显示48dp
        final int minFaceSize = (int) Ui.dipToPx(getResources(),48);
        //屏幕宽度
        final int totalScreen = UiTool.getScreenWidth(getActivity());
        //每一行显示多少列
        final int spanCount = totalScreen/minFaceSize;
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                //与面板数相同
                return Face.all(getContext()).size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view==object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.lay_face_content, container,false);
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(),spanCount));
                //设置适配器
                List<Face.Bean> faces = Face.all(getContext()).get(position).faces;
                FaceAdapter faceAdapter = new FaceAdapter(faces, new RecyclerAdapter.AdapterListener<Face.Bean>() {
                    @Override
                    public void onItemClick(RecyclerAdapter.ViewHolder holder, Face.Bean bean) {
                        if(mPanelCallback == null){
                            return;
                        }
                        EditText editText = mPanelCallback.getInputEditText();
                        Face.inputFace(getContext(),editText.getText(),bean,
                                (int)(editText.getTextSize()+Ui.dipToPx(getResources(),2)));
                    }

                    @Override
                    public void onItemLongClick(RecyclerAdapter.ViewHolder holder, Face.Bean bean) {

                    }

                });
                recyclerView.setAdapter(faceAdapter);
                //添加
                container.addView(recyclerView);
                return recyclerView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                //移除
                container.removeView((View) object);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return Face.all(getContext()).get(position).name;
            }
        });
    }

    private void initRecord(View root) {

    }

    private void initGallery(View root) {

    }

    public void showFace() {

    }

    public void showRecord() {

    }

    public void showGallery() {

    }

    //回调聊天界面的Callback
    public interface PanelCallback{
        EditText getInputEditText();
    }
}
