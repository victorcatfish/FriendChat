package com.victor.friendchat.ui.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.victor.friendchat.R;
import com.victor.friendchat.base.BaseActivity;
import com.victor.friendchat.uitl.UIUtils;

import org.xutils.view.annotation.ViewInject;

public class LoginActivity extends BaseActivity {

    @ViewInject(R.id.et_login_user)
    private EditText mEtUser;
    @ViewInject(R.id.et_login_pwd)
    private EditText mEtPwd;
    @ViewInject(R.id.tv_login)
    private TextView mTvLogin;
    @ViewInject(R.id.tv_register)
    private TextView mTvRegister;
    @ViewInject(R.id.tv_forgot_pwd)
    private TextView mTvForgotPwd;
    @ViewInject(R.id.rb_login_qq)
    private RadioButton mRbLoginQQ;
    @ViewInject(R.id.rb_login_weibo)
    private RadioButton mRbLoginWeibo;
    @ViewInject(R.id.rb_login_wechat)
    private RadioButton mRbLoginWechat;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_login;
    }

    @Override
    protected void initToolBar() {
        mTvToolBarTitle.setText(R.string.login);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
     }

    @Override
    protected void initListener() {
        mTvLogin.setOnClickListener(this);
        mTvRegister.setOnClickListener(this);
        mTvForgotPwd.setOnClickListener(this);
        mRbLoginQQ.setOnClickListener(this);
        mRbLoginWeibo.setOnClickListener(this);
        mRbLoginWechat.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tv_login:
                UIUtils.showLongToast(this, "登录");
                break;
            case R.id.tv_register:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_forgot_pwd:
                UIUtils.showShortToast(this, "忘记密码");
                break;
            case R.id.rb_login_qq:
                UIUtils.showShortToast(this, "QQ登录");
                break;
            case R.id.rb_login_weibo:
                UIUtils.showShortToast(this, "微博登录");
                break;
            case R.id.rb_login_wechat:
                UIUtils.showShortToast(this, "微信登录");
                break;
        }

    }
}
