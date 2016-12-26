package com.victor.friendchat.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.victor.friendchat.R;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    @ViewInject(R.id.tool_bar)
    protected Toolbar mToolbar;
    protected TextView mTvToolBarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        x.view().inject(this);
        mTvToolBarTitle = (TextView) mToolbar.findViewById(R.id.tv_toolbar_title);
        initView();
        initToolBar();
        initListener();
    }

    /**
     * 初始化布局
     */
    protected void initView() {

    }

    /**
     * 初始化监听器
     */
    protected void initListener() {

    }

    /**
     * 初始化toolBar
     */
    protected void initToolBar() {

    }

    /**
     * 获取布局资源id
     * @return 布局资源id
     */
    protected abstract int getLayoutResource();

    @Override
    public void onClick(View v) {

    }

}
