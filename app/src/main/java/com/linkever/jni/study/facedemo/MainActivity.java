package com.linkever.jni.study.facedemo;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.linkever.jni.study.facedemo.face.Face;
import com.linkever.jni.study.facedemo.frags.PanelFragment;

import net.qiujuer.genius.ui.Ui;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements PanelFragment.PanelCallback {

    @BindView(R.id.edit_txt)
    EditText mContent;
    @BindView(R.id.txt)
    TextView txt;
    FragmentTransaction mFragmentTransaction;
    PanelFragment mPanelFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        mPanelFragment = new PanelFragment();
        mFragmentTransaction.add(R.id.frag_face, mPanelFragment);
        mFragmentTransaction.commit();
        mPanelFragment.setup(this);
    }

    @OnClick(R.id.btn_ok)
    public void Ok(View view){
//        String str = TextUtils.isEmpty(mContent.getText()) ? "" : mContent.getText().toString();
        String str = mContent.getText().toString();
        Spannable spannable = new SpannableString(str);
        //表情解析
        Face.decode(mContent,spannable, (int) txt.getTextSize());
//        Face.decode(mContent,spannable, (int) Ui.dipToPx(getResources(),20));
        //把内容设置到布局上
        txt.setText(spannable);
    }

    @Override
    public EditText getInputEditText() {
        return mContent;
    }

}
